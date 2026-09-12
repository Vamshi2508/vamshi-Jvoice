package com.jvoice.study.data.repository

import com.jvoice.study.data.mock.MockDataSource
import com.jvoice.study.data.model.ExamResult
import com.jvoice.study.data.model.ExamType
import com.jvoice.study.data.model.LeaderboardPeriod
import com.jvoice.study.data.model.OverallStats
import com.jvoice.study.data.model.PerformanceBand
import com.jvoice.study.data.model.StudyPlanStep
import com.jvoice.study.data.model.SubjectPerformance
import com.jvoice.study.data.model.TopicPerformance
import com.jvoice.study.data.model.TopicScore
import com.jvoice.study.data.model.WeakAreaGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.jvoice.core.i18n.LocalizedText

/**
 * All performance analysis: subject and topic accuracy, strong / weak
 * classification and weak-topic recommendations.
 *
 * The aggregate is seeded from earlier practice ([MockDataSource.seedTopicScores])
 * and every newly submitted exam is folded in, so an exam where the student gets
 * all Mathematics questions wrong immediately drags Mathematics towards Weak.
 */
object PerformanceRepository {

    private val _results = MutableStateFlow(MockDataSource.pastResults)
    val results: StateFlow<List<ExamResult>> = _results.asStateFlow()

    /** Live topic-level tally: topicId -> (correct, total). */
    private val _topicTally = MutableStateFlow(
        MockDataSource.seedTopicScores.associate { it.topicId to Pair(it.correct, it.total) }
    )
    val topicTally: StateFlow<Map<String, Pair<Int, Int>>> = _topicTally.asStateFlow()

    // ------------------------------------------------------------------ recording
    fun recordResult(result: ExamResult) {
        _results.update { listOf(result) + it }
        _topicTally.update { current ->
            val updated = current.toMutableMap()
            result.topicScores.forEach { score ->
                val existing = updated[score.topicId] ?: Pair(0, 0)
                updated[score.topicId] = Pair(
                    existing.first + score.correct,
                    existing.second + score.total
                )
            }
            updated
        }
    }

    fun resultById(id: String): ExamResult? = _results.value.firstOrNull { it.id == id }

    fun latestResult(): ExamResult? = _results.value.maxByOrNull { it.takenAt }

    // ------------------------------------------------------------- topic analysis
    /**
     * Only the selected exam's subjects are analysed. A Bank aspirant's Reasoning
     * accuracy is theirs; History never appears in their weak areas because it is
     * not on their paper.
     */
    fun topicPerformance(): List<TopicPerformance> {
        val tally = _topicTally.value
        return StudyRepository.topics.value.mapNotNull { topic ->
            if (!ExamTrackRepository.isInScope(topic.subjectId)) return@mapNotNull null
            val counts = tally[topic.id] ?: return@mapNotNull null
            TopicPerformance(topic, counts.first, counts.second)
        }
    }

    fun topicPerformanceFor(subjectId: String): List<TopicPerformance> =
        topicPerformance()
            .filter { it.topic.subjectId == subjectId }
            .sortedBy { it.accuracy }

    fun topicPerformanceById(topicId: String): TopicPerformance? =
        topicPerformance().firstOrNull { it.topic.id == topicId }

    // ----------------------------------------------------------- subject analysis
    /** Subject figures are summed from the topic tally, so the two views agree. */
    fun subjectPerformance(): List<SubjectPerformance> {
        val byTopic = topicPerformance().groupBy { it.topic.subjectId }
        return StudyRepository.subjects.value.filter { ExamTrackRepository.isInScope(it.id) }.map { subject ->
            val rows = byTopic[subject.id].orEmpty()
            SubjectPerformance(
                subject = subject,
                correct = rows.sumOf { it.correct },
                total = rows.sumOf { it.total }
            )
        }
    }

    fun subjectPerformanceById(subjectId: String): SubjectPerformance? =
        subjectPerformance().firstOrNull { it.subject.id == subjectId }

    fun attemptedSubjects(): List<SubjectPerformance> =
        subjectPerformance().filter { it.attempted }.sortedByDescending { it.accuracy }

    fun strongSubjects(): List<SubjectPerformance> =
        attemptedSubjects().filter { it.band == PerformanceBand.STRONG }

    fun needsPracticeSubjects(): List<SubjectPerformance> =
        attemptedSubjects().filter { it.band == PerformanceBand.NEEDS_PRACTICE }

    fun weakSubjects(): List<SubjectPerformance> =
        attemptedSubjects().filter { it.band == PerformanceBand.WEAK }.sortedBy { it.accuracy }

    fun strongTopics(): List<TopicPerformance> =
        topicPerformance().filter { it.attempted && it.band == PerformanceBand.STRONG }
            .sortedByDescending { it.accuracy }

    fun weakTopics(): List<TopicPerformance> =
        topicPerformance().filter { it.attempted && it.band == PerformanceBand.WEAK }
            .sortedBy { it.accuracy }

