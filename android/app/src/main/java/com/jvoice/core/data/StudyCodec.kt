package com.jvoice.core.data

import com.jvoice.core.i18n.LocalizedText
import com.jvoice.study.data.model.ContentStatus
import com.jvoice.study.data.model.Difficulty
import com.jvoice.study.data.model.Exam
import com.jvoice.study.data.model.ExamKeyDate
import com.jvoice.study.data.model.ExamSection
import com.jvoice.study.data.model.ExamTrack
import com.jvoice.study.data.model.ExamTrackGroup
import com.jvoice.study.data.model.ExamType
import com.jvoice.study.data.model.Question
import com.jvoice.study.data.model.QuestionSource
import com.jvoice.study.data.model.QuestionType
import com.jvoice.study.data.model.Quiz
import com.jvoice.study.data.model.StudyArticle
import com.jvoice.study.data.model.Subject
import com.jvoice.study.data.model.Topic

/**
 * Firestore ↔ model for the Study & Exams module.
 *
 * Same conventions as [NewsCodec]: enums by `name`, timestamps as epoch millis,
 * [LocalizedText] as a nested `{en, te}` map.
 *
 * ## The one thing to be careful about
 *
 * [Question.options] is a *positional* list of bilingual values, and
 * [Question.correctIndex] points into it. Storing options as a list of maps
 * preserves that order, which is what lets one index be correct in both
 * languages. If options were ever stored as a map keyed by text, the order — and
 * therefore every answer key — would be lost.
 */

/* ================================================================== subject */

fun Subject.toMap(): Map<String, Any?> = mapOf(
    "name" to name.toMap(),
    "emoji" to emoji,
    "isEnabled" to isEnabled
)

fun subjectFrom(id: String, data: Map<String, Any?>): Subject = Subject(
    id = id,
    name = localizedFrom(data["name"]),
    emoji = data.str("emoji", "📘"),
    isEnabled = data.bool("isEnabled", default = true)
)

/* ==================================================================== topic */

fun Topic.toMap(): Map<String, Any?> = mapOf(
    "subjectId" to subjectId,
    "name" to name.toMap(),
    "order" to order,
    "difficulty" to difficulty.name,
    "isEnabled" to isEnabled
)

fun topicFrom(id: String, data: Map<String, Any?>): Topic = Topic(
    id = id,
    subjectId = data.str("subjectId"),
    name = localizedFrom(data["name"]),
    order = data.int("order"),
    difficulty = data.enum("difficulty", Difficulty.MEDIUM),
    isEnabled = data.bool("isEnabled", default = true)
)

/* =============================================================== exam track */

fun ExamTrack.toMap(): Map<String, Any?> = mapOf(
    "name" to name.toMap(),
    "shortName" to shortName,
    "emoji" to emoji,
    "group" to group.name,
    "tagline" to tagline.toMap(),
    "qualification" to qualification.toMap(),
    "ageLimit" to ageLimit.toMap(),
    "vacancyLabel" to vacancyLabel.toMap(),
    "examDateLabel" to examDateLabel.toMap(),
    "totalQuestions" to totalQuestions,
    "totalMarks" to totalMarks,
    "durationMinutes" to durationMinutes,
    "negativeMarking" to negativeMarking.toMap(),
    "sections" to sections.map {
        mapOf("subjectId" to it.subjectId, "questions" to it.questions, "marks" to it.marks)
    },
    "stages" to stages.toMapList(),
    "keyDates" to keyDates.map {
        mapOf("label" to it.label.toMap(), "dateLabel" to it.dateLabel, "note" to it.note.toMap())
    },
    "isEnabled" to isEnabled
)

fun examTrackFrom(id: String, data: Map<String, Any?>): ExamTrack = ExamTrack(
    id = id,
    name = localizedFrom(data["name"]),
    shortName = data.str("shortName"),
    emoji = data.str("emoji", "🎯"),
    group = data.enum("group", ExamTrackGroup.GROUPS),
    tagline = localizedFrom(data["tagline"]),
    qualification = localizedFrom(data["qualification"]),
    ageLimit = localizedFrom(data["ageLimit"]),
    vacancyLabel = localizedFrom(data["vacancyLabel"]),
    examDateLabel = localizedFrom(data["examDateLabel"]),
    totalQuestions = data.int("totalQuestions"),
    totalMarks = data.int("totalMarks"),
    durationMinutes = data.int("durationMinutes"),
    negativeMarking = localizedFrom(data["negativeMarking"]),
    sections = (data["sections"] as? List<*>).orEmpty().mapNotNull { raw ->
        val row = raw as? Map<*, *> ?: return@mapNotNull null
        ExamSection(
            subjectId = row["subjectId"] as? String ?: return@mapNotNull null,
            questions = (row["questions"] as? Number)?.toInt() ?: 0,
            marks = (row["marks"] as? Number)?.toInt() ?: 0
        )
    },
    stages = localizedListFrom(data["stages"]),
    keyDates = (data["keyDates"] as? List<*>).orEmpty().mapNotNull { raw ->
        val row = raw as? Map<*, *> ?: return@mapNotNull null
        ExamKeyDate(
            label = localizedFrom(row["label"]),
            dateLabel = row["dateLabel"] as? String ?: "",
            note = localizedFrom(row["note"])
        )
    },
    isEnabled = data.bool("isEnabled", default = true)
)

