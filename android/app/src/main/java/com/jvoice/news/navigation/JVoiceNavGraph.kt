package com.jvoice.news.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.jvoice.aishorts.navigation.AIShortRoutes
import com.jvoice.aishorts.ui.AIScriptScreen
import com.jvoice.aishorts.ui.AIShortPreviewScreen
import com.jvoice.aishorts.ui.AIShortViewModel
import com.jvoice.aishorts.ui.AIShortsDashboardScreen
import com.jvoice.aishorts.ui.CreateAIShortScreen
import com.jvoice.aishorts.ui.GenerationProgressScreen
import com.jvoice.aishorts.ui.GenerationReviewScreen
import com.jvoice.aishorts.ui.MediaSelectionScreen
import com.jvoice.aishorts.ui.TemplateManagementScreen
import com.jvoice.aishorts.ui.TemplateSelectionScreen
import com.jvoice.aishorts.ui.TrimEditorScreen
import com.jvoice.aishorts.ui.VoiceSelectionScreen
import com.jvoice.core.flags.FeatureFlags
import com.jvoice.core.flags.FlaggedRoute
import com.jvoice.news.data.model.User
import com.jvoice.study.data.mock.MockDataSource
import com.jvoice.study.data.model.StudyRole
import com.jvoice.study.data.model.StudyUser
import com.jvoice.study.navigation.studentDestinations
import com.jvoice.news.data.model.UserRole
import com.jvoice.news.ui.admin.AdminDashboardScreen
import com.jvoice.news.ui.admin.AdminViewModel
import com.jvoice.news.ui.admin.CategoryManagementScreen
import com.jvoice.news.ui.admin.NewsManagementScreen
import com.jvoice.news.ui.admin.ReporterManagementScreen
import com.jvoice.news.ui.editor.ArticleReviewScreen
import com.jvoice.news.ui.editor.EditorDashboardScreen
import com.jvoice.news.ui.editor.EditorViewModel
import com.jvoice.news.ui.editor.ReviewQueueScreen
import com.jvoice.news.ui.reader.CategoryNewsScreen
import com.jvoice.news.ui.reader.CommentsScreen
import com.jvoice.news.ui.reader.NewsFlipScreen
import com.jvoice.news.ui.reader.VideoClipsScreen
import com.jvoice.news.ui.reader.NewsDetailScreen
import com.jvoice.news.ui.reader.ReaderCategoriesScreen
import com.jvoice.news.ui.reader.ReaderHomeScreen
import com.jvoice.news.ui.reader.ReaderNotificationsScreen
import com.jvoice.news.ui.reader.ReaderProfileScreen
import com.jvoice.news.ui.reader.ReaderSavedScreen
import com.jvoice.news.ui.reader.ReaderViewModel
import com.jvoice.news.ui.reader.SearchScreen
import com.jvoice.news.ui.reporter.CreateNewsScreen
import com.jvoice.news.ui.reporter.MyNewsScreen
import com.jvoice.news.ui.reporter.ReporterDashboardScreen
import com.jvoice.news.ui.reporter.ReporterViewModel
import com.jvoice.news.ui.superadmin.RoleManagementScreen
import com.jvoice.news.ui.superadmin.SuperAdminDashboardScreen
import com.jvoice.news.ui.superadmin.SuperAdminViewModel
import com.jvoice.news.ui.superadmin.SystemSettingsScreen
import com.jvoice.news.ui.superadmin.UserManagementScreen

/**
 * One NavHost per signed-in role. The role selector lives outside the graph
 * (see MainActivity) so switching role rebuilds the whole graph cleanly.
 */
