package com.jvoice.study.ui.auth

import androidx.lifecycle.ViewModel
import com.jvoice.study.data.model.StudyRole
import com.jvoice.study.data.model.StudyUser
import com.jvoice.study.data.repository.StudyRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * Dummy session holder for Module 2. Selecting a role on the landing page just
 * switches the active demo identity - there is no real authentication.
 */
class StudySessionViewModel : ViewModel() {

    val currentUser: StateFlow<StudyUser?> = StudyRepository.currentUser

    fun signInAs(role: StudyRole) = StudyRepository.signInAs(role)

    fun signOut() = StudyRepository.signOut()
}
