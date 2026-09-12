package com.jvoice.aishorts.data.repository

import com.jvoice.aishorts.data.model.AIScene
import com.jvoice.aishorts.data.model.AIShort
import com.jvoice.aishorts.data.model.AIShortStatus
import com.jvoice.aishorts.data.model.JobStage
import com.jvoice.aishorts.data.model.MediaType
import com.jvoice.aishorts.data.model.RegenerateTarget
import com.jvoice.aishorts.data.model.ShortLanguage
import com.jvoice.aishorts.data.model.ShortsFilter
import com.jvoice.aishorts.data.model.VideoGenerationJob
import com.jvoice.aishorts.data.model.VideoMedia
import com.jvoice.aishorts.data.model.VideoTemplate
import com.jvoice.aishorts.data.model.VoiceOption
import com.jvoice.aishorts.data.model.VoiceStyle
import com.jvoice.aishorts.domain.service.AIScriptService
import com.jvoice.aishorts.domain.service.MediaIntelligence
import com.jvoice.aishorts.domain.service.MediaService
import com.jvoice.aishorts.domain.service.MockAIScriptService
import com.jvoice.aishorts.domain.service.MockMediaService
import com.jvoice.aishorts.domain.service.MockTextToSpeechService
import com.jvoice.aishorts.domain.service.MockVideoRenderingService
import com.jvoice.aishorts.domain.service.ServiceResult
import com.jvoice.aishorts.domain.service.TextToSpeechService
import com.jvoice.aishorts.domain.service.VideoRenderingService
import com.jvoice.news.data.model.NewsArticle
import com.jvoice.news.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.jvoice.aishorts.data.mock.AIShortConfig

/**
 * Owns AI Shorts. Nothing here touches [NewsRepository]'s data - a short only
 * *references* an article by id, so news continues to work with no short attached.
 *
 * The services are injected so a backend-backed implementation can be swapped in
 * without the Editor UI noticing.
 */
object AIShortRepository {

    private var scriptService: AIScriptService = MockAIScriptService
    private var ttsService: TextToSpeechService = MockTextToSpeechService
    private var renderService: VideoRenderingService = MockVideoRenderingService
    private var mediaService: MediaService = MockMediaService

    /** Swap in real, backend-backed services later. */
    fun configure(
        script: AIScriptService = scriptService,
        tts: TextToSpeechService = ttsService,
        render: VideoRenderingService = renderService,
        media: MediaService = mediaService
    ) {
        scriptService = script
        ttsService = tts
        renderService = render
        mediaService = media
    }

    // Starts empty. Shorts are produced by the desk running the pipeline on a
    // published story - a pre-populated list was demo dressing.
    private val _shorts = MutableStateFlow<List<AIShort>>(emptyList())
    val shorts: StateFlow<List<AIShort>> = _shorts.asStateFlow()

    private val _templates = MutableStateFlow(AIShortConfig.templates)
    val templates: StateFlow<List<VideoTemplate>> = _templates.asStateFlow()

    val voices: List<VoiceOption> = AIShortConfig.voices

    private var idCounter = 100
    private fun nextId(prefix: String): String {
        idCounter += 1
        return prefix + idCounter
    }

    private fun now() = System.currentTimeMillis()

    // ------------------------------------------------------------- lookups
    fun shortById(id: String): AIShort? = _shorts.value.firstOrNull { it.id == id }

    fun shortsForArticle(newsId: String): List<AIShort> = _shorts.value.filter { it.newsId == newsId }

    /** Used by the feed to decide whether to show a video badge. */
    fun publishedShortFor(newsId: String): AIShort? =
        _shorts.value.firstOrNull {
            it.newsId == newsId &&
                (it.status == AIShortStatus.PUBLISHED || it.status == AIShortStatus.APPROVED)
        }

    fun templateById(id: String?): VideoTemplate? =
        if (id == null) null else _templates.value.firstOrNull { it.id == id }

    fun voiceById(id: String?): VoiceOption? =
        if (id == null) null else voices.firstOrNull { it.id == id }

    fun activeTemplates(): List<VideoTemplate> = _templates.value.filter { it.isActive }

