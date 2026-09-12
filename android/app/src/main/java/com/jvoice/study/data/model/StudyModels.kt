package com.jvoice.study.data.model

import com.jvoice.core.i18n.AppLanguage
import com.jvoice.core.i18n.LocalizedText
import com.jvoice.core.i18n.anyMatches

/**
 * J Voice - Module 2 (Study & Exam Preparation) models.
 *
 * Everything is backed by local mock data. The shapes below are deliberately
 * plain data classes with no framework annotations, so a Firebase / REST layer
 * can be dropped in later behind the repositories without touching the UI.
 *
 * Every student-visible string is a [LocalizedText] rather than a [String] - a
 * topic name, an article body, a question, its options, its explanation. A
 * student preparing in Telugu reads the whole paper in Telugu; the same question
 * bank serves an English candidate without a second dataset.
 */

enum class StudyRole(val label: String, val teluguLabel: String) {
    STUDENT("Student", "విద్యార్థి"),
    CONTENT_CREATOR("Content Creator", "కంటెంట్ క్రియేటర్"),
    EXAM_ADMIN("Exam Admin", "ఎగ్జామ్ అడ్మిన్"),
    STUDY_ADMIN("Study Admin", "స్టడీ అడ్మిన్"),
    SUPER_ADMIN("Super Admin", "సూపర్ అడ్మిన్");

    /** `name` is taken by [Enum], so the bilingual value is spelled out. */
    val localizedLabel: LocalizedText get() = LocalizedText(en = label, te = teluguLabel)
}

data class StudyUser(
    val id: String,
    val name: String,
    val email: String,
    val role: StudyRole,
    val isActive: Boolean = true,
    val joinedOn: String = "01 Jan 2025",
    val avatarUrl: String = ""
)

data class Student(
    val userId: String,
    val name: String,
    val targetExam: String = "Group-2 / SSC",
    val className: String = "Degree final year",
    val studyStreakDays: Int = 0,
    val avatarUrl: String = ""
)

// --------------------------------------------------------------- exam tracks

/** How the exam picker groups the tracks. */
enum class ExamTrackGroup(val label: String, val teluguLabel: String) {
    POLICE("Police & Uniform", "పోలీస్ - యూనిఫాం"),
    GROUPS("TSPSC / APPSC Groups", "టీఎస్‌పీఎస్‌సీ / ఎపీపీఎస్‌సీ గ్రూప్స్"),
    SSC_RAILWAY("SSC & Railways", "ఎస్‌ఎస్‌సీ - రైల్వే"),
    BANKING("Banking & Insurance", "బ్యాంకింగ్ - బీమా"),
    TEACHING("Teaching", "టీచింగ్");

    /** `name` is taken by [Enum], so the bilingual value is spelled out. */
    val localizedLabel: LocalizedText get() = LocalizedText(en = label, te = teluguLabel)
}

/**
 * One dated milestone of an exam - notification release, last date to apply,
 * hall ticket, exam day. Admin-managed, so the student's hub always shows the
 * dates the desk has entered.
 */
data class ExamKeyDate(
    val label: LocalizedText,
    /** Dates stay one string - digits and month codes read the same either way. */
    val dateLabel: String,
    val note: LocalizedText = LocalizedText.EMPTY
)

/** One row of an exam's paper pattern: a subject and its share of the paper. */
data class ExamSection(
    val subjectId: String,
    val questions: Int,
    val marks: Int
)

/**
 * A target exam - Police Constable, Group-4, SSC and so on. The student picks one
 * on entering the Study module and everything after that (subjects, topics,
 * quizzes, daily tests, grand tests, analysis and ranks) is scoped to it.
 */
data class ExamTrack(
    val id: String,
    val name: LocalizedText,
    /** Short code - "Group-4", "SSC CGL". Not translated; it is how it is known. */
    val shortName: String,
    val emoji: String,
    val group: ExamTrackGroup,
    val tagline: LocalizedText,
    val qualification: LocalizedText,
    val ageLimit: LocalizedText,
    val vacancyLabel: LocalizedText,
    val examDateLabel: LocalizedText,
    val totalQuestions: Int,
    val totalMarks: Int,
    val durationMinutes: Int,
    val negativeMarking: LocalizedText,
    val sections: List<ExamSection>,
    val stages: List<LocalizedText> = emptyList(),
    val keyDates: List<ExamKeyDate> = emptyList(),
    val isEnabled: Boolean = true
) {
    val subjectIds: List<String> get() = sections.map { it.subjectId }

    /** Both languages on one line - for the desk's exam-type list. */
    val displayName: String get() = name.inline()

    fun patternLabel(language: AppLanguage): String = if (language == AppLanguage.TELUGU)
        "$totalQuestions ప్రశ్నలు • $totalMarks మార్కులు • $durationMinutes నిమిషాలు"
    else "$totalQuestions Q • $totalMarks marks • $durationMinutes min"

    fun sectionFor(subjectId: String): ExamSection? = sections.firstOrNull { it.subjectId == subjectId }
    fun covers(subjectId: String): Boolean = sections.any { it.subjectId == subjectId }

    /** Matches on either language, plus the short code students actually type. */
    fun matches(query: String): Boolean {
        val q = query.trim()
        if (q.isBlank()) return true
        return name.matches(q) ||
            shortName.contains(q, ignoreCase = true) ||
            tagline.matches(q)
    }
}

