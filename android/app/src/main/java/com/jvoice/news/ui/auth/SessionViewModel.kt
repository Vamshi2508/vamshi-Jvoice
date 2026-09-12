package com.jvoice.news.ui.auth

import androidx.lifecycle.ViewModel
import com.jvoice.news.data.model.User
import com.jvoice.news.data.model.UserRole
import com.jvoice.news.data.repository.NewsRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * Dummy session holder. There is no real authentication in Module 1 - picking a
 * role on the selector screen simply switches the active demo identity.
 */
class SessionViewModel : ViewModel() {

    val currentUser: StateFlow<User?> = NewsRepository.currentUser

    fun signInAs(role: UserRole) = NewsRepository.signInAs(role)

    fun signOut() = NewsRepository.signOut()
}
