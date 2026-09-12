package com.jvoice.news.ui.reporter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvoice.news.data.model.Category
import com.jvoice.news.data.model.NewsArticle
import com.jvoice.news.data.model.NewsStatus
import com.jvoice.news.data.model.NotificationItem
import com.jvoice.news.data.model.UserRole
import com.jvoice.news.data.repository.NewsRepository
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

data class ReporterStatsUi(
    val total: Int = 0,
    val drafts: Int = 0,
    val pending: Int = 0,
    val approved: Int = 0,
    val rejected: Int = 0,
    val published: Int = 0,
    val sentBack: Int = 0
)

/** Editable form state for Create / Edit News. */
/**
 * The reporter's filing form, bilingual.
 *
 * Validation runs against "is either language filled", not "is this language
 * filled". A reporter filing in Telugu at 2am should not be blocked on English
 * copy - the story goes out in Telugu and the desk adds the translation later.
 * [needsTranslation] is what surfaces that debt.
 *
 * `tagsText` stays a single comma-separated string per language rather than a
 * list, because that is what the text box holds; it is split on save.
 */
data class ArticleForm(
    val id: String? = null,
    val headline: LocalizedText = LocalizedText.EMPTY,
    val shortDescription: LocalizedText = LocalizedText.EMPTY,
    val content: LocalizedText = LocalizedText.EMPTY,
    val categoryId: String = "",
    val location: String = "Hyderabad",
    val imageUrl: String = "",
    val photosText: String = "",
    val videosText: String = "",
    val tagsText: LocalizedText = LocalizedText.EMPTY,
    val isBreaking: Boolean = false
) {
    /** The fields the language tabs report completeness for. */
    val localizedFields: List<LocalizedText>
        get() = listOf(headline, shortDescription, content)

    val headlineError: LocalizedText?
        get() = if (headline.isBlank) lt("Headline is required", "శీర్షిక తప్పనిసరి") else null
    val descriptionError: LocalizedText?
        get() = if (shortDescription.isBlank) lt("Short description is required", "సంక్షిప్త వివరణ తప్పనిసరి") else null

    /**
     * Length is checked on the longest version written, not on a chosen language:
     * a filled Telugu body and an empty English one is a valid story, and
     * measuring the empty side would reject it.
     */
    val contentError: LocalizedText?
        get() = if (maxOf(content.en.length, content.te.length) < 20)
            lt("Article should be at least 20 characters", "కథనం కనిష్టం 20 అక్షరాలు ఉండాలి") else null

    val categoryError: LocalizedText?
        get() = if (categoryId.isBlank()) lt("Pick a category", "విభాగం ఎంచుకోండి") else null

    val isValid: Boolean
        get() = headlineError == null && descriptionError == null &&
            contentError == null && categoryError == null

    /** Languages still missing from at least one of the copy fields. */
    val needsTranslation: List<AppLanguage>
        get() = AppLanguage.entries.filter { language ->
            localizedFields.any { it.rawFor(language).isBlank() }
        }
}

class ReporterViewModel : ViewModel() {

    private val reporterUser get() = NewsRepository.currentUser.value

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _form = MutableStateFlow(ArticleForm())
    val form: StateFlow<ArticleForm> = _form.asStateFlow()

    val categories: StateFlow<List<Category>> = NewsRepository.categories
    val locations: List<String> = NewsRepository.locations

