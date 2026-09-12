package com.jvoice.study.data.repository

import com.jvoice.study.data.mock.MockDataSource
import com.jvoice.study.data.model.Difficulty
import com.jvoice.study.data.model.Exam
import com.jvoice.study.data.model.ExamAttempt
import com.jvoice.study.data.model.ExamResult
import com.jvoice.study.data.model.ExamType
import com.jvoice.study.data.model.LeaderboardPeriod
import com.jvoice.study.data.model.StudyNotificationType
import com.jvoice.study.data.model.StudyRole
import com.jvoice.study.engine.ExamEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.jvoice.core.i18n.LanguagePreference
import com.jvoice.core.i18n.AppLanguage
import com.jvoice.core.i18n.LocalizedText

/**
 * Owns exams (daily, grand test, topic quiz, practice sets) and the single live
 * attempt. Grading is delegated to [ExamEngine]; the graded result is handed to
 * [PerformanceRepository] so the analysis screens update immediately.
 */
object ExamRepository {

    private val _exams = MutableStateFlow(MockDataSource.exams)
    val exams: StateFlow<List<Exam>> = _exams.asStateFlow()

    private val _attempt = MutableStateFlow<ExamAttempt?>(null)
    val attempt: StateFlow<ExamAttempt?> = _attempt.asStateFlow()

    private val _lastResult = MutableStateFlow<ExamResult?>(null)
    val lastResult: StateFlow<ExamResult?> = _lastResult.asStateFlow()

    private var idCounter = 900
    private fun nextId(prefix: String): String {
        idCounter += 1
        return prefix + idCounter
    }

    // ------------------------------------------------------------------ lookups
    fun examById(id: String): Exam? = _exams.value.firstOrNull { it.id == id }

    /**
     * Papers visible for the exam the student picked. A paper tagged with a track
     * belongs to that track only; untagged papers are general practice and show up
     * for every track. With no track chosen yet, everything is visible.
     */
    private fun inScope(): List<Exam> {
        val trackId = ExamTrackRepository.selectedId.value
        return _exams.value.filter { it.belongsTo(trackId) }
    }

    fun examsForTrack(trackId: String): List<Exam> = _exams.value.filter { it.belongsTo(trackId) }

    /** Papers tagged with this exam type only - what blocks deleting it. */
    fun trackExamsFor(trackId: String): List<Exam> = _exams.value.filter { it.trackIds.contains(trackId) }

    /** Papers built specifically for the selected exam, general practice aside. */
    fun trackExams(): List<Exam> {
        val trackId = ExamTrackRepository.selectedId.value ?: return emptyList()
        return _exams.value.filter { it.trackIds.contains(trackId) }
    }

    fun generalExams(): List<Exam> = _exams.value.filter { it.isGeneral }

    fun dailyExams(): List<Exam> = inScope().filter { it.type == ExamType.DAILY }

    fun grandTests(): List<Exam> = inScope().filter { it.type == ExamType.GRAND_TEST }

    /** The selected exam's own daily paper wins over the general one. */
    fun todaysExam(): Exam? {
        val daily = dailyExams().filter { it.isActive }
        val trackId = ExamTrackRepository.selectedId.value
        return daily.firstOrNull { trackId != null && it.trackIds.contains(trackId) }
            ?: daily.firstOrNull { it.dateLabel == "Today" }
            ?: daily.firstOrNull()
    }

    fun currentGrandTest(): Exam? {
        val grand = grandTests().filter { it.isActive }
        val trackId = ExamTrackRepository.selectedId.value
        return grand.firstOrNull { trackId != null && it.trackIds.contains(trackId) }
            ?: grand.firstOrNull()
    }

    // ------------------------------------------------------ building attempts
    /** Starts a daily exam or grand test. */
    fun startExam(examId: String): ExamAttempt? {
        val exam = examById(examId) ?: return null
        val attempt = ExamEngine.startAttempt(exam, System.currentTimeMillis())
        _attempt.value = attempt
        return attempt
    }

