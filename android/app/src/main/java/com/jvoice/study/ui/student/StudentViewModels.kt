package com.jvoice.study.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvoice.study.data.model.Exam
import com.jvoice.study.data.model.ExamAttempt
import com.jvoice.study.data.model.ExamResult
import com.jvoice.study.data.model.ExamKeyDate
import com.jvoice.study.data.model.ExamSectionRow
import com.jvoice.study.data.model.ExamTrack
import com.jvoice.study.data.model.ExamTrackGroup
import com.jvoice.study.data.model.ExamType
import com.jvoice.study.data.model.LeaderboardEntry
import com.jvoice.study.data.model.LeaderboardPeriod
import com.jvoice.study.data.model.OverallStats
import com.jvoice.study.data.model.Question
import com.jvoice.study.data.model.StudyArticle
import com.jvoice.study.data.model.StudyNotification
import com.jvoice.study.data.model.StudyRole
import com.jvoice.study.data.model.Subject
import com.jvoice.study.data.model.SubjectPerformance
import com.jvoice.study.data.model.Topic
import com.jvoice.study.data.model.TopicPerformance
import com.jvoice.study.data.model.WeakAreaGroup
import com.jvoice.study.data.repository.ExamRepository
import com.jvoice.study.data.repository.ExamTrackRepository
import com.jvoice.study.data.repository.LeaderboardRepository
import com.jvoice.study.data.repository.PerformanceRepository
import com.jvoice.study.data.repository.StudyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/* ============================================================= exam tracks */

/** Everything the exam hub shows for the exam the student picked. */
data class TrackHubState(
    val track: ExamTrack? = null,
    val sections: List<ExamSectionRow> = emptyList(),
    val progress: Int = 0,
    val topicsCompleted: Int = 0,
    val totalTopics: Int = 0,
    val streakDays: Int = 0,
    val dailyExam: Exam? = null,
    val grandTest: Exam? = null,
    val dailyExams: List<Exam> = emptyList(),
    val grandTests: List<Exam> = emptyList(),
    val quizTopics: List<Topic> = emptyList(),
    val recentResults: List<ExamResult> = emptyList(),
    val rank: Int = 0,
    val participants: Int = 0,
    val points: Int = 0,
    val leaderboardPreview: List<LeaderboardEntry> = emptyList(),
    val weakAreas: List<WeakAreaGroup> = emptyList(),
    val continueTopic: Topic? = null,
    // quick-link and headline data
    val keyDates: List<ExamKeyDate> = emptyList(),
    val notifications: List<StudyNotification> = emptyList(),
    val articleCount: Int = 0,
    val quizCount: Int = 0,
    val pastPaperCount: Int = 0,
    val attemptCount: Int = 0
)

/**
 * Drives the exam picker and the exam hub. The picker is the first screen of the
 * Study tab; nothing is shown until an exam is chosen, and everything the hub
 * displays is scoped to that exam.
 */
class ExamTrackViewModel : ViewModel() {

