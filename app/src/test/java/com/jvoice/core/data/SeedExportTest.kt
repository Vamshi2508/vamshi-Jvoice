package com.jvoice.core.data

import com.jvoice.study.data.mock.MockDataSource
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ONE-OFF migration step: writes the bundled study content to `jvoice-seed.json`
 * so `firebase/seed-content.mjs` can upload it to Firestore.
 *
 * ## Why a unit test rather than [DevExport]
 *
 * [DevExport] does the same job, but only on a device: it needs a `Context` for
 * `getExternalFilesDir`, so using it means an emulator plus an `adb pull`. The
 * data it serialises has no Android dependency at all — [MockDataSource] and the
 * `toMap()` codecs in `StudyCodec.kt` are plain Kotlin — so the export runs just
 * as well on the JVM, and running it here makes it reproducible on any machine
 * with a checkout and no device.
 *
 * The fidelity argument in [DevExport]'s docs is preserved, because the part that
 * matters is the *codecs*: every field name, enum spelling and Telugu string
 * comes from the same `toMap()` functions the app writes with. Only the
 * Map -> JSON step is local, and it is a copy of [DevExport.toJson]'s rules —
 * necessary because `org.json` is stubbed out in Android unit tests.
 *
 * Deleted together with [DevExport] and [MockDataSource] once the study
 * repositories read from Firestore.
 *
 *   ./gradlew :app:testDebugUnitTest --tests '*SeedExportTest'
 */
class SeedExportTest {

    @Test
    fun writeSeedJson() {
        val target = System.getProperty("seed.out")
            ?: error("seed.out system property not set - see app/build.gradle.kts testOptions")

        val collections = linkedMapOf(
            Firestore.SUBJECTS to MockDataSource.subjects.map { it.id to it.toMap() },
            Firestore.TOPICS to MockDataSource.topics.map { it.id to it.toMap() },
            Firestore.EXAM_TRACKS to MockDataSource.examTracks.map { it.id to it.toMap() },
            Firestore.STUDY_ARTICLES to MockDataSource.articles.map { it.id to it.toMap() },
            Firestore.QUESTIONS to MockDataSource.questions.map { it.id to it.toMap() },
            Firestore.QUIZZES to MockDataSource.quizzes.map { it.id to it.toMap() },
            Firestore.EXAMS to MockDataSource.exams.map { it.id to it.toMap() }
        )

        // Duplicate ids would silently collapse into one document on upload, and
        // the references between collections (topic -> subjectId, quiz ->
        // questionIds, exam -> both) would point at whichever survived.
        collections.forEach { (name, entries) ->
            val ids = entries.map { it.first }
            val duplicates = ids.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
            assertTrue("duplicate ids in $name: $duplicates", duplicates.isEmpty())
            assertTrue("$name is empty", ids.isNotEmpty())
        }

        val root = collections.mapValues { (_, entries) -> entries.toMap() }

        File(target).apply { parentFile?.mkdirs() }
            .writeText(render(root, 0) + "\n", Charsets.UTF_8)

        println("SEED EXPORT WRITTEN: $target")
        collections.forEach { (name, entries) -> println("  $name: ${entries.size}") }
    }

    /**
     * Recursive Map/List -> JSON text, matching [DevExport]'s conventions:
     * nulls are written as JSON `null` rather than omitted, so the uploader can
     * tell "absent" from "deliberately empty".
     */
    private fun render(value: Any?, depth: Int): String {
        val pad = "  ".repeat(depth + 1)
        val closePad = "  ".repeat(depth)
        return when (value) {
            null -> "null"
            is Map<*, *> ->
                if (value.isEmpty()) "{}"
                else value.entries.joinToString(",\n", "{\n", "\n$closePad}") { (k, v) ->
                    "$pad${quote(k.toString())}: ${render(v, depth + 1)}"
                }
            is List<*> ->
                if (value.isEmpty()) "[]"
                else value.joinToString(",\n", "[\n", "\n$closePad]") { "$pad${render(it, depth + 1)}" }
            is Boolean -> value.toString()
            is Number -> value.toString()
            is String -> quote(value)
            else -> quote(value.toString())
        }
    }

    /** Telugu text is left as UTF-8; only the characters JSON forbids are escaped. */
    private fun quote(s: String): String = buildString {
        append('"')
        s.forEach { c ->
            when {
                c == '"' -> append("\\\"")
                c == '\\' -> append("\\\\")
                c == '\n' -> append("\\n")
                c == '\r' -> append("\\r")
                c == '\t' -> append("\\t")
                c < ' ' -> append("\\u%04x".format(c.code))
                else -> append(c)
            }
        }
        append('"')
    }
}
