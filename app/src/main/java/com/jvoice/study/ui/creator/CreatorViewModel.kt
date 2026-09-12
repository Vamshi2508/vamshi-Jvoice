package com.jvoice.study.ui.creator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvoice.study.data.model.ContentStatus
import com.jvoice.study.data.model.Difficulty
import com.jvoice.study.data.model.Question
import com.jvoice.study.data.model.QuestionType
import com.jvoice.study.data.model.StudyArticle
import com.jvoice.study.data.repository.StudyRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.jvoice.core.i18n.AppLanguage
import com.jvoice.core.i18n.LocalizedText
import com.jvoice.core.i18n.lt
import com.jvoice.core.i18n.LanguagePreference

data class CreatorCounts(
    val articles: Int = 0,
    val quizzes: Int = 0,
    val questions: Int = 0,
    val drafts: Int = 0,
    val pending: Int = 0,
    val published: Int = 0
)

/**
 * Editable article form used by the Content Creator editor, in both languages.
 *
 * Validation asks "is either language filled", not "is this language filled": a
 * creator writing a Telugu page for Telugu students should not be blocked on
 * English copy. [needsTranslation] is what carries the outstanding work forward.
 */
data class ArticleForm(
    val id: String? = null,
    val subjectId: String = "",
    val topicId: String = "",
    val title: LocalizedText = LocalizedText.EMPTY,
    val description: LocalizedText = LocalizedText.EMPTY,
    val content: LocalizedText = LocalizedText.EMPTY,
    val pointsText: LocalizedText = LocalizedText.EMPTY,
    val examplesText: LocalizedText = LocalizedText.EMPTY,
    val readingMinutes: String = "5"
) {
    /** The fields the language tabs report completeness for. */
    val localizedFields: List<LocalizedText>
        get() = listOf(title, description, content)

    val titleError: LocalizedText?
        get() = if (title.isBlank) lt("Title is required", "శీర్షిక తప్పనిసరి") else null
    val topicError: LocalizedText?
        get() = if (topicId.isBlank()) lt("Choose a subject and topic", "సబ్జెక్ట్, టాపిక్ ఎంచుకోండి") else null

    /**
     * Measured on the longer of the two versions. A filled Telugu body with no
     * English yet is a valid page, and measuring the empty side would reject it.
     */
    val contentError: LocalizedText?
        get() = if (maxOf(content.en.length, content.te.length) < 30)
            lt("Content should be at least 30 characters", "కంటెంట్ కనిష్టం 30 అక్షరాలు ఉండాలి") else null

    val isValid: Boolean get() = titleError == null && topicError == null && contentError == null

    val needsTranslation: List<AppLanguage>
        get() = AppLanguage.entries.filter { language ->
            localizedFields.any { it.rawFor(language).isBlank() }
        }
}

/**
 * Editable question form: four options plus the correct one, in both languages.
 *
 * The options list is positional and shared across languages - slot 2 is the
 * same option in Telugu and English - which is what lets [correctIndex] stay a
 * single number instead of one per language.
 */
data class QuestionForm(
    val id: String? = null,
    val subjectId: String = "",
    val topicId: String = "",
    val text: LocalizedText = LocalizedText.EMPTY,
    val options: List<LocalizedText> = List(4) { LocalizedText.EMPTY },
    val correctIndex: Int = 0,
    val explanation: LocalizedText = LocalizedText.EMPTY,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val type: QuestionType = QuestionType.MCQ
) {
    /** Stem, options and explanation - everything the tabs badge. */
    val localizedFields: List<LocalizedText>
        get() = listOf(text) + options.filterNot { it.isBlank } + listOf(explanation)

    val textError: LocalizedText?
        get() = if (text.isBlank) lt("Question text is required", "ప్రశ్న తప్పనిసరి") else null
    val topicError: LocalizedText?
        get() = if (topicId.isBlank()) lt("Choose a subject and topic", "సబ్జెక్ట్, టాపిక్ ఎంచుకోండి") else null

    /** An option counts as filled once it exists in *either* language. */
    val optionsError: LocalizedText?
        get() = if (options.count { !it.isBlank } < 2)
            lt("At least two options are required", "కనిష్టం రెండు ఆప్షన్లు తప్పనిసరి") else null

    val isValid: Boolean
        get() = textError == null && topicError == null && optionsError == null &&
            options.getOrNull(correctIndex)?.isBlank == false

    val needsTranslation: List<AppLanguage>
        get() = AppLanguage.entries.filter { language ->
            localizedFields.any { it.rawFor(language).isBlank() }
        }

    /** Sets one option slot in one language, leaving the other side alone. */
    fun withOption(index: Int, language: AppLanguage, value: String): QuestionForm =
        copy(options = options.toMutableList().also {
            it[index] = it[index].with(language, value)
        })
}

class CreatorViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _articleForm = MutableStateFlow(ArticleForm())
    val articleForm: StateFlow<ArticleForm> = _articleForm.asStateFlow()

    private val _questionForm = MutableStateFlow(QuestionForm())
    val questionForm: StateFlow<QuestionForm> = _questionForm.asStateFlow()

    private val _statusFilter = MutableStateFlow<ContentStatus?>(null)
    val statusFilter: StateFlow<ContentStatus?> = _statusFilter.asStateFlow()

    val subjects = StudyRepository.subjects

    val counts: StateFlow<CreatorCounts> = combine(
        StudyRepository.articles,
        StudyRepository.questions,
        StudyRepository.quizzes
    ) { articles, questions, quizzes ->
        CreatorCounts(
            articles = articles.size,
            quizzes = quizzes.size,
            questions = questions.size,
            drafts = articles.count { it.status == ContentStatus.DRAFT },
            pending = articles.count { it.status == ContentStatus.PENDING_REVIEW },
            published = articles.count { it.status == ContentStatus.PUBLISHED }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CreatorCounts())

    val articles: StateFlow<List<StudyArticle>> = combine(
        StudyRepository.articles,
        _statusFilter
    ) { _, status -> StudyRepository.articlesByStatus(status) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val questions: StateFlow<List<Question>> = StudyRepository.questions
        .map { it.take(60) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            delay(450)
            _isLoading.value = false
        }
    }

    fun setStatusFilter(status: ContentStatus?) { _statusFilter.value = status }

    fun topicsOf(subjectId: String) = StudyRepository.topicsOf(subjectId)
    fun subjectName(id: String) = StudyRepository.subjectName(id)
    fun topicName(id: String) = StudyRepository.topicName(id)
    fun enabledSubjects() = StudyRepository.enabledSubjects()

    // ------------------------------------------------------------- article form
    fun newArticle() {
        val subject = StudyRepository.enabledSubjects().firstOrNull()
        _articleForm.value = ArticleForm(
            subjectId = subject?.id.orEmpty(),
            topicId = subject?.let { StudyRepository.topicsOf(it.id).firstOrNull()?.id }.orEmpty()
        )
    }

    fun loadArticle(articleId: String) {
        val article = StudyRepository.articleById(articleId) ?: return newArticle()
        _articleForm.value = ArticleForm(
            id = article.id,
            subjectId = article.subjectId,
            topicId = article.topicId,
            // Loaded as pairs, not resolved to one language: opening a page to
            // fix its Telugu must not discard the English already written.
            title = article.title,
            description = article.description,
            content = article.content,
            pointsText = unzipLines(article.importantPoints),
            examplesText = unzipLines(article.examples),
            readingMinutes = article.readingMinutes.toString()
        )
    }

    fun updateArticleForm(transform: (ArticleForm) -> ArticleForm) {
        _articleForm.value = transform(_articleForm.value)
    }

    fun saveArticle(status: ContentStatus, authorName: String): Boolean {
        val form = _articleForm.value
        if (status != ContentStatus.DRAFT && !form.isValid) return false
        if (form.title.isBlank || form.topicId.isBlank()) return false
        StudyRepository.saveArticle(
            existingId = form.id,
            subjectId = form.subjectId,
            topicId = form.topicId,
            title = form.title.trimmed(),
            description = form.description.trimmed(),
            content = form.content.trimmed(),
            importantPoints = zipLines(form.pointsText),
            examples = zipLines(form.examplesText),
            readingMinutes = form.readingMinutes.toIntOrNull() ?: 5,
            status = status,
            authorName = authorName
        )
        return true
    }

    fun deleteArticle(id: String) = StudyRepository.deleteArticle(id)
    fun publishArticle(id: String) = StudyRepository.setArticleStatus(id, ContentStatus.PUBLISHED)

    // ------------------------------------------------------------ question form
    fun newQuestion() {
        val subject = StudyRepository.enabledSubjects().firstOrNull()
        _questionForm.value = QuestionForm(
            subjectId = subject?.id.orEmpty(),
            topicId = subject?.let { StudyRepository.topicsOf(it.id).firstOrNull()?.id }.orEmpty()
        )
    }

    fun loadQuestion(questionId: String) {
        val question = StudyRepository.questionById(questionId) ?: return newQuestion()
        _questionForm.value = QuestionForm(
            id = question.id,
            subjectId = question.subjectId,
            topicId = question.topicId,
            text = question.text,
            options = question.options +
                List((4 - question.options.size).coerceAtLeast(0)) { LocalizedText.EMPTY },
            correctIndex = question.correctIndex,
            explanation = question.explanation,
            difficulty = question.difficulty,
            type = question.type
        )
    }

    fun updateQuestionForm(transform: (QuestionForm) -> QuestionForm) {
        _questionForm.value = transform(_questionForm.value)
    }

    fun saveQuestion(status: ContentStatus): Boolean {
        val form = _questionForm.value
        if (!form.isValid) return false
        // Filtered on "filled in either language", so a half-translated option
        // survives the save instead of being silently dropped.
        val options = form.options.filterNot { it.isBlank }
        val correctOption = form.options[form.correctIndex]
        StudyRepository.saveQuestion(
            existingId = form.id,
            subjectId = form.subjectId,
            topicId = form.topicId,
            text = form.text.trimmed(),
            options = options.map { it.trimmed() },
            correctIndex = options.indexOf(correctOption).coerceAtLeast(0),
            explanation = form.explanation.trimmed(),
            difficulty = form.difficulty,
            type = form.type,
            status = status
        )
        return true
    }

    fun deleteQuestion(id: String) = StudyRepository.deleteQuestion(id)

    // ------------------------------------------------------------------- quizzes
    fun createQuizForTopic(topicId: String, authorName: String): Boolean {
        val questions = StudyRepository.questionsForTopic(topicId)
        if (questions.size < 2) return false
        val topic = StudyRepository.topicById(topicId) ?: return false
        StudyRepository.saveQuiz(
            existingId = null,
            subjectId = topic.subjectId,
            topicId = topicId,
            title = LocalizedText(
                en = topic.name.en + " - Topic Quiz",
                te = topic.name.get(AppLanguage.TELUGU) + " - \u0c1f\u0c3e\u0c2a\u0c3f\u0c15\u0c4d \u0c15\u0c4d\u0c35\u0c3f\u0c1c\u0c4d"
            ),
            questionIds = questions.take(10).map { it.id },
            durationMinutes = 10,
            status = ContentStatus.PUBLISHED,
            authorName = authorName
        )
        return true
    }

    val quizzes = StudyRepository.quizzes

    /**
     * Flattens bilingual bullets back into the two multi-line boxes.
     *
     * The inverse of [zipLines]. Blank sides are kept as empty lines rather than
     * dropped, so line N in the Telugu box still lines up with line N in the
     * English box when the page is saved again.
     */
    private fun unzipLines(items: List<LocalizedText>): LocalizedText = LocalizedText(
        en = items.joinToString("\n") { it.en },
        te = items.joinToString("\n") { it.te }
    )

    /**
     * Splits the two multi-line boxes and pairs the lines up by position.
     *
     * Position is the contract: the second Telugu bullet is the translation of
     * the second English one. A shorter list leaves the tail untranslated rather
     * than misaligning the pairs.
     */
    private fun zipLines(text: LocalizedText): List<LocalizedText> {
        fun lines(value: String) =
            value.split("\n").map { it.trim() }.filter { it.isNotBlank() }

        val en = lines(text.en)
        val te = lines(text.te)
        return (0 until maxOf(en.size, te.size)).map { index ->
            LocalizedText(
                en = en.getOrElse(index) { "" },
                te = te.getOrElse(index) { "" }
            )
        }
    }
}
