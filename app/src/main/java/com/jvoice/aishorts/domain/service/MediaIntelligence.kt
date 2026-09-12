package com.jvoice.aishorts.domain.service

import com.jvoice.aishorts.data.model.AIScene
import com.jvoice.aishorts.data.model.MediaType
import com.jvoice.aishorts.data.model.TemplateCategory
import com.jvoice.aishorts.data.model.VideoMedia
import com.jvoice.aishorts.data.model.VideoTemplate

/**
 * The "intelligence" layer: template recommendation and scene-to-media mapping.
 *
 * Both are heuristics over data the Editor already supplied. Neither invents
 * anything - media suggestions only ever point at assets attached to the article,
 * and every suggestion is a starting point the Editor can override.
 */
object MediaIntelligence {

    /** Maps a news category id onto the template category that suits it. */
    fun recommendTemplate(
        categoryId: String,
        isBreaking: Boolean,
        templates: List<VideoTemplate>
    ): VideoTemplate? {
        if (isBreaking) {
            templates.firstOrNull { it.isActive && it.category == TemplateCategory.BREAKING }
                ?.let { return it }
        }
        val wanted = when (categoryId) {
            "cat_politics" -> TemplateCategory.POLITICS
            "cat_sports" -> TemplateCategory.SPORTS
            "cat_business", "cat_technology" -> TemplateCategory.BUSINESS
            "cat_education", "cat_jobs" -> TemplateCategory.EDUCATION
            "cat_telangana", "cat_ap" -> TemplateCategory.LOCAL
            "cat_health" -> TemplateCategory.EXPLAINER
            else -> TemplateCategory.STANDARD
        }
        return templates.firstOrNull { it.isActive && it.category == wanted }
            ?: templates.firstOrNull { it.isActive && it.category == TemplateCategory.STANDARD }
            ?: templates.firstOrNull { it.isActive }
    }

    fun recommendationReason(categoryId: String, isBreaking: Boolean): String = when {
        isBreaking -> "This story is marked breaking"
        categoryId == "cat_sports" -> "Sports story"
        categoryId == "cat_politics" -> "Politics story"
        categoryId == "cat_business" || categoryId == "cat_technology" -> "Business or technology story"
        categoryId == "cat_education" || categoryId == "cat_jobs" -> "Education story"
        categoryId == "cat_telangana" || categoryId == "cat_ap" -> "Regional story"
        else -> "General news"
    }

    /**
     * Suggests which asset each scene should show.
     *
     * Rules, in order:
     *  - the opener gets a video if one exists (motion holds attention)
     *  - the closing sign-off gets the cover photo (branding beat)
     *  - the middle scenes cycle through the remaining assets so nothing repeats
     *    until everything has been used once
     */
    fun suggestMedia(scenes: List<AIScene>, media: List<VideoMedia>): Map<String, String> {
        if (scenes.isEmpty() || media.isEmpty()) return emptyMap()

        val videos = media.filter { it.type == MediaType.VIDEO }
        val photos = media.filter { it.type == MediaType.IMAGE }
        val suggestions = mutableMapOf<String, String>()

        val lastIndex = scenes.lastIndex
        // Middle scenes draw from everything except what the opener took.
        val pool = ArrayDeque(
            (videos.drop(if (videos.isNotEmpty()) 1 else 0) + photos).ifEmpty { media }
        )

        scenes.forEachIndexed { index, scene ->
            val pick = when {
                index == 0 && videos.isNotEmpty() -> videos.first()
                index == lastIndex && photos.isNotEmpty() -> photos.first()
                pool.isNotEmpty() -> pool.removeFirst()
                else -> media[index % media.size]
            }
            suggestions[scene.id] = pick.id
        }
        return suggestions
    }

    /**
     * Suggests a clip window inside a long video. Skips the first couple of
     * seconds (usually camera settle) and takes the scene's own duration.
     */
    fun suggestClip(sourceDurationMs: Int, sceneSeconds: Int): Pair<Int, Int> {
        val wanted = sceneSeconds * 1000
        if (sourceDurationMs <= wanted) return 0 to sourceDurationMs
        val start = minOf(2_000, sourceDurationMs - wanted)
        return start to (start + wanted)
    }
}