    val tracks: StateFlow<List<ExamTrack>> = ExamTrackRepository.tracks
    val selectedId: StateFlow<String?> = ExamTrackRepository.selectedId

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val grouped: StateFlow<List<Pair<ExamTrackGroup, List<ExamTrack>>>> = combine(
        tracks,
        _query
    ) { _, query -> ExamTrackRepository.grouped(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val scope = combine(
        ExamTrackRepository.selectedId,
        ExamTrackRepository.tracks,
        StudyRepository.completedTopicIds,
        PerformanceRepository.topicTally
    ) { _, _, _, _ -> Unit }

    val hub: StateFlow<TrackHubState> = combine(
        scope,
        PerformanceRepository.results,
        ExamRepository.exams
    ) { _, results, _ -> buildHub(results) }
        // Seeded eagerly so the hub never flashes an empty state on entry.
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            buildHub(PerformanceRepository.results.value)
        )

    val unreadCount: StateFlow<Int> = StudyRepository.notifications
        .map { StudyRepository.unreadCountFor(StudyRole.STUDENT) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun setQuery(value: String) { _query.value = value }

    fun select(trackId: String) {
        ExamTrackRepository.select(trackId)
        _query.value = ""
    }

    /** "Change exam" - back to the picker. */
    fun changeExam() = ExamTrackRepository.clearSelection()

    fun summary(track: ExamTrack) = ExamTrackRepository.summary(track)
    fun subjectName(id: String) = StudyRepository.subjectName(id)
    fun topicName(id: String) = StudyRepository.topicName(id)
    fun quizForTopic(topicId: String) = StudyRepository.quizForTopic(topicId)
    fun questionCount(topicId: String) = StudyRepository.questionsForTopic(topicId).size
    fun articlesForTopic(topicId: String) = StudyRepository.articlesForTopic(topicId)
    fun articleCountForTopic(topicId: String) = StudyRepository.articlesForTopic(topicId).size
    fun quizSets(topicId: String) = StudyRepository.quizSetsForTopic(topicId)
    fun questionsOfQuiz(quiz: com.jvoice.study.data.model.Quiz) =
        StudyRepository.questionsOfQuiz(quiz)
    fun sampleQuestions(topicId: String) = StudyRepository.sampleQuestionsForTopic(topicId)
    fun previousQuestions(topicId: String) = StudyRepository.previousQuestionsForTopic(topicId)

    private fun buildHub(results: List<ExamResult>): TrackHubState {
        val track = ExamTrackRepository.selected() ?: return TrackHubState()
        val topics = ExamTrackRepository.topicsOf(track)
        val scopedSubjects = track.subjectIds.toSet()
        return TrackHubState(
            track = track,
            sections = ExamTrackRepository.sectionRows(track),
            progress = ExamTrackRepository.syllabusProgressPercent(track),
            topicsCompleted = topics.count { StudyRepository.isTopicCompleted(it.id) },
            totalTopics = topics.size,
            streakDays = StudyRepository.student.studyStreakDays,
            dailyExam = ExamRepository.todaysExam(),
            grandTest = ExamRepository.currentGrandTest(),
            dailyExams = ExamRepository.dailyExams(),
            grandTests = ExamRepository.grandTests(),
            quizTopics = topics.filter { StudyRepository.quizForTopic(it.id) != null }.take(6),
            recentResults = results
                .filter { it.subjectScores.isEmpty() || it.subjectScores.any { s -> scopedSubjects.contains(s.subjectId) } }
                .sortedByDescending { it.takenAt }
                .take(3),
            rank = LeaderboardRepository.rank(LeaderboardPeriod.WEEKLY),
            participants = LeaderboardRepository.participants(LeaderboardPeriod.WEEKLY),
            points = LeaderboardRepository.points(LeaderboardPeriod.WEEKLY),
            leaderboardPreview = LeaderboardRepository.topEntries(LeaderboardPeriod.WEEKLY, 3),
            weakAreas = PerformanceRepository.weakAreaGroups().take(3),
            continueTopic = StudyRepository.continueStudyingTopic(),
            keyDates = track.keyDates,
            notifications = StudyRepository.notificationsFor(StudyRole.STUDENT).take(3),
            articleCount = track.subjectIds.sumOf { StudyRepository.articleCountForSubject(it) },
            quizCount = topics.count { StudyRepository.quizForTopic(it.id) != null },
            pastPaperCount = topics.sumOf { StudyRepository.previousQuestionsForTopic(it.id).size },
            attemptCount = results.count {
                it.subjectScores.isEmpty() || it.subjectScores.any { s -> scopedSubjects.contains(s.subjectId) }
            }
        )
    }
}

/* ==================================================================== home */

data class HomeState(
    val greeting: String = "Good Morning",
    val studentName: String = "",
    val todayProgress: Int = 0,
    val todaysExam: Exam? = null,
    val grandTest: Exam? = null,
    val continueTopic: Topic? = null,
    val continueSubjectName: String = "",
    val recommended: List<TopicPerformance> = emptyList(),
    val strongSubjects: List<SubjectPerformance> = emptyList(),
    val weakSubjects: List<SubjectPerformance> = emptyList(),
    val recentQuizResults: List<ExamResult> = emptyList(),
    val rank: Int = 0,
    val participants: Int = 0,
    val leaderboardPreview: List<LeaderboardEntry> = emptyList(),
    val streakDays: Int = 0,
    val examName: String = ""
)

class StudentHomeViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val state: StateFlow<HomeState> = combine(
        StudyRepository.completedTopicIds,
        PerformanceRepository.topicTally,
        PerformanceRepository.results,
        ExamRepository.exams,
        ExamTrackRepository.selectedId
    ) { _, _, results, _, _ ->
        val continueTopic = StudyRepository.continueStudyingTopic()
        HomeState(
            greeting = greetingForHour(),
            studentName = StudyRepository.currentUser.value?.name ?: StudyRepository.student.name,
            todayProgress = PerformanceRepository.todayProgressPercent(),
            todaysExam = ExamRepository.todaysExam(),
            grandTest = ExamRepository.currentGrandTest(),
            continueTopic = continueTopic,
            continueSubjectName = continueTopic?.let { StudyRepository.subjectName(it.subjectId) } ?: "",
            recommended = PerformanceRepository.recommendedTopics(5),
            strongSubjects = PerformanceRepository.strongSubjects().take(3),
            weakSubjects = PerformanceRepository.weakSubjects().take(3),
            recentQuizResults = results
                .filter { it.type == ExamType.TOPIC_QUIZ || it.type == ExamType.PRACTICE || it.type == ExamType.DAILY }
                .sortedByDescending { it.takenAt }
                .take(3),
            rank = LeaderboardRepository.rank(LeaderboardPeriod.WEEKLY),
            participants = LeaderboardRepository.participants(LeaderboardPeriod.WEEKLY),
            leaderboardPreview = LeaderboardRepository.topEntries(LeaderboardPeriod.WEEKLY, 3),
            streakDays = StudyRepository.student.studyStreakDays,
            examName = ExamTrackRepository.selectedName()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeState())

    val unreadCount: StateFlow<Int> = StudyRepository.notifications
        .map { StudyRepository.unreadCountFor(StudyRole.STUDENT) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        viewModelScope.launch {
            delay(550)
            _isLoading.value = false
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(800)
            _isRefreshing.value = false
        }
    }

    private fun greetingForHour(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }
}

/* =================================================================== study */

class StudyBrowseViewModel : ViewModel() {

    /** Only the selected exam's subjects, in that exam's own section order. */
    val subjects: StateFlow<List<Subject>> = combine(
        StudyRepository.subjects,
        ExamTrackRepository.selectedId
    ) { _, _ -> ExamTrackRepository.subjectsOf(ExamTrackRepository.selected()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val completed: StateFlow<Set<String>> = StudyRepository.completedTopicIds

    val trackLabel: StateFlow<String?> = ExamTrackRepository.selectedId
        .map { ExamTrackRepository.selected()?.shortName }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val searchResults: StateFlow<List<StudyArticle>> = combine(
        _query,
        StudyRepository.articles
    ) { query, _ -> StudyRepository.searchContent(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) { _query.value = value }

    fun enabledSubjects() = StudyRepository.enabledSubjects()
    fun topicsOf(subjectId: String) = StudyRepository.topicsOf(subjectId)
    fun subjectById(id: String) = StudyRepository.subjectById(id)
    fun subjectName(id: String) = StudyRepository.subjectName(id)
    fun topicById(id: String) = StudyRepository.topicById(id)
    fun subjectProgress(subjectId: String) = StudyRepository.subjectProgressPercent(subjectId)
    fun articleForTopic(topicId: String) = StudyRepository.publishedArticleForTopic(topicId)
    fun quizForTopic(topicId: String) = StudyRepository.quizForTopic(topicId)
    fun questionCount(topicId: String) = StudyRepository.questionsForTopic(topicId).size
    fun articlesForTopic(topicId: String) = StudyRepository.articlesForTopic(topicId)
    fun articleCountForTopic(topicId: String) = StudyRepository.articlesForTopic(topicId).size
    fun quizSets(topicId: String) = StudyRepository.quizSetsForTopic(topicId)
    fun questionsOfQuiz(quiz: com.jvoice.study.data.model.Quiz) =
        StudyRepository.questionsOfQuiz(quiz)
    fun sampleQuestions(topicId: String) = StudyRepository.sampleQuestionsForTopic(topicId)
    fun previousQuestions(topicId: String) = StudyRepository.previousQuestionsForTopic(topicId)
    fun neighbours(topicId: String) = StudyRepository.neighbourTopics(topicId)
    fun isCompleted(topicId: String) = StudyRepository.isTopicCompleted(topicId)
    fun toggleCompleted(topicId: String) = StudyRepository.toggleTopicCompleted(topicId)
    fun topicAccuracy(topicId: String) = PerformanceRepository.topicPerformanceById(topicId)
    fun articleCount(subjectId: String) = StudyRepository.articleCountForSubject(subjectId)
    fun questionCountForSubject(subjectId: String) = StudyRepository.questionCountForSubject(subjectId)
}

/* ==================================================================== exam */

class ExamViewModel : ViewModel() {

    val attempt: StateFlow<ExamAttempt?> = ExamRepository.attempt
    val exams: StateFlow<List<Exam>> = ExamRepository.exams

    /** Papers for the selected exam plus general practice papers. */
    val scopedExams: StateFlow<List<Exam>> = combine(
        ExamRepository.exams,
        ExamTrackRepository.selectedId
    ) { list, trackId -> list.filter { it.belongsTo(trackId) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val trackLabel: StateFlow<String?> = ExamTrackRepository.selectedId
        .map { ExamTrackRepository.selected()?.shortName }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun isTrackExam(exam: Exam): Boolean =
        ExamTrackRepository.selectedId.value?.let { exam.trackIds.contains(it) } ?: false

    private val _submittedResult = MutableStateFlow<ExamResult?>(null)
    val submittedResult: StateFlow<ExamResult?> = _submittedResult.asStateFlow()

    private val _autoSubmitted = MutableStateFlow(false)
    val autoSubmitted: StateFlow<Boolean> = _autoSubmitted.asStateFlow()

    private var timerJob: Job? = null

    fun dailyExams() = ExamRepository.dailyExams()
    fun grandTests() = ExamRepository.grandTests()
    fun todaysExam() = ExamRepository.todaysExam()
    fun currentGrandTest() = ExamRepository.currentGrandTest()
    fun examById(id: String) = ExamRepository.examById(id)
    fun questionById(id: String): Question? = StudyRepository.questionById(id)
    fun subjectName(id: String) = StudyRepository.subjectName(id)
    fun topicName(id: String) = StudyRepository.topicName(id)

    // ------------------------------------------------------------ starting
    fun startExam(examId: String) {
        if (ExamRepository.attempt.value?.exam?.id == examId &&
            ExamRepository.attempt.value?.submitted == false
        ) {
            startTimer()
            return
        }
        ExamRepository.startExam(examId)
        _submittedResult.value = null
        _autoSubmitted.value = false
        startTimer()
    }

    fun startTopicQuiz(topicId: String) {
        val existing = ExamRepository.attempt.value
        if (existing != null && !existing.submitted && existing.exam.type == ExamType.TOPIC_QUIZ &&
            existing.exam.subjectIds.isNotEmpty() && existing.exam.id.endsWith(topicId)
        ) {
            startTimer()
            return
        }
        ExamRepository.startTopicQuiz(topicId)
        _submittedResult.value = null
        _autoSubmitted.value = false
        startTimer()
    }

    fun startPractice(topicId: String) {
        ExamRepository.startPracticeSet(topicId)
        _submittedResult.value = null
        _autoSubmitted.value = false
        startTimer()
    }

    /** One-second tick that auto-submits when the clock runs out. */
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                val current = ExamRepository.attempt.value ?: break
                if (current.submitted) break
                if (current.remainingSeconds <= 0) {
                    _autoSubmitted.value = true
                    submit()
                    break
                }
                ExamRepository.tick()
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    // ------------------------------------------------------------ answering
    fun select(index: Int) = ExamRepository.select(index)
    fun clearSelection() = ExamRepository.clearSelection()
    fun toggleMark() = ExamRepository.toggleMarkForReview()
    fun goTo(index: Int) = ExamRepository.goTo(index)
    fun next() = ExamRepository.next()
    fun previous() = ExamRepository.previous()

    fun submit(): ExamResult? {
        stopTimer()
        val result = ExamRepository.submitAttempt()
        _submittedResult.value = result
        return result
    }

    fun abandon() {
        stopTimer()
        ExamRepository.abandonAttempt()
        _submittedResult.value = null
    }

    override fun onCleared() {
        stopTimer()
        super.onCleared()
    }
}

/* ============================================================= performance */

class PerformanceViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val subjectPerformance: StateFlow<List<SubjectPerformance>> = combine(
        PerformanceRepository.topicTally,
        StudyRepository.subjects
    ) { _, _ -> PerformanceRepository.attemptedSubjects() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val weakAreas: StateFlow<List<WeakAreaGroup>> = PerformanceRepository.topicTally
        .map { PerformanceRepository.weakAreaGroups() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val stats: StateFlow<OverallStats> = combine(
        PerformanceRepository.results,
        StudyRepository.completedTopicIds
    ) { _, _ -> PerformanceRepository.overallStats() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OverallStats())

    val trend: StateFlow<List<Pair<String, Int>>> = PerformanceRepository.results
        .map { PerformanceRepository.recentExamTrend(7) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val results: StateFlow<List<ExamResult>> = PerformanceRepository.results

    init {
        viewModelScope.launch {
            delay(450)
            _isLoading.value = false
        }
    }

    val trackLabel: StateFlow<String?> = ExamTrackRepository.selectedId
        .map { ExamTrackRepository.selected()?.shortName }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun strong() = PerformanceRepository.strongSubjects()
    fun needsPractice() = PerformanceRepository.needsPracticeSubjects()
    fun weak() = PerformanceRepository.weakSubjects()
    fun subjectById(id: String) = StudyRepository.subjectById(id)
    fun subjectPerformanceById(id: String) = PerformanceRepository.subjectPerformanceById(id)
    fun topicsFor(subjectId: String) = PerformanceRepository.topicPerformanceFor(subjectId)
    fun topicPerformance(topicId: String) = PerformanceRepository.topicPerformanceById(topicId)
    fun studyPlan(topicId: String) = PerformanceRepository.studyPlanFor(topicId)
    fun resultById(id: String) = PerformanceRepository.resultById(id)
    fun grandTestImprovement() = PerformanceRepository.grandTestImprovement()
    fun questionById(id: String) = StudyRepository.questionById(id)
    fun topicName(id: String) = StudyRepository.topicName(id)
    fun subjectName(id: String) = StudyRepository.subjectName(id)
    fun articleForTopic(topicId: String) = StudyRepository.publishedArticleForTopic(topicId)
    fun quizForTopic(topicId: String) = StudyRepository.quizForTopic(topicId)
}

/* ============================================================= leaderboard */

class LeaderboardViewModel : ViewModel() {

    private val _period = MutableStateFlow(LeaderboardPeriod.WEEKLY)
    val period: StateFlow<LeaderboardPeriod> = _period.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val periodAndTrack = combine(_period, ExamTrackRepository.selectedId) { period, _ -> period }

    val entries: StateFlow<List<LeaderboardEntry>> = periodAndTrack
        .map { LeaderboardRepository.topEntries(it, 50) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentUser: StateFlow<LeaderboardEntry?> = periodAndTrack
        .map { LeaderboardRepository.currentUserEntry(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val neighbourhood: StateFlow<List<LeaderboardEntry>> = periodAndTrack
        .map { LeaderboardRepository.neighbourhood(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val trackLabel: StateFlow<String?> = ExamTrackRepository.selectedId
        .map { ExamTrackRepository.selected()?.shortName }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            delay(400)
            _isLoading.value = false
        }
    }

    fun setPeriod(value: LeaderboardPeriod) { _period.value = value }
    fun participants() = LeaderboardRepository.participants(_period.value)
}

/* ============================================================ notifications */

class StudyNotificationsViewModel : ViewModel() {

    val notifications: StateFlow<List<StudyNotification>> = StudyRepository.notifications
        .map { StudyRepository.notificationsFor(StudyRole.STUDENT) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun markRead(id: String) = StudyRepository.markNotificationRead(id)
    fun markAllRead() = StudyRepository.markAllNotificationsRead(StudyRole.STUDENT)
}
