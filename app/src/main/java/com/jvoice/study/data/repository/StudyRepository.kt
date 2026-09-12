package com.jvoice.study.data.repository

import com.jvoice.study.data.mock.MockDataSource
import com.jvoice.study.data.model.ContentStatus
import com.jvoice.study.data.model.Difficulty
import com.jvoice.study.data.model.Question
import com.jvoice.study.data.model.QuestionSource
import com.jvoice.study.data.model.QuestionType
import com.jvoice.study.data.model.Quiz
import com.jvoice.study.data.model.StudyArticle
import com.jvoice.study.data.model.StudyNotification
import com.jvoice.study.data.model.StudyNotificationType
import com.jvoice.study.data.model.StudyRole
import com.jvoice.study.data.model.StudyUser
import com.jvoice.study.data.model.Subject
import com.jvoice.study.data.model.Topic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.jvoice.core.i18n.AppLanguage
import com.jvoice.core.i18n.LanguagePreference
import com.jvoice.core.i18n.LocalizedText

/**
 * Syllabus and content repository: subjects, topics, articles, questions and
 * quizzes, plus the student's topic-completion state.
 *
 * In-memory singleton for the prototype. Every screen reads through here, so a
 * Firestore or REST implementation can replace the body of these methods later.
 */
object StudyRepository {

    private val _subjects = MutableStateFlow(MockDataSource.subjects)
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    private val _topics = MutableStateFlow(MockDataSource.topics)
    val topics: StateFlow<List<Topic>> = _topics.asStateFlow()

    private val _articles = MutableStateFlow(MockDataSource.articles)
    val articles: StateFlow<List<StudyArticle>> = _articles.asStateFlow()

    private val _questions = MutableStateFlow(MockDataSource.questions)
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _quizzes = MutableStateFlow(MockDataSource.quizzes)
    val quizzes: StateFlow<List<Quiz>> = _quizzes.asStateFlow()

    private val _completedTopicIds = MutableStateFlow(MockDataSource.completedTopicIds)
    val completedTopicIds: StateFlow<Set<String>> = _completedTopicIds.asStateFlow()

    private val _users = MutableStateFlow(MockDataSource.users)
    val users: StateFlow<List<StudyUser>> = _users.asStateFlow()

    private val _notifications = MutableStateFlow(MockDataSource.notifications)
    val notifications: StateFlow<List<StudyNotification>> = _notifications.asStateFlow()

    private val _currentUser = MutableStateFlow<StudyUser?>(null)
    val currentUser: StateFlow<StudyUser?> = _currentUser.asStateFlow()

    private var idCounter = 500
    private fun nextId(prefix: String): String {
        idCounter += 1
        return prefix + idCounter
    }

    val rolePermissions = MockDataSource.rolePermissions
    val student = MockDataSource.student

    // ------------------------------------------------------------------ session
    fun signInAs(role: StudyRole) {
        _currentUser.value = MockDataSource.demoAccounts[role]
    }

    fun signOut() {
        _currentUser.value = null
    }

    // ------------------------------------------------------------------ lookups
    fun subjectById(id: String): Subject? = _subjects.value.firstOrNull { it.id == id }
    /**
     * Display name for a subject, in the language the student is reading in.
     *
     * Reads the preference directly rather than taking a language parameter -
     * these are called from deep inside list builders that have no composition
     * to read from and no business carrying a language around.
     */
    fun subjectName(
        id: String,
        language: AppLanguage = LanguagePreference.current
    ): String = subjectById(id)?.name?.get(language)
        ?: if (language == AppLanguage.TELUGU) "సాధారణం" else "General"
    fun topicById(id: String): Topic? = _topics.value.firstOrNull { it.id == id }
    fun topicName(
        id: String,
        language: AppLanguage = LanguagePreference.current
    ): String = topicById(id)?.name?.get(language)
        ?: if (language == AppLanguage.TELUGU) "టాపిక్" else "Topic"
    fun questionById(id: String): Question? = _questions.value.firstOrNull { it.id == id }
    fun articleById(id: String): StudyArticle? = _articles.value.firstOrNull { it.id == id }
    fun quizById(id: String): Quiz? = _quizzes.value.firstOrNull { it.id == id }

    fun enabledSubjects(): List<Subject> = _subjects.value.filter { it.isEnabled }

    fun topicsOf(subjectId: String): List<Topic> =
        _topics.value.filter { it.subjectId == subjectId && it.isEnabled }.sortedBy { it.order }