    /** The template the story's category suggests. Always overridable. */
    fun recommendedTemplateFor(newsId: String): VideoTemplate? {
        val article = NewsRepository.articleById(newsId) ?: return null
        return MediaIntelligence.recommendTemplate(article.categoryId, article.isBreaking, _templates.value)
    }

    fun recommendationReasonFor(newsId: String): String {
        val article = NewsRepository.articleById(newsId) ?: return ""
        return MediaIntelligence.recommendationReason(article.categoryId, article.isBreaking)
    }

    /** Re-runs the media suggestion for a script the Editor has changed. */
    fun resuggestMedia(shortId: String) {
        update(shortId) { short ->
            val script = short.script ?: return@update short
            val suggestions = MediaIntelligence.suggestMedia(script.scenes, short.media)
            short.copy(
                script = script.copy(
                    scenes = script.scenes.map { scene ->
                        val mediaId = suggestions[scene.id] ?: scene.mediaId
                        val asset = short.media.firstOrNull { it.id == mediaId }
                        scene.copy(mediaId = mediaId, mediaType = asset?.type)
                    }
                )
            )
        }
    }

    fun voicesFor(language: ShortLanguage): List<VoiceOption> =
        voices.filter { it.language == language }

    fun filtered(filter: ShortsFilter, query: String): List<AIShort> {
        val q = query.trim().lowercase()
        return _shorts.value
            .filter { filter.matches(it.status) }
            .filter {
                q.isBlank() ||
                    it.newsHeadline.matches(query) ||
                    it.location.lowercase().contains(q) ||
                    it.status.label.lowercase().contains(q) ||
                    NewsRepository.categoryName(it.categoryId).lowercase().contains(q)
            }
            .sortedByDescending { it.updatedAt }
    }

    fun countFor(filter: ShortsFilter): Int = _shorts.value.count { filter.matches(it.status) }

    // ------------------------------------------------------------ mutation
    private fun update(shortId: String, transform: (AIShort) -> AIShort) {
        _shorts.update { list ->
            list.map { if (it.id == shortId) transform(it).copy(updatedAt = now()) else it }
        }
    }

    /**
     * Creates a draft for an article. One article may carry several shorts (a
     * Breaking cut and a Local cut, say), so [forceNew] starts an extra one even
     * when a draft already exists.
     */
    fun createDraft(article: NewsArticle, createdBy: String, forceNew: Boolean = false): AIShort {
        if (!forceNew) {
            val existing = _shorts.value.firstOrNull {
                it.newsId == article.id &&
                    it.status != AIShortStatus.PUBLISHED &&
                    it.status != AIShortStatus.APPROVED
            }
            if (existing != null) return existing
        }

        // The template the story's category suggests; the Editor can override it.
        val recommended = MediaIntelligence.recommendTemplate(
            categoryId = article.categoryId,
            isBreaking = article.isBreaking,
            templates = _templates.value
        )
        val default = recommended ?: _templates.value.firstOrNull { it.isDefault && it.isActive }
        val short = AIShort(
            id = nextId("short_"),
            newsId = article.id,
            newsHeadline = article.headline,
            categoryId = article.categoryId,
            location = article.location,
            templateId = default?.id,
            language = ShortLanguage.TELUGU,
            durationSeconds = 30,
            status = AIShortStatus.DRAFT,
            media = mediaService.mediaFromArticle(article),
            thumbnailUrl = article.imageUrl,
            createdBy = createdBy,
            createdAt = now(),
            updatedAt = now()
        )
        _shorts.update { listOf(short) + it }
        return short
    }