    val myArticles: StateFlow<List<NewsArticle>> = combine(
        NewsRepository.articles,
        NewsRepository.currentUser
    ) { articles, user ->
        articles.filter { it.reporterId == (user?.id ?: "") }.sortedByDescending { it.updatedAt }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val stats: StateFlow<ReporterStatsUi> = myArticles.map { list ->
        ReporterStatsUi(
            total = list.size,
            drafts = list.count { it.status == NewsStatus.DRAFT },
            pending = list.count { it.status == NewsStatus.SUBMITTED || it.status == NewsStatus.UNDER_REVIEW },
            approved = list.count { it.status == NewsStatus.APPROVED || it.status == NewsStatus.PUBLISHED },
            rejected = list.count { it.status == NewsStatus.REJECTED },
            published = list.count { it.status == NewsStatus.PUBLISHED },
            sentBack = list.count { it.status == NewsStatus.SENT_BACK }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReporterStatsUi())

    val notifications: StateFlow<List<NotificationItem>> = NewsRepository.notifications
        .map { NewsRepository.notificationsFor(UserRole.REPORTER) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            delay(500)
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

    fun categoryName(id: String) = NewsRepository.categoryName(id)

    fun articlesWithStatus(status: NewsStatus) = myArticles.value.filter { it.status == status }

    // ------------------------------------------------------------------ form
    fun startNewArticle() {
        _form.value = ArticleForm(
            categoryId = NewsRepository.categories.value.firstOrNull { it.isEnabled }?.id ?: "",
            location = reporterUser?.location ?: "Hyderabad",
            imageUrl = "https://picsum.photos/seed/new" + System.currentTimeMillis() % 1000 + "/900/600"
        )
    }

    fun loadForEdit(articleId: String) {
        val article = NewsRepository.articleById(articleId) ?: return startNewArticle()
        _form.value = ArticleForm(
            id = article.id,
            headline = article.headline,
            shortDescription = article.shortDescription,
            content = article.content,
            categoryId = article.categoryId,
            location = article.location,
            imageUrl = article.imageUrl,
            photosText = article.photoUrls.joinToString("\n"),
            videosText = article.videoUrls.joinToString("\n"),
            // Rebuilt per language so each box shows only its own tags.
            tagsText = LocalizedText(
                en = article.tags.mapNotNull { it.en.ifBlank { null } }.joinToString(", "),
                te = article.tags.mapNotNull { it.te.ifBlank { null } }.joinToString(", ")
            ),
            isBreaking = article.isBreaking
        )
    }

    fun updateForm(transform: (ArticleForm) -> ArticleForm) {
        _form.value = transform(_form.value)
    }

    /** @return true when the article was stored. */
    fun save(submit: Boolean): Boolean {
        val current = _form.value
        if (submit && !current.isValid) return false
        // A draft only needs a headline in one language to be worth keeping.
        if (!submit && current.headline.isBlank) return false

        val user = reporterUser ?: return false
        NewsRepository.createOrUpdateArticle(
            existingId = current.id,
            headline = current.headline.trimmed(),
            shortDescription = current.shortDescription.trimmed(),
            content = current.content.trimmed(),
            categoryId = current.categoryId,
            location = current.location,
            imageUrl = current.imageUrl.ifBlank {
                "https://picsum.photos/seed/jv" + System.currentTimeMillis() % 9999 + "/900/600"
            },
            tags = zipTags(current.tagsText),
            isBreaking = current.isBreaking,
            submit = submit,
            photoUrls = current.photosText.lines().map { it.trim() }.filter { it.isNotBlank() },
            videoUrls = current.videosText.lines().map { it.trim() }.filter { it.isNotBlank() },
            reporterId = user.id,
            reporterName = user.name
        )
        return true
    }

    fun deleteDraft(articleId: String) = NewsRepository.deleteDraft(articleId)

    fun canEdit(article: NewsArticle) = article.status.isEditableByReporter

    /**
     * Pairs up the two comma-separated tag boxes by position.
     *
     * Position is the only signal available - the boxes are free text, so there
     * is nothing to join on. Any surplus on one side becomes a single-language
     * tag rather than being dropped, which is the honest outcome: the tag exists,
     * its translation does not yet.
     */
    private fun zipTags(text: LocalizedText): List<LocalizedText> {
        fun split(value: String) =
            value.split(",").map { it.trim().removePrefix("#") }.filter { it.isNotBlank() }

        val en = split(text.en)
        val te = split(text.te)
        return (0 until maxOf(en.size, te.size)).map { index ->
            LocalizedText(
                en = en.getOrElse(index) { "" },
                te = te.getOrElse(index) { "" }
            )
        }
    }
}