/** A section of the pattern joined to its subject, for the pattern card. */
data class ExamSectionRow(
    val section: ExamSection,
    val subject: Subject,
    val topicCount: Int,
    val completedTopics: Int,
    val questionCount: Int,
    val accuracy: Int?
) {
    val progress: Int get() = if (topicCount == 0) 0 else completedTopics * 100 / topicCount
}

/** Live counts shown on an exam card in the picker. */
data class ExamTrackSummary(
    val track: ExamTrack,
    val subjects: Int,
    val topics: Int,
    val articles: Int,
    val questions: Int,
    val tests: Int
)

// ------------------------------------------------------------------ syllabus

data class Subject(
    val id: String,
    val name: LocalizedText,
    val emoji: String = "📘",
    val isEnabled: Boolean = true
) {
    /** Both languages on one line - for the desk's subject list. */
    val displayName: String get() = name.inline()
}

enum class Difficulty(val label: String, val teluguLabel: String) {
    EASY("Easy", "సులభం"),
    MEDIUM("Medium", "మధ్యస్థం"),
    HARD("Hard", "కష్టం");

    val localizedLabel: LocalizedText get() = LocalizedText(en = label, te = teluguLabel)
}

data class Topic(
    val id: String,
    val subjectId: String,
    val name: LocalizedText,
    val order: Int = 0,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val isEnabled: Boolean = true
)

// ------------------------------------------------------------------ content

enum class ContentStatus(val label: String, val teluguLabel: String) {
    DRAFT("Draft", "డ్రాఫ్ట్"),
    PENDING_REVIEW("Pending Review", "సమీక్ష పెండింగ్"),
    PUBLISHED("Published", "ప్రజురించినది");

    val localizedLabel: LocalizedText get() = LocalizedText(en = label, te = teluguLabel)
}

data class StudyArticle(
    val id: String,
    val subjectId: String,
    val topicId: String,
    val title: LocalizedText,
    val description: LocalizedText,
    val content: LocalizedText,
    val importantPoints: List<LocalizedText> = emptyList(),
    val examples: List<LocalizedText> = emptyList(),
    /**
     * Formulas are bilingual too. The symbols are universal but the words around
     * them are not - "Distance = Speed x Time" has to read as
     * "దూరం = వేగం x కాలం" for a Telugu student.
     */
    val formulas: List<LocalizedText> = emptyList(),
    val readingMinutes: Int = 5,
    val status: ContentStatus = ContentStatus.PUBLISHED,
    val authorName: String = "J Voice Desk",
    val createdAt: Long = 0L
) {
    /** True when every part of the page exists in both languages. */
    val isFullyTranslated: Boolean
        get() = title.isComplete && description.isComplete && content.isComplete

    fun matches(query: String): Boolean {
        val q = query.trim()
        if (q.isBlank()) return true
        return title.matches(q) || description.matches(q) || content.matches(q) ||
            importantPoints.anyMatches(q)
    }
}

enum class QuestionType(val label: String, val teluguLabel: String) {
    MCQ("Multiple choice", "బహుళీఐచ్ఛికం"),
    TRUE_FALSE("True / False", "ఒప్పు / తప్పు"),
    ASSERTION("Assertion & Reason", "ప్రతిపాదన - కారణం");

    val localizedLabel: LocalizedText get() = LocalizedText(en = label, te = teluguLabel)
}

/** Where a question came from — practice written for the topic, or a real paper. */
enum class QuestionSource(val label: String, val teluguLabel: String) {
    SAMPLE("Sample", "నమూనా"),
    PREVIOUS("Previously asked", "ఇంతకుముందు అడిగినవి");

    val localizedLabel: LocalizedText get() = LocalizedText(en = label, te = teluguLabel)
}