    // -------------------------------------------------------------- script
    suspend fun generateScript(shortId: String): ServiceResult<Unit> {
        val short = shortById(shortId) ?: return ServiceResult.Failure("NOT_FOUND", "Draft not found.")
        val article = NewsRepository.articleById(short.newsId)
            ?: return ServiceResult.Failure("ARTICLE_MISSING", "The source article is no longer available.")

        update(shortId) { it.copy(status = AIShortStatus.SCRIPT_GENERATING, errorMessage = null) }

        return when (val result = scriptService.generateScript(article, short.language, short.durationSeconds)) {
            is ServiceResult.Success -> {
                // Suggest which asset each scene should show, using only the
                // media already attached to the article.
                val suggestions = MediaIntelligence.suggestMedia(result.value.scenes, short.media)
                val withMedia = result.value.copy(
                    scenes = result.value.scenes.map { scene ->
                        val mediaId = suggestions[scene.id]
                        val asset = short.media.firstOrNull { m -> m.id == mediaId }
                        val clip = if (asset?.type == MediaType.VIDEO && asset.durationMs > 0) {
                            MediaIntelligence.suggestClip(asset.durationMs, scene.durationSeconds)
                        } else 0 to 0
                        scene.copy(
                            aiShortId = shortId,
                            mediaId = mediaId,
                            mediaType = asset?.type,
                            clipStartMs = clip.first,
                            clipEndMs = clip.second
                        )
                    }
                )
                update(shortId) {
                    it.copy(
                        script = withMedia,
                        status = AIShortStatus.SCRIPT_READY,
                        cost = it.cost.copy(
                            generationCount = it.cost.generationCount + 1,
                            estimatedCost = it.cost.estimatedCost + 0.06
                        )
                    )
                }
                ServiceResult.Success(Unit)
            }
            is ServiceResult.Failure -> {
                update(shortId) { it.copy(status = AIShortStatus.FAILED, errorMessage = result.message) }
                result
            }
        }
    }

    fun updateSceneText(shortId: String, sceneId: String, text: String) {
        update(shortId) { short ->
            val script = short.script ?: return@update short
            short.copy(
                script = script.copy(
                    isEditedByHuman = true,
                    scenes = script.scenes.map {
                        if (it.id == sceneId) it.copy(text = text, caption = text) else it
                    }
                )
            )
        }
    }

    fun updateSceneDuration(shortId: String, sceneId: String, seconds: Int) {
        update(shortId) { short ->
            val script = short.script ?: return@update short
            val updated = script.scenes.map {
                if (it.id == sceneId) it.copy(endSeconds = it.startSeconds + seconds.coerceIn(2, 20)) else it
            }
            short.copy(script = script.copy(scenes = resequence(updated), isEditedByHuman = true))
        }
    }

    fun deleteScene(shortId: String, sceneId: String) {
        update(shortId) { short ->
            val script = short.script ?: return@update short
            if (script.scenes.size <= 1) return@update short
            short.copy(
                script = script.copy(
                    scenes = resequence(script.scenes.filterNot { it.id == sceneId }),
                    isEditedByHuman = true
                )
            )
        }
    }

    fun addScene(shortId: String) {
        update(shortId) { short ->
            val script = short.script ?: return@update short
            val last = script.scenes.lastOrNull()
            val newScene = AIScene(
                id = nextId("scene_"),
                order = script.scenes.size,
                text = "",
                startSeconds = last?.endSeconds ?: 0,
                endSeconds = (last?.endSeconds ?: 0) + 5,
                caption = ""
            )
            short.copy(script = script.copy(scenes = resequence(script.scenes + newScene), isEditedByHuman = true))
        }
    }

    /** Keeps scene indexes and the timeline contiguous after an edit. */
    private fun resequence(scenes: List<AIScene>): List<AIScene> {
        var cursor = 0
        return scenes.mapIndexed { index, scene ->
            val duration = scene.durationSeconds
            val updated = scene.copy(order = index, startSeconds = cursor, endSeconds = cursor + duration)
            cursor += duration
            updated
        }
    }

    // ------------------------------------------------------------- choices
    fun selectTemplate(shortId: String, templateId: String) =
        update(shortId) { it.copy(templateId = templateId) }

    fun selectLanguage(shortId: String, language: ShortLanguage) =
        update(shortId) { short ->
            // Dropping a voice that does not speak the new language avoids a mismatch.
            val voiceStillValid = voiceById(short.voiceId)?.language == language
            short.copy(language = language, voiceId = if (voiceStillValid) short.voiceId else null)
        }

    fun selectVoice(shortId: String, voiceId: String, style: VoiceStyle) =
        update(shortId) { it.copy(voiceId = voiceId, voiceStyle = style) }