/* ============================================================ study article */

fun StudyArticle.toMap(): Map<String, Any?> = mapOf(
    "subjectId" to subjectId,
    "topicId" to topicId,
    "title" to title.toMap(),
    "description" to description.toMap(),
    "content" to content.toMap(),
    "importantPoints" to importantPoints.toMapList(),
    "examples" to examples.toMapList(),
    "formulas" to formulas.toMapList(),
    "readingMinutes" to readingMinutes,
    "status" to status.name,
    "authorName" to authorName,
    "createdAt" to createdAt
)

fun studyArticleFrom(id: String, data: Map<String, Any?>): StudyArticle = StudyArticle(
    id = id,
    subjectId = data.str("subjectId"),
    topicId = data.str("topicId"),
    title = localizedFrom(data["title"]),
    description = localizedFrom(data["description"]),
    content = localizedFrom(data["content"]),
    importantPoints = localizedListFrom(data["importantPoints"]),
    examples = localizedListFrom(data["examples"]),
    formulas = localizedListFrom(data["formulas"]),
    readingMinutes = data.int("readingMinutes", 5),
    status = data.enum("status", ContentStatus.PUBLISHED),
    authorName = data.str("authorName", "J Voice Desk"),
    createdAt = data.long("createdAt")
)

/* ================================================================= question */

fun Question.toMap(): Map<String, Any?> = mapOf(
    "subjectId" to subjectId,
    "topicId" to topicId,
    "text" to text.toMap(),
    // Positional. See the file note: correctIndex points into this list, so the
    // order is the answer key.
    "options" to options.toMapList(),
    "correctIndex" to correctIndex,
    "explanation" to explanation.toMap(),
    "difficulty" to difficulty.name,
    "type" to type.name,
    "source" to source.name,
    "paperName" to paperName,
    "year" to year,
    "status" to status.name
)

fun questionFrom(id: String, data: Map<String, Any?>): Question = Question(
    id = id,
    subjectId = data.str("subjectId"),
    topicId = data.str("topicId"),
    text = localizedFrom(data["text"]),
    options = localizedListFrom(data["options"]),
    correctIndex = data.int("correctIndex"),
    explanation = localizedFrom(data["explanation"]),
    difficulty = data.enum("difficulty", Difficulty.MEDIUM),
    type = data.enum("type", QuestionType.MCQ),
    source = data.enum("source", QuestionSource.SAMPLE),
    paperName = data.str("paperName"),
    year = data.str("year"),
    status = data.enum("status", ContentStatus.PUBLISHED)
)

/* ===================================================================== quiz */

fun Quiz.toMap(): Map<String, Any?> = mapOf(
    "subjectId" to subjectId,
    "topicId" to topicId,
    "title" to title.toMap(),
    "questionIds" to questionIds,
    "durationMinutes" to durationMinutes,
    "status" to status.name,
    "authorName" to authorName
)

fun quizFrom(id: String, data: Map<String, Any?>): Quiz = Quiz(
    id = id,
    subjectId = data.str("subjectId"),
    topicId = data.str("topicId"),
    title = localizedFrom(data["title"]),
    questionIds = data.strList("questionIds"),
    durationMinutes = data.int("durationMinutes", 10),
    status = data.enum("status", ContentStatus.PUBLISHED),
    authorName = data.str("authorName", "J Voice Desk")
)

/* ===================================================================== exam */

fun Exam.toMap(): Map<String, Any?> = mapOf(
    "title" to title.toMap(),
    "type" to type.name,
    "dateLabel" to dateLabel,
    "durationMinutes" to durationMinutes,
    "questionIds" to questionIds,
    "subjectIds" to subjectIds,
    "difficulty" to difficulty.name,
    "instructions" to instructions.toMap(),
    "isActive" to isActive,
    "trackIds" to trackIds
)

fun examFrom(id: String, data: Map<String, Any?>): Exam = Exam(
    id = id,
    title = localizedFrom(data["title"]),
    type = data.enum("type", ExamType.DAILY),
    dateLabel = data.str("dateLabel"),
    durationMinutes = data.int("durationMinutes", 20),
    questionIds = data.strList("questionIds"),
    subjectIds = data.strList("subjectIds"),
    difficulty = data.enum("difficulty", Difficulty.MEDIUM),
    instructions = localizedFrom(data["instructions"]),
    isActive = data.bool("isActive", default = true),
    trackIds = data.strList("trackIds")
)