    /** Wraps a topic quiz in a throwaway [Exam] so the same engine drives it. */
    fun startTopicQuiz(topicId: String): ExamAttempt? {
        val quiz = StudyRepository.quizForTopic(topicId) ?: return null
        if (quiz.questionIds.isEmpty()) return null
        val exam = Exam(
            id = "quizrun_" + quiz.id,
            // Built from the topic name in each language rather than translated
            // after joining, so the suffix reads naturally on both sides.
            title = LocalizedText(
                en = StudyRepository.topicName(topicId, AppLanguage.ENGLISH) + " - Topic Quiz",
                te = StudyRepository.topicName(topicId, AppLanguage.TELUGU) + " - టాపిక్ క్విజ్"
            ),
            type = ExamType.TOPIC_QUIZ,
            dateLabel = "Now",
            durationMinutes = quiz.durationMinutes,
            questionIds = quiz.questionIds,
            subjectIds = listOf(quiz.subjectId),
            instructions = LocalizedText(
                "Answer all questions and submit to see your score.",
                "అన్ని ప్రశ్నలకు సమాధానం ఇచ్చి, స్కోరు చూసేందుకు సమర్పించండి."
            )
        )
        val attempt = ExamEngine.startAttempt(exam, System.currentTimeMillis())
        _attempt.value = attempt
        return attempt
    }

    /**
     * Builds a practice set for a weak topic on the fly. Falls back to the
     * subject's questions when the topic itself has too few items.
     */
    fun startPracticeSet(topicId: String, size: Int = 20): ExamAttempt? {
        val topic = StudyRepository.topicById(topicId) ?: return null
        val topicQuestions = StudyRepository.questionsForTopic(topicId)
        val filler = StudyRepository.filterQuestions(subjectId = topic.subjectId)
            .filterNot { q -> topicQuestions.any { it.id == q.id } }
        val ids = (topicQuestions + filler).take(size).map { it.id }
        if (ids.isEmpty()) return null

        val exam = Exam(
            id = nextId("practice_"),
            title = LocalizedText(
                en = topic.name.en + " - Practice Set",
                te = topic.name.get(AppLanguage.TELUGU) + " - ప్రాక్టీస్ సెట్"
            ),
            type = ExamType.PRACTICE,
            dateLabel = "Now",
            durationMinutes = (ids.size * 3) / 4 + 1,
            questionIds = ids,
            subjectIds = listOf(topic.subjectId),
            difficulty = topic.difficulty,
            instructions = LocalizedText(
                en = "Focused practice on a weak topic. " + ids.size + " questions.",
                te = "బలహీన టాపిక్‌పై గురిపెట్టిన ప్రాక్టీస్. " + ids.size + " ప్రశ్నలు."
            )
        )
        val attempt = ExamEngine.startAttempt(exam, System.currentTimeMillis())
        _attempt.value = attempt
        return attempt
    }

    // ------------------------------------------------------- attempt mutations
    fun select(optionIndex: Int) = _attempt.update { it?.let { a -> ExamEngine.select(a, optionIndex) } }
    fun clearSelection() = _attempt.update { it?.let { a -> ExamEngine.clearSelection(a) } }
    fun toggleMarkForReview() = _attempt.update { it?.let { a -> ExamEngine.toggleMarkForReview(a) } }
    fun goTo(index: Int) = _attempt.update { it?.let { a -> ExamEngine.goTo(a, index) } }
    fun next() = _attempt.update { it?.let { a -> ExamEngine.next(a) } }
    fun previous() = _attempt.update { it?.let { a -> ExamEngine.previous(a) } }
    fun tick() = _attempt.update { it?.let { a -> ExamEngine.tick(a) } }

    fun abandonAttempt() {
        _attempt.value = null
    }

    /** Grades the live attempt, stores the result and updates performance. */
    fun submitAttempt(): ExamResult? {
        val attempt = _attempt.value ?: return null
        val isGrandTest = attempt.exam.type == ExamType.GRAND_TEST
        val result = ExamEngine.grade(
            attempt = attempt,
            questionLookup = { StudyRepository.questionById(it) },
            resultId = nextId("res_"),
            finishedAt = System.currentTimeMillis(),
            rank = if (isGrandTest) {
                MockDataSource.currentRank(LeaderboardPeriod.GRAND_TEST, ExamTrackRepository.selectedId.value)
            } else null,
            participants = if (isGrandTest) MockDataSource.PARTICIPANTS else null
        )
        PerformanceRepository.recordResult(result)
        _lastResult.value = result
        _attempt.value = attempt.copy(submitted = true)

        StudyRepository.pushNotification(
            title = LocalizedText(
                en = attempt.exam.title.en + " result published",
                te = attempt.exam.title.get(AppLanguage.TELUGU) + " ఫలితం విడుదలైంది"
            ),
            message = LocalizedText(
                en = "You scored " + result.score + "/" + result.totalQuestions +
                    " with " + result.accuracy + "% accuracy.",
                te = "మీ స్కోరు " + result.score + "/" + result.totalQuestions +
                    ", కచ్చితత్వం " + result.accuracy + "%."
            ),
            type = StudyNotificationType.RESULT,
            targetRole = StudyRole.STUDENT
        )
        return result
    }

