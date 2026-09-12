package com.jvoice.study.engine

import com.jvoice.study.data.model.AnsweredQuestion
import com.jvoice.study.data.model.AttemptQuestion
import com.jvoice.study.data.model.Exam
import com.jvoice.study.data.model.ExamAttempt
import com.jvoice.study.data.model.ExamResult
import com.jvoice.study.data.model.PerformanceBand
import com.jvoice.study.data.model.Question
import com.jvoice.study.data.model.SubjectScore
import com.jvoice.study.data.model.TopicScore
import com.jvoice.core.i18n.LanguagePreference
import com.jvoice.core.i18n.LocalizedText

/**
 * The exam engine: pure functions that create an attempt, mutate it as the
 * student works, and grade it into an [ExamResult] with subject and topic
 * breakdowns. No Android or coroutine dependencies, so it stays unit-testable
 * and unaffected by a future backend swap.
 */
object ExamEngine {

    fun startAttempt(exam: Exam, startedAt: Long): ExamAttempt = ExamAttempt(
        exam = exam,
        questions = exam.questionIds.mapIndexed { index, id ->
            AttemptQuestion(questionId = id, visited = index == 0)
        },
        currentIndex = 0,
        remainingSeconds = exam.durationMinutes * 60,
        startedAt = startedAt,
        submitted = false
    )

    fun select(attempt: ExamAttempt, optionIndex: Int): ExamAttempt =
        attempt.updateCurrent { it.copy(selectedIndex = optionIndex, visited = true) }

    fun clearSelection(attempt: ExamAttempt): ExamAttempt =
        attempt.updateCurrent { it.copy(selectedIndex = null) }

    fun toggleMarkForReview(attempt: ExamAttempt): ExamAttempt =
        attempt.updateCurrent { it.copy(markedForReview = !it.markedForReview, visited = true) }

    fun goTo(attempt: ExamAttempt, index: Int): ExamAttempt {
        if (index !in attempt.questions.indices) return attempt
        val marked = attempt.questions.mapIndexed { i, q ->
            if (i == index) q.copy(visited = true) else q
        }
        return attempt.copy(questions = marked, currentIndex = index)
    }

    fun next(attempt: ExamAttempt): ExamAttempt =
        goTo(attempt, (attempt.currentIndex + 1).coerceAtMost(attempt.questions.lastIndex))

    fun previous(attempt: ExamAttempt): ExamAttempt =
        goTo(attempt, (attempt.currentIndex - 1).coerceAtLeast(0))

    fun tick(attempt: ExamAttempt): ExamAttempt =
        attempt.copy(remainingSeconds = (attempt.remainingSeconds - 1).coerceAtLeast(0))

    private fun ExamAttempt.updateCurrent(transform: (AttemptQuestion) -> AttemptQuestion): ExamAttempt {
        val updated = questions.mapIndexed { i, q -> if (i == currentIndex) transform(q) else q }
        return copy(questions = updated)
    }

    /**
     * Grades an attempt. [questionLookup] resolves a question id to the full
     * question so the engine can read the correct option, subject and topic.
     */
    fun grade(
        attempt: ExamAttempt,
        questionLookup: (String) -> Question?,
        resultId: String,
        finishedAt: Long,
        rank: Int? = null,
        participants: Int? = null
    ): ExamResult {
        val answers = mutableListOf<AnsweredQuestion>()
        val subjectTally = mutableMapOf<String, IntArray>()   // subjectId -> [correct, total]
        val topicTally = mutableMapOf<Pair<String, String>, IntArray>() // (topicId, subjectId)

        var correct = 0
        var wrong = 0
        var skipped = 0

        attempt.questions.forEach { attemptQuestion ->
            val question = questionLookup(attemptQuestion.questionId) ?: return@forEach
            val answered = AnsweredQuestion(
                questionId = question.id,
                selectedIndex = attemptQuestion.selectedIndex,
                correctIndex = question.correctIndex,
                markedForReview = attemptQuestion.markedForReview
            )
            answers += answered

            when {
                answered.isSkipped -> skipped++
                answered.isCorrect -> correct++
                else -> wrong++
            }

            val isCorrect = if (answered.isCorrect) 1 else 0
            subjectTally.getOrPut(question.subjectId) { intArrayOf(0, 0) }.let {
                it[0] += isCorrect
                it[1] += 1
            }
            topicTally.getOrPut(question.topicId to question.subjectId) { intArrayOf(0, 0) }.let {
                it[0] += isCorrect
                it[1] += 1
            }
        }

        val elapsed = ((finishedAt - attempt.startedAt) / 1000).toInt()
            .coerceIn(0, attempt.exam.durationMinutes * 60)

        return ExamResult(
            id = resultId,
            examId = attempt.exam.id,
            // Stored as the pair, not resolved to one language: a student who
            // switches language later should see this result's title change too.
            examTitle = attempt.exam.title,
            type = attempt.exam.type,
            totalQuestions = attempt.questions.size,
            correct = correct,
            wrong = wrong,
            skipped = skipped,
            timeTakenSeconds = elapsed,
            takenAt = finishedAt,
            subjectScores = subjectTally.map { (subjectId, tally) ->
                SubjectScore(subjectId, tally[0], tally[1])
            }.sortedByDescending { it.accuracy },
            topicScores = topicTally.map { (key, tally) ->
                TopicScore(key.first, key.second, tally[0], tally[1])
            },
            answers = answers,
            rank = rank,
            participants = participants
        )
    }

    /**
     * The classification rule used across the module:
     * 80-100 strong, 50-79 needs practice, 0-49 weak.
     */
    fun bandOf(accuracy: Int): PerformanceBand = PerformanceBand.of(accuracy)
}