data class Question(
    val id: String,
    val subjectId: String,
    val topicId: String,
    val text: LocalizedText,
    /**
     * Options are bilingual and positional: index 0 is the same option in both
     * languages, which is what makes [correctIndex] a single number rather than
     * one per language. Translations must therefore keep their order.
     */
    val options: List<LocalizedText>,
    val correctIndex: Int,
    val explanation: LocalizedText = LocalizedText.EMPTY,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val type: QuestionType = QuestionType.MCQ,
    val source: QuestionSource = QuestionSource.SAMPLE,
    val paperName: String = "",
    val year: String = "",
    val status: ContentStatus = ContentStatus.PUBLISHED
) {
    val correctOption: LocalizedText
        get() = options.getOrElse(correctIndex) { LocalizedText.EMPTY }

    fun options(language: AppLanguage): List<String> = options.map { it.get(language) }

    /** True when the stem, every option and the explanation all exist in both. */
    val isFullyTranslated: Boolean
        get() = text.isComplete && options.all { it.isComplete } &&
            (explanation.isBlank || explanation.isComplete)

    fun matches(query: String): Boolean {
        val q = query.trim()
        if (q.isBlank()) return true
        return text.matches(q) || options.anyMatches(q) || explanation.matches(q)
    }

    /** "TSPSC Group-2 · 2023" — blank for sample questions. */
    val paperLabel: String
        get() = listOf(paperName, year).filter { it.isNotBlank() }.joinToString(" · ")
}

data class Quiz(
    val id: String,
    val subjectId: String,
    val topicId: String,
    val title: LocalizedText,
    val questionIds: List<String>,
    val durationMinutes: Int = 10,
    val status: ContentStatus = ContentStatus.PUBLISHED,
    val authorName: String = "J Voice Desk"
) {
    /**
     * True for the set built from previously-asked paper questions.
     *
     * Keyed on the id rather than the title. The UI used to compare the title to
     * the literal "Previously asked", which quietly stopped working once titles
     * became translatable - a Telugu set would never have matched.
     */
    val isPastPaperSet: Boolean get() = id.endsWith("_past")
}

// ------------------------------------------------------------------ exams

enum class ExamType(val label: String, val teluguLabel: String) {
    TOPIC_QUIZ("Topic Quiz", "టాపిక్ క్విజ్"),
    DAILY("Daily Exam", "రోజువారీ పరీక్ష"),
    GRAND_TEST("Weekly Grand Test", "వారపు గ్రాండ్ టెస్ట్"),
    PRACTICE("Practice Set", "ప్రాక్టీస్ సెట్");

    val localizedLabel: LocalizedText get() = LocalizedText(en = label, te = teluguLabel)
}

data class Exam(
    val id: String,
    val title: LocalizedText,
    val type: ExamType,
    val dateLabel: String,
    val durationMinutes: Int,
    val questionIds: List<String>,
    val subjectIds: List<String>,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val instructions: LocalizedText = LocalizedText.EMPTY,
    val isActive: Boolean = true,
    /** Exam tracks this paper belongs to. Empty = open to every track. */
    val trackIds: List<String> = emptyList()
) {
    val questionCount: Int get() = questionIds.size
    val isGeneral: Boolean get() = trackIds.isEmpty()

    fun belongsTo(trackId: String?): Boolean =
        trackId == null || trackIds.isEmpty() || trackIds.contains(trackId)
}

/** Per-question state while an attempt is in progress. */
data class AttemptQuestion(
    val questionId: String,
    val selectedIndex: Int? = null,
    val markedForReview: Boolean = false,
    val visited: Boolean = false
) {
    val isAnswered: Boolean get() = selectedIndex != null
}

enum class PaletteState { CURRENT, ANSWERED, MARKED, ANSWERED_MARKED, NOT_ANSWERED, NOT_VISITED }

/** A live exam / quiz attempt. Held in memory by ExamRepository. */
data class ExamAttempt(
    val exam: Exam,
    val questions: List<AttemptQuestion>,
    val currentIndex: Int = 0,
    val remainingSeconds: Int = 0,
    val startedAt: Long = 0L,
    val submitted: Boolean = false
) {
    val answeredCount: Int get() = questions.count { it.isAnswered }
    val markedCount: Int get() = questions.count { it.markedForReview }
    val notAnsweredCount: Int get() = questions.size - answeredCount
    val progress: Float get() = if (questions.isEmpty()) 0f else answeredCount.toFloat() / questions.size

    fun paletteState(index: Int): PaletteState {
        val q = questions.getOrNull(index) ?: return PaletteState.NOT_VISITED
        return when {
            index == currentIndex -> PaletteState.CURRENT
            q.isAnswered && q.markedForReview -> PaletteState.ANSWERED_MARKED
            q.markedForReview -> PaletteState.MARKED
            q.isAnswered -> PaletteState.ANSWERED
            q.visited -> PaletteState.NOT_ANSWERED
            else -> PaletteState.NOT_VISITED
        }
    }
}

// ------------------------------------------------------------------ results

data class SubjectScore(val subjectId: String, val correct: Int, val total: Int) {
    val accuracy: Int get() = if (total == 0) 0 else (correct * 100) / total
}