    fun clearAttemptAfterResult() {
        _attempt.value = null
    }

    // ----------------------------------------------------- exam admin actions
    fun saveExam(
        existingId: String?,
        title: LocalizedText,
        type: ExamType,
        dateLabel: String,
        durationMinutes: Int,
        questionCount: Int,
        subjectIds: List<String>,
        difficulty: Difficulty,
        instructions: LocalizedText,
        isActive: Boolean,
        /** null keeps whatever the exam already had; empty makes it general. */
        trackIds: List<String>? = null
    ): String {
        val id = existingId ?: nextId("exam_")
        val perSubject = if (subjectIds.isEmpty()) 0 else (questionCount / subjectIds.size).coerceAtLeast(1)
        val picked = subjectIds
            .flatMap { subjectId ->
                StudyRepository.filterQuestions(subjectId = subjectId).take(perSubject).map { it.id }
            }
            .distinct()
        val ids = if (picked.size >= questionCount) {
            picked.take(questionCount)
        } else {
            val filler = StudyRepository.questions.value.map { it.id }.filterNot { picked.contains(it) }
            (picked + filler).distinct().take(questionCount)
        }

        val existing = existingId?.let { examById(it) }
        if (existing == null) {
            val exam = Exam(
                id, title, type, dateLabel, durationMinutes, ids, subjectIds,
                difficulty, instructions, isActive,
                trackIds = trackIds.orEmpty()
            )
            _exams.update { listOf(exam) + it }
            if (isActive) {
                StudyRepository.pushNotification(
                    title = if (type == ExamType.GRAND_TEST)
                        LocalizedText("New Grand Test available", "కొత్త గ్రాండ్ టెస్ట్ అందుబాటులో ఉంది")
                    else LocalizedText("New Daily Exam available", "కొత్త రోజువారీ పరీక్ష అందుబాటులో ఉంది"),
                    message = LocalizedText(
                        en = title.en + " - " + ids.size + " questions, " + durationMinutes + " minutes.",
                        te = title.get(AppLanguage.TELUGU) + " - " + ids.size + " ప్రశ్నలు, " +
                            durationMinutes + " నిమిషాలు."
                    ),
                    type = if (type == ExamType.GRAND_TEST) StudyNotificationType.GRAND_TEST
                    else StudyNotificationType.DAILY_EXAM,
                    targetRole = StudyRole.STUDENT
                )
            }
        } else {
            _exams.update { list ->
                list.map {
                    if (it.id == id) {
                        it.copy(
                            title = title,
                            type = type,
                            dateLabel = dateLabel,
                            durationMinutes = durationMinutes,
                            questionIds = ids,
                            subjectIds = subjectIds,
                            difficulty = difficulty,
                            instructions = instructions,
                            isActive = isActive,
                            trackIds = trackIds ?: it.trackIds
                        )
                    } else it
                }
            }
        }
        return id
    }

    fun setExamActive(examId: String, active: Boolean) {
        _exams.update { list -> list.map { if (it.id == examId) it.copy(isActive = active) else it } }
        if (active) {
            examById(examId)?.let { exam ->
                StudyRepository.pushNotification(
                    title = LocalizedText(
                        en = exam.title.en + " is live",
                        te = exam.title.get(AppLanguage.TELUGU) + " లైవ్‌లో ఉంది"
                    ),
                    message = LocalizedText(
                        en = exam.questionCount.toString() + " questions, " + exam.durationMinutes + " minutes.",
                        te = exam.questionCount.toString() + " ప్రశ్నలు, " +
                            exam.durationMinutes + " నిమిషాలు."
                    ),
                    type = if (exam.type == ExamType.GRAND_TEST) StudyNotificationType.GRAND_TEST
                    else StudyNotificationType.DAILY_EXAM,
                    targetRole = StudyRole.STUDENT
                )
            }
        }
    }

    fun deleteExam(examId: String) {
        _exams.update { list -> list.filterNot { it.id == examId } }
    }
}
