package com.jvoice.study.data.repository

import com.jvoice.study.data.mock.MockDataSource
import com.jvoice.study.data.model.ExamKeyDate
import com.jvoice.study.data.model.ExamSection
import com.jvoice.study.data.model.ExamSectionRow
import com.jvoice.study.data.model.ExamTrack
import com.jvoice.study.data.model.ExamTrackGroup
import com.jvoice.study.data.model.ExamTrackSummary
import com.jvoice.study.data.model.StudyNotificationType
import com.jvoice.study.data.model.StudyRole
import com.jvoice.study.data.model.Subject
import com.jvoice.study.data.model.Topic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.jvoice.core.i18n.AppLanguage
import com.jvoice.core.i18n.LocalizedText

/**
 * The exam a student is preparing for - Police Constable, Group-4, SSC and so on.
 *
 * Nothing in the Study module is shown until a track is chosen: the picker is the
 * first screen of the Study tab. Once chosen, this repository is the scope filter
 * every other repository consults, so subjects, topics, quizzes, tests, analysis
 * and ranks all belong to that one exam.
 *
 * In-memory for the prototype; a real build would persist the choice per user.
 */
object ExamTrackRepository {

    private val _tracks = MutableStateFlow(MockDataSource.examTracks)
    val tracks: StateFlow<List<ExamTrack>> = _tracks.asStateFlow()

    private val _selectedId = MutableStateFlow<String?>(null)
    val selectedId: StateFlow<String?> = _selectedId.asStateFlow()

    // ------------------------------------------------------------------ session
    fun select(trackId: String) {
        _selectedId.value = trackId
    }

    fun clearSelection() {
        _selectedId.value = null
    }

    fun hasSelection(): Boolean = _selectedId.value != null

    fun selected(): ExamTrack? = _selectedId.value?.let { trackById(it) }

    fun selectedName(): String = selected()?.shortName ?: "All exams"

    // ------------------------------------------------------------------ lookups
    fun trackById(id: String): ExamTrack? = _tracks.value.firstOrNull { it.id == id }

    fun enabledTracks(): List<ExamTrack> = _tracks.value.filter { it.isEnabled }

