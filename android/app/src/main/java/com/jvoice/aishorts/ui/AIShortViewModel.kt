package com.jvoice.aishorts.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvoice.aishorts.data.model.AIShort
import com.jvoice.aishorts.data.model.RegenerateTarget
import com.jvoice.aishorts.data.model.ShortLanguage
import com.jvoice.aishorts.data.model.ShortsFilter
import com.jvoice.aishorts.data.model.VideoTemplate
import com.jvoice.aishorts.data.model.VoiceStyle
import com.jvoice.aishorts.data.repository.AIShortRepository
import com.jvoice.aishorts.domain.service.ServiceResult
import com.jvoice.news.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * One ViewModel for the whole AI Shorts area. The wizard's state lives in the
 * repository, not here, so moving between steps never loses progress.
 */
class AIShortViewModel : ViewModel() {

    private val _filter = MutableStateFlow(ShortsFilter.ALL)
    val filter: StateFlow<ShortsFilter> = _filter.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /** Set while a suspending service call is in flight. */
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    val shorts: StateFlow<List<AIShort>> = combine(
        AIShortRepository.shorts,
        _filter,
        _query
    ) { _, filter, query ->
        AIShortRepository.filtered(filter, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val templates: StateFlow<List<VideoTemplate>> = AIShortRepository.templates

    fun setFilter(value: ShortsFilter) { _filter.value = value }
    fun setQuery(value: String) { _query.value = value }
    fun clearError() { _lastError.value = null }

    fun countFor(filter: ShortsFilter) = AIShortRepository.countFor(filter)

    // ------------------------------------------------------------- lookups
    fun shortById(id: String) = AIShortRepository.shortById(id)
    fun articleById(id: String) = NewsRepository.articleById(id)
    fun categoryName(id: String) = NewsRepository.categoryName(id)
    fun templateById(id: String?) = AIShortRepository.templateById(id)
    fun voiceById(id: String?) = AIShortRepository.voiceById(id)
    fun activeTemplates() = AIShortRepository.activeTemplates()
    fun voicesFor(language: ShortLanguage) = AIShortRepository.voicesFor(language)
    fun shortsForArticle(newsId: String) = AIShortRepository.shortsForArticle(newsId)

    // --------------------------------------------------------------- flow
    /** Entry point from the Editor's article screen. */
    fun createDraftFor(newsId: String, createdBy: String, forceNew: Boolean = false): AIShort? {
        val article = NewsRepository.articleById(newsId) ?: return null
        return AIShortRepository.createDraft(article, createdBy, forceNew)
    }

    fun generateScript(shortId: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _busy.value = true
            val result = AIShortRepository.generateScript(shortId)
            _busy.value = false
            when (result) {
                is ServiceResult.Success -> onDone(true)
                is ServiceResult.Failure -> {
                    _lastError.value = result.message
                    onDone(false)
                }
            }
        }
    }

    fun updateSceneText(shortId: String, sceneId: String, text: String) =
        AIShortRepository.updateSceneText(shortId, sceneId, text)

    fun updateSceneDuration(shortId: String, sceneId: String, seconds: Int) =
        AIShortRepository.updateSceneDuration(shortId, sceneId, seconds)

    fun deleteScene(shortId: String, sceneId: String) = AIShortRepository.deleteScene(shortId, sceneId)
    fun addScene(shortId: String) = AIShortRepository.addScene(shortId)

    fun selectTemplate(shortId: String, templateId: String) =
        AIShortRepository.selectTemplate(shortId, templateId)

    fun selectLanguage(shortId: String, language: ShortLanguage) =
        AIShortRepository.selectLanguage(shortId, language)

    fun selectVoice(shortId: String, voiceId: String, style: VoiceStyle) =
        AIShortRepository.selectVoice(shortId, voiceId, style)

    fun setDuration(shortId: String, seconds: Int) = AIShortRepository.setDuration(shortId, seconds)

    fun assignMedia(shortId: String, sceneId: String, mediaId: String) =
        AIShortRepository.assignMedia(shortId, sceneId, mediaId)

    fun setClipWindow(shortId: String, sceneId: String, startMs: Int, endMs: Int) =
        AIShortRepository.setClipWindow(shortId, sceneId, startMs, endMs)

    fun mediaFor(shortId: String, mediaId: String?) =
        AIShortRepository.mediaFor(shortId, mediaId)

    fun resuggestMedia(shortId: String) = AIShortRepository.resuggestMedia(shortId)

    fun recommendedTemplateFor(newsId: String) =
        AIShortRepository.recommendedTemplateFor(newsId)

    fun recommendationReasonFor(newsId: String) =
        AIShortRepository.recommendationReasonFor(newsId)

    fun uploadMedia(shortId: String, label: String) {
        viewModelScope.launch {
            _busy.value = true
            val result = AIShortRepository.uploadMedia(shortId, label)
            _busy.value = false
            if (result is ServiceResult.Failure) _lastError.value = result.message
        }
    }

    fun previewVoice(voiceId: String, style: VoiceStyle): String? {
        val voice = AIShortRepository.voiceById(voiceId) ?: return null
        return AIShortRepository.previewVoiceClip(voice, style)
    }

    fun startGeneration(shortId: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val result = AIShortRepository.startGeneration(shortId)
            when (result) {
                is ServiceResult.Success -> onDone(true)
                is ServiceResult.Failure -> {
                    _lastError.value = result.message
                    onDone(false)
                }
            }
        }
    }

    fun approve(shortId: String) = AIShortRepository.approve(shortId)
    fun publish(shortId: String) = AIShortRepository.publish(shortId)
    fun unpublish(shortId: String) = AIShortRepository.unpublish(shortId)
    fun regenerate(shortId: String, target: RegenerateTarget) =
        AIShortRepository.regenerate(shortId, target)
    fun deleteShort(shortId: String) = AIShortRepository.deleteShort(shortId)

    // ------------------------------------------------ template management
    fun setTemplateActive(id: String, active: Boolean) =
        AIShortRepository.setTemplateActive(id, active)

    fun setDefaultTemplate(id: String) = AIShortRepository.setDefaultTemplate(id)
}
