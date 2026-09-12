package com.jvoice.core.data

import android.content.Context
import android.util.Log
import com.jvoice.news.BuildConfig
import com.jvoice.study.data.mock.MockDataSource
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * ONE-OFF migration helper: serialises the bundled study content to JSON so it
 * can be uploaded to Firestore, then deleted.
 *
 * ## Why this exists at all
 *
 * The study content — subjects, topics, exam tracks, ~50 articles and ~150
 * questions — is written as typed Kotlin, complete with its bilingual
 * [com.jvoice.core.i18n.LocalizedText] pairs. Retyping it into a JavaScript seed
 * script would mean hand-copying every Telugu string, which is exactly how
 * translations get corrupted.
 *
 * Instead it is serialised here, by the same codecs the app uses at runtime, so
 * what lands in Firestore is byte-identical to what the app would have written.
 * The JSON is then uploaded by `firebase/seed-content.mjs` using the Admin SDK,
 * which bypasses the security rules — necessary because the rules require a
 * signed-in desk role and there is no account to sign in as yet.
 *
 * ## This file is temporary
 *
 * It is the only remaining reference to `MockDataSource`. Once the upload is
 * verified, this file and the mock data are both deleted. Guarded by
 * [BuildConfig.DEBUG] so it can never run in a release build.
 */
object DevExport {

    private const val TAG = "DevExport"
    private const val FILE_NAME = "jvoice-seed.json"

    /**
     * Writes the export to the app's external files directory, where `adb pull`
     * can reach it without root.
     *
     * Skips if the file already exists, so a relaunch does not rewrite it.
     * Returns the path, or null if nothing was written.
     */
    fun exportIfNeeded(context: Context): String? {
        if (!BuildConfig.DEBUG) return null
        return try {
            val dir = context.getExternalFilesDir(null) ?: return null
            val file = File(dir, FILE_NAME)
            if (file.exists()) {
                Log.i(TAG, "Export already present at ${file.absolutePath}")
                return file.absolutePath
            }
            file.writeText(build().toString(2))
            Log.i(TAG, "SEED EXPORT WRITTEN: ${file.absolutePath} (${file.length()} bytes)")
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Export failed: ${e.message}", e)
            null
        }
    }

    /**
     * Builds `{ collectionName: { docId: {...} } }`.
     *
     * Keyed by document id rather than an array, because the ids are meaningful:
     * topics reference `subjectId`, quizzes reference `questionIds`, exams
     * reference both. Letting Firestore generate new ids would break every one of
     * those references, so the existing ids are preserved deliberately.
     */
    private fun build(): JSONObject {
        val root = JSONObject()

        root.put(Firestore.SUBJECTS, collection(MockDataSource.subjects.map { it.id to it.toMap() }))
        root.put(Firestore.TOPICS, collection(MockDataSource.topics.map { it.id to it.toMap() }))
        root.put(Firestore.EXAM_TRACKS, collection(MockDataSource.examTracks.map { it.id to it.toMap() }))
        root.put(Firestore.STUDY_ARTICLES, collection(MockDataSource.articles.map { it.id to it.toMap() }))
        root.put(Firestore.QUESTIONS, collection(MockDataSource.questions.map { it.id to it.toMap() }))
        root.put(Firestore.QUIZZES, collection(MockDataSource.quizzes.map { it.id to it.toMap() }))
        root.put(Firestore.EXAMS, collection(MockDataSource.exams.map { it.id to it.toMap() }))

        Log.i(
            TAG,
            "Export contents: ${MockDataSource.subjects.size} subjects, " +
                "${MockDataSource.topics.size} topics, " +
                "${MockDataSource.examTracks.size} exam tracks, " +
                "${MockDataSource.articles.size} articles, " +
                "${MockDataSource.questions.size} questions, " +
                "${MockDataSource.quizzes.size} quizzes, " +
                "${MockDataSource.exams.size} exams"
        )
        return root
    }

    private fun collection(entries: List<Pair<String, Map<String, Any?>>>): JSONObject {
        val out = JSONObject()
        entries.forEach { (id, data) -> out.put(id, toJson(data)) }
        return out
    }

    /**
     * Recursive Map/List -> JSON.
     *
     * Nulls are written as JSON null rather than omitted, so the uploader can
     * tell "absent" from "deliberately empty" — an article with no publish date
     * is different from one whose field was dropped in transit.
     */
    private fun toJson(value: Any?): Any = when (value) {
        null -> JSONObject.NULL
        is Map<*, *> -> JSONObject().also { obj ->
            value.forEach { (k, v) -> obj.put(k.toString(), toJson(v)) }
        }
        is List<*> -> JSONArray().also { arr -> value.forEach { arr.put(toJson(it)) } }
        is String, is Number, is Boolean -> value
        else -> value.toString()
    }
}
