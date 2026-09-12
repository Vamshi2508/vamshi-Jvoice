package com.jvoice.aishorts.data.model

import com.jvoice.core.i18n.AppLanguage
import com.jvoice.core.i18n.LocalizedText

/**
 * J Voice AI Shorts - models.
 *
 * An AI Short is a separate object that *points at* a news article; the article
 * itself is never modified. A story works perfectly well with no short attached.
 */

// ------------------------------------------------------------------ status

enum class AIShortStatus(val label: String) {
    DRAFT("Draft"),
    SCRIPT_GENERATING("Generating script"),
    SCRIPT_READY("Script ready"),
    WAITING_FOR_REVIEW("Waiting for review"),
    PREPARING_MEDIA("Preparing media"),
    VOICE_GENERATING("Generating voice"),
    RENDER_QUEUED("Queued for render"),
    RENDERING("Rendering"),
    READY("Ready"),
    APPROVED("Approved"),
    PUBLISHED("Published"),
    FAILED("Failed");

    val isBusy: Boolean
        get() = this == SCRIPT_GENERATING || this == PREPARING_MEDIA ||
            this == VOICE_GENERATING || this == RENDER_QUEUED || this == RENDERING

    val isPlayable: Boolean
        get() = this == READY || this == APPROVED || this == PUBLISHED
}

/** The dashboard's filter buckets. */
enum class ShortsFilter(val label: String) {
    ALL("All"),
    DRAFT("Draft"),
    GENERATING("Generating"),
    READY("Ready"),
    APPROVED("Approved"),
    PUBLISHED("Published"),
    FAILED("Failed");

    fun matches(status: AIShortStatus): Boolean = when (this) {
        ALL -> true
        DRAFT -> status == AIShortStatus.DRAFT ||
            status == AIShortStatus.SCRIPT_READY ||
            status == AIShortStatus.WAITING_FOR_REVIEW
        GENERATING -> status.isBusy
        READY -> status == AIShortStatus.READY
        APPROVED -> status == AIShortStatus.APPROVED
        PUBLISHED -> status == AIShortStatus.PUBLISHED
        FAILED -> status == AIShortStatus.FAILED
    }
}

// ---------------------------------------------------------------- language

enum class ShortLanguage(val label: String, val code: String, val isAvailable: Boolean) {
    TELUGU("Telugu", "te", true),
    ENGLISH("English", "en", true),
    // Reserved: the architecture carries them, the UI marks them unavailable.
    HINDI("Hindi", "hi", false),
    TAMIL("Tamil", "ta", false),
    KANNADA("Kannada", "kn", false);

    /**
     * The app language this short should read its source article in.
     *
     * A wider enum than [AppLanguage] on purpose - the shorts pipeline is built
     * to carry languages the rest of the app does not publish in yet. The three
     * reserved ones have no article text to draw on, so they fall back to Telugu,
     * which is what the desk writes first.
     */
    val appLanguage: AppLanguage
        get() = when (this) {
            ENGLISH -> AppLanguage.ENGLISH
            else -> AppLanguage.TELUGU
        }
}

// ---------------------------------------------------------------- template

enum class TemplateCategory(val label: String) {
    BREAKING("Breaking"),
    STANDARD("Standard"),
    LOCAL("Local"),
    POLITICS("Politics"),
    SPORTS("Sports"),
    WEATHER("Weather"),
    BUSINESS("Business"),
    EDUCATION("Education"),
    EXPLAINER("Explainer"),
    QUICK("Quick News")
}

data class VideoTemplate(
    val id: String,
    val name: String,
    val description: String,
    val category: TemplateCategory,
    val thumbnailUrl: String,
    val aspectRatio: String = "9:16",
    val maxDurationSeconds: Int = 60,
    val sceneCount: Int = 5,
    val accentColorHex: Long = 0xFFB3261E,
    val recommendedFor: String = "",
    /**
     * The layout, as token strings resolved by [com.jvoice.aishorts.domain.service.TemplateEngine].
     * Templates are configuration - there is no per-template render code.
     */
    val layout: List<String> = emptyList(),
    val supportedDurations: List<Int> = listOf(15, 30, 45, 60),
    val isActive: Boolean = true,
    val isDefault: Boolean = false
)

// ------------------------------------------------------------------- voice

enum class VoiceGender(val label: String) { FEMALE("Female"), MALE("Male") }

enum class VoiceStyle(val label: String, val hint: String) {
    PROFESSIONAL("Professional", "Even, neutral news read"),
    BREAKING_NEWS("Breaking News", "Urgent, higher energy"),
    CALM("Calm", "Slower and softer"),
    FAST("Fast", "Quicker pace, fits more words")
}

data class VoiceOption(
    val id: String,
    val name: String,
    val language: ShortLanguage,
    val gender: VoiceGender,
    val defaultStyle: VoiceStyle = VoiceStyle.PROFESSIONAL,
    /** Placeholder - no TTS provider is wired up in this build. */
    val sampleUrl: String = "",
    val isAvailable: Boolean = true
)

// ------------------------------------------------------------------- media

enum class MediaType { IMAGE, VIDEO }

enum class MediaSource(val label: String) {
    NEWS_ARTICLE("From the article"),
    EDITOR_UPLOAD("Uploaded"),
    STOCK("Stock library"),
    AI_GENERATED("AI generated")
}

