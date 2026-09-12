package com.jvoice.aishorts.domain.service

import com.jvoice.aishorts.data.model.AIShort
import com.jvoice.aishorts.data.model.VideoTemplate

/**
 * The template engine.
 *
 * Templates are **configuration, not code**: each one declares a layout made of
 * token strings, and the engine substitutes values from the short at render time.
 * Adding an eleventh template means adding a data entry, never a new renderer.
 *
 * Supported tokens:
 *   {{headline}} {{category}} {{location}} {{logo}} {{ticker}}
 *   {{voice}} {{language}} {{duration}} {{captions}}
 *   {{scene_N_text}} {{scene_N_media}} {{scene_N_caption}} {{scene_N_duration}}
 */
object TemplateEngine {

    const val TOKEN_HEADLINE = "{{headline}}"
    const val TOKEN_CATEGORY = "{{category}}"
    const val TOKEN_LOCATION = "{{location}}"
    const val TOKEN_LOGO = "{{logo}}"
    const val TOKEN_TICKER = "{{ticker}}"
    const val TOKEN_VOICE = "{{voice}}"
    const val TOKEN_LANGUAGE = "{{language}}"
    const val TOKEN_DURATION = "{{duration}}"
    const val TOKEN_CAPTIONS = "{{captions}}"

    fun sceneTextToken(index: Int) = "{{scene_" + index + "_text}}"
    fun sceneMediaToken(index: Int) = "{{scene_" + index + "_media}}"
    fun sceneCaptionToken(index: Int) = "{{scene_" + index + "_caption}}"
    fun sceneDurationToken(index: Int) = "{{scene_" + index + "_duration}}"

    /**
     * Builds the value map for one short. A real renderer would receive exactly
     * this and fill the template's layout slots with it.
     */
    fun bind(
        short: AIShort,
        categoryName: String,
        voiceName: String,
        mediaUrlFor: (mediaId: String?) -> String
    ): Map<String, String> {
        val values = mutableMapOf(
            TOKEN_HEADLINE to short.newsHeadline.get(short.language.appLanguage),
            TOKEN_CATEGORY to categoryName,
            TOKEN_LOCATION to short.location,
            TOKEN_LOGO to "jvoice_logo",
            TOKEN_TICKER to "J VOICE",
            TOKEN_VOICE to voiceName,
            TOKEN_LANGUAGE to short.language.label,
            TOKEN_DURATION to short.durationSeconds.toString() + "s",
            TOKEN_CAPTIONS to (short.script?.scenes?.joinToString(" | ") { it.caption } ?: "")
        )
        short.script?.scenes?.forEach { scene ->
            val n = scene.order + 1
            values[sceneTextToken(n)] = scene.text
            values[sceneCaptionToken(n)] = scene.caption
            values[sceneDurationToken(n)] = scene.durationSeconds.toString() + "s"
            values[sceneMediaToken(n)] = mediaUrlFor(scene.mediaId)
        }
        return values
    }

    /** Substitutes tokens in a layout line. Unknown tokens are left visible. */
    fun resolve(line: String, values: Map<String, String>): String {
        var out = line
        values.forEach { (token, value) -> out = out.replace(token, value) }
        return out
    }

    /** A human-readable render plan - what the renderer would be handed. */
    fun renderPlan(template: VideoTemplate, values: Map<String, String>): List<String> =
        template.layout.map { resolve(it, values) }
}
