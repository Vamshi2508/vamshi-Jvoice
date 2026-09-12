package com.jvoice.study.navigation

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
import com.jvoice.study.data.model.ExamType
import com.jvoice.shell.LocalModuleSwitcher
import com.jvoice.study.data.model.StudyRole
import com.jvoice.study.data.model.StudyUser
import com.jvoice.study.ui.creator.ArticleEditorScreen
import com.jvoice.study.ui.creator.CreatorArticlesScreen
import com.jvoice.study.ui.creator.CreatorDashboardScreen
import com.jvoice.study.ui.creator.CreatorQuestionsScreen
import com.jvoice.study.ui.creator.CreatorViewModel
import com.jvoice.study.ui.creator.QuestionEditorScreen
import com.jvoice.study.ui.examadmin.ExamAdminDashboardScreen
import com.jvoice.study.ui.examadmin.ExamAdminViewModel
import com.jvoice.study.ui.examadmin.ExamEditorScreen
import com.jvoice.study.ui.examadmin.ExamListScreen
import com.jvoice.study.ui.examadmin.ExamResultsAdminScreen
import com.jvoice.study.ui.examadmin.QuestionBankScreen
import com.jvoice.study.ui.student.AnalysisScreen
import com.jvoice.study.ui.student.ChooseExamPrompt
import com.jvoice.study.ui.student.ExamResultScreen
import com.jvoice.study.ui.student.ExamPickerScreen
import com.jvoice.study.ui.student.ExamRunnerScreen
import com.jvoice.study.ui.student.ExamViewModel
import com.jvoice.study.ui.student.ExamKeyDatesScreen
import com.jvoice.study.ui.student.ExamTrackHubScreen
import com.jvoice.study.ui.student.ExamTrackViewModel
import com.jvoice.study.ui.student.ExamsHubScreen
import com.jvoice.study.ui.student.LeaderboardScreen
import com.jvoice.study.ui.student.LeaderboardViewModel
import com.jvoice.study.ui.student.PerformanceHistoryScreen
import com.jvoice.study.ui.student.PerformanceViewModel
import com.jvoice.study.ui.student.ReviewAnswersScreen
import com.jvoice.study.ui.student.RunnerMode
import com.jvoice.study.ui.student.StudentHomeScreen
import com.jvoice.study.ui.student.StudentHomeViewModel
import com.jvoice.study.ui.student.StudentProfileScreen
import com.jvoice.study.ui.student.StudyArticleScreen
import com.jvoice.study.ui.student.StudyBrowseScreen
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import com.jvoice.study.ui.student.ExamEntryScreen
import com.jvoice.study.data.repository.ExamRepository
import com.jvoice.study.data.repository.LeaderboardRepository
import com.jvoice.study.ui.student.PastPapersScreen
import com.jvoice.study.ui.student.StudyBrowseViewModel
import com.jvoice.study.ui.student.StudyNotificationsScreen
import com.jvoice.study.ui.student.StudyNotificationsViewModel
import com.jvoice.study.ui.student.SubjectAnalysisScreen
import com.jvoice.study.ui.student.SubjectTopicsScreen
import com.jvoice.study.ui.student.WeakAreasScreen
import com.jvoice.study.ui.student.WeakTopicScreen
import com.jvoice.study.ui.studyadmin.ContentManagementScreen
import com.jvoice.study.ui.studyadmin.ExamTypeEditorScreen
import com.jvoice.study.ui.studyadmin.ExamTypeManagementScreen
import com.jvoice.study.ui.studyadmin.StudyAdminDashboardScreen
import com.jvoice.study.ui.studyadmin.StudyAdminViewModel
import com.jvoice.study.ui.studyadmin.SubjectManagementScreen
import com.jvoice.study.ui.studyadmin.TopicManagementScreen
import com.jvoice.study.ui.superadmin.StudyRolePermissionsScreen
import com.jvoice.study.ui.superadmin.StudySuperAdminDashboardScreen
import com.jvoice.study.ui.superadmin.StudySuperAdminViewModel
import com.jvoice.study.ui.superadmin.StudyUserManagementScreen
import com.jvoice.core.i18n.current

