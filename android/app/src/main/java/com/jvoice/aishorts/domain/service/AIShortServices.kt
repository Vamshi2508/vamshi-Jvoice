package com.jvoice.aishorts.domain.service

import com.jvoice.aishorts.data.model.AIScene
import com.jvoice.aishorts.data.model.AIScript
import com.jvoice.aishorts.data.model.MediaSource
import com.jvoice.aishorts.data.model.MediaType
import com.jvoice.aishorts.data.model.ShortLanguage
import com.jvoice.aishorts.data.model.VideoMedia
import com.jvoice.aishorts.data.model.VoiceOption
import com.jvoice.aishorts.data.model.VoiceStyle
import com.jvoice.news.data.model.NewsArticle
import kotlinx.coroutines.delay
import com.jvoice.core.i18n.AppLanguage

/**
 * The seam between J Voice and whatever eventually does the work.
 *
 * Nothing here holds a key or talks to a provider. The production path is
 * Android -> J Voice backend -> AI / TTS / renderer, so these interfaces are what
 * a backend client will implement later; the Editor UI will not have to change.
 */

/** Result wrapper so failures surface as states, never as thrown exceptions. */
sealed interface ServiceResult<out T> {
    data class Success<T>(val value: T) : ServiceResult<T>
    data class Failure(val code: String, val message: String) : ServiceResult<Nothing>
}

interface AIScriptService {
    /**
     * Turns an existing article into a timed scene list.
     *
     * Contract: the script may only summarise [article]. It must not introduce
     * facts, figures, quotes, names, places or dates that are not in the source.
     */
    suspend fun generateScript(
        article: NewsArticle,
        language: ShortLanguage,
        targetSeconds: Int
    ): ServiceResult<AIScript>
}

interface TextToSpeechService {
    suspend fun synthesize(script: AIScript, voice: VoiceOption, style: VoiceStyle): ServiceResult<String>
    fun previewClipFor(voice: VoiceOption, style: VoiceStyle): String
}

interface VideoRenderingService {
    /** Emits progress 0..100 while rendering; returns the output URLs. */
    suspend fun render(
        shortId: String,
        onProgress: suspend (Int) -> Unit
    ): ServiceResult<RenderOutput>
}

data class RenderOutput(val videoUrl: String, val thumbnailUrl: String)

interface MediaService {
    /** Visuals already attached to the article - always the first choice. */
    fun mediaFromArticle(article: NewsArticle): List<VideoMedia>
    suspend fun uploadMedia(label: String): ServiceResult<VideoMedia>
    /** Reserved for a future licensed stock library. */
    fun stockLibrary(): List<VideoMedia>
}

/* ===================================================================== mocks */

/**
 * Builds the script by **reusing sentences from the article itself**.
 *
 * This is deliberate: because every scene line is lifted from the source text,
 * the mock physically cannot invent a number, a name or a quote. The only added
 * line is the J Voice sign-off, which states no fact.
 */
object MockAIScriptService : AIScriptService {

    private const val OUTRO_EN = "Follow J Voice for more updates."
    private const val OUTRO_TE = "మరిన్ని అప్‌డేట్‌ల కోసం J Voice ను అనుసరించండి."

    override suspend fun generateScript(
        article: NewsArticle,
        language: ShortLanguage,
        targetSeconds: Int
    ): ServiceResult<AIScript> {
        delay(1_400)

        // The script is cut from the article in the short's own language, so a
        // Telugu short is built from the Telugu copy rather than translated
        // after the fact.
        val sentences = extractSentences(article, language.appLanguage)
        if (sentences.isEmpty()) {
            return ServiceResult.Failure(
                code = "SCRIPT_EMPTY_SOURCE",
                message = "This article has too little text to summarise."
            )
        }

        // One opener from the headline, up to three body lines, one sign-off.
        val bodyLines = sentences.take(3)
        val lines = buildList {
            add(article.headline.get(language.appLanguage).trim())
            addAll(bodyLines)
            add(if (language == ShortLanguage.TELUGU) OUTRO_TE else OUTRO_EN)
        }

        val scenes = spreadOverTimeline(lines, targetSeconds)
        return ServiceResult.Success(
            AIScript(
                sourceArticleId = article.id,
                scenes = scenes,
                language = language,
                generatedAt = System.currentTimeMillis(),
                isEditedByHuman = false
            )
        )
    }

