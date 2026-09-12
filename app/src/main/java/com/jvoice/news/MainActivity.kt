package com.jvoice.news

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.jvoice.core.auth.AuthGate
import com.jvoice.core.auth.DeskSessionGate
import com.jvoice.core.auth.SessionStore
import com.jvoice.core.auth.StaffLoginScreen
import com.jvoice.core.firebase.FirebaseAvailability
import com.jvoice.core.i18n.FirstRunLanguageDialog
import com.jvoice.core.i18n.LanguagePreference
import com.jvoice.core.flags.FeatureFlags
import com.jvoice.core.flags.LocalFeatureFlags
import com.jvoice.core.i18n.LocalAppLanguage
import com.jvoice.news.data.model.UserRole
import com.jvoice.news.data.repository.NewsRepository
import com.jvoice.news.data.repository.ReadStateRepository
import com.jvoice.news.navigation.JVoiceNavGraph
import com.jvoice.news.theme.JVoiceTheme
import com.jvoice.news.ui.auth.LandingScreen
import com.jvoice.news.ui.auth.SessionViewModel
import com.jvoice.shell.AppModule
import com.jvoice.shell.LocalModuleSwitcher
import com.jvoice.shell.ModuleSwitcher
import com.jvoice.study.data.model.StudyRole
import com.jvoice.study.navigation.StudyNavGraph
import com.jvoice.study.ui.auth.StudySessionViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Read state, the language choice and the desk session are the three things
        // that persist across launches.
        ReadStateRepository.init(this)
        LanguagePreference.init(this)
        SessionStore.init(this)
        // Attempted once, here, so every later Firebase call can just ask
        // FirebaseAvailability rather than each guarding initialisation itself.
        FirebaseAvailability.init(this)
        // Attaches the Firestore content listeners and the RTDB staff listener.
        // Must come after FirebaseAvailability.init - both no-op without it, so
        // the wrong order would leave every screen permanently empty rather than
        // failing loudly.
        NewsRepository.start()
        // App-level flags. Starts here rather than lazily in a screen so the
        // first paint already has them and nothing flickers off after a beat.
        FeatureFlags.start()
        // TEMPORARY: one-off export of the bundled study content so it can be
        // uploaded to Firestore. Debug-only, and removed together with the mock
        // data once the upload is verified. See DevExport.
        com.jvoice.core.data.DevExport.exportIfNeeded(this)
        setContent { JVoiceApp() }
    }
}

/**
 * App shell.
 *
 * Two ways in, and they are deliberately different:
 *
 *  * **Readers and students** enter anonymously from the landing screen. They have
 *    no account; their state lives on the device.
 *  * **Desk staff** sign in through [StaffLoginScreen] against Firebase, and their
 *    role comes from `users/{uid}/role`.
 *
 * A stored desk session short-circuits the landing screen on launch, but only after
 * [DeskSessionGate] has cleared it against the server's two kill-switches.
 */
