package com.jvoice.aishorts.data.mock

import com.jvoice.aishorts.data.model.AIScene
import com.jvoice.aishorts.data.model.AIScript
import com.jvoice.aishorts.data.model.AIShort
import com.jvoice.aishorts.data.model.AIShortStatus
import com.jvoice.aishorts.data.model.MediaSource
import com.jvoice.aishorts.data.model.MediaType
import com.jvoice.aishorts.data.model.ShortAnalytics
import com.jvoice.aishorts.data.model.ShortCost
import com.jvoice.aishorts.data.model.ShortLanguage
import com.jvoice.aishorts.data.model.TemplateCategory
import com.jvoice.aishorts.data.model.VideoMedia
import com.jvoice.aishorts.data.model.VideoTemplate
import com.jvoice.aishorts.data.model.VoiceGender
import com.jvoice.aishorts.data.model.VoiceOption
import com.jvoice.aishorts.data.model.VoiceStyle
import com.jvoice.core.i18n.lt

/**
 * Configuration for the AI Shorts pipeline: the video templates the desk can
 * render with, and the voices available to narrate them.
 *
 * This is NOT seed data. It used to sit in a file called AIShortMockData
 * alongside a list of fabricated shorts, which made it look like demo content -
 * the fabricated shorts are gone, and what remains describes what the pipeline
 * can actually do.
 *
 * Kept in code rather than Firestore because a template is a rendering
 * capability: adding one means the renderer knows how to draw it, so it ships
 * with the build. The desk can enable, disable and pick a default at runtime, and
 * that state is held by AIShortRepository.
 */
object AIShortConfig {

    private val NOW = System.currentTimeMillis()
    private const val MINUTE = 60_000L
    private const val HOUR = 60 * MINUTE
    private const val DAY = 24 * HOUR

    private fun thumb(seed: String) = "https://picsum.photos/seed/" + seed + "/720/1280"

    /** Keeps the ten templates declarative - they are data, not code. */
    private fun template(
        id: String,
        name: String,
        category: TemplateCategory,
        description: String,
        recommendedFor: String,
        accent: Long,
        sceneCount: Int,
        layout: List<String>,
        maxDuration: Int = 60,
        supported: List<Int> = listOf(15, 30, 45, 60),
        isDefault: Boolean = false
    ) = VideoTemplate(
        id = id,
        name = name,
        description = description,
        category = category,
        thumbnailUrl = thumb(id),
        maxDurationSeconds = maxDuration,
        sceneCount = sceneCount,
        accentColorHex = accent,
        recommendedFor = recommendedFor,
        layout = layout,
        supportedDurations = supported,
        isDefault = isDefault
    )