@Composable
fun JVoiceNavGraph(
    navController: NavHostController,
    user: User,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onSignOut: () -> Unit
) {
    val startDestination = when (user.role) {
        UserRole.READER -> Routes.READER_HOME
        UserRole.REPORTER -> Routes.REPORTER_DASHBOARD
        UserRole.EDITOR -> Routes.EDITOR_DASHBOARD
        UserRole.NEWS_ADMIN -> Routes.ADMIN_DASHBOARD
        UserRole.SUPER_ADMIN -> Routes.SUPER_DASHBOARD
    }

    NavHost(navController = navController, startDestination = startDestination) {
        readerGraph(navController, user, isDarkTheme, onToggleTheme, onSignOut)
        // Module 2's Student screens, hosted inside the News NavHost. They keep
        // their own bottom bar — News, Home, Study, Exams, Ranks, Profile — so the
        // module stays navigable after crossing over from News.
        //
        // The News tab is the way back and it is a plain tab switch, not a module
        // crossing: the reader's feed is a destination in *this* NavHost, so
        // switching to it keeps the reader's scroll position and back stack
        // instead of rebuilding the session.
        studentDestinations(
            navController = navController,
            user = studyDemoStudent(),
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            onSignOut = onSignOut,
            onOpenNews = { navController.switchTab(Routes.READER_HOME) }
        )
        reporterGraph(navController, user, onSignOut)
        editorGraph(navController, onSignOut)
        adminGraph(navController, onSignOut)
        superAdminGraph(navController, onSignOut)
        aiShortsGraph(navController, user, onSignOut)
    }
}