    /** Every published piece of material filed under one topic. */
    fun articlesForTopic(topicId: String): List<StudyArticle> =
        _articles.value.filter { it.topicId == topicId && it.status == ContentStatus.PUBLISHED }

    fun publishedArticleForTopic(topicId: String): StudyArticle? =
        _articles.value.firstOrNull { it.topicId == topicId && it.status == ContentStatus.PUBLISHED }

    /** Every published set filed under a topic — one card each in the app. */
    fun quizSetsForTopic(topicId: String): List<Quiz> =
        _quizzes.value.filter { it.topicId == topicId && it.status == ContentStatus.PUBLISHED }

    /** The questions of one set, in the order the set lists them. */
    fun questionsOfQuiz(quiz: Quiz): List<Question> =
        quiz.questionIds.mapNotNull { id -> _questions.value.firstOrNull { it.id == id } }

    fun quizForTopic(topicId: String): Quiz? =
        _quizzes.value.firstOrNull { it.topicId == topicId && it.status == ContentStatus.PUBLISHED }

    fun questionsForTopic(topicId: String): List<Question> =
        _questions.value.filter { it.topicId == topicId && it.status == ContentStatus.PUBLISHED }

    /** Practice questions written for this topic. */
    fun sampleQuestionsForTopic(topicId: String): List<Question> =
        questionsForTopic(topicId).filter { it.source == QuestionSource.SAMPLE }

    /** What real papers have already asked on this topic. */
    fun previousQuestionsForTopic(topicId: String): List<Question> =
        questionsForTopic(topicId).filter { it.source == QuestionSource.PREVIOUS }

    fun questionCountForSubject(subjectId: String): Int =
        _questions.value.count { it.subjectId == subjectId }

    fun articleCountForSubject(subjectId: String): Int =
        _articles.value.count { it.subjectId == subjectId }

    /** Previous / next topic within the same subject, for article navigation. */
    fun neighbourTopics(topicId: String): Pair<Topic?, Topic?> {
        val topic = topicById(topicId) ?: return null to null
        val siblings = topicsOf(topic.subjectId)
        val index = siblings.indexOfFirst { it.id == topicId }
        if (index == -1) return null to null
        return siblings.getOrNull(index - 1) to siblings.getOrNull(index + 1)
    }

    // ------------------------------------------------------------------ progress
    fun markTopicCompleted(topicId: String) {
        _completedTopicIds.update { it + topicId }
    }

    fun toggleTopicCompleted(topicId: String): Boolean {
        var nowDone = false
        _completedTopicIds.update {
            if (it.contains(topicId)) {
                nowDone = false
                it - topicId
            } else {
                nowDone = true
                it + topicId
            }
        }
        return nowDone
    }

    fun isTopicCompleted(topicId: String) = _completedTopicIds.value.contains(topicId)

    fun subjectProgressPercent(subjectId: String): Int {
        val topics = topicsOf(subjectId)
        if (topics.isEmpty()) return 0
        val done = topics.count { _completedTopicIds.value.contains(it.id) }
        return done * 100 / topics.size
    }

    fun overallProgressPercent(): Int {
        val all = _topics.value.filter { it.isEnabled }
        if (all.isEmpty()) return 0
        return _completedTopicIds.value.size * 100 / all.size
    }

    /** The next unfinished topic on the selected exam - powers "Continue Studying". */
    fun continueStudyingTopic(): Topic? =
        _topics.value
            .filter {
                it.isEnabled &&
                    !_completedTopicIds.value.contains(it.id) &&
                    ExamTrackRepository.isInScope(it.subjectId)
            }
            .sortedWith(compareBy({ it.subjectId }, { it.order }))
            .firstOrNull()