data class TopicScore(
    val topicId: String,
    val subjectId: String,
    val correct: Int,
    val total: Int
) {
    val accuracy: Int get() = if (total == 0) 0 else (correct * 100) / total
}

/** One graded answer, kept so the student can review the paper afterwards. */
data class AnsweredQuestion(
    val questionId: String,
    val selectedIndex: Int?,
    val correctIndex: Int,
    val markedForReview: Boolean = false
) {
    val isCorrect: Boolean get() = selectedIndex != null && selectedIndex == correctIndex
    val isSkipped: Boolean get() = selectedIndex == null
}

data class ExamResult(
    val id: String,
    val examId: String,
    val examTitle: LocalizedText,
    val type: ExamType,
    val totalQuestions: Int,
    val correct: Int,
    val wrong: Int,
    val skipped: Int,
    val timeTakenSeconds: Int,
    val takenAt: Long,
    val subjectScores: List<SubjectScore> = emptyList(),
    val topicScores: List<TopicScore> = emptyList(),
    val answers: List<AnsweredQuestion> = emptyList(),
    val rank: Int? = null,
    val participants: Int? = null
) {
    val score: Int get() = correct
    val accuracy: Int get() = if (totalQuestions == 0) 0 else (correct * 100) / totalQuestions
    val timeTakenLabel: String
        get() = String.format("%02d:%02d", timeTakenSeconds / 60, timeTakenSeconds % 60)
}

// ------------------------------------------------------------------ performance

enum class PerformanceBand(val label: String, val emoji: String, val teluguLabel: String) {
    STRONG("Strong", "🟢", "బలం"),
    NEEDS_PRACTICE("Needs Practice", "🟡", "ప్రాక్టీస్ కావాలి"),
    WEAK("Weak", "🔴", "బలహీనం");

    val localizedLabel: LocalizedText get() = LocalizedText(en = label, te = teluguLabel)

    companion object {
        /** 80-100 strong, 50-79 needs practice, 0-49 weak. */
        fun of(accuracy: Int): PerformanceBand = when {
            accuracy >= 80 -> STRONG
            accuracy >= 50 -> NEEDS_PRACTICE
            else -> WEAK
        }
    }
}

data class SubjectPerformance(
    val subject: Subject,
    val correct: Int,
    val total: Int
) {
    val accuracy: Int get() = if (total == 0) 0 else (correct * 100) / total
    val band: PerformanceBand get() = PerformanceBand.of(accuracy)
    val attempted: Boolean get() = total > 0
}

data class TopicPerformance(
    val topic: Topic,
    val correct: Int,
    val total: Int
) {
    val accuracy: Int get() = if (total == 0) 0 else (correct * 100) / total
    val band: PerformanceBand get() = PerformanceBand.of(accuracy)
    val attempted: Boolean get() = total > 0
}

data class OverallStats(
    val examsCompleted: Int = 0,
    val quizzesCompleted: Int = 0,
    val averageScorePercent: Int = 0,
    val averageAccuracy: Int = 0,
    val bestScorePercent: Int = 0,
    val currentRank: Int = 0,
    val participants: Int = 0,
    val studyStreakDays: Int = 0,
    val topicsCompleted: Int = 0
)

// ------------------------------------------------------------------ leaderboard

enum class LeaderboardPeriod(val label: String, val teluguLabel: String) {
    DAILY("Daily", "రోజు"),
    WEEKLY("Weekly", "వారం"),
    MONTHLY("Monthly", "నెల"),
    GRAND_TEST("Grand Test", "గ్రాండ్ టెస్ట్");

    val localizedLabel: LocalizedText get() = LocalizedText(en = label, te = teluguLabel)
}

data class LeaderboardEntry(
    val rank: Int,
    val studentId: String,
    val name: String,
    val points: Int,
    val testsCompleted: Int,
    val accuracy: Int,
    val isCurrentUser: Boolean = false
)

// ------------------------------------------------------------------ notifications

enum class StudyNotificationType {
    DAILY_EXAM, GRAND_TEST, NEW_ARTICLE, RESULT, RANK, RECOMMENDATION, SYSTEM
}

data class StudyNotification(
    val id: String,
    val title: LocalizedText,
    val message: LocalizedText,
    val timeMillis: Long,
    val type: StudyNotificationType,
    val isRead: Boolean = false,
    val targetRole: StudyRole? = null
)

// ------------------------------------------------------------------ recommendations

/** One row of "Explore Your Weak Areas". */
data class WeakAreaGroup(
    val subject: Subject,
    val accuracy: Int,
    val band: PerformanceBand,
    val topics: List<TopicPerformance>
)

data class StudyPlanStep(
    val order: Int,
    val title: LocalizedText,
    val subtitle: LocalizedText,
    val done: Boolean = false
)

data class RolePermissionRow(val role: StudyRole, val permissions: List<String>)