@Composable
fun JVoiceApp() {
    val systemDark = isSystemInDarkTheme()
    var darkTheme by remember(systemDark) { mutableStateOf(systemDark) }
    val scope = rememberCoroutineScope()

    // The chosen language is provided to the whole tree, so every screen renders
    // content in it without carrying it through its own signature.
    val language by LanguagePreference.language.collectAsState()
    val hasChosenLanguage by LanguagePreference.hasChosen.collectAsState()

    // Runtime notification permission - only meaningful from API 33. Below that
    // the grant is implicit at install time, so there is nothing to ask.
    val notificationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Prototype pushes nothing; the grant is recorded by the OS. */ }

    fun requestNotifications() {
        LanguagePreference.markNotificationsAsked()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    JVoiceTheme(darkTheme = darkTheme) {
        val featureFlags by FeatureFlags.flags.collectAsState()
        CompositionLocalProvider(
            LocalAppLanguage provides language,
            LocalFeatureFlags provides featureFlags
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val newsSession: SessionViewModel = viewModel()
                val studySession: StudySessionViewModel = viewModel()
                val newsUser by newsSession.currentUser.collectAsState()
                val studyUser by studySession.currentUser.collectAsState()

                val deskSession by SessionStore.session.collectAsState()

                // Landing page is the entry screen; the news role selector is one tap deeper.
                var showStaffLogin by remember { mutableStateOf(false) }

                // Set once the launch gate has cleared this uid, so the checks run on
                // launch rather than on every recomposition.
                var gatedUid by remember { mutableStateOf<String?>(null) }

                /**
                 * A Super Admin owns both modules, so let them cross over directly.
                 * Supplied through a CompositionLocal so no existing screen signature
                 * has to change.
                 */
                fun enterNews(role: UserRole) {
                    showStaffLogin = false
                    studySession.signOut()
                    newsSession.signInAs(role)
                }

                fun enterStudy(role: StudyRole) {
                    showStaffLogin = false
                    newsSession.signOut()
                    studySession.signInAs(role)
                }

                /**
                 * Routes a signed-in desk account into whichever module its role owns.
                 * A role carrying both (Super Admin) starts in News.
                 */
                fun enterForDeskRole(session: SessionStore.DeskSession) {
                    // The article query differs for a signed-in desk account, so
                    // the listener has to be rebuilt - see NewsRepository.attachArticles.
                    NewsRepository.onSessionChanged()
                    val newsRole = session.role.newsRole
                    val studyRole = session.role.studyRole
                    when {
                        newsRole != null -> enterNews(newsRole)
                        studyRole != null -> enterStudy(studyRole)
                        // Unreachable: JvRole entries all carry at least one module
                        // role, and a record with no usable role never becomes a
                        // session. Signing out is the safe response if that changes.
                        else -> scope.launch { AuthGate.logout() }
                    }
                }

                /** Ends both the module session and the Firebase desk session. */
                fun signOutEverything() {
                    showStaffLogin = false
                    gatedUid = null
                    newsSession.signOut()
                    studySession.signOut()
                    if (SessionStore.isSignedIn) {
                        scope.launch {
                            AuthGate.logout()
                            // Back to the anonymous, published-only query.
                            NewsRepository.onSessionChanged()
                        }
                    }
                }

                fun switcherFor(current: AppModule) = ModuleSwitcher(
                    current = current,
                    switchToOtherAsSuperAdmin = {
                        if (current == AppModule.NEWS) {
                            enterStudy(StudyRole.SUPER_ADMIN)
                        } else {
                            enterNews(UserRole.SUPER_ADMIN)
                        }
                    },
                    openStudyAsStudent = { enterStudy(StudyRole.STUDENT) },
                    openNewsAsReader = { enterNews(UserRole.READER) }
                )

                val session = deskSession
                when {
                    // A stored desk session that has not yet cleared the launch gate.
                    // Nothing else renders until the two server checks answer.
                    session != null && gatedUid != session.uid &&
                        newsUser == null && studyUser == null -> {
                        DeskSessionGate(
                            session = session,
                            installedVersion = BuildConfig.VERSION_CODE.toLong(),
                            onReady = {
                                gatedUid = session.uid
                                enterForDeskRole(session)
                            },
                            onRejected = {
                                // AuthGate.logout has already cleared the session, so
                                // falling through lands on the landing screen.
                                gatedUid = null
                            }
                        )
                    }

                    newsUser != null -> {
                        // A fresh NavHostController per role keeps each role's graph isolated.
                        val navController = rememberNavController()
                        CompositionLocalProvider(
                            LocalModuleSwitcher provides switcherFor(AppModule.NEWS)
                        ) {
                            JVoiceNavGraph(
                                navController = navController,
                                user = newsUser!!,
                                isDarkTheme = darkTheme,
                                onToggleTheme = { darkTheme = it },
                                onSignOut = ::signOutEverything
                            )
                        }
                    }

                    studyUser != null -> {
                        val navController = rememberNavController()
                        CompositionLocalProvider(
                            LocalModuleSwitcher provides switcherFor(AppModule.STUDY)
                        ) {
                            StudyNavGraph(
                                navController = navController,
                                user = studyUser!!,
                                isDarkTheme = darkTheme,
                                onToggleTheme = { darkTheme = it },
                                onSignOut = ::signOutEverything
                            )
                        }
                    }

                    showStaffLogin -> {
                        BackHandler { showStaffLogin = false }
                        StaffLoginScreen(
                            onSignedIn = { signedIn ->
                                gatedUid = signedIn.uid
                                enterForDeskRole(signedIn)
                            },
                            onBack = { showStaffLogin = false }
                        )
                    }

                    else -> LandingScreen(
                        onNewsRoleSelected = newsSession::signInAs,
                        onStudyRoleSelected = studySession::signInAs,
                        onStaffSignIn = { showStaffLogin = true }
                    )
                }

                // Sits above whatever is showing, so the very first launch answers
                // the language and notification questions before anything else.
                // Once answered it never returns - the profile tab owns the choice
                // from then on.
                if (!hasChosenLanguage) {
                    FirstRunLanguageDialog(
                        selected = language,
                        // `apply`, not `set`: the tap flips the app over live but
                        // leaves the dialogue open for the notification question.
                        onSelect = LanguagePreference::apply,
                        onAllowNotifications = { requestNotifications() },
                        onSkipNotifications = { LanguagePreference.markNotificationsAsked() },
                        // Answering either notification button is what closes the
                        // dialogue - the default language stands if no row was tapped.
                        onDone = { LanguagePreference.markChosen() }
                    )
                }
            }
        }
    }
}
