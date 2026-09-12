package com.jvoice.news.ui.superadmin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvoice.news.data.model.NewsStatus
import com.jvoice.news.data.model.RolePermission
import com.jvoice.news.data.model.SystemSettings
import com.jvoice.news.data.model.User
import com.jvoice.news.data.model.UserRole
import com.jvoice.news.data.repository.NewsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SuperAdminStats(
    val totalUsers: Int = 0,
    val totalReporters: Int = 0,
    val totalEditors: Int = 0,
    val totalAdmins: Int = 0,
    val totalArticles: Int = 0,
    val published: Int = 0,
    val pending: Int = 0,
    val rejected: Int = 0
)

class SuperAdminViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _roleFilter = MutableStateFlow<UserRole?>(null)
    val roleFilter: StateFlow<UserRole?> = _roleFilter.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val settings: StateFlow<SystemSettings> = NewsRepository.settings
    val rolePermissions: List<RolePermission> = NewsRepository.rolePermissions

    val stats: StateFlow<SuperAdminStats> = combine(
        NewsRepository.users,
        NewsRepository.articles
    ) { users, articles ->
        SuperAdminStats(
            totalUsers = users.count { it.role == UserRole.READER },
            totalReporters = users.count { it.role == UserRole.REPORTER },
            totalEditors = users.count { it.role == UserRole.EDITOR },
            totalAdmins = users.count { it.role == UserRole.NEWS_ADMIN || it.role == UserRole.SUPER_ADMIN },
            totalArticles = articles.size,
            published = articles.count { it.status == NewsStatus.PUBLISHED },
            pending = articles.count {
                it.status == NewsStatus.SUBMITTED || it.status == NewsStatus.UNDER_REVIEW
            },
            rejected = articles.count { it.status == NewsStatus.REJECTED }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SuperAdminStats())

    val users: StateFlow<List<User>> = combine(
        NewsRepository.users,
        _roleFilter,
        _query
    ) { users, role, query ->
        val q = query.trim().lowercase()
        users
            .filter { role == null || it.role == role }
            .filter {
                q.isBlank() || it.name.lowercase().contains(q) ||
                    it.email.lowercase().contains(q) ||
                    it.location.lowercase().contains(q)
            }
            .sortedBy { it.role.ordinal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categoryCount: StateFlow<Int> = NewsRepository.categories
        .let { flow ->
            combine(flow, NewsRepository.articles) { categories, _ -> categories.size }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        viewModelScope.launch {
            delay(500)
            _isLoading.value = false
        }
    }

    fun setRoleFilter(role: UserRole?) { _roleFilter.value = role }
    fun setQuery(value: String) { _query.value = value }

    fun setActive(userId: String, active: Boolean) = NewsRepository.setUserActive(userId, active)
    fun changeRole(userId: String, role: UserRole) = NewsRepository.changeUserRole(userId, role)
    fun updateUser(userId: String, name: String, email: String, location: String) =
        NewsRepository.updateUser(userId, name, email, location)

    fun updateSettings(transform: (SystemSettings) -> SystemSettings) =
        NewsRepository.updateSettings(transform)

    val locations: List<String> = NewsRepository.locations
}
