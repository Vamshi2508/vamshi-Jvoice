package com.jvoice.study.ui.examadmin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvoice.study.data.model.ContentStatus
import com.jvoice.study.data.model.Difficulty
import com.jvoice.study.data.model.Exam
import com.jvoice.study.data.model.ExamResult
import com.jvoice.study.data.model.ExamTrack
import com.jvoice.study.data.model.ExamType
import com.jvoice.study.data.model.Question
import com.jvoice.study.data.model.QuestionType
import com.jvoice.study.data.repository.ExamRepository
import com.jvoice.study.data.repository.ExamTrackRepository
import com.jvoice.study.data.repository.PerformanceRepository
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
import com.jvoice.core.i18n.LanguagePreference
import com.jvoice.core.i18n.AppLanguage
import com.jvoice.core.i18n.LocalizedText
import com.jvoice.core.i18n.lt

data class ExamAdminCounts(
    val dailyExams: Int = 0,
    val activeDaily: Int = 0,
    val grandTests: Int = 0,
    val activeGrand: Int = 0,
    val questions: Int = 0,
    val results: Int = 0
)

/** Form for creating or editing a Daily Exam / Grand Test. */
data class ExamForm(
    val id: String? = null,
    val title: LocalizedText = LocalizedText.EMPTY,
    val type: ExamType = ExamType.DAILY,
    val dateLabel: String = "Today",
    val durationText: String = "20",
    val questionCountText: String = "20",
    val subjectIds: List<String> = emptyList(),
    /** Exam types this paper is for. Empty means every exam type sees it. */
    val trackIds: List<String> = emptyList(),
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val instructions: LocalizedText = LocalizedText.EMPTY,
    val isActive: Boolean = true
) {
    /** The fields the language tabs report completeness for. */
    val localizedFields: List<LocalizedText> get() = listOf(title, instructions)

    val titleError: String? get() = if (title.isBlank) "Title is required" else null
    val subjectError: String? get() = if (subjectIds.isEmpty()) "Select at least one subject" else null
    val countError: String?
        get() = (questionCountText.toIntOrNull() ?: 0).let {
            if (it < 1) "Enter the number of questions" else null
        }
    val durationError: String?
        get() = (durationText.toIntOrNull() ?: 0).let {
            if (it < 1) "Enter the duration in minutes" else null
        }
    val isValid: Boolean
        get() = titleError == null && subjectError == null && countError == null && durationError == null
}

class ExamAdminViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _form = MutableStateFlow(ExamForm())
    val form: StateFlow<ExamForm> = _form.asStateFlow()

    private val _bankSubject = MutableStateFlow<String?>(null)
    val bankSubject: StateFlow<String?> = _bankSubject.asStateFlow()

    private val _bankDifficulty = MutableStateFlow<Difficulty?>(null)
    val bankDifficulty: StateFlow<Difficulty?> = _bankDifficulty.asStateFlow()

    private val _bankQuery = MutableStateFlow("")
    val bankQuery: StateFlow<String> = _bankQuery.asStateFlow()

    val subjects = StudyRepository.subjects
    val exams: StateFlow<List<Exam>> = ExamRepository.exams
    val tracks: StateFlow<List<ExamTrack>> = ExamTrackRepository.tracks

    val counts: StateFlow<ExamAdminCounts> = combine(
        ExamRepository.exams,
        StudyRepository.questions,
        PerformanceRepository.results
    ) { exams, questions, results ->
        ExamAdminCounts(
            dailyExams = exams.count { it.type == ExamType.DAILY },
            activeDaily = exams.count { it.type == ExamType.DAILY && it.isActive },
            grandTests = exams.count { it.type == ExamType.GRAND_TEST },
            activeGrand = exams.count { it.type == ExamType.GRAND_TEST && it.isActive },
            questions = questions.size,
            results = results.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExamAdminCounts())

    val bankQuestions: StateFlow<List<Question>> = combine(
        StudyRepository.questions,
        _bankSubject,
        _bankDifficulty,
        _bankQuery
    ) { _, subject, difficulty, query ->
        StudyRepository.filterQuestions(
            subjectId = subject,
            difficulty = difficulty,
            query = query
        ).take(80)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val results: StateFlow<List<ExamResult>> = PerformanceRepository.results
        .map { it.sortedByDescending { r -> r.takenAt } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            delay(450)
            _isLoading.value = false
        }
    }

    fun setBankSubject(id: String?) { _bankSubject.value = id }
    fun setBankDifficulty(value: Difficulty?) { _bankDifficulty.value = value }
    fun setBankQuery(value: String) { _bankQuery.value = value }

    fun subjectName(id: String) = StudyRepository.subjectName(id)
    fun topicName(id: String) = StudyRepository.topicName(id)
    fun dailyExams() = ExamRepository.dailyExams()
    fun grandTests() = ExamRepository.grandTests()

    // ------------------------------------------------------------------- form
    fun newExam(type: ExamType) {
        val isGrand = type == ExamType.GRAND_TEST
        _form.value = ExamForm(
            title = if (isGrand)
                lt("Weekly Grand Test", "వారపు గ్రాండ్ టెస్ట్")
            else lt("Daily Exam", "రోజువారీ పరీక్ష"),
            type = type,
            dateLabel = if (isGrand) "This Sunday" else "Today",
            durationText = if (isGrand) "90" else "20",
            questionCountText = if (isGrand) "100" else "20",
            subjectIds = if (isGrand) StudyRepository.enabledSubjects().map { it.id }
            else StudyRepository.enabledSubjects().take(5).map { it.id },
            difficulty = if (isGrand) Difficulty.HARD else Difficulty.MEDIUM,
            instructions = if (isGrand) lt(
                "100 questions in 90 minutes covering all subjects.",
                "90 నిమిషాల్లో అన్ని సబ్జెక్టులను కవర్ చేసే 100 ప్రశ్నలు."
            ) else lt(
                "20 questions, 20 minutes. No negative marking.",
                "20 ప్రశ్నలు, 20 నిమిషాలు. నెగెటివ్ మార్కింగ్ లేదు."
            ),
            isActive = true
        )
    }

    fun loadExam(examId: String) {
        val exam = ExamRepository.examById(examId) ?: return newExam(ExamType.DAILY)
        _form.value = ExamForm(
            id = exam.id,
            // Loaded as the pair so editing one language keeps the other.
            title = exam.title,
            type = exam.type,
            dateLabel = exam.dateLabel,
            durationText = exam.durationMinutes.toString(),
            questionCountText = exam.questionCount.toString(),
            subjectIds = exam.subjectIds,
            trackIds = exam.trackIds,
            difficulty = exam.difficulty,
            instructions = exam.instructions,
            isActive = exam.isActive
        )
    }

    fun updateForm(transform: (ExamForm) -> ExamForm) {
        _form.value = transform(_form.value)
    }

    fun toggleTrack(trackId: String) {
        _form.value = _form.value.let { form ->
            form.copy(
                trackIds = if (form.trackIds.contains(trackId)) {
                    form.trackIds - trackId
                } else {
                    form.trackIds + trackId
                }
            )
        }
    }

    /** Fills the paper from an exam type's own pattern in one tap. */
    fun applyTrackPattern(trackId: String) {
        val track = ExamTrackRepository.trackById(trackId) ?: return
        _form.value = _form.value.copy(
            trackIds = listOf(trackId),
            title = LocalizedText(
                en = track.shortName + " " +
                    (if (_form.value.type == ExamType.GRAND_TEST) "Grand Test" else "Daily Exam"),
                te = track.shortName + " " +
                    (if (_form.value.type == ExamType.GRAND_TEST) "గ్రాండ్ టెస్ట్" else "రోజువారీ పరీక్ష")
            ),
            subjectIds = track.subjectIds,
            durationText = track.durationMinutes.toString(),
            questionCountText = track.totalQuestions.toString(),
            instructions = LocalizedText(
                en = track.patternLabel(AppLanguage.ENGLISH) + "  •  " + track.negativeMarking.en,
                te = track.patternLabel(AppLanguage.TELUGU) + "  •  " +
                    track.negativeMarking.get(AppLanguage.TELUGU)
            )
        )
    }

    fun trackName(id: String) = ExamTrackRepository.trackById(id)?.shortName ?: "Exam"

    fun toggleSubject(subjectId: String) {
        _form.value = _form.value.let { form ->
            form.copy(
                subjectIds = if (form.subjectIds.contains(subjectId)) {
                    form.subjectIds - subjectId
                } else {
                    form.subjectIds + subjectId
                }
            )
        }
    }

    fun save(): Boolean {
        val form = _form.value
        if (!form.isValid) return false
        ExamRepository.saveExam(
            existingId = form.id,
            title = form.title.trimmed(),
            type = form.type,
            dateLabel = form.dateLabel.trim().ifBlank { "Today" },
            durationMinutes = form.durationText.toIntOrNull() ?: 20,
            questionCount = form.questionCountText.toIntOrNull() ?: 20,
            subjectIds = form.subjectIds,
            difficulty = form.difficulty,
            instructions = form.instructions.trimmed(),
            isActive = form.isActive,
            trackIds = form.trackIds
        )
        return true
    }

    fun setActive(examId: String, active: Boolean) = ExamRepository.setExamActive(examId, active)
    fun deleteExam(examId: String) = ExamRepository.deleteExam(examId)

    val questionTypes = QuestionType.entries
    val statuses = ContentStatus.entries
}
