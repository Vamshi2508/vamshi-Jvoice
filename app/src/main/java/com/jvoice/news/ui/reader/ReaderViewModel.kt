package com.jvoice.news.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvoice.news.data.model.Category
import com.jvoice.news.data.model.NewsArticle
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

data class HomeFeed(
    val breaking: List<NewsArticle> = emptyList(),
    val latest: List<NewsArticle> = emptyList(),
    val trending: List<NewsArticle> = emptyList(),
    val categorySections: List<Pair<Category, List<NewsArticle>>> = emptyList()
)

class ReaderViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val categories: StateFlow<List<Category>> = NewsRepository.categories
    val savedIds: StateFlow<Set<String>> = NewsRepository.savedArticleIds
    val selectedLocation: StateFlow<String> = NewsRepository.selectedLocation
    val locations: List<String> = NewsRepository.locations

    /** Home feed recomputes whenever the shared repository changes (e.g. an editor publishes). */
    val feed: StateFlow<HomeFeed> = combine(
        NewsRepository.articles,
        NewsRepository.categories,
        NewsRepository.selectedLocation
    ) { _, categories, _ ->
        val published = NewsRepository.publishedArticles()
        HomeFeed(
            breaking = published.filter { it.isBreaking }.take(5),
            latest = published.take(10),
            trending = published.filter { it.isTrending }.sortedByDescending { it.views }.take(8),
            categorySections = categories
                .filter { it.isEnabled }
                .mapNotNull { category ->
                    val items = published.filter { it.categoryId == category.id }.take(4)
                    if (items.isEmpty()) null else category to items
                }
                .take(6)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeFeed())

    val notifications: StateFlow<List<NotificationItem>> = NewsRepository.notifications
        .map { NewsRepository.notificationsFor(UserRole.READER) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val unreadCount: StateFlow<Int> = NewsRepository.notifications
        .map { NewsRepository.unreadCountFor(UserRole.READER) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Clips feed: every published article, breaking first. */
    val clips: StateFlow<List<NewsArticle>> = NewsRepository.articles
        .map {
            val published = NewsRepository.publishedArticles()
            published.filter { a -> a.isBreaking } + published.filterNot { a -> a.isBreaking }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val savedArticles: StateFlow<List<NewsArticle>> = combine(
        NewsRepository.articles,
        NewsRepository.savedArticleIds
    ) { articles, saved ->
        articles.filter { saved.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // Simulated first load so the loading state is visible in the prototype.
        viewModelScope.launch {
            delay(650)
            _isLoading.value = false
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _errorMessage.value = null
            delay(900)
            _isRefreshing.value = false
        }
    }

    fun retry() {
        _errorMessage.value = null
        _isLoading.value = true
        viewModelScope.launch {
            delay(600)
            _isLoading.value = false
        }
    }

    fun currentUserName(): String = NewsRepository.currentUser.value?.name ?: "Reader"

    fun categoryName(id: String) = NewsRepository.categoryName(id)

    fun setLocation(location: String) = NewsRepository.setLocation(location)

    fun toggleSave(articleId: String): Boolean = NewsRepository.toggleSaved(articleId)

    fun isSaved(articleId: String) = NewsRepository.isSaved(articleId)

    fun articleById(id: String) = NewsRepository.articleById(id)

    fun registerView(id: String) = NewsRepository.registerView(id)

    fun reportArticle(id: String) = NewsRepository.reportArticle(id)

    fun related(article: NewsArticle) = NewsRepository.relatedArticles(article)

    fun moreFromCategory(article: NewsArticle) = NewsRepository.moreFromCategory(article)

    fun articlesInCategory(categoryId: String) =
        NewsRepository.publishedArticles().filter { it.categoryId == categoryId }

    fun markNotificationRead(id: String) = NewsRepository.markNotificationRead(id)

    fun markAllRead() = NewsRepository.markAllNotificationsRead(UserRole.READER)

    fun clearSaved() = NewsRepository.clearSaved()
}