    // ------------------------------------------------------- weak-area explorer
    /**
     * "Explore Your Weak Areas": subjects that are weak or need practice, each
     * with their lowest-scoring topics. Weak subjects come first.
     */
    fun weakAreaGroups(): List<WeakAreaGroup> =
        subjectPerformance()
            .filter { it.attempted && it.band != PerformanceBand.STRONG }
            .sortedBy { it.accuracy }
            .mapNotNull { subjectPerf ->
                val topics = topicPerformanceFor(subjectPerf.subject.id)
                    .filter { it.attempted && it.band != PerformanceBand.STRONG }
                    .take(3)
                if (topics.isEmpty()) null
                else WeakAreaGroup(subjectPerf.subject, subjectPerf.accuracy, subjectPerf.band, topics)
            }

    /** The three-step plan shown when a student opens a weak topic. */
    fun studyPlanFor(topicId: String): List<StudyPlanStep> {
        val hasArticle = StudyRepository.publishedArticleForTopic(topicId) != null
        val hasQuiz = StudyRepository.quizForTopic(topicId) != null
        val questionCount = StudyRepository.questionsForTopic(topicId).size
        val practiceCount = minOf(20, maxOf(questionCount, 10))
        return listOf(
            StudyPlanStep(
                1,
                LocalizedText(
                    "Read the study article",
                    "స్టడీ కథనం చదవండి"
                ),
                if (hasArticle) LocalizedText(
                    "Revise the concept and the important points",
                    "భావనను, ముఖ్యాంశాలను రివిజన్ చేసుకోండి"
                ) else LocalizedText(
                    "No article published yet",
                    "ఇంకా కథనం ప్రజురించలేదు"
                ),
                done = StudyRepository.isTopicCompleted(topicId)
            ),
            StudyPlanStep(
                2,
                LocalizedText(
                    "Take the topic quiz",
                    "టాపిక్ క్విజ్ రాయండి"
                ),
                if (hasQuiz) LocalizedText(
                    "10 questions to check your understanding",
                    "మీ అవగాహనను పరీక్షించే 10 ప్రశ్నలు"
                ) else LocalizedText(
                    "Quiz not available for this topic",
                    "ఈ టాపిక్‌కు క్విజ్ అందుబాటులో లేదు"
                )
            ),
            StudyPlanStep(
                3,
                LocalizedText(
                    en = "Practice $practiceCount questions",
                    te = "$practiceCount ప్రశ్నలు ప్రాక్టీస్ చేయండి"
                ),
                LocalizedText(
                    "Focused practice set drawn from the question bank",
                    "ప్రశ్న బ్యాంక్ నుంచి తీసిన గురికుదిరిన ప్రాక్టీస్ సెట్"
                )
            )
        )
    }

    /** Topics recommended on the home screen: weakest first, then needs-practice. */
    fun recommendedTopics(limit: Int = 5): List<TopicPerformance> =
        topicPerformance()
            .filter { it.attempted && it.band != PerformanceBand.STRONG }
            .sortedBy { it.accuracy }
            .take(limit)

    // ------------------------------------------------------------------- history
    fun examResults(): List<ExamResult> =
        _results.value.filter { it.type == ExamType.DAILY }.sortedBy { it.takenAt }

    fun grandTestResults(): List<ExamResult> =
        _results.value.filter { it.type == ExamType.GRAND_TEST }.sortedBy { it.takenAt }

    fun quizResults(): List<ExamResult> =
        _results.value.filter { it.type == ExamType.TOPIC_QUIZ || it.type == ExamType.PRACTICE }

    /** Accuracy of the last [count] exams, oldest first - the trend chart. */
    fun recentExamTrend(count: Int = 7): List<Pair<String, Int>> =
        examResults().takeLast(count).mapIndexed { index, result ->
            ("Exam " + (index + 1)) to result.accuracy
        }

    /** Improvement between the two most recent grand tests. */
    fun grandTestImprovement(): Triple<Int, Int, Int>? {
        val tests = grandTestResults()
        if (tests.size < 2) return null
        val previous = tests[tests.size - 2].score
        val current = tests.last().score
        return Triple(previous, current, current - previous)
    }

    fun overallStats(): OverallStats {
        val exams = examResults()
        val quizzes = quizResults()
        val all = exams + grandTestResults() + quizzes
        val accuracies = all.map { it.accuracy }
        return OverallStats(
            examsCompleted = exams.size + grandTestResults().size,
            quizzesCompleted = quizzes.size,
            averageScorePercent = if (accuracies.isEmpty()) 0 else accuracies.average().toInt(),
            averageAccuracy = if (accuracies.isEmpty()) 0 else accuracies.average().toInt(),
            bestScorePercent = accuracies.maxOrNull() ?: 0,
            currentRank = MockDataSource.currentRank(
                LeaderboardPeriod.WEEKLY,
                ExamTrackRepository.selectedId.value
            ),
            participants = MockDataSource.PARTICIPANTS,
            studyStreakDays = MockDataSource.student.studyStreakDays,
            topicsCompleted = StudyRepository.completedTopicIds.value.size
        )
    }

    /** Today's progress ring on the home screen, over the selected exam only. */
    fun todayProgressPercent(): Int {
        val syllabus = ExamTrackRepository.syllabusProgressPercent(ExamTrackRepository.selected())
        val examDone = examResults().any {
            System.currentTimeMillis() - it.takenAt < 12 * 60 * 60 * 1000L
        }
        return ((syllabus * 3 / 4) + (if (examDone) 25 else 0)).coerceIn(0, 100)
    }

    /** Seeded topic scores for a result that predates per-question storage. */
    fun seedScoresFor(result: ExamResult): List<TopicScore> = result.topicScores
}