    // ------------------------------------------------------------- templates
    val templates: List<VideoTemplate> = listOf(
        template(
            "tpl_breaking", "Breaking News", TemplateCategory.BREAKING,
            "Red alert bar, hard cuts, bold animated captions.",
            "Urgent stories that just broke", 0xFFB3261E, 5, isDefault = true,
            layout = listOf(
                "badge: BREAKING",
                "ticker: {{ticker}}",
                "title: {{headline}}",
                "strap: {{category}} | {{location}}",
                "scene1: {{scene_1_media}} + {{scene_1_text}} ({{scene_1_duration}})",
                "scene2: {{scene_2_media}} + {{scene_2_text}} ({{scene_2_duration}})",
                "scene3: {{scene_3_media}} + {{scene_3_text}} ({{scene_3_duration}})",
                "captions: {{captions}}",
                "voice: {{voice}}",
                "outro: {{logo}}"
            )
        ),
        template(
            "tpl_standard", "Standard News", TemplateCategory.STANDARD,
            "Clean J Voice styling with a lower third.",
            "Everyday reporting", 0xFF1565C0, 5,
            layout = listOf(
                "title: {{headline}}",
                "lower_third: {{category}} | {{location}}",
                "scene1: {{scene_1_media}} + {{scene_1_text}}",
                "scene2: {{scene_2_media}} + {{scene_2_text}}",
                "scene3: {{scene_3_media}} + {{scene_3_text}}",
                "captions: {{captions}}",
                "outro: {{logo}}"
            )
        ),
        template(
            "tpl_local", "Local News", TemplateCategory.LOCAL,
            "Location pin, district strap, map accent.",
            "District and city stories", 0xFF1B7F4B, 4,
            layout = listOf(
                "badge: LOCAL",
                "pin: {{location}}",
                "title: {{headline}}",
                "scene1: {{scene_1_media}} + {{scene_1_text}}",
                "scene2: {{scene_2_media}} + {{scene_2_text}}",
                "captions: {{captions}}",
                "outro: {{logo}}"
            )
        ),
        template(
            "tpl_politics", "Politics", TemplateCategory.POLITICS,
            "Sober political styling with a key-point highlight panel.",
            "Assembly, party and policy stories", 0xFF4A148C, 5,
            layout = listOf(
                "title: {{headline}}",
                "strap: {{category}} | {{location}}",
                "highlight: {{scene_2_text}}",
                "scene1: {{scene_1_media}} + {{scene_1_text}}",
                "scene2: {{scene_2_media}} + {{scene_2_text}}",
                "scene3: {{scene_3_media}} + {{scene_3_text}}",
                "captions: {{captions}}",
                "outro: {{logo}}"
            )
        ),
        template(
            "tpl_sports", "Sports", TemplateCategory.SPORTS,
            "Scoreboard motif, fast transitions.",
            "Match results and sport updates", 0xFFE07B00, 5,
            layout = listOf(
                "scoreboard: {{headline}}",
                "strap: {{category}}",
                "scene1: {{scene_1_media}} + {{scene_1_text}}",
                "scene2: {{scene_2_media}} + {{scene_2_text}}",
                "scene3: {{scene_3_media}} + {{scene_3_text}}",
                "captions: {{captions}}",
                "outro: {{logo}}"
            )
        ),
        template(
            "tpl_weather", "Weather", TemplateCategory.WEATHER,
            "Forecast panel with condition icons.",
            "Rain, heat and cyclone updates", 0xFF00695C, 4,
            layout = listOf(
                "panel: WEATHER | {{location}}",
                "title: {{headline}}",
                "scene1: {{scene_1_media}} + {{scene_1_text}}",
                "scene2: {{scene_2_media}} + {{scene_2_text}}",
                "captions: {{captions}}",
                "outro: {{logo}}"
            )
        ),
        template(
            "tpl_business", "Business", TemplateCategory.BUSINESS,
            "Financial styling with a figures panel.",
            "Markets, industry and technology", 0xFF37474F, 5,
            layout = listOf(
                "title: {{headline}}",
                "strap: {{category}} | {{location}}",
                "figures: {{scene_2_text}}",
                "scene1: {{scene_1_media}} + {{scene_1_text}}",
                "scene2: {{scene_2_media}} + {{scene_2_text}}",
                "scene3: {{scene_3_media}} + {{scene_3_text}}",
                "captions: {{captions}}",
                "outro: {{logo}}"
            )
        ),
        template(
            "tpl_education", "Education", TemplateCategory.EDUCATION,
            "Explainer layout with bullet reveals.",
            "Exams, notifications and policy", 0xFF7B4DFF, 6,
            layout = listOf(
                "title: {{headline}}",
                "bullets: {{scene_1_text}} / {{scene_2_text}} / {{scene_3_text}}",
                "scene1: {{scene_1_media}} + {{scene_1_text}}",
                "scene2: {{scene_2_media}} + {{scene_2_text}}",
                "scene3: {{scene_3_media}} + {{scene_3_text}}",
                "captions: {{captions}}",
                "outro: {{logo}}"
            )
        ),
        template(
            "tpl_explainer", "Explainer", TemplateCategory.EXPLAINER,
            "More scenes, more text, step-by-step build.",
            "Stories that need unpacking", 0xFF00838F, 7,
            layout = listOf(
                "title: {{headline}}",
                "step1: {{scene_1_text}}",
                "step2: {{scene_2_text}}",
                "step3: {{scene_3_text}}",
                "scene1: {{scene_1_media}}",
                "scene2: {{scene_2_media}}",
                "scene3: {{scene_3_media}}",
                "captions: {{captions}}",
                "outro: {{logo}}"
            )
        ),
        template(
            "tpl_quick", "Quick News", TemplateCategory.QUICK,
            "Minimal text, fast cuts, built for 15 seconds.",
            "One-line updates", 0xFFAD1457, 3,
            maxDuration = 30,
            supported = listOf(15, 30),
            layout = listOf(
                "title: {{headline}}",
                "scene1: {{scene_1_media}} + {{scene_1_text}}",
                "scene2: {{scene_2_media}} + {{scene_2_text}}",
                "outro: {{logo}}"
            )
        )
    )

    // ---------------------------------------------------------------- voices
    val voices: List<VoiceOption> = listOf(
        VoiceOption("v_te_f", "Telugu Female", ShortLanguage.TELUGU, VoiceGender.FEMALE, VoiceStyle.PROFESSIONAL),
        VoiceOption("v_te_m", "Telugu Male", ShortLanguage.TELUGU, VoiceGender.MALE, VoiceStyle.BREAKING_NEWS),
        VoiceOption("v_en_f", "English Female", ShortLanguage.ENGLISH, VoiceGender.FEMALE, VoiceStyle.PROFESSIONAL),
        VoiceOption("v_en_m", "English Male", ShortLanguage.ENGLISH, VoiceGender.MALE, VoiceStyle.CALM)
    )
}