    fun setDuration(shortId: String, seconds: Int) =
        update(shortId) { it.copy(durationSeconds = seconds) }

    fun assignMedia(shortId: String, sceneId: String, mediaId: String) {
        update(shortId) { short ->
            val script = short.script ?: return@update short
            val asset = short.media.firstOrNull { it.id == mediaId }
            short.copy(
                script = script.copy(
                    scenes = script.scenes.map { scene ->
                        if (scene.id != sceneId) scene else {
                            val clip = if (asset?.type == MediaType.VIDEO && asset.durationMs > 0) {
                                MediaIntelligence.suggestClip(asset.durationMs, scene.durationSeconds)
                            } else 0 to 0
                            scene.copy(
                                mediaId = mediaId,
                                mediaType = asset?.type,
                                clipStartMs = clip.first,
                                clipEndMs = clip.second
                            )
                        }
                    }
                )
            )
        }
    }

    /**
     * Sets the clip window for a scene's video. Metadata only - the source asset
     * is never trimmed, re-encoded or overwritten.
     */
    fun setClipWindow(shortId: String, sceneId: String, startMs: Int, endMs: Int) {
        update(shortId) { short ->
            val script = short.script ?: return@update short
            short.copy(
                script = script.copy(
                    scenes = script.scenes.map {
                        if (it.id == sceneId) {
                            it.copy(
                                clipStartMs = startMs.coerceAtLeast(0),
                                clipEndMs = endMs.coerceAtLeast(startMs + 1000)
                            )
                        } else it
                    }
                )
            )
        }
    }

    fun mediaFor(shortId: String, mediaId: String?): VideoMedia? =
        shortById(shortId)?.media?.firstOrNull { it.id == mediaId }

    suspend fun uploadMedia(shortId: String, label: String): ServiceResult<VideoMedia> {
        return when (val result = mediaService.uploadMedia(label)) {
            is ServiceResult.Success -> {
                update(shortId) { it.copy(media = it.media + result.value) }
                result
            }
            is ServiceResult.Failure -> result
        }
    }

    fun previewVoiceClip(voice: VoiceOption, style: VoiceStyle): String =
        ttsService.previewClipFor(voice, style)

    // ---------------------------------------------------------- generation
    /**
     * Runs the pipeline: voice, media, render. Progress is written onto the short
     * so any screen observing it can show the stage and percentage.
     */
    suspend fun startGeneration(shortId: String): ServiceResult<Unit> {
        val short = shortById(shortId) ?: return ServiceResult.Failure("NOT_FOUND", "Draft not found.")
        val script = short.script
            ?: return ServiceResult.Failure("NO_SCRIPT", "Generate the script first.")
        val voice = voiceById(short.voiceId)
            ?: return ServiceResult.Failure("NO_VOICE", "Choose a voice first.")

        val job = VideoGenerationJob(
            jobId = nextId("job_"),
            newsId = short.newsId,
            aiShortId = short.id,
            status = AIShortStatus.PREPARING_MEDIA,
            progress = 0,
            stage = JobStage.QUEUED,
            createdAt = now(),
            startedAt = now()
        )
        update(shortId) { it.copy(status = AIShortStatus.PREPARING_MEDIA, job = job, errorMessage = null) }

        // 1. voice
        update(shortId) {
            it.copy(
                status = AIShortStatus.VOICE_GENERATING,
                job = it.job?.copy(stage = JobStage.VOICE, progress = 10, status = AIShortStatus.VOICE_GENERATING)
            )
        }
        when (val tts = ttsService.synthesize(script, voice, short.voiceStyle)) {
            is ServiceResult.Failure -> {
                failJob(shortId, tts.code, tts.message)
                return tts
            }
            is ServiceResult.Success -> Unit
        }

        // 2. media
        update(shortId) {
            it.copy(
                status = AIShortStatus.PREPARING_MEDIA,
                job = it.job?.copy(stage = JobStage.MEDIA, progress = 25, status = AIShortStatus.PREPARING_MEDIA)
            )
        }

        // 3. queued for the renderer
        update(shortId) {
            it.copy(
                status = AIShortStatus.RENDER_QUEUED,
                job = it.job?.copy(progress = 30, status = AIShortStatus.RENDER_QUEUED)
            )
        }

        // 4. render
        update(shortId) {
            it.copy(status = AIShortStatus.RENDERING, job = it.job?.copy(stage = JobStage.RENDER, status = AIShortStatus.RENDERING))
        }
        val rendered = renderService.render(shortId) { percent ->
            update(shortId) { it.copy(job = it.job?.copy(progress = percent)) }
        }

        return when (rendered) {
            is ServiceResult.Success -> {
                update(shortId) {
                    it.copy(
                        status = AIShortStatus.READY,
                        videoUrl = rendered.value.videoUrl,
                        thumbnailUrl = rendered.value.thumbnailUrl,
                        job = it.job?.copy(
                            status = AIShortStatus.READY,
                            stage = JobStage.DONE,
                            progress = 100,
                            completedAt = now()
                        ),
                        cost = it.cost.copy(estimatedCost = it.cost.estimatedCost + 0.30)
                    )
                }
                ServiceResult.Success(Unit)
            }
            is ServiceResult.Failure -> {
                failJob(shortId, rendered.code, rendered.message)
                rendered
            }
        }
    }