    // ------------------------------------------------------------ search / lists
    fun searchContent(query: String): List<StudyArticle> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return emptyList()
        return _articles.value.filter { article ->
            ExamTrackRepository.isInScope(article.subjectId) &&
                (
                    // Searches both languages, so a student browsing in Telugu
                    // still finds a page by its English title.
                    article.matches(query) ||
                        subjectById(article.subjectId)?.name?.matches(query) == true ||
                        topicById(article.topicId)?.name?.matches(query) == true
                    )
        }
    }

    fun filterQuestions(
        subjectId: String? = null,
        topicId: String? = null,
        difficulty: Difficulty? = null,
        type: QuestionType? = null,
        status: ContentStatus? = null,
        query: String = ""
    ): List<Question> {
        val q = query.trim().lowercase()
        return _questions.value.filter { question ->
            (subjectId == null || question.subjectId == subjectId) &&
                (topicId == null || question.topicId == topicId) &&
                (difficulty == null || question.difficulty == difficulty) &&
                (type == null || question.type == type) &&
                (status == null || question.status == status) &&
                (q.isBlank() || question.matches(query))
        }
    }

    fun articlesByStatus(status: ContentStatus?): List<StudyArticle> =
        _articles.value.filter { status == null || it.status == status }.sortedByDescending { it.createdAt }

    fun articlesByAuthor(author: String): List<StudyArticle> =
        _articles.value.filter { it.authorName == author }.sortedByDescending { it.createdAt }

    // --------------------------------------------------- content creator actions
    fun saveArticle(
        existingId: String?,
        subjectId: String,
        topicId: String,
        title: LocalizedText,
        description: LocalizedText,
        content: LocalizedText,
        importantPoints: List<LocalizedText>,
        examples: List<LocalizedText>,
        readingMinutes: Int,
        status: ContentStatus,
        authorName: String
    ): String {
        val id = existingId ?: nextId("a_new_")
        val existing = existingId?.let { articleById(it) }
        if (existing == null) {
            val article = StudyArticle(
                id = id,
                subjectId = subjectId,
                topicId = topicId,
                title = title,
                description = description,
                content = content,
                importantPoints = importantPoints,
                examples = examples,
                readingMinutes = readingMinutes,
                status = status,
                authorName = authorName,
                createdAt = System.currentTimeMillis()
            )
            _articles.update { listOf(article) + it }
        } else {
            _articles.update { list ->
                list.map {
                    if (it.id == id) {
                        it.copy(
                            subjectId = subjectId,
                            topicId = topicId,
                            title = title,
                            description = description,
                            content = content,
                            importantPoints = importantPoints,
                            examples = examples,
                            readingMinutes = readingMinutes,
                            status = status
                        )
                    } else it
                }
            }
        }
        if (status == ContentStatus.PENDING_REVIEW) {
            pushNotification(
                LocalizedText("Article submitted for review", "సమీక్ష కోసం కథనం సమర్పించబడింది"),
                title.trimmedTo(60),
                StudyNotificationType.SYSTEM,
                StudyRole.STUDY_ADMIN
            )
        }
        if (status == ContentStatus.PUBLISHED) {
            pushNotification(
                LocalizedText("New study article", "కొత్త స్టడీ ఆర్టికల్"),
                title.trimmedTo(60),
                StudyNotificationType.NEW_ARTICLE,
                StudyRole.STUDENT
            )
        }
        return id
    }

    fun setArticleStatus(articleId: String, status: ContentStatus) {
        _articles.update { list -> list.map { if (it.id == articleId) it.copy(status = status) else it } }
        if (status == ContentStatus.PUBLISHED) {
            articleById(articleId)?.let {
                pushNotification(
                    LocalizedText("New study article", "కొత్త స్టడీ ఆర్టికల్"),
                    it.title.trimmedTo(60),
                    StudyNotificationType.NEW_ARTICLE,
                    StudyRole.STUDENT
                )
            }
        }
    }

    fun deleteArticle(articleId: String) {
        _articles.update { list -> list.filterNot { it.id == articleId } }
    }

    fun saveQuestion(
        existingId: String?,
        subjectId: String,
        topicId: String,
        text: LocalizedText,
        options: List<LocalizedText>,
        correctIndex: Int,
        explanation: LocalizedText,
        difficulty: Difficulty,
        type: QuestionType,
        status: ContentStatus
    ): String {
        val id = existingId ?: nextId("q_new_")
        val existing = existingId?.let { questionById(it) }
        if (existing == null) {
            val question = Question(
                id = id,
                subjectId = subjectId,
                topicId = topicId,
                text = text,
                options = options,
                correctIndex = correctIndex,
                explanation = explanation,
                difficulty = difficulty,
                type = type,
                status = status
            )
            _questions.update { listOf(question) + it }
        } else {
            _questions.update { list ->
                list.map {
                    if (it.id == id) {
                        it.copy(
                            subjectId = subjectId,
                            topicId = topicId,
                            text = text,
                            options = options,
                            correctIndex = correctIndex,
                            explanation = explanation,
                            difficulty = difficulty,
                            type = type,
                            status = status
                        )
                    } else it
                }
            }
        }
        return id
    }

    fun deleteQuestion(questionId: String) {
        _questions.update { list -> list.filterNot { it.id == questionId } }
    }

    fun saveQuiz(
        existingId: String?,
        subjectId: String,
        topicId: String,
        title: LocalizedText,
        questionIds: List<String>,
        durationMinutes: Int,
        status: ContentStatus,
        authorName: String
    ): String {
        val id = existingId ?: nextId("quiz_new_")
        val existing = existingId?.let { quizById(it) }
        if (existing == null) {
            _quizzes.update {
                listOf(Quiz(id, subjectId, topicId, title, questionIds, durationMinutes, status, authorName)) + it
            }
        } else {
            _quizzes.update { list ->
                list.map {
                    if (it.id == id) {
                        it.copy(
                            subjectId = subjectId,
                            topicId = topicId,
                            title = title,
                            questionIds = questionIds,
                            durationMinutes = durationMinutes,
                            status = status
                        )
                    } else it
                }
            }
        }
        return id
    }

    fun deleteQuiz(quizId: String) {
        _quizzes.update { list -> list.filterNot { it.id == quizId } }
    }

    // ------------------------------------------------------ study admin actions
    fun addSubject(name: LocalizedText, emoji: String) {
        _subjects.update { it + Subject(nextId("sub_"), name, emoji.ifBlank { "📘" }) }
    }

    fun updateSubject(id: String, name: LocalizedText, emoji: String) {
        _subjects.update { list ->
            list.map { if (it.id == id) it.copy(name = name, emoji = emoji) else it }
        }
    }

    fun toggleSubjectEnabled(id: String) {
        _subjects.update { list -> list.map { if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it } }
    }

    /** Subjects that still own topics cannot be deleted. */
    fun deleteSubject(id: String): Boolean {
        if (_topics.value.any { it.subjectId == id }) return false
        _subjects.update { list -> list.filterNot { it.id == id } }
        return true
    }

    fun addTopic(subjectId: String, name: LocalizedText, difficulty: Difficulty) {
        val order = (_topics.value.filter { it.subjectId == subjectId }.maxOfOrNull { it.order } ?: 0) + 1
        _topics.update {
            it + Topic(nextId("t_"), subjectId, name, order, difficulty)
        }
    }

    fun updateTopic(id: String, subjectId: String, name: LocalizedText, difficulty: Difficulty) {
        _topics.update { list ->
            list.map {
                if (it.id == id) {
                    it.copy(subjectId = subjectId, name = name, difficulty = difficulty)
                } else it
            }
        }
    }

    fun toggleTopicEnabled(id: String) {
        _topics.update { list -> list.map { if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it } }
    }

    /** Topics carrying articles or questions cannot be deleted. */
    fun deleteTopic(id: String): Boolean {
        val inUse = _articles.value.any { it.topicId == id } || _questions.value.any { it.topicId == id }
        if (inUse) return false
        _topics.update { list -> list.filterNot { it.id == id } }
        return true
    }

    // ------------------------------------------------------- super admin: users
    fun setUserActive(userId: String, active: Boolean) {
        _users.update { list -> list.map { if (it.id == userId) it.copy(isActive = active) else it } }
    }

    fun changeUserRole(userId: String, role: StudyRole) {
        _users.update { list -> list.map { if (it.id == userId) it.copy(role = role) else it } }
    }

    fun updateUser(userId: String, name: String, email: String) {
        _users.update { list -> list.map { if (it.id == userId) it.copy(name = name, email = email) else it } }
    }

    fun addUser(name: String, email: String, role: StudyRole) {
        _users.update { it + StudyUser(nextId("u_"), name, email, role) }
    }

    fun countUsers(role: StudyRole) = _users.value.count { it.role == role }

    // ---------------------------------------------------------- notifications
    fun pushNotification(
        title: LocalizedText,
        message: LocalizedText,
        type: StudyNotificationType,
        targetRole: StudyRole? = null
    ) {
        val item = StudyNotification(
            id = nextId("sn_"),
            title = title,
            message = message,
            timeMillis = System.currentTimeMillis(),
            type = type,
            isRead = false,
            targetRole = targetRole
        )
        _notifications.update { listOf(item) + it }
    }

    fun notificationsFor(role: StudyRole): List<StudyNotification> =
        _notifications.value
            .filter { it.targetRole == null || it.targetRole == role }
            .sortedByDescending { it.timeMillis }

    fun unreadCountFor(role: StudyRole): Int = notificationsFor(role).count { !it.isRead }

    fun markNotificationRead(id: String) {
        _notifications.update { list -> list.map { if (it.id == id) it.copy(isRead = true) else it } }
    }

    fun markAllNotificationsRead(role: StudyRole) {
        _notifications.update { list ->
            list.map { if (it.targetRole == null || it.targetRole == role) it.copy(isRead = true) else it }
        }
    }
}