    fun search(query: String): List<ExamTrack> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return enabledTracks()
        return enabledTracks().filter { track ->
            // ExamTrack.matches covers both languages plus the short code.
            track.matches(query) || track.group.label.lowercase().contains(q)
        }
    }

    fun grouped(query: String = ""): List<Pair<ExamTrackGroup, List<ExamTrack>>> {
        val matches = search(query)
        return ExamTrackGroup.values().mapNotNull { group ->
            val rows = matches.filter { it.group == group }
            if (rows.isEmpty()) null else group to rows
        }
    }

    // -------------------------------------------------------------------- scope
    /** Subject ids in scope, or null when no track has been chosen yet. */
    fun scopeSubjectIds(): Set<String>? = selected()?.subjectIds?.toSet()

    fun isInScope(subjectId: String): Boolean {
        val scope = scopeSubjectIds() ?: return true
        return scope.contains(subjectId)
    }

    /** Subjects of the selected track, in the exam's own section order. */
    fun subjectsOf(track: ExamTrack?): List<Subject> {
        if (track == null) return StudyRepository.enabledSubjects()
        return track.subjectIds.mapNotNull { StudyRepository.subjectById(it) }.filter { it.isEnabled }
    }

    fun topicsOf(track: ExamTrack?): List<Topic> =
        subjectsOf(track).flatMap { StudyRepository.topicsOf(it.id) }

    // ---------------------------------------------------------------- pattern
    /** The exam pattern joined to live syllabus and performance data. */
    fun sectionRows(track: ExamTrack): List<ExamSectionRow> =
        track.sections.mapNotNull { section ->
            val subject = StudyRepository.subjectById(section.subjectId) ?: return@mapNotNull null
            val topics = StudyRepository.topicsOf(subject.id)
            ExamSectionRow(
                section = section,
                subject = subject,
                topicCount = topics.size,
                completedTopics = topics.count { StudyRepository.isTopicCompleted(it.id) },
                questionCount = StudyRepository.questionCountForSubject(subject.id),
                accuracy = PerformanceRepository.subjectPerformanceById(subject.id)
                    ?.takeIf { it.attempted }?.accuracy
            )
        }

    // ---------------------------------------------------------------- progress
    fun syllabusProgressPercent(track: ExamTrack?): Int {
        val topics = topicsOf(track)
        if (topics.isEmpty()) return 0
        val done = topics.count { StudyRepository.isTopicCompleted(it.id) }
        return done * 100 / topics.size
    }

    // ------------------------------------------------- admin: manage exam types
    private var idCounter = 700
    private fun nextId(): String {
        idCounter += 1
        return "track_" + idCounter
    }

    /**
     * Study Admin / Super Admin own this list. Students only ever see what is
     * configured here, so adding an exam type in the admin drawer makes it
     * appear in the student's exam picker straight away.
     */
    fun saveTrack(
        existingId: String?,
        name: LocalizedText,
        shortName: String,
        emoji: String,
        group: ExamTrackGroup,
        tagline: LocalizedText,
        qualification: LocalizedText,
        ageLimit: LocalizedText,
        vacancyLabel: LocalizedText,
        examDateLabel: LocalizedText,
        durationMinutes: Int,
        negativeMarking: LocalizedText,
        sections: List<ExamSection>,
        stages: List<LocalizedText>,
        keyDates: List<ExamKeyDate>,
        isEnabled: Boolean
    ): String {
        val id = existingId ?: nextId()
        val totalQuestions = sections.sumOf { it.questions }
        val totalMarks = sections.sumOf { it.marks }
        val existing = existingId?.let { trackById(it) }
        if (existing == null) {
            val track = ExamTrack(
                id = id,
                name = name,
                // The short code falls back to the English name - it is a code,
                // and codes are quoted in English on every notification.
                shortName = shortName.ifBlank { name.get(AppLanguage.ENGLISH).take(12) },
                emoji = emoji.ifBlank { "🎯" },
                group = group,
                tagline = tagline,
                qualification = qualification,
                ageLimit = ageLimit,
                vacancyLabel = vacancyLabel,
                examDateLabel = examDateLabel,
                totalQuestions = totalQuestions,
                totalMarks = totalMarks,
                durationMinutes = durationMinutes,
                negativeMarking = negativeMarking,
                sections = sections,
                stages = stages,
                keyDates = keyDates,
                isEnabled = isEnabled
            )
            _tracks.update { it + track }
            if (isEnabled) {
                StudyRepository.pushNotification(
                    title = LocalizedText(
                        en = "New exam added: " + name.en,
                        te = "కొత్త పరీక్ష జోడించబడింది: " + name.te.ifBlank { name.en }
                    ),
                    message = LocalizedText(
                        en = totalQuestions.toString() + " questions • " + sections.size +
                            " subjects • " + durationMinutes + " minutes.",
                        te = totalQuestions.toString() + " ప్రశ్నలు • " + sections.size +
                            " సబ్జెక్టులు • " + durationMinutes + " నిమిషాలు."
                    ),
                    type = StudyNotificationType.SYSTEM,
                    targetRole = StudyRole.STUDENT
                )
            }
        } else {
            _tracks.update { list ->
                list.map {
                    if (it.id == id) {
                        it.copy(
                            name = name,
                            shortName = shortName.ifBlank { it.shortName },
                            emoji = emoji.ifBlank { it.emoji },
                            group = group,
                            tagline = tagline,
                            qualification = qualification,
                            ageLimit = ageLimit,
                            vacancyLabel = vacancyLabel,
                            examDateLabel = examDateLabel,
                            totalQuestions = totalQuestions,
                            totalMarks = totalMarks,
                            durationMinutes = durationMinutes,
                            negativeMarking = negativeMarking,
                            sections = sections,
                            stages = stages,
                            keyDates = keyDates,
                            isEnabled = isEnabled
                        )
                    } else it
                }
            }
        }
        return id
    }

    fun toggleEnabled(id: String) {
        _tracks.update { list -> list.map { if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it } }
        // A student sitting on a disabled exam is sent back to the picker.
        if (_selectedId.value == id && trackById(id)?.isEnabled == false) clearSelection()
    }

    /** Exam types that still own papers cannot be deleted. */
    fun deleteTrack(id: String): Boolean {
        if (ExamRepository.trackExamsFor(id).isNotEmpty()) return false
        _tracks.update { list -> list.filterNot { it.id == id } }
        if (_selectedId.value == id) clearSelection()
        return true
    }

    /** Replaces one section's figures; used by the pattern editor. */
    fun setSections(id: String, sections: List<ExamSection>) {
        _tracks.update { list ->
            list.map {
                if (it.id == id) {
                    it.copy(
                        sections = sections,
                        totalQuestions = sections.sumOf { s -> s.questions },
                        totalMarks = sections.sumOf { s -> s.marks }
                    )
                } else it
            }
        }
    }

    // ----------------------------------------------------------------- counts
    fun summary(track: ExamTrack): ExamTrackSummary {
        val subjects = subjectsOf(track)
        val topics = subjects.flatMap { StudyRepository.topicsOf(it.id) }
        return ExamTrackSummary(
            track = track,
            subjects = subjects.size,
            topics = topics.size,
            articles = subjects.sumOf { StudyRepository.articleCountForSubject(it.id) },
            questions = subjects.sumOf { StudyRepository.questionCountForSubject(it.id) },
            tests = ExamRepository.examsForTrack(track.id).size
        )
    }
}
