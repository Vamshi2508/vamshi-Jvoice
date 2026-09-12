package com.jvoice.study.ui.studyadmin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvoice.study.data.model.ContentStatus
import com.jvoice.study.data.model.Difficulty
import com.jvoice.study.data.model.ExamKeyDate
import com.jvoice.study.data.model.ExamSection
import com.jvoice.study.data.model.ExamTrack
import com.jvoice.study.data.model.ExamTrackGroup
import com.jvoice.study.data.model.StudyArticle
import com.jvoice.study.data.model.Subject
import com.jvoice.study.data.model.Topic
import com.jvoice.study.data.repository.ExamRepository
import com.jvoice.study.data.repository.ExamTrackRepository
import com.jvoice.study.data.repository.StudyRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.jvoice.core.i18n.LanguagePreference
import com.jvoice.core.i18n.LocalizedText

data class StudyAdminCounts(
    val subjects: Int = 0,
    val enabledSubjects: Int = 0,
    val topics: Int = 0,
    val articles: Int = 0,
    val published: Int = 0,
    val pending: Int = 0,
    val questions: Int = 0,
    val quizzes: Int = 0
)

/** Editor state for one exam type (Constable, Group-4, SSC…). */
data class TrackForm(
    val id: String? = null,
    val name: LocalizedText = LocalizedText.EMPTY,
    /** Short code - "Group-4", "SSC CGL". Not translated; it is how it is known. */
    val shortName: String = "",
    val emoji: String = "🎯",
    val group: ExamTrackGroup = ExamTrackGroup.POLICE,
    val tagline: LocalizedText = LocalizedText.EMPTY,
    val qualification: LocalizedText = LocalizedText.EMPTY,
    val ageLimit: LocalizedText = LocalizedText.EMPTY,
    val vacancyLabel: LocalizedText = LocalizedText.EMPTY,
    val examDateLabel: LocalizedText = LocalizedText.EMPTY,
    val durationText: String = "",
    val negativeMarking: LocalizedText = LocalizedText(
        "No negative marking",
        "నెగెటివ్ మార్కింగ్ లేదు"
    ),
    val stagesText: LocalizedText = LocalizedText.EMPTY,
    /** One date per line: "Label | 12 Sep 2026 | optional note". */
    val keyDatesText: LocalizedText = LocalizedText.EMPTY,
    val sections: List<ExamSection> = emptyList(),
    val isEnabled: Boolean = true
) {
    val totalQuestions: Int get() = sections.sumOf { it.questions }
    val totalMarks: Int get() = sections.sumOf { it.marks }

    /** The fields the language tabs report completeness for. */
    val localizedFields: List<LocalizedText>
        get() = listOf(name, tagline, qualification, ageLimit, vacancyLabel, examDateLabel)

    /**
     * Stage names, paired across languages by position - the third Telugu stage
     * is the translation of the third English one.
     */
    val stages: List<LocalizedText>
        get() {
            fun split(value: String) =
                value.split(",").map { it.trim() }.filter { it.isNotBlank() }
            val en = split(stagesText.en)
            val te = split(stagesText.te)
            return (0 until maxOf(en.size, te.size)).map { index ->
                LocalizedText(en.getOrElse(index) { "" }, te.getOrElse(index) { "" })
            }
        }

    /**
     * Key dates, one per line as `Label | date | optional note`.
     *
     * The date column is taken from the English box only - a date is digits and a
     * month code, and having two editable copies of it just invites them to
     * disagree. Only the label and note are translated.
     */
    val keyDates: List<ExamKeyDate>
        get() {
            fun rows(value: String) = value.lines()
                .map { line -> line.split("|").map { it.trim() } }
                .filter { it.getOrNull(0).orEmpty().isNotBlank() }
            val en = rows(keyDatesText.en)
            val te = rows(keyDatesText.te)
            return (0 until maxOf(en.size, te.size)).mapNotNull { index ->
                val enRow = en.getOrNull(index).orEmpty()
                val teRow = te.getOrNull(index).orEmpty()
                val label = LocalizedText(
                    en = enRow.getOrNull(0).orEmpty(),
                    te = teRow.getOrNull(0).orEmpty()
                )
                val date = enRow.getOrNull(1).orEmpty().ifBlank { teRow.getOrNull(1).orEmpty() }
                if (label.isBlank || date.isBlank()) null
                else ExamKeyDate(
                    label = label,
                    dateLabel = date,
                    note = LocalizedText(
                        en = enRow.getOrNull(2).orEmpty(),
                        te = teRow.getOrNull(2).orEmpty()
                    )
                )
            }
        }

    /** One language is enough to save; the other can follow. */
    val nameError: String? get() = if (name.isBlank) "Exam name is required" else null
    val durationError: String?
        get() = if ((durationText.toIntOrNull() ?: 0) <= 0) "Duration must be a positive number" else null
    val sectionError: String?
        get() = when {
            sections.isEmpty() -> "Add at least one subject to the pattern"
            totalQuestions <= 0 -> "The pattern needs at least one question"
            else -> null
        }
    val isValid: Boolean get() = nameError == null && durationError == null && sectionError == null
}

class StudyAdminViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _topicSubjectFilter = MutableStateFlow<String?>(null)
    val topicSubjectFilter: StateFlow<String?> = _topicSubjectFilter.asStateFlow()

    private val _contentStatusFilter = MutableStateFlow<ContentStatus?>(null)
    val contentStatusFilter: StateFlow<ContentStatus?> = _contentStatusFilter.asStateFlow()

    val subjects: StateFlow<List<Subject>> = StudyRepository.subjects

    val counts: StateFlow<StudyAdminCounts> = combine(
        StudyRepository.subjects,
        StudyRepository.topics,
        StudyRepository.articles,
        StudyRepository.questions,
        StudyRepository.quizzes
    ) { subjects, topics, articles, questions, quizzes ->
        StudyAdminCounts(
            subjects = subjects.size,
            enabledSubjects = subjects.count { it.isEnabled },
            topics = topics.size,
            articles = articles.size,
            published = articles.count { it.status == ContentStatus.PUBLISHED },
            pending = articles.count { it.status == ContentStatus.PENDING_REVIEW },
            questions = questions.size,
            quizzes = quizzes.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StudyAdminCounts())

    val topics: StateFlow<List<Topic>> = combine(
        StudyRepository.topics,
        _topicSubjectFilter
    ) { topics, filter ->
        topics
            .filter { filter == null || it.subjectId == filter }
            .sortedWith(compareBy({ it.subjectId }, { it.order }))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val articles: StateFlow<List<StudyArticle>> = combine(
        StudyRepository.articles,
        _contentStatusFilter
    ) { _, status -> StudyRepository.articlesByStatus(status) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            delay(450)
            _isLoading.value = false
        }
    }

    fun setTopicSubjectFilter(id: String?) { _topicSubjectFilter.value = id }
    fun setContentStatusFilter(status: ContentStatus?) { _contentStatusFilter.value = status }

    fun subjectName(id: String) = StudyRepository.subjectName(id)
    fun topicName(id: String) = StudyRepository.topicName(id)
    fun topicCountFor(subjectId: String) = StudyRepository.topicsOf(subjectId).size
    fun questionCountFor(subjectId: String) = StudyRepository.questionCountForSubject(subjectId)

    // ---------------------------------------------------------------- subjects
    fun addSubject(name: LocalizedText, emoji: String) =
        StudyRepository.addSubject(name, emoji)

    fun updateSubject(id: String, name: LocalizedText, emoji: String) =
        StudyRepository.updateSubject(id, name, emoji)

    fun toggleSubject(id: String) = StudyRepository.toggleSubjectEnabled(id)

    /** Returns false when the subject still owns topics. */
    fun deleteSubject(id: String) = StudyRepository.deleteSubject(id)

    // ------------------------------------------------------------------ topics
    fun addTopic(subjectId: String, name: LocalizedText, difficulty: Difficulty) =
        StudyRepository.addTopic(subjectId, name, difficulty)

    fun updateTopic(id: String, subjectId: String, name: LocalizedText, difficulty: Difficulty) =
        StudyRepository.updateTopic(id, subjectId, name, difficulty)

    fun toggleTopic(id: String) = StudyRepository.toggleTopicEnabled(id)

    /** Returns false when articles or questions still reference the topic. */
    fun deleteTopic(id: String) = StudyRepository.deleteTopic(id)

    // ------------------------------------------------------------- exam types
    val tracks: StateFlow<List<ExamTrack>> = ExamTrackRepository.tracks

    private val _trackForm = MutableStateFlow(TrackForm())
    val trackForm: StateFlow<TrackForm> = _trackForm.asStateFlow()

    fun newTrack() {
        _trackForm.value = TrackForm()
    }

    fun loadTrack(trackId: String) {
        val track = ExamTrackRepository.trackById(trackId) ?: return newTrack()
        _trackForm.value = TrackForm(
            id = track.id,
            name = track.name,
            shortName = track.shortName,
            emoji = track.emoji,
            group = track.group,
            tagline = track.tagline,
            qualification = track.qualification,
            ageLimit = track.ageLimit,
            vacancyLabel = track.vacancyLabel,
            examDateLabel = track.examDateLabel,
            durationText = track.durationMinutes.toString(),
            negativeMarking = track.negativeMarking,
            // Flattened per language so each box shows only its own side.
            stagesText = LocalizedText(
                en = track.stages.joinToString(", ") { it.en },
                te = track.stages.joinToString(", ") { it.te }
            ),
            keyDatesText = LocalizedText(
                en = track.keyDates.joinToString("\n") { d ->
                    d.label.en + " | " + d.dateLabel + (if (d.note.en.isBlank()) "" else " | " + d.note.en)
                },
                te = track.keyDates.joinToString("\n") { d ->
                    d.label.te + " | " + d.dateLabel + (if (d.note.te.isBlank()) "" else " | " + d.note.te)
                }
            ),
            sections = track.sections,
            isEnabled = track.isEnabled
        )
    }

    fun updateTrackForm(transform: (TrackForm) -> TrackForm) {
        _trackForm.value = transform(_trackForm.value)
    }

    /** Adds the subject to the pattern, or drops it when already there. */
    fun toggleSection(subjectId: String) {
        _trackForm.value = _trackForm.value.let { form ->
            if (form.sections.any { it.subjectId == subjectId }) {
                form.copy(sections = form.sections.filterNot { it.subjectId == subjectId })
            } else {
                form.copy(sections = form.sections + ExamSection(subjectId, 10, 10))
            }
        }
    }

    fun setSectionQuestions(subjectId: String, questions: Int) {
        _trackForm.value = _trackForm.value.let { form ->
            form.copy(
                sections = form.sections.map {
                    if (it.subjectId == subjectId) it.copy(questions = questions) else it
                }
            )
        }
    }

    fun setSectionMarks(subjectId: String, marks: Int) {
        _trackForm.value = _trackForm.value.let { form ->
            form.copy(
                sections = form.sections.map {
                    if (it.subjectId == subjectId) it.copy(marks = marks) else it
                }
            )
        }
    }

    fun sectionFor(subjectId: String): ExamSection? =
        _trackForm.value.sections.firstOrNull { it.subjectId == subjectId }

    fun saveTrack(): Boolean {
        val form = _trackForm.value
        if (!form.isValid) return false
        val id = ExamTrackRepository.saveTrack(
            existingId = form.id,
            name = form.name.trimmed(),
            shortName = form.shortName.trim(),
            emoji = form.emoji.trim(),
            group = form.group,
            tagline = form.tagline.trimmed(),
            qualification = form.qualification.trimmed(),
            ageLimit = form.ageLimit.trimmed(),
            vacancyLabel = form.vacancyLabel.trimmed(),
            examDateLabel = form.examDateLabel.trimmed(),
            durationMinutes = form.durationText.toIntOrNull() ?: 0,
            negativeMarking = form.negativeMarking.trimmed(),
            sections = form.sections,
            stages = form.stages,
            keyDates = form.keyDates,
            isEnabled = form.isEnabled
        )
        _trackForm.value = form.copy(id = id)
        return true
    }

    fun toggleTrack(id: String) = ExamTrackRepository.toggleEnabled(id)

    /** Returns false when papers are still tagged with this exam type. */
    fun deleteTrack(id: String) = ExamTrackRepository.deleteTrack(id)

    fun trackPaperCount(id: String) = ExamRepository.trackExamsFor(id).size

    fun trackSummary(track: ExamTrack) = ExamTrackRepository.summary(track)

    // ----------------------------------------------------------------- content
    fun publishArticle(id: String) = StudyRepository.setArticleStatus(id, ContentStatus.PUBLISHED)
    fun sendBackArticle(id: String) = StudyRepository.setArticleStatus(id, ContentStatus.DRAFT)
    fun deleteArticle(id: String) = StudyRepository.deleteArticle(id)
}