    /**
     * Sentences from the body in [language], minus the demo disclaimer line.
     *
     * Falls back through [com.jvoice.core.i18n.LocalizedText.get], so a story
     * filed in one language still yields a script - it will just be in that
     * language, which beats returning an empty one.
     */
    private fun extractSentences(article: NewsArticle, language: AppLanguage): List<String> =
        article.content.get(language)
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("(") }
            .flatMap { paragraph -> paragraph.split(". ", "। ").asSequence() }
            .map { it.trim().trimEnd('.') }
            .filter { it.length > 12 }
            .map { if (it.endsWith(".")) it else "$it." }
            .toList()

    /** Gives longer lines more screen time, then snaps the last scene to the target. */
    private fun spreadOverTimeline(lines: List<String>, targetSeconds: Int): List<AIScene> {
        val weights = lines.map { it.length.coerceAtLeast(20) }
        val totalWeight = weights.sum().toFloat()
        var cursor = 0
        return lines.mapIndexed { index, text ->
            val isLast = index == lines.lastIndex
            val slice = ((weights[index] / totalWeight) * targetSeconds).toInt().coerceAtLeast(3)
            val end = if (isLast) targetSeconds else (cursor + slice).coerceAtMost(targetSeconds - 3)
            val scene = AIScene(
                id = "scene_" + (index + 1),
                order = index,
                text = text,
                startSeconds = cursor,
                endSeconds = end.coerceAtLeast(cursor + 2),
                caption = text
            )
            cursor = scene.endSeconds
            scene
        }
    }
}

object MockTextToSpeechService : TextToSpeechService {
    override suspend fun synthesize(
        script: AIScript,
        voice: VoiceOption,
        style: VoiceStyle
    ): ServiceResult<String> {
        delay(900)
        if (!voice.isAvailable) {
            return ServiceResult.Failure("TTS_VOICE_UNAVAILABLE", "That voice is not available yet.")
        }
        return ServiceResult.Success("mock://voice/" + voice.id + "/" + style.name.lowercase())
    }

    override fun previewClipFor(voice: VoiceOption, style: VoiceStyle): String =
        "mock://voice-preview/" + voice.id + "/" + style.name.lowercase()
}

object MockVideoRenderingService : VideoRenderingService {
    override suspend fun render(
        shortId: String,
        onProgress: suspend (Int) -> Unit
    ): ServiceResult<RenderOutput> {
        // Simulates a queued render climbing to 100%.
        val steps = listOf(10, 25, 40, 55, 70, 85, 95, 100)
        steps.forEach { step ->
            delay(550)
            onProgress(step)
        }
        return ServiceResult.Success(
            RenderOutput(
                videoUrl = "mock://short/" + shortId + ".mp4",
                thumbnailUrl = "https://picsum.photos/seed/short_" + shortId + "/720/1280"
            )
        )
    }
}

object MockMediaService : MediaService {

    /**
     * The Editor's own media is the primary visual source: the article's cover
     * photo, then any extra photos, then any videos supplied with the story.
     * Nothing is fabricated here.
     */
    override fun mediaFromArticle(article: NewsArticle): List<VideoMedia> {
        val photos = article.allPhotos.mapIndexed { index, url ->
            VideoMedia(
                id = "m_" + article.id + "_p" + index,
                url = url,
                type = MediaType.IMAGE,
                source = MediaSource.NEWS_ARTICLE,
                label = if (index == 0) "Cover photo" else "Photo " + (index + 1)
            )
        }
        val videos = article.videoUrls.mapIndexed { index, url ->
            VideoMedia(
                id = "m_" + article.id + "_v" + index,
                url = url,
                type = MediaType.VIDEO,
                source = MediaSource.NEWS_ARTICLE,
                label = "Video " + (index + 1),
                // No player in this build, so a still stands in for the frame.
                posterUrl = article.allPhotos.getOrElse(index) { article.imageUrl },
                // Deterministic mock length so the trim editor has a timeline.
                durationMs = 95_000 + (url.hashCode().mod(60)) * 1_000
            )
        }
        return photos + videos
    }

    override suspend fun uploadMedia(label: String): ServiceResult<VideoMedia> {
        delay(700)
        val id = "m_up_" + System.currentTimeMillis()
        return ServiceResult.Success(
            VideoMedia(
                id = id,
                url = "https://picsum.photos/seed/" + id + "/720/1280",
                type = MediaType.IMAGE,
                source = MediaSource.EDITOR_UPLOAD,
                label = label.ifBlank { "Uploaded image" }
            )
        )
    }

    /** Empty until a licensed provider is contracted. */
    override fun stockLibrary(): List<VideoMedia> = emptyList()
}