/**
 * Module 2 navigation. One NavHost per signed-in role; the landing page lives
 * outside the graph so switching role rebuilds it cleanly.
 */
@Composable
fun StudyNavGraph(
    navController: NavHostController,
    user: StudyUser,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onSignOut: () -> Unit
) {
    val startDestination = when (user.role) {
        StudyRole.STUDENT -> StudyRoutes.HOME
        StudyRole.CONTENT_CREATOR -> StudyRoutes.CREATOR_DASHBOARD
        StudyRole.EXAM_ADMIN -> StudyRoutes.EXAM_ADMIN_DASHBOARD
        StudyRole.STUDY_ADMIN -> StudyRoutes.STUDY_ADMIN_DASHBOARD
        StudyRole.SUPER_ADMIN -> StudyRoutes.SUPER_DASHBOARD
    }

    NavHost(navController = navController, startDestination = startDestination) {
        studentDestinations(navController, user, isDarkTheme, onToggleTheme, onSignOut)
        creatorGraph(navController, user, onSignOut)
        examAdminGraph(navController, user, onSignOut)
        studyAdminGraph(navController, user, onSignOut)
        superAdminGraph(navController, onSignOut)
    }
}

private fun NavHostController.switchTab(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/* ================================================================ student */

/**
 * The Student destinations. Public and host-agnostic: the Study shell mounts them
 * with the student's own bottom bar, while the News shell mounts them behind its
 * own pill bar so "Study" behaves as a tab rather than a takeover.
 */
fun NavGraphBuilder.studentDestinations(
    navController: NavHostController,
    user: StudyUser,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onSignOut: () -> Unit,
    hostBar: (@Composable () -> Unit)? = null,
    /**
     * How the student bottom bar's News tab gets back to the reader.
     *
     * The News shell passes a plain tab switch - same NavHost, reader state kept.
     * The standalone Study module passes null and the bar falls back to crossing
     * modules through [com.jvoice.shell.ModuleSwitcher].
     */
    onOpenNews: (() -> Unit)? = null
) {
    val openTopic: (String) -> Unit = { id -> navController.navigate(StudyRoutes.article(id)) }
    val openResult: (String) -> Unit = { id -> navController.navigate(StudyRoutes.result(id)) }

    // The Study tab always starts here: the exam picker until an exam is chosen,
    // that exam hub afterwards. Every screen below it is scoped to the choice.
    composable(StudyRoutes.HOME) {
        val trackVm: ExamTrackViewModel = viewModel()
        val homeVm: StudentHomeViewModel = viewModel()
        val selectedId by trackVm.selectedId.collectAsState()
        val bar: @Composable () -> Unit =
            { if (hostBar != null) hostBar() else StudentBar(navController, homeVm, onOpenNews) }

        if (selectedId == null) {
            ExamPickerScreen(viewModel = trackVm, onSelected = {}, bottomBar = bar)
        } else {
            ExamTrackHubScreen(
                viewModel = trackVm,
                onChangeExam = { trackVm.changeExam() },
                onOpenSubject = { id -> navController.navigate(StudyRoutes.subjectTopics(id)) },
                onOpenTopic = openTopic,
                onStartQuiz = { id -> navController.navigate(StudyRoutes.quizRunner(id)) },
                onStartExam = { id -> navController.navigate(StudyRoutes.examRunner(id)) },
                onOpenExams = { navController.switchTab(StudyRoutes.EXAMS) },
                onOpenBrowse = { navController.switchTab(StudyRoutes.BROWSE) },
                onOpenLeaderboard = { navController.switchTab(StudyRoutes.LEADERBOARD) },
                onOpenResult = openResult,
                onOpenPerformance = { navController.navigate(StudyRoutes.PERFORMANCE) },
                onOpenWeakAreas = { navController.navigate(StudyRoutes.WEAK_AREAS) },
                onOpenNotifications = { navController.navigate(StudyRoutes.NOTIFICATIONS) },
                onOpenKeyDates = { navController.navigate(StudyRoutes.KEY_DATES) },
                onOpenPastPapers = { navController.navigate(StudyRoutes.PAST_PAPERS) },
                onOpenDashboard = { navController.navigate(StudyRoutes.DASHBOARD) },
                bottomBar = bar
            )
        }
    }

    /** Reached from "Change exam" elsewhere; selecting one pops back. */
    composable(StudyRoutes.TRACKS) {
        val trackVm: ExamTrackViewModel = viewModel()
        val homeVm: StudentHomeViewModel = viewModel()
        ExamPickerScreen(
            viewModel = trackVm,
            onSelected = { navController.popBackStack() },
            bottomBar = { if (hostBar != null) hostBar() else StudentBar(navController, homeVm, onOpenNews) }
        )
    }

    /** Everything real papers have already asked, grouped by subject. */
    composable(StudyRoutes.PAST_PAPERS) {
        val vm: StudyBrowseViewModel = viewModel()
        val homeVm: StudentHomeViewModel = viewModel()
        PastPapersScreen(
            viewModel = vm,
            onOpenTopic = openTopic,
            onBack = { navController.popBackStack() },
            bottomBar = { if (hostBar != null) hostBar() else StudentBar(navController, homeVm, onOpenNews) }
        )
    }

    /** The full study dashboard - progress, recommendations, recent activity. */
    composable(StudyRoutes.DASHBOARD) {
        val vm: StudentHomeViewModel = viewModel()
        StudentHomeScreen(
            viewModel = vm,
            onStartExam = { id -> navController.navigate(StudyRoutes.examRunner(id)) },
            onOpenGrandTest = { navController.switchTab(StudyRoutes.EXAMS) },
            onOpenTopic = openTopic,
            onOpenSubjectAnalysis = { id -> navController.navigate(StudyRoutes.subjectAnalysis(id)) },
            onOpenAnalysis = { navController.navigate(StudyRoutes.ANALYSIS) },
            onOpenWeakAreas = { navController.navigate(StudyRoutes.WEAK_AREAS) },
            onOpenLeaderboard = { navController.switchTab(StudyRoutes.LEADERBOARD) },
            onOpenNotifications = { navController.navigate(StudyRoutes.NOTIFICATIONS) },
            onOpenPerformance = { navController.navigate(StudyRoutes.PERFORMANCE) },
            onOpenResult = openResult,
            bottomBar = { if (hostBar != null) hostBar() else StudentBar(navController, vm, onOpenNews) }
        )
    }

    composable(StudyRoutes.BROWSE) {
        val vm: StudyBrowseViewModel = viewModel()
        val homeVm: StudentHomeViewModel = viewModel()
        val trackVm: ExamTrackViewModel = viewModel()
        val selectedId by trackVm.selectedId.collectAsState()
        val bar: @Composable () -> Unit =
            { if (hostBar != null) hostBar() else StudentBar(navController, homeVm, onOpenNews) }

        if (selectedId == null) {
            ChooseExamPrompt(
                title = "Study",
                onChooseExam = { navController.switchTab(StudyRoutes.HOME) },
                bottomBar = bar
            )
        } else {
            StudyBrowseScreen(
                viewModel = vm,
                onOpenSubject = { id -> navController.navigate(StudyRoutes.subjectTopics(id)) },
                onOpenTopic = openTopic,
                onChangeExam = { navController.navigate(StudyRoutes.TRACKS) },
                bottomBar = bar
            )
        }
    }

    composable(StudyRoutes.EXAMS) {
        val vm: ExamViewModel = viewModel()
        val homeVm: StudentHomeViewModel = viewModel()
        val trackVm: ExamTrackViewModel = viewModel()
        val selectedId by trackVm.selectedId.collectAsState()
        val bar: @Composable () -> Unit =
            { if (hostBar != null) hostBar() else StudentBar(navController, homeVm, onOpenNews) }

        if (selectedId == null) {
            ChooseExamPrompt(
                title = "Exams",
                onChooseExam = { navController.switchTab(StudyRoutes.HOME) },
                bottomBar = bar
            )
        } else {
            ExamsHubScreen(
                viewModel = vm,
                onStartExam = { id -> navController.navigate(StudyRoutes.examRunner(id)) },
                onOpenResults = { navController.navigate(StudyRoutes.PERFORMANCE) },
                onChangeExam = { navController.navigate(StudyRoutes.TRACKS) },
                bottomBar = bar
            )
        }
    }

    composable(StudyRoutes.LEADERBOARD) {
        val vm: LeaderboardViewModel = viewModel()
        val homeVm: StudentHomeViewModel = viewModel()
        val trackVm: ExamTrackViewModel = viewModel()
        val selectedId by trackVm.selectedId.collectAsState()
        val bar: @Composable () -> Unit =
            { if (hostBar != null) hostBar() else StudentBar(navController, homeVm, onOpenNews) }

        if (selectedId == null) {
            ChooseExamPrompt(
                title = "Ranks",
                onChooseExam = { navController.switchTab(StudyRoutes.HOME) },
                bottomBar = bar
            )
        } else {
            LeaderboardScreen(
                viewModel = vm,
                onChangeExam = { navController.navigate(StudyRoutes.TRACKS) },
                bottomBar = bar
            )
        }
    }

    composable(StudyRoutes.PROFILE) {
        val vm: PerformanceViewModel = viewModel()
        val homeVm: StudentHomeViewModel = viewModel()
        StudentProfileScreen(
            user = user,
            performanceViewModel = vm,
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            onOpenPerformance = { navController.navigate(StudyRoutes.PERFORMANCE) },
            onOpenAnalysis = { navController.navigate(StudyRoutes.ANALYSIS) },
            onChangeExam = { navController.navigate(StudyRoutes.TRACKS) },
            onSignOut = onSignOut,
            bottomBar = { if (hostBar != null) hostBar() else StudentBar(navController, homeVm, onOpenNews) }
        )
    }

    composable(
        route = StudyRoutes.SUBJECT_TOPICS,
        arguments = listOf(navArgument(StudyRoutes.ARG_SUBJECT_ID) { type = NavType.StringType })
    ) { entry ->
        val vm: StudyBrowseViewModel = viewModel()
        SubjectTopicsScreen(
            viewModel = vm,
            subjectId = entry.arguments?.getString(StudyRoutes.ARG_SUBJECT_ID).orEmpty(),
            onOpenTopic = openTopic,
            onStartQuiz = { id -> navController.navigate(StudyRoutes.quizRunner(id)) },
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = StudyRoutes.ARTICLE,
        arguments = listOf(navArgument(StudyRoutes.ARG_TOPIC_ID) { type = NavType.StringType })
    ) { entry ->
        val vm: StudyBrowseViewModel = viewModel()
        StudyArticleScreen(
            viewModel = vm,
            topicId = entry.arguments?.getString(StudyRoutes.ARG_TOPIC_ID).orEmpty(),
            onStartQuiz = { id -> navController.navigate(StudyRoutes.quizRunner(id)) },
            onOpenTopic = { id ->
                navController.navigate(StudyRoutes.article(id)) { launchSingleTop = true }
            },
            onBack = { navController.popBackStack() }
        )
    }

    // ---- runners
    composable(
        route = StudyRoutes.EXAM_RUNNER,
        arguments = listOf(navArgument(StudyRoutes.ARG_EXAM_ID) { type = NavType.StringType })
    ) { entry ->
        val vm: ExamViewModel = viewModel()
        val examId = entry.arguments?.getString(StudyRoutes.ARG_EXAM_ID).orEmpty()

        // a scored exam asks who is sitting it before the timer starts
        var registered by rememberSaveable(examId) { mutableStateOf(false) }
        val exam = ExamRepository.examById(examId)

        if (!registered && exam != null) {
            ExamEntryScreen(
                examTitle = exam.title.current(),
                questionCount = exam.questionCount,
                durationMinutes = exam.durationMinutes,
                onStart = { name, mobile ->
                    LeaderboardRepository.setCandidate(name, mobile)
                    registered = true
                },
                onBack = { navController.popBackStack() }
            )
        } else {
            ExamRunnerScreen(
                viewModel = vm,
                mode = RunnerMode.EXAM,
                id = examId,
                onFinished = { resultId ->
                    navController.navigate(StudyRoutes.result(resultId)) {
                        popUpTo(StudyRoutes.EXAM_RUNNER) { inclusive = true }
                    }
                },
                onExit = { navController.popBackStack() }
            )
        }
    }

    composable(
        route = StudyRoutes.QUIZ_RUNNER,
        arguments = listOf(navArgument(StudyRoutes.ARG_TOPIC_ID) { type = NavType.StringType })
    ) { entry ->
        val vm: ExamViewModel = viewModel()
        ExamRunnerScreen(
            viewModel = vm,
            mode = RunnerMode.QUIZ,
            id = entry.arguments?.getString(StudyRoutes.ARG_TOPIC_ID).orEmpty(),
            onFinished = { resultId ->
                navController.navigate(StudyRoutes.result(resultId)) {
                    popUpTo(StudyRoutes.QUIZ_RUNNER) { inclusive = true }
                }
            },
            onExit = { navController.popBackStack() }
        )
    }

    composable(
        route = StudyRoutes.PRACTICE_RUNNER,
        arguments = listOf(navArgument(StudyRoutes.ARG_TOPIC_ID) { type = NavType.StringType })
    ) { entry ->
        val vm: ExamViewModel = viewModel()
        ExamRunnerScreen(
            viewModel = vm,
            mode = RunnerMode.PRACTICE,
            id = entry.arguments?.getString(StudyRoutes.ARG_TOPIC_ID).orEmpty(),
            onFinished = { resultId ->
                navController.navigate(StudyRoutes.result(resultId)) {
                    popUpTo(StudyRoutes.PRACTICE_RUNNER) { inclusive = true }
                }
            },
            onExit = { navController.popBackStack() }
        )
    }

    // ---- results
    composable(
        route = StudyRoutes.RESULT,
        arguments = listOf(navArgument(StudyRoutes.ARG_RESULT_ID) { type = NavType.StringType })
    ) { entry ->
        val vm: PerformanceViewModel = viewModel()
        ExamResultScreen(
            viewModel = vm,
            resultId = entry.arguments?.getString(StudyRoutes.ARG_RESULT_ID).orEmpty(),
            onReviewAnswers = { id -> navController.navigate(StudyRoutes.review(id)) },
            onStudyWeakTopics = { navController.navigate(StudyRoutes.WEAK_AREAS) },
            onOpenSubjectAnalysis = { id -> navController.navigate(StudyRoutes.subjectAnalysis(id)) },
            onBackHome = {
                navController.navigate(StudyRoutes.HOME) {
                    popUpTo(StudyRoutes.HOME) { inclusive = true }
                }
            },
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = StudyRoutes.REVIEW,
        arguments = listOf(navArgument(StudyRoutes.ARG_RESULT_ID) { type = NavType.StringType })
    ) { entry ->
        val vm: PerformanceViewModel = viewModel()
        ReviewAnswersScreen(
            viewModel = vm,
            resultId = entry.arguments?.getString(StudyRoutes.ARG_RESULT_ID).orEmpty(),
            onBack = { navController.popBackStack() }
        )
    }

    // ---- analysis
    composable(StudyRoutes.ANALYSIS) {
        val vm: PerformanceViewModel = viewModel()
        AnalysisScreen(
            viewModel = vm,
            onOpenSubject = { id -> navController.navigate(StudyRoutes.subjectAnalysis(id)) },
            onOpenWeakAreas = { navController.navigate(StudyRoutes.WEAK_AREAS) },
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = StudyRoutes.SUBJECT_ANALYSIS,
        arguments = listOf(navArgument(StudyRoutes.ARG_SUBJECT_ID) { type = NavType.StringType })
    ) { entry ->
        val vm: PerformanceViewModel = viewModel()
        SubjectAnalysisScreen(
            viewModel = vm,
            subjectId = entry.arguments?.getString(StudyRoutes.ARG_SUBJECT_ID).orEmpty(),
            onOpenTopic = { id -> navController.navigate(StudyRoutes.weakTopic(id)) },
            onBack = { navController.popBackStack() }
        )
    }

    composable(StudyRoutes.WEAK_AREAS) {
        val vm: PerformanceViewModel = viewModel()
        WeakAreasScreen(
            viewModel = vm,
            onOpenTopic = { id -> navController.navigate(StudyRoutes.weakTopic(id)) },
            onStartPractice = { id -> navController.navigate(StudyRoutes.practiceRunner(id)) },
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = StudyRoutes.WEAK_TOPIC,
        arguments = listOf(navArgument(StudyRoutes.ARG_TOPIC_ID) { type = NavType.StringType })
    ) { entry ->
        val vm: PerformanceViewModel = viewModel()
        WeakTopicScreen(
            viewModel = vm,
            topicId = entry.arguments?.getString(StudyRoutes.ARG_TOPIC_ID).orEmpty(),
            onReadArticle = openTopic,
            onTakeQuiz = { id -> navController.navigate(StudyRoutes.quizRunner(id)) },
            onStartPractice = { id -> navController.navigate(StudyRoutes.practiceRunner(id)) },
            onBack = { navController.popBackStack() }
        )
    }

    composable(StudyRoutes.PERFORMANCE) {
        val vm: PerformanceViewModel = viewModel()
        PerformanceHistoryScreen(
            viewModel = vm,
            onOpenResult = openResult,
            onOpenSubject = { id -> navController.navigate(StudyRoutes.subjectAnalysis(id)) },
            onBack = { navController.popBackStack() }
        )
    }

    /** "Important dates" from the exam hub's quick links. */
    composable(StudyRoutes.KEY_DATES) {
        val trackVm: ExamTrackViewModel = viewModel()
        val homeVm: StudentHomeViewModel = viewModel()
        ExamKeyDatesScreen(
            viewModel = trackVm,
            onBack = { navController.popBackStack() },
            bottomBar = { if (hostBar != null) hostBar() else StudentBar(navController, homeVm, onOpenNews) }
        )
    }

    composable(StudyRoutes.NOTIFICATIONS) {
        val vm: StudyNotificationsViewModel = viewModel()
        StudyNotificationsScreen(
            viewModel = vm,
            onOpenExam = { navController.switchTab(StudyRoutes.EXAMS) },
            onBack = { navController.popBackStack() }
        )
    }
}

@Composable
private fun StudentBar(
    navController: NavHostController,
    viewModel: StudentHomeViewModel,
    onOpenNews: (() -> Unit)?
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val unread by viewModel.unreadCount.collectAsState()
    // Inside the News shell the host supplies the way back. Standalone, the
    // module switcher is the only route out, so fall back to it rather than
    // dropping the tab and stranding the student again.
    val switcher = LocalModuleSwitcher.current
    val openNews = onOpenNews ?: switcher?.openNewsAsReader
    StudentBottomBar(
        currentRoute = backStackEntry?.destination?.route,
        unreadCount = unread,
        onNavigate = { route -> navController.switchTab(route) },
        onOpenNews = openNews
    )
}

/* ================================================================ creator */

private fun NavGraphBuilder.creatorGraph(
    navController: NavHostController,
    user: StudyUser,
    onSignOut: () -> Unit
) {
    composable(StudyRoutes.CREATOR_DASHBOARD) {
        val vm: CreatorViewModel = viewModel()
        CreatorDashboardScreen(
            viewModel = vm,
            onNavigate = { route -> navController.switchTab(route) },
            onNewArticle = { navController.navigate(StudyRoutes.creatorArticleNew()) },
            onEditArticle = { id -> navController.navigate(StudyRoutes.creatorArticleEdit(id)) },
            onSignOut = onSignOut
        )
    }

    composable(StudyRoutes.CREATOR_ARTICLES) {
        val vm: CreatorViewModel = viewModel()
        CreatorArticlesScreen(
            viewModel = vm,
            onNavigate = { route -> navController.switchTab(route) },
            onNewArticle = { navController.navigate(StudyRoutes.creatorArticleNew()) },
            onEditArticle = { id -> navController.navigate(StudyRoutes.creatorArticleEdit(id)) },
            onSignOut = onSignOut
        )
    }

    composable(StudyRoutes.CREATOR_QUESTIONS) {
        val vm: CreatorViewModel = viewModel()
        CreatorQuestionsScreen(
            viewModel = vm,
            onNavigate = { route -> navController.switchTab(route) },
            onNewQuestion = { navController.navigate(StudyRoutes.creatorQuestionNew()) },
            onEditQuestion = { id -> navController.navigate(StudyRoutes.creatorQuestionEdit(id)) },
            onSignOut = onSignOut
        )
    }

    composable(
        route = StudyRoutes.CREATOR_ARTICLE_EDITOR,
        arguments = listOf(
            navArgument(StudyRoutes.ARG_ARTICLE_ID) {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) { entry ->
        val vm: CreatorViewModel = viewModel()
        ArticleEditorScreen(
            viewModel = vm,
            articleId = entry.arguments?.getString(StudyRoutes.ARG_ARTICLE_ID),
            authorName = user.name,
            onDone = { navController.popBackStack() },
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = StudyRoutes.CREATOR_QUESTION_EDITOR,
        arguments = listOf(
            navArgument(StudyRoutes.ARG_QUESTION_ID) {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) { entry ->
        val vm: CreatorViewModel = viewModel()
        QuestionEditorScreen(
            viewModel = vm,
            questionId = entry.arguments?.getString(StudyRoutes.ARG_QUESTION_ID),
            onDone = { navController.popBackStack() },
            onBack = { navController.popBackStack() }
        )
    }
}

/* ============================================================= exam admin */

private fun NavGraphBuilder.examAdminGraph(
    navController: NavHostController,
    user: StudyUser,
    onSignOut: () -> Unit
) {
    composable(StudyRoutes.EXAM_ADMIN_DASHBOARD) {
        val vm: ExamAdminViewModel = viewModel()
        ExamAdminDashboardScreen(
            viewModel = vm,
            onNavigate = { route -> navController.switchTab(route) },
            onSignOut = onSignOut
        )
    }

    composable(StudyRoutes.EXAM_ADMIN_DAILY) {
        val vm: ExamAdminViewModel = viewModel()
        ExamListScreen(
            viewModel = vm,
            type = ExamType.DAILY,
            onNavigate = { route -> navController.switchTab(route) },
            onCreate = { navController.navigate(StudyRoutes.examEditorNew("DAILY")) },
            onEdit = { id -> navController.navigate(StudyRoutes.examEditorEdit(id, "DAILY")) },
            onViewResults = { navController.switchTab(StudyRoutes.EXAM_ADMIN_RESULTS) },
            onSignOut = onSignOut
        )
    }

    composable(StudyRoutes.EXAM_ADMIN_GRAND) {
        val vm: ExamAdminViewModel = viewModel()
        ExamListScreen(
            viewModel = vm,
            type = ExamType.GRAND_TEST,
            onNavigate = { route -> navController.switchTab(route) },
            onCreate = { navController.navigate(StudyRoutes.examEditorNew("GRAND_TEST")) },
            onEdit = { id -> navController.navigate(StudyRoutes.examEditorEdit(id, "GRAND_TEST")) },
            onViewResults = { navController.switchTab(StudyRoutes.EXAM_ADMIN_RESULTS) },
            onSignOut = onSignOut
        )
    }

    composable(StudyRoutes.EXAM_ADMIN_BANK) {
        val vm: ExamAdminViewModel = viewModel()
        QuestionBankScreen(
            viewModel = vm,
            role = user.role,
            currentRoute = StudyRoutes.EXAM_ADMIN_BANK,
            onNavigate = { route -> navController.switchTab(route) },
            onSignOut = onSignOut
        )
    }

    composable(StudyRoutes.EXAM_ADMIN_RESULTS) {
        val vm: ExamAdminViewModel = viewModel()
        ExamResultsAdminScreen(
            viewModel = vm,
            onNavigate = { route -> navController.switchTab(route) },
            onSignOut = onSignOut
        )
    }

    composable(
        route = StudyRoutes.EXAM_ADMIN_EDITOR,
        arguments = listOf(
            navArgument(StudyRoutes.ARG_EXAM_ID) {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument(StudyRoutes.ARG_TYPE) {
                type = NavType.StringType
                defaultValue = "DAILY"
            }
        )
    ) { entry ->
        val vm: ExamAdminViewModel = viewModel()
        val typeName = entry.arguments?.getString(StudyRoutes.ARG_TYPE) ?: "DAILY"
        ExamEditorScreen(
            viewModel = vm,
            examId = entry.arguments?.getString(StudyRoutes.ARG_EXAM_ID),
            type = if (typeName == "GRAND_TEST") ExamType.GRAND_TEST else ExamType.DAILY,
            onDone = { navController.popBackStack() },
            onBack = { navController.popBackStack() }
        )
    }
}

/* ============================================================ study admin */

private fun NavGraphBuilder.studyAdminGraph(
    navController: NavHostController,
    user: StudyUser,
    onSignOut: () -> Unit
) {
    composable(StudyRoutes.STUDY_ADMIN_DASHBOARD) {
        val vm: StudyAdminViewModel = viewModel()
        StudyAdminDashboardScreen(
            viewModel = vm,
            role = user.role,
            onNavigate = { route -> navController.switchTab(route) },
            onSignOut = onSignOut
        )
    }

    // Exam types are configured here; the student exam picker mirrors this list.
    composable(StudyRoutes.STUDY_ADMIN_EXAM_TYPES) {
        val vm: StudyAdminViewModel = viewModel()
        ExamTypeManagementScreen(
            viewModel = vm,
            role = user.role,
            onNavigate = { route -> navController.switchTab(route) },
            onCreate = { navController.navigate(StudyRoutes.trackEditorNew()) },
            onEdit = { id -> navController.navigate(StudyRoutes.trackEditorEdit(id)) },
            onSignOut = onSignOut
        )
    }

    composable(
        route = StudyRoutes.STUDY_ADMIN_TRACK_EDITOR,
        arguments = listOf(
            navArgument(StudyRoutes.ARG_TRACK_ID) {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) { entry ->
        val vm: StudyAdminViewModel = viewModel()
        ExamTypeEditorScreen(
            viewModel = vm,
            trackId = entry.arguments?.getString(StudyRoutes.ARG_TRACK_ID),
            onDone = { navController.popBackStack() },
            onBack = { navController.popBackStack() }
        )
    }

    composable(StudyRoutes.STUDY_ADMIN_SUBJECTS) {
        val vm: StudyAdminViewModel = viewModel()
        SubjectManagementScreen(
            viewModel = vm,
            role = user.role,
            onNavigate = { route -> navController.switchTab(route) },
            onSignOut = onSignOut
        )
    }

    composable(StudyRoutes.STUDY_ADMIN_TOPICS) {
        val vm: StudyAdminViewModel = viewModel()
        TopicManagementScreen(
            viewModel = vm,
            role = user.role,
            onNavigate = { route -> navController.switchTab(route) },
            onSignOut = onSignOut
        )
    }

    composable(StudyRoutes.STUDY_ADMIN_CONTENT) {
        val vm: StudyAdminViewModel = viewModel()
        ContentManagementScreen(
            viewModel = vm,
            role = user.role,
            onNavigate = { route -> navController.switchTab(route) },
            onSignOut = onSignOut
        )
    }

    composable(StudyRoutes.STUDY_ADMIN_BANK) {
        val vm: ExamAdminViewModel = viewModel()
        QuestionBankScreen(
            viewModel = vm,
            role = user.role,
            currentRoute = StudyRoutes.STUDY_ADMIN_BANK,
            onNavigate = { route -> navController.switchTab(route) },
            onSignOut = onSignOut
        )
    }
}

/* ============================================================ super admin */

private fun NavGraphBuilder.superAdminGraph(
    navController: NavHostController,
    onSignOut: () -> Unit
) {
    composable(StudyRoutes.SUPER_DASHBOARD) {
        val vm: StudySuperAdminViewModel = viewModel()
        StudySuperAdminDashboardScreen(
            viewModel = vm,
            onNavigate = { route -> navController.switchTab(route) },
            onSignOut = onSignOut
        )
    }

    composable(StudyRoutes.SUPER_USERS) {
        val vm: StudySuperAdminViewModel = viewModel()
        StudyUserManagementScreen(
            viewModel = vm,
            onNavigate = { route -> navController.switchTab(route) },
            onSignOut = onSignOut
        )
    }

    composable(StudyRoutes.SUPER_ROLES) {
        val vm: StudySuperAdminViewModel = viewModel()
        StudyRolePermissionsScreen(
            viewModel = vm,
            onNavigate = { route -> navController.switchTab(route) },
            onSignOut = onSignOut
        )
    }
}
