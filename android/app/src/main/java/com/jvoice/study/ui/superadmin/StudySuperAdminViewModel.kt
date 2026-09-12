package com.jvoice.study.ui.superadmin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvoice.study.data.model.ExamType
import com.jvoice.study.data.model.StudyRole
import com.jvoice.study.data.model.StudyUser
import com.jvoice.study.data.repository.ExamRepository
import com.jvoice.study.data.repository.StudyRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SuperAdminCounts(
    val students: Int = 0,
    val creators: Int = 0,
    val examAdmins: Int = 0,
    val studyAdmins: Int = 0,
    val questions: Int = 0,
    val articles: Int = 0,
    val quizzes: Int = 0,
    val dailyExams: Int = 0,
    val grandTests: Int = 0,
    val subjects: Int = 0
)

class StudySuperAdminViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _roleFilter = MutableStateFlow<StudyRole?>(null)
    val roleFilter: StateFlow<StudyRole?> = _roleFilter.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val rolePermissions = StudyRepository.rolePermissions

    val counts: StateFlow<SuperAdminCounts> = combine(
        StudyRepository.users,
        StudyRepository.questions,
        StudyRepository.articles,
        StudyRepository.quizzes,
        ExamRepository.exams
    ) { users, questions, articles, quizzes, exams ->
        SuperAdminCounts(
            students = users.count { it.role == StudyRole.STUDENT },
            creators = users.count { it.role == StudyRole.CONTENT_CREATOR },
            examAdmins = users.count { it.role == StudyRole.EXAM_ADMIN },
            studyAdmins = users.count { it.role == StudyRole.STUDY_ADMIN },
            questions = questions.size,
            articles = articles.size,
            quizzes = quizzes.size,
            dailyExams = exams.count { it.type == ExamType.DAILY },
            grandTests = exams.count { it.type == ExamType.GRAND_TEST },
            subjects = StudyRepository.subjects.value.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SuperAdminCounts())

    val users: StateFlow<List<StudyUser>> = combine(
        StudyRepository.users,
        _roleFilter,
        _query
    ) { users, role, query ->
        val q = query.trim().lowercase()
        users
            .filter { role == null || it.role == role }
            .filter { q.isBlank() || it.name.lowercase().contains(q) || it.email.lowercase().contains(q) }
            .sortedBy { it.role.ordinal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            delay(450)
            _isLoading.value = false
        }
    }

    fun setRoleFilter(role: StudyRole?) { _roleFilter.value = role }
    fun setQuery(value: String) { _query.value = value }

    fun setActive(userId: String, active: Boolean) = StudyRepository.setUserActive(userId, active)
    fun changeRole(userId: String, role: StudyRole) = StudyRepository.changeUserRole(userId, role)
    fun updateUser(userId: String, name: String, email: String) =
        StudyRepository.updateUser(userId, name, email)

    fun addUser(name: String, email: String, role: StudyRole) =
        StudyRepository.addUser(name, email, role)

    fun countFor(role: StudyRole) = StudyRepository.countUsers(role)
}