data class VideoMedia(
    val id: String,
    val url: String,
    val type: MediaType = MediaType.IMAGE,
    val source: MediaSource = MediaSource.NEWS_ARTICLE,
    val label: String = "",
    /** Still shown for a video asset; the video itself is never modified. */
    val posterUrl: String = "",
    /** Source length, for the trim editor. Zero for stills. */
    val durationMs: Int = 0,
    /** AI visuals must always be labelled as illustrative, never presented as footage. */
    val isIllustrative: Boolean = false
) {
    val displayUrl: String get() = if (type == MediaType.VIDEO && posterUrl.isNotBlank()) posterUrl else url
}

// ------------------------------------------------------------------ script

data class AIScene(
    val id: String,
    val aiShortId: String = "",
    val order: Int,
    val text: String,
    val startSeconds: Int,
    val endSeconds: Int,
    val mediaId: String? = null,
    val mediaType: MediaType? = null,
    /**
     * Clip window inside the source video, in milliseconds. Metadata only -
     * the original asset is never trimmed or re-encoded.
     */
    val clipStartMs: Int = 0,
    val clipEndMs: Int = 0,
    val caption: String = text
) {
    val durationSeconds: Int get() = (endSeconds - startSeconds).coerceAtLeast(1)
    val timeLabel: String get() = startSeconds.toString() + "-" + endSeconds + " sec"
    val hasClip: Boolean get() = clipEndMs > clipStartMs
    val clipLabel: String
        get() = if (!hasClip) "Full asset"
        else msLabel(clipStartMs) + " - " + msLabel(clipEndMs) +
            "  (" + ((clipEndMs - clipStartMs) / 1000) + "s)"

    private fun msLabel(ms: Int): String {
        val total = ms / 1000
        val m = total / 60
        val sec = total % 60
        return (if (m < 10) "0" else "") + m + ":" + (if (sec < 10) "0" else "") + sec
    }
}

data class AIScript(
    val sourceArticleId: String,
    val scenes: List<AIScene> = emptyList(),
    val language: ShortLanguage = ShortLanguage.ENGLISH,
    val generatedAt: Long = 0L,
    val isEditedByHuman: Boolean = false
) {
    val totalSeconds: Int get() = scenes.maxOfOrNull { it.endSeconds } ?: 0
}

// -------------------------------------------------------------------- job

enum class JobStage(val label: String) {
    QUEUED("Queued"),
    SCRIPT("Script generated"),
    VOICE("Voice prepared"),
    MEDIA("Media prepared"),
    RENDER("Rendering video"),
    DONE("Finished")
}

data class VideoGenerationJob(
    val jobId: String,
    val newsId: String,
    val aiShortId: String,
    val status: AIShortStatus = AIShortStatus.PREPARING_MEDIA,
    val progress: Int = 0,
    val stage: JobStage = JobStage.QUEUED,
    /** Which backend did the work. "mock" until a real one is wired up. */
    val provider: String = "mock",
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val createdAt: Long = 0L,
    val startedAt: Long? = null,
    val completedAt: Long? = null
)

// -------------------------------------------------- analytics + cost (future)

data class ShortAnalytics(
    val views: Int = 0,
    val watchTimeSeconds: Int = 0,
    val completionRate: Int = 0,
    val likes: Int = 0,
    val shares: Int = 0,
    val saves: Int = 0
)

data class ShortCost(
    val generationCount: Int = 0,
    val regenerationCount: Int = 0,
    /** Indicative only - no billing is connected. */
    val estimatedCost: Double = 0.0
)

// ------------------------------------------------------------------- short

/** What a regenerate request should redo. */
enum class RegenerateTarget(val label: String, val hint: String) {
    SCRIPT("Script", "Rewrite the scenes from the article"),
    VOICE("Voice", "Re-record with the current voice settings"),
    TEMPLATE("Template", "Pick a different look"),
    MEDIA("Media", "Reassign the visuals"),
    FULL_VIDEO("Complete video", "Redo everything and render again")
}

data class AIShort(
    val id: String,
    val newsId: String,
    /** Carried in both languages so the render can be re-cut in either. */
    val newsHeadline: LocalizedText,
    val categoryId: String = "",
    val location: String = "",
    val templateId: String? = null,
    val language: ShortLanguage = ShortLanguage.TELUGU,
    val voiceId: String? = null,
    val voiceStyle: VoiceStyle = VoiceStyle.PROFESSIONAL,
    val durationSeconds: Int = 30,
    val status: AIShortStatus = AIShortStatus.DRAFT,
    val script: AIScript? = null,
    val media: List<VideoMedia> = emptyList(),
    val thumbnailUrl: String = "",
    val videoUrl: String = "",
    val job: VideoGenerationJob? = null,
    val analytics: ShortAnalytics = ShortAnalytics(),
    val cost: ShortCost = ShortCost(),
    val errorMessage: String? = null,
    val createdBy: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val approvedAt: Long? = null,
    val publishedAt: Long? = null
) {
    val hasScript: Boolean get() = script?.scenes?.isNotEmpty() == true
    val hasTemplate: Boolean get() = !templateId.isNullOrBlank()
    val hasVoice: Boolean get() = !voiceId.isNullOrBlank()
    val hasMedia: Boolean get() = script?.scenes?.any { it.mediaId != null } == true

    /** Drives the "continue setup" checklist on a draft. */
    val setupComplete: Boolean get() = hasScript && hasTemplate && hasVoice && hasMedia

    val sceneCount: Int get() = script?.scenes?.size ?: 0
    val mediaCount: Int get() = script?.scenes?.mapNotNull { it.mediaId }?.distinct()?.size ?: 0
}
