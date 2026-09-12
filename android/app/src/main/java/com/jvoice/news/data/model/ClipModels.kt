package com.jvoice.news.data.model

/**
 * Short-video clip for the Clips tab.
 *
 * Additive model - the existing news models are untouched. Playback is not wired
 * to any media source in this build: [videoUrl] is a placeholder so the screen can
 * be pointed at real media later without changing the UI.
 */
data class NewsClip(
    val id: String,
    val title: String,
    val description: String,
    val categoryId: String,
    val location: String,
    val thumbnailUrl: String,
    val videoUrl: String = "",
    val durationSeconds: Int,
    val reporterName: String,
    val publishedAt: Long,
    val views: Int,
    val likes: Int,
    val isBreaking: Boolean = false,
    val relatedArticleId: String? = null
) {
    val durationLabel: String
        get() = (durationSeconds / 60).toString() + ":" +
            (if (durationSeconds % 60 < 10) "0" else "") + (durationSeconds % 60)
}
