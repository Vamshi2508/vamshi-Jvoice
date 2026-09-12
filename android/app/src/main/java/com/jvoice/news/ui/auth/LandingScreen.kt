package com.jvoice.news.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jvoice.news.data.model.NewsStatus
import com.jvoice.news.data.model.UserRole
import com.jvoice.news.data.repository.NewsRepository
import com.jvoice.study.data.model.StudyRole
import com.jvoice.study.data.repository.StudyRepository
import com.jvoice.core.i18n.current
import com.jvoice.news.components.JVoiceLogo
import com.jvoice.core.i18n.lt
import com.jvoice.news.theme.JvBlueDark
import com.jvoice.news.theme.JvInk
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material3.Button
import com.jvoice.core.auth.LoginViewModel
import com.jvoice.core.firebase.FirebaseAvailability
import com.jvoice.core.i18n.Strings
import com.jvoice.core.i18n.tr

/** The two modules the prototype currently ships. */
enum class DemoModule(val label: String) {
    NEWS("Module 1 · News"),
    STUDY("Module 2 · Study")
}

private data class RoleRowInfo(
    val title: String,
    val teluguTitle: String,
    val icon: ImageVector,
    val what: String,
    val signsInAs: String,
    val onSelect: () -> Unit
)

/**
 * Demo landing page and the app's entry screen. A segmented switch chooses the
 * module; each module lists its roles, its live mock-data counts and the
 * workflow it demonstrates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingScreen(
    onNewsRoleSelected: (UserRole) -> Unit,
    onStudyRoleSelected: (StudyRole) -> Unit,
    /** Opens the real Firebase-backed staff sign-in. */
    onStaffSignIn: () -> Unit = {}
) {
    var module by remember { mutableStateOf(DemoModule.NEWS) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp)
    ) {
        Hero()

        Spacer(Modifier.height(16.dp))

        StaffSignInCard(onStaffSignIn = onStaffSignIn)

        Spacer(Modifier.height(18.dp))

        SingleChoiceSegmentedButtonRow(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            DemoModule.entries.forEachIndexed { index, value ->
                SegmentedButton(
                    selected = module == value,
                    onClick = { module = value },
                    shape = SegmentedButtonDefaults.itemShape(index, DemoModule.entries.size),
                    label = { Text(value.label) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        when (module) {
            DemoModule.NEWS -> NewsModuleSection(onNewsRoleSelected)
            DemoModule.STUDY -> StudyModuleSection(onStudyRoleSelected)
        }

        Text(
            lt(
                "Live on Firebase • news and study content load from the project",
                "Firebase‌లో లైవ్ • వార్తలు, స్టడీ కంటెంట్ ప్రాజెక్ట్ నుంచి"
            ).current(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )
    }
}

/* ------------------------------------------------------------------ module 1 */

@Composable
private fun NewsModuleSection(
    onRoleSelected: (UserRole) -> Unit
) {
    val articles by NewsRepository.articles.collectAsState()
    val categories by NewsRepository.categories.collectAsState()
    val users by NewsRepository.users.collectAsState()
    val published = articles.count { it.status == NewsStatus.PUBLISHED }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DemoStat(articles.size.toString(), "Articles", Modifier.weight(1f))
        DemoStat(published.toString(), "Published", Modifier.weight(1f))
        DemoStat(categories.size.toString(), "Categories", Modifier.weight(1f))
        DemoStat(users.size.toString(), "Users", Modifier.weight(1f))
    }

    Spacer(Modifier.height(20.dp))

    WorkflowCard(
        title = "The news workflow",
        steps = listOf(
            "Reporter" to "writes an article and submits it for review",
            "Editor" to "edits the copy, then approves and publishes",
            "Reader" to "sees it instantly in the home feed and search",
            "News Admin" to "pins it, marks it breaking, or removes it"
        )
    )

    Spacer(Modifier.height(22.dp))
    RolesHeader("Read without an account")

    // One row, and no credential: readers are anonymous by design. Desk staff
    // sign in through the card above instead.
    RoleRow(
        RoleRowInfo(
            "Reader",
            UserRole.READER.teluguLabel,
            Icons.AutoMirrored.Filled.MenuBook,
            "Home feed, categories, search, saved news",
            "No account needed"
        ) { onRoleSelected(UserRole.READER) }
    )
}


/**
 * The real sign-in, offered above the demo role rows.
 *
 * Two things are stated rather than left implicit: that this is for staff, and
 * that readers do not need an account. Without the second line a reader landing
 * here reasonably assumes they have to make one.
 *
 * When the build has no Firebase config the button is disabled and says so, rather
 * than opening a form that cannot possibly succeed.
 */
@Composable
private fun StaffSignInCard(onStaffSignIn: () -> Unit) {
    val configured = FirebaseAvailability.isAvailable
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Badge,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        tr(Strings.Auth.staffSignIn),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        tr(Strings.Auth.subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onStaffSignIn,
                enabled = configured,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(tr(Strings.Auth.signIn) + "  \u00b7  @" + LoginViewModel.LOGIN_DOMAIN)
            }
            if (!configured) {
                Spacer(Modifier.height(8.dp))
                Text(
                    tr(Strings.Auth.demoModeNotice),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/* ------------------------------------------------------------------ module 2 */

@Composable
private fun StudyModuleSection(onRoleSelected: (StudyRole) -> Unit) {
    val subjects by StudyRepository.subjects.collectAsState()
    val topics by StudyRepository.topics.collectAsState()
    val questions by StudyRepository.questions.collectAsState()
    val articles by StudyRepository.articles.collectAsState()

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DemoStat(subjects.size.toString(), "Subjects", Modifier.weight(1f))
        DemoStat(topics.size.toString(), "Topics", Modifier.weight(1f))
        DemoStat(articles.size.toString(), "Articles", Modifier.weight(1f))
        DemoStat(questions.size.toString(), "Questions", Modifier.weight(1f))
    }

    Spacer(Modifier.height(20.dp))

    WorkflowCard(
        title = "The study workflow",
        steps = listOf(
            "Student" to "reads a topic article, then takes the topic quiz",
            "Daily Exam" to "20 questions with timer, palette and mark-for-review",
            "Analysis" to "subject and topic accuracy classify strong and weak areas",
            "Weak Areas" to "recommended topics, practice sets, then the Grand Test"
        )
    )

    Spacer(Modifier.height(22.dp))
    RolesHeader("Study without an account")

    // Same as the news side: students are anonymous, the study desk signs in.
    RoleRow(
        RoleRowInfo(
            "Student",
            StudyRole.STUDENT.teluguLabel,
            Icons.Default.School,
            "Syllabus, study material, quizzes, daily exams and ranks",
            "No account needed"
        ) { onRoleSelected(StudyRole.STUDENT) }
    )
}

private fun demoStudyName(role: StudyRole): String =
    com.jvoice.study.data.mock.MockDataSource.demoAccounts[role]?.name.orEmpty()

/* ------------------------------------------------------------------ pieces */

@Composable
private fun Hero() {
    Box(
        Modifier
            .fillMaxWidth()
            // Deep navy to ink, not the brand red it used to be: the wordmark's
            // first block is scarlet, and on a red hero it vanished into the
            // background. This is also the ground the logo itself sits on.
            .background(
                Brush.verticalGradient(
                    listOf(JvBlueDark, JvInk)
                )
            )
            .padding(horizontal = 20.dp, vertical = 32.dp)
    ) {
        Column {
            // The supplied logo artwork, which already contains the wordmark and
            // the tagline - so the app name is not printed beside it, which would
            // be saying it twice. Sized by width because the lockup is wider than
            // it is tall and its ratio is fixed by the artwork.
            JVoiceLogo(width = 250.dp)
            Spacer(Modifier.height(10.dp))
            Text(
                lt("Your news • Your studies", "మీ వార్తలు • మీ చదువు").current(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "Demo build — two modules",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                "News, and Study & Exam Preparation. Both fully navigable on local dummy data.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun RolesHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
    Text(
        "Dummy login — no password. Switch role any time from the profile screen or drawer.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
    )
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun WorkflowCard(title: String, steps: List<Pair<String, String>>) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
    Spacer(Modifier.height(8.dp))
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(14.dp)) {
            steps.forEachIndexed { index, (who, what) ->
                Row(
                    Modifier.padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                (index + 1).toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(who, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        what,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoStat(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RoleRow(info: RoleRowInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { info.onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        info.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(info.title, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        info.teluguTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    info.what,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (info.signsInAs.isNotBlank()) {
                    Text(
                        "as " + info.signsInAs,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
