package com.jvoice.aishorts.navigation

/** Routes for the AI Shorts area. Added alongside the existing news routes. */
object AIShortRoutes {

    const val DASHBOARD = "aishorts/dashboard"
    const val TEMPLATE_ADMIN = "aishorts/templates"

    const val CREATE = "aishorts/create/{newsId}"
    const val SCRIPT = "aishorts/script/{shortId}"
    const val TEMPLATE = "aishorts/template/{shortId}"
    const val VOICE = "aishorts/voice/{shortId}"
    const val MEDIA = "aishorts/media/{shortId}"
    const val REVIEW = "aishorts/review/{shortId}"
    const val PROGRESS = "aishorts/progress/{shortId}"
    const val PREVIEW = "aishorts/preview/{shortId}"
    const val TRIM = "aishorts/trim/{shortId}/{sceneId}"

    fun create(newsId: String) = "aishorts/create/$newsId"
    fun script(shortId: String) = "aishorts/script/$shortId"
    fun template(shortId: String) = "aishorts/template/$shortId"
    fun voice(shortId: String) = "aishorts/voice/$shortId"
    fun media(shortId: String) = "aishorts/media/$shortId"
    fun review(shortId: String) = "aishorts/review/$shortId"
    fun progress(shortId: String) = "aishorts/progress/$shortId"
    fun preview(shortId: String) = "aishorts/preview/$shortId"
    fun trim(shortId: String, sceneId: String) = "aishorts/trim/$shortId/$sceneId"

    const val ARG_NEWS_ID = "newsId"
    const val ARG_SHORT_ID = "shortId"
    const val ARG_SCENE_ID = "sceneId"
}