    private fun failJob(shortId: String, code: String, message: String) {
        update(shortId) {
            it.copy(
                status = AIShortStatus.FAILED,
                errorMessage = message,
                job = it.job?.copy(
                    status = AIShortStatus.FAILED,
                    errorCode = code,
                    errorMessage = message,
                    completedAt = now()
                )
            )
        }
    }

    // ------------------------------------------------------------ decisions
    fun approve(shortId: String) =
        update(shortId) { it.copy(status = AIShortStatus.APPROVED, approvedAt = now()) }

    /**
     * Marks the short published and links it to the article. The article's own
     * publishing flow is untouched - this only attaches the video.
     */
    fun publish(shortId: String) =
        update(shortId) { it.copy(status = AIShortStatus.PUBLISHED, publishedAt = now()) }

    fun unpublish(shortId: String) =
        update(shortId) { it.copy(status = AIShortStatus.APPROVED, publishedAt = null) }

    fun regenerate(shortId: String, target: RegenerateTarget) {
        update(shortId) { short ->
            val bumped = short.cost.copy(
                regenerationCount = short.cost.regenerationCount + 1,
                estimatedCost = short.cost.estimatedCost + 0.12
            )
            // Only throw away what actually has to be redone.
            when (target) {
                // Script changes invalidate everything downstream.
                RegenerateTarget.SCRIPT -> short.copy(
                    status = AIShortStatus.DRAFT,
                    script = null,
                    videoUrl = "",
                    job = null,
                    cost = bumped
                )
                // Voice change: script, template and media all survive.
                RegenerateTarget.VOICE -> short.copy(
                    status = AIShortStatus.SCRIPT_READY,
                    videoUrl = "",
                    cost = bumped
                )
                // Template change: nothing but the render is invalidated.
                RegenerateTarget.TEMPLATE -> short.copy(
                    status = AIShortStatus.SCRIPT_READY,
                    videoUrl = "",
                    cost = bumped
                )
                // Media change: script and voice survive.
                RegenerateTarget.MEDIA -> short.copy(
                    status = AIShortStatus.SCRIPT_READY,
                    videoUrl = "",
                    cost = bumped
                )
                RegenerateTarget.FULL_VIDEO -> short.copy(
                    status = AIShortStatus.SCRIPT_READY,
                    videoUrl = "",
                    job = null,
                    cost = bumped
                )
            }
        }
    }

    fun deleteShort(shortId: String) {
        _shorts.update { list -> list.filterNot { it.id == shortId } }
    }

    // ------------------------------------------------- template management
    fun setTemplateActive(templateId: String, active: Boolean) {
        _templates.update { list ->
            list.map { if (it.id == templateId) it.copy(isActive = active) else it }
        }
    }

    fun setDefaultTemplate(templateId: String) {
        _templates.update { list ->
            list.map { it.copy(isDefault = it.id == templateId) }
        }
    }
}