/** Switch between top-level destinations without stacking duplicates. */
private fun NavHostController.switchTab(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/* ------------------------------------------------------------------ reader */

private fun NavGraphBuilder.readerGraph(
    navController: NavHostController,
    user: User,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onSignOut: () -> Unit
) {
    val openArticle: (String) -> Unit = { id -> navController.navigate(Routes.newsDetail(id)) }
    val openComments: (String) -> Unit = { id -> navController.navigate(Routes.comments(id)) }
    val openCategory: (String) -> Unit = { id -> navController.navigate(Routes.categoryNews(id)) }

    // News tab: short-news flip cards with a category chip row on top.
    composable(Routes.READER_HOME) {
        val vm: ReaderViewModel = viewModel()
        NewsFlipScreen(
            viewModel = vm,
            onOpenArticle = openArticle,
            onOpenComments = openComments,
            onOpenSearch = { navController.navigate(Routes.READER_SEARCH) },
            onOpenNotifications = { navController.navigate(Routes.READER_NOTIFICATIONS) },
            onOpenClassicFeed = { navController.navigate(Routes.READER_FEED) },
            bottomBar = { ReaderBar(navController, vm) }
        )
    }

    // The original sectioned feed, still available from the News top bar.
    composable(Routes.READER_FEED) {
        val vm: ReaderViewModel = viewModel()
        ReaderHomeScreen(
            viewModel = vm,
            onOpenArticle = openArticle,
            onOpenSearch = { navController.navigate(Routes.READER_SEARCH) },
            onOpenNotifications = { navController.navigate(Routes.READER_NOTIFICATIONS) },
            onOpenCategory = openCategory,
            bottomBar = { ReaderBar(navController, vm) }
        )
    }

    // Clips tab: short-form vertical video, behind the shortsTab flag.
    //
    // Guarded here as well as in ReaderBottomBar because hiding the tab only
    // closes the entrance - see FlaggedRoute. The redirect pops Clips itself
    // rather than switching tabs, so Back does not walk straight back into a
    // flow that is turned off.
    composable(Routes.READER_CLIPS) {
        FlaggedRoute(
            key = FeatureFlags.Keys.SHORTS_TAB,
            onBlocked = {
                navController.navigate(Routes.READER_HOME) {
                    popUpTo(Routes.READER_CLIPS) { inclusive = true }
                    launchSingleTop = true
                }
            }
        ) {
            val vm: ReaderViewModel = viewModel()
            VideoClipsScreen(
                viewModel = vm,
                onOpenArticle = openArticle,
                bottomBar = { ReaderBar(navController, vm) }
            )
        }
    }

    composable(Routes.READER_CATEGORIES) {
        val vm: ReaderViewModel = viewModel()
        ReaderCategoriesScreen(
            viewModel = vm,
            onOpenCategory = openCategory,
            bottomBar = { ReaderBar(navController, vm) }
        )
    }

    composable(Routes.READER_SAVED) {
        val vm: ReaderViewModel = viewModel()
        ReaderSavedScreen(
            viewModel = vm,
            onOpenArticle = openArticle,
            bottomBar = { ReaderBar(navController, vm) }
        )
    }

    composable(Routes.READER_NOTIFICATIONS) {
        val vm: ReaderViewModel = viewModel()
        ReaderNotificationsScreen(
            viewModel = vm,
            onOpenArticle = openArticle,
            bottomBar = { ReaderBar(navController, vm) }
        )
    }

    composable(Routes.READER_PROFILE) {
        val vm: ReaderViewModel = viewModel()
        ReaderProfileScreen(
            user = user,
            viewModel = vm,
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            onSignOut = onSignOut,
            onOpenSaved = { navController.navigate(Routes.READER_SAVED) },
            onOpenCategories = { navController.navigate(Routes.READER_CATEGORIES) },
            onOpenNotifications = { navController.navigate(Routes.READER_NOTIFICATIONS) },
            bottomBar = { ReaderBar(navController, vm) }
        )
    }

    composable(
        route = Routes.READER_COMMENTS,
        arguments = listOf(navArgument(Routes.ARG_ARTICLE_ID) { type = NavType.StringType })
    ) { entry ->
        val vm: ReaderViewModel = viewModel()
        CommentsScreen(
            viewModel = vm,
            articleId = entry.arguments?.getString(Routes.ARG_ARTICLE_ID).orEmpty(),
            authorName = user.name,
            onBack = { navController.popBackStack() }
        )
    }

    composable(Routes.READER_SEARCH) {
        val vm: ReaderViewModel = viewModel()
        SearchScreen(
            viewModel = vm,
            onOpenArticle = openArticle,
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = Routes.NEWS_DETAIL,
        arguments = listOf(navArgument(Routes.ARG_ARTICLE_ID) { type = NavType.StringType })
    ) { entry ->
        val vm: ReaderViewModel = viewModel()
        NewsDetailScreen(
            viewModel = vm,
            articleId = entry.arguments?.getString(Routes.ARG_ARTICLE_ID).orEmpty(),
            onOpenArticle = openArticle,
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = Routes.CATEGORY_NEWS,
        arguments = listOf(navArgument(Routes.ARG_CATEGORY_ID) { type = NavType.StringType })
    ) { entry ->
        val vm: ReaderViewModel = viewModel()
        CategoryNewsScreen(
            viewModel = vm,
            categoryId = entry.arguments?.getString(Routes.ARG_CATEGORY_ID).orEmpty(),
            onOpenArticle = openArticle,
            onBack = { navController.popBackStack() }
        )
    }
}

@Composable
private fun ReaderBar(navController: NavHostController, viewModel: ReaderViewModel) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val unread by viewModel.unreadCount.collectAsState()
    ReaderBottomBar(
        currentRoute = backStackEntry?.destination?.route,
        unreadCount = unread,
        onNavigate = { route -> navController.switchTab(route) }
    )
}

/** The same pill bar, for study destinations hosted inside the News shell. */
@Composable
private fun ReaderPillBar(navController: NavHostController) {
    val vm: ReaderViewModel = viewModel()
    ReaderBar(navController, vm)
}

/** The demo student identity used when Study is browsed from the News shell. */
private fun studyDemoStudent(): StudyUser =
    MockDataSource.demoAccounts[StudyRole.STUDENT]
        ?: MockDataSource.users.first { it.role == StudyRole.STUDENT }

/* ------------------------------------------------------------------ reporter */

private fun NavGraphBuilder.reporterGraph(
    navController: NavHostController,
    user: User,
    onSignOut: () -> Unit
) {
    composable(Routes.REPORTER_DASHBOARD) {
        val vm: ReporterViewModel = viewModel()
        ReporterDashboardScreen(
            user = user,
            viewModel = vm,
            onCreateNews = { navController.navigate(Routes.reporterCreate()) },
            onOpenMyNews = { navController.navigate(Routes.REPORTER_MY_NEWS) },
            onEditArticle = { id -> navController.navigate(Routes.reporterEdit(id)) },
            onSignOut = onSignOut
        )
    }

    composable(Routes.REPORTER_MY_NEWS) {
        val vm: ReporterViewModel = viewModel()
        MyNewsScreen(
            viewModel = vm,
            onEditArticle = { id -> navController.navigate(Routes.reporterEdit(id)) },
            onCreateNews = { navController.navigate(Routes.reporterCreate()) }
        )
    }

    composable(
        route = Routes.REPORTER_EDITOR,
        arguments = listOf(
            navArgument(Routes.ARG_ARTICLE_ID) {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) { entry ->
        val vm: ReporterViewModel = viewModel()
        CreateNewsScreen(
            viewModel = vm,
            articleId = entry.arguments?.getString(Routes.ARG_ARTICLE_ID),
            onDone = { navController.popBackStack() },
            onBack = { navController.popBackStack() }
        )
    }
}

/* ------------------------------------------------------------------ editor */

private fun NavGraphBuilder.editorGraph(
    navController: NavHostController,
    onSignOut: () -> Unit
) {
    composable(Routes.EDITOR_DASHBOARD) {
        val vm: EditorViewModel = viewModel()
        EditorDashboardScreen(
            viewModel = vm,
            onNavigate = { route -> navController.switchTab(route) },
            onOpenReview = { id -> navController.navigate(Routes.editorReview(id)) },
            onSignOut = onSignOut
        )
    }

    composable(Routes.EDITOR_QUEUE) {
        val vm: EditorViewModel = viewModel()
        ReviewQueueScreen(
            viewModel = vm,
            onNavigate = { route -> navController.switchTab(route) },
            onOpenReview = { id -> navController.navigate(Routes.editorReview(id)) },
            onSignOut = onSignOut
        )
    }

    composable(
        route = Routes.EDITOR_REVIEW,
        arguments = listOf(navArgument(Routes.ARG_ARTICLE_ID) { type = NavType.StringType })
    ) { entry ->
        val vm: EditorViewModel = viewModel()
        ArticleReviewScreen(
            viewModel = vm,
            articleId = entry.arguments?.getString(Routes.ARG_ARTICLE_ID).orEmpty(),
            onDone = { navController.popBackStack() },
            onBack = { navController.popBackStack() },
            onCreateAIShort = { newsId -> navController.navigate(AIShortRoutes.create(newsId)) }
        )
    }
}

/* ------------------------------------------------------------------ news admin */

private fun NavGraphBuilder.adminGraph(
    navController: NavHostController,
    onSignOut: () -> Unit
) {
    composable(Routes.ADMIN_DASHBOARD) {
        val vm: AdminViewModel = viewModel()
        AdminDashboardScreen(
            viewModel = vm,
            onNavigate = { route -> navController.switchTab(route) },
            onSignOut = onSignOut
        )
    }

    composable(Routes.ADMIN_NEWS) {
        val vm: AdminViewModel = viewModel()
        NewsManagementScreen(
            viewModel = vm,
            onNavigate = { route -> navController.switchTab(route) },
            onOpenArticle = { id -> navController.navigate(Routes.editorReview(id)) },
            onSignOut = onSignOut
        )
    }

    composable(Routes.ADMIN_CATEGORIES) {
        val vm: AdminViewModel = viewModel()
        CategoryManagementScreen(
            viewModel = vm,
            onNavigate = { route -> navController.switchTab(route) },
            onSignOut = onSignOut
        )
    }

    composable(Routes.ADMIN_REPORTERS) {
        val vm: AdminViewModel = viewModel()
        ReporterManagementScreen(
            viewModel = vm,
            onNavigate = { route -> navController.switchTab(route) },
            onSignOut = onSignOut
        )
    }
}

/* ------------------------------------------------------------------ super admin */

private fun NavGraphBuilder.superAdminGraph(
    navController: NavHostController,
    onSignOut: () -> Unit
) {
    composable(Routes.SUPER_DASHBOARD) {
        val vm: SuperAdminViewModel = viewModel()
        SuperAdminDashboardScreen(
            viewModel = vm,
            onNavigate = { route -> navController.switchTab(route) },
            onSignOut = onSignOut
        )
    }

    composable(Routes.SUPER_USERS) {
        val vm: SuperAdminViewModel = viewModel()
        UserManagementScreen(
            viewModel = vm,
            onNavigate = { route -> navController.switchTab(route) },
            onSignOut = onSignOut
        )
    }

    composable(Routes.SUPER_ROLES) {
        val vm: SuperAdminViewModel = viewModel()
        RoleManagementScreen(
            viewModel = vm,
            onNavigate = { route -> navController.switchTab(route) },
            onSignOut = onSignOut
        )
    }

    composable(Routes.SUPER_SETTINGS) {
        val vm: SuperAdminViewModel = viewModel()
        SystemSettingsScreen(
            viewModel = vm,
            onNavigate = { route -> navController.switchTab(route) },
            onOpenCategories = { navController.navigate(Routes.ADMIN_CATEGORIES) },
            onSignOut = onSignOut
        )
    }
}

/* ---------------------------------------------------------------- ai shorts */

private fun NavGraphBuilder.aiShortsGraph(
    navController: NavHostController,
    user: User,
    onSignOut: () -> Unit
) {
    val canApprove = user.role == UserRole.EDITOR ||
        user.role == UserRole.NEWS_ADMIN ||
        user.role == UserRole.SUPER_ADMIN
    val canPublish = user.role == UserRole.NEWS_ADMIN || user.role == UserRole.SUPER_ADMIN

    composable(AIShortRoutes.DASHBOARD) {
        val vm: AIShortViewModel = viewModel()
        AIShortsDashboardScreen(
            viewModel = vm,
            role = user.role,
            onNavigate = { route -> navController.switchTab(route) },
            onOpenShort = { id -> navController.navigate(AIShortRoutes.preview(id)) },
            onContinueSetup = { id -> navController.navigate(AIShortRoutes.script(id)) },
            onSignOut = onSignOut
        )
    }

    composable(AIShortRoutes.TEMPLATE_ADMIN) {
        val vm: AIShortViewModel = viewModel()
        TemplateManagementScreen(
            viewModel = vm,
            role = user.role,
            onNavigate = { route -> navController.switchTab(route) },
            onSignOut = onSignOut
        )
    }

    composable(
        route = AIShortRoutes.CREATE,
        arguments = listOf(navArgument(AIShortRoutes.ARG_NEWS_ID) { type = NavType.StringType })
    ) { entry ->
        val vm: AIShortViewModel = viewModel()
        CreateAIShortScreen(
            viewModel = vm,
            newsId = entry.arguments?.getString(AIShortRoutes.ARG_NEWS_ID).orEmpty(),
            createdBy = user.name,
            onOpenStep = { route -> navController.navigate(route) },
            onBack = { navController.popBackStack() },
            routeForScript = { AIShortRoutes.script(it) },
            routeForTemplate = { AIShortRoutes.template(it) },
            routeForVoice = { AIShortRoutes.voice(it) },
            routeForMedia = { AIShortRoutes.media(it) },
            routeForReview = { AIShortRoutes.review(it) }
        )
    }

    composable(
        route = AIShortRoutes.SCRIPT,
        arguments = listOf(navArgument(AIShortRoutes.ARG_SHORT_ID) { type = NavType.StringType })
    ) { entry ->
        val vm: AIShortViewModel = viewModel()
        val id = entry.arguments?.getString(AIShortRoutes.ARG_SHORT_ID).orEmpty()
        AIScriptScreen(
            viewModel = vm,
            shortId = id,
            onContinue = { navController.navigate(AIShortRoutes.template(id)) },
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = AIShortRoutes.TEMPLATE,
        arguments = listOf(navArgument(AIShortRoutes.ARG_SHORT_ID) { type = NavType.StringType })
    ) { entry ->
        val vm: AIShortViewModel = viewModel()
        val id = entry.arguments?.getString(AIShortRoutes.ARG_SHORT_ID).orEmpty()
        TemplateSelectionScreen(
            viewModel = vm,
            shortId = id,
            onContinue = { navController.navigate(AIShortRoutes.voice(id)) },
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = AIShortRoutes.VOICE,
        arguments = listOf(navArgument(AIShortRoutes.ARG_SHORT_ID) { type = NavType.StringType })
    ) { entry ->
        val vm: AIShortViewModel = viewModel()
        val id = entry.arguments?.getString(AIShortRoutes.ARG_SHORT_ID).orEmpty()
        VoiceSelectionScreen(
            viewModel = vm,
            shortId = id,
            onContinue = { navController.navigate(AIShortRoutes.media(id)) },
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = AIShortRoutes.MEDIA,
        arguments = listOf(navArgument(AIShortRoutes.ARG_SHORT_ID) { type = NavType.StringType })
    ) { entry ->
        val vm: AIShortViewModel = viewModel()
        val id = entry.arguments?.getString(AIShortRoutes.ARG_SHORT_ID).orEmpty()
        MediaSelectionScreen(
            viewModel = vm,
            shortId = id,
            onContinue = { navController.navigate(AIShortRoutes.review(id)) },
            onTrim = { sceneId -> navController.navigate(AIShortRoutes.trim(id, sceneId)) },
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = AIShortRoutes.REVIEW,
        arguments = listOf(navArgument(AIShortRoutes.ARG_SHORT_ID) { type = NavType.StringType })
    ) { entry ->
        val vm: AIShortViewModel = viewModel()
        val id = entry.arguments?.getString(AIShortRoutes.ARG_SHORT_ID).orEmpty()
        GenerationReviewScreen(
            viewModel = vm,
            shortId = id,
            onGenerate = { navController.navigate(AIShortRoutes.progress(id)) },
            onOpenStep = { route -> navController.navigate(route) },
            onBack = { navController.popBackStack() },
            routeForScript = { AIShortRoutes.script(it) },
            routeForTemplate = { AIShortRoutes.template(it) },
            routeForVoice = { AIShortRoutes.voice(it) },
            routeForMedia = { AIShortRoutes.media(it) }
        )
    }

    composable(
        route = AIShortRoutes.PROGRESS,
        arguments = listOf(navArgument(AIShortRoutes.ARG_SHORT_ID) { type = NavType.StringType })
    ) { entry ->
        val vm: AIShortViewModel = viewModel()
        val id = entry.arguments?.getString(AIShortRoutes.ARG_SHORT_ID).orEmpty()
        GenerationProgressScreen(
            viewModel = vm,
            shortId = id,
            onFinished = {
                navController.navigate(AIShortRoutes.preview(id)) {
                    popUpTo(AIShortRoutes.PROGRESS) { inclusive = true }
                }
            },
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = AIShortRoutes.TRIM,
        arguments = listOf(
            navArgument(AIShortRoutes.ARG_SHORT_ID) { type = NavType.StringType },
            navArgument(AIShortRoutes.ARG_SCENE_ID) { type = NavType.StringType }
        )
    ) { entry ->
        val vm: AIShortViewModel = viewModel()
        TrimEditorScreen(
            viewModel = vm,
            shortId = entry.arguments?.getString(AIShortRoutes.ARG_SHORT_ID).orEmpty(),
            sceneId = entry.arguments?.getString(AIShortRoutes.ARG_SCENE_ID).orEmpty(),
            onDone = { navController.popBackStack() },
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = AIShortRoutes.PREVIEW,
        arguments = listOf(navArgument(AIShortRoutes.ARG_SHORT_ID) { type = NavType.StringType })
    ) { entry ->
        val vm: AIShortViewModel = viewModel()
        val id = entry.arguments?.getString(AIShortRoutes.ARG_SHORT_ID).orEmpty()
        AIShortPreviewScreen(
            viewModel = vm,
            shortId = id,
            canApprove = canApprove,
            canPublish = canPublish,
            onEditStep = { route -> navController.navigate(route) },
            onRegenerateScript = { navController.navigate(AIShortRoutes.script(id)) },
            onBack = { navController.popBackStack() },
            routeForScript = { AIShortRoutes.script(it) },
            routeForTemplate = { AIShortRoutes.template(it) },
            routeForVoice = { AIShortRoutes.voice(it) },
            routeForMedia = { AIShortRoutes.media(it) }
        )
    }
}
