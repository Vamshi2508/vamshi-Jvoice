package com.jvoice.aishorts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jvoice.aishorts.data.model.AIShort
import com.jvoice.aishorts.data.model.AIShortStatus
import com.jvoice.aishorts.data.model.MediaType
import com.jvoice.aishorts.data.model.ShortLanguage
import com.jvoice.aishorts.data.model.VoiceStyle
import com.jvoice.aishorts.data.repository.AIShortRepository
import com.jvoice.news.components.EmptyState
import com.jvoice.news.components.LoadingState
import com.jvoice.news.components.Pill
import com.jvoice.news.components.SectionHeader
import kotlinx.coroutines.launch
import com.jvoice.core.i18n.Places
import com.jvoice.core.i18n.current
import com.jvoice.core.i18n.currentLanguage

/* ==================================================================== shared */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WizardScaffold(
    title: String,
    step: String,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            step,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = bottomBar,
        content = content
    )
}

@Composable
private fun shortOrNull(viewModel: AIShortViewModel, shortId: String): AIShort? {
    val all by AIShortRepository.shorts.collectAsState()
    return remember(shortId, all) { viewModel.shortById(shortId) }
}

/* ============================================================ 1. create/open */

/**
 * Entry point from the Editor's article screen. Shows the source article, creates
 * (or reopens) the draft, and lists what still needs doing.
 */
@Composable
fun CreateAIShortScreen(
    viewModel: AIShortViewModel,
    newsId: String,
    createdBy: String,
    onOpenStep: (route: String) -> Unit,
    onBack: () -> Unit,
    routeForScript: (String) -> String,
    routeForTemplate: (String) -> String,
    routeForVoice: (String) -> String,
    routeForMedia: (String) -> String,
    routeForReview: (String) -> String
) {
    val article = remember(newsId) { viewModel.articleById(newsId) }
    var shortId by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(newsId) {
        shortId = viewModel.createDraftFor(newsId, createdBy)?.id
    }

    val short = shortId?.let { shortOrNull(viewModel, it) }

    WizardScaffold(
        title = "Create AI Short",
        step = "From an existing news article",
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        bottomBar = {
            if (short != null) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                        Text("Save Draft")
                    }
                    Button(
                        onClick = {
                            onOpenStep(
                                if (short.hasScript) routeForReview(short.id) else routeForScript(short.id)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (short.hasScript) "Continue" else "Generate script")
                    }
                }
            }
        }
    ) { padding ->
        if (article == null) {
            EmptyState(
                title = "Article not found",
                description = "This story is no longer in the local data.",
                modifier = Modifier.padding(padding),
                actionLabel = "Back",
                onAction = onBack
            )
            return@WizardScaffold
        }
        if (short == null) {
            LoadingState(Modifier.padding(padding), "Preparing the draft...")
            return@WizardScaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Source article",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(article.headline.current(), style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Pill(viewModel.categoryName(article.categoryId), MaterialTheme.colorScheme.primary)
                            Pill(Places.render(article.location, currentLanguage()), MaterialTheme.colorScheme.outline)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            article.shortDescription.current(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "The article is the source of truth. The script may only summarise it.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // One article can carry several cuts - Breaking, Local, and so on.
            val siblings = viewModel.shortsForArticle(newsId).filter { it.id != short.id }
            if (siblings.isNotEmpty()) {
                item {
                    SectionHeader(
                        "Other shorts for this story",
                        subtitle = siblings.size.toString() + " already exist"
                    )
                }
                items(siblings, key = { it.id }) { sibling ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpenStep(routeForReview(sibling.id)) }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                viewModel.templateById(sibling.templateId)?.name ?: "No template",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                sibling.language.label + "  •  " + sibling.durationSeconds + " sec",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        ShortStatusPill(sibling.status)
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { shortId = viewModel.createDraftFor(newsId, createdBy, forceNew = true)?.id },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Start another cut of this story")
                }
            }

            item { SectionHeader("Setup", subtitle = "You can leave and come back - progress is kept") }
            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    SetupStepRow(
                        1, "Script", if (short.hasScript) short.sceneCount.toString() + " scenes" else "Not generated",
                        short.hasScript,
                        Modifier.clickable { onOpenStep(routeForScript(short.id)) }
                    )
                    SetupStepRow(
                        2, "Template",
                        viewModel.templateById(short.templateId)?.name ?: "Not chosen",
                        short.hasTemplate,
                        Modifier.clickable { onOpenStep(routeForTemplate(short.id)) }
                    )
                    SetupStepRow(
                        3, "Voice",
                        viewModel.voiceById(short.voiceId)?.name ?: "Not chosen",
                        short.hasVoice,
                        Modifier.clickable { onOpenStep(routeForVoice(short.id)) }
                    )
                    SetupStepRow(
                        4, "Media",
                        if (short.hasMedia) short.mediaCount.toString() + " assets assigned" else "Not assigned",
                        short.hasMedia,
                        Modifier.clickable { onOpenStep(routeForMedia(short.id)) }
                    )
                }
            }
        }
    }
}

/* ================================================================ 2. script */

@Composable
fun AIScriptScreen(
    viewModel: AIShortViewModel,
    shortId: String,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val short = shortOrNull(viewModel, shortId)
    val busy by viewModel.busy.collectAsState()
    val error by viewModel.lastError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Generate on first open when there is nothing yet.
    LaunchedEffect(shortId) {
        val current = viewModel.shortById(shortId)
        if (current != null && !current.hasScript) viewModel.generateScript(shortId)
    }

    WizardScaffold(
        title = "AI Generated Script",
        step = "Step 1 of 4  •  editable before anything is rendered",
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.generateScript(shortId) { ok ->
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (ok) "Script regenerated" else "Could not regenerate the script"
                                )
                            }
                        }
                    },
                    enabled = !busy,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Regenerate")
                }
                Button(
                    onClick = onContinue,
                    enabled = short?.hasScript == true && !busy,
                    modifier = Modifier.weight(1f)
                ) { Text("Continue") }
            }
        }
    ) { padding ->
        if (short == null) {
            EmptyState("Draft not found", modifier = Modifier.padding(padding))
            return@WizardScaffold
        }
        if (busy && !short.hasScript) {
            LoadingState(Modifier.padding(padding), "Summarising the article...")
            return@WizardScaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (error != null && !short.hasScript) {
                item {
                    ShortErrorCard(
                        title = "Unable to generate script",
                        message = error ?: "",
                        primaryLabel = "Retry",
                        onPrimary = { viewModel.generateScript(shortId) },
                        secondaryLabel = "Add scene manually",
                        onSecondary = {
                            viewModel.addScene(shortId)
                            viewModel.clearError()
                        }
                    )
                }
            }

            item {
                Surface(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Every line is taken from the article. Nothing is invented - edit freely before continuing.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            val scenes = short.script?.scenes.orEmpty()
            items(scenes, key = { it.id }) { scene ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Scene " + (scene.order + 1),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(Modifier.width(8.dp))
                            Pill(scene.timeLabel, MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.weight(1f))
                            IconButton(
                                onClick = { viewModel.deleteScene(shortId, scene.id) },
                                enabled = scenes.size > 1
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete scene",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = scene.text,
                            onValueChange = { viewModel.updateSceneText(shortId, scene.id, it) },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Duration",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = {
                                viewModel.updateSceneDuration(shortId, scene.id, scene.durationSeconds - 1)
                            }) { Text("-") }
                            Text(
                                scene.durationSeconds.toString() + "s",
                                style = MaterialTheme.typography.titleSmall
                            )
                            TextButton(onClick = {
                                viewModel.updateSceneDuration(shortId, scene.id, scene.durationSeconds + 1)
                            }) { Text("+") }
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { viewModel.addScene(shortId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add scene")
                }
            }

            item {
                Text(
                    "Total " + (short.script?.totalSeconds ?: 0) + " sec across " + scenes.size + " scenes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/* ============================================================== 3. template */

@Composable
fun TemplateSelectionScreen(
    viewModel: AIShortViewModel,
    shortId: String,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val short = shortOrNull(viewModel, shortId)
    val templates by viewModel.templates.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    WizardScaffold(
        title = "Choose Template",
        step = "Step 2 of 4",
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        bottomBar = {
            Button(
                onClick = onContinue,
                enabled = short?.hasTemplate == true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) { Text("Continue") }
        }
    ) { padding ->
        if (short == null) {
            EmptyState("Draft not found", modifier = Modifier.padding(padding))
            return@WizardScaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { SectionHeader("Language", subtitle = "Drives the voice options") }
            item {
                Row(
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShortLanguage.entries.forEach { language ->
                        FilterChip(
                            selected = short.language == language,
                            enabled = language.isAvailable,
                            onClick = { viewModel.selectLanguage(shortId, language) },
                            label = {
                                Text(language.label + if (!language.isAvailable) " (soon)" else "")
                            }
                        )
                    }
                }
            }

            item { SectionHeader("Duration", subtitle = "30 seconds is the J Voice default") }
            item {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 30, 45, 60).forEach { seconds ->
                        FilterChip(
                            selected = short.durationSeconds == seconds,
                            onClick = { viewModel.setDuration(shortId, seconds) },
                            label = { Text(seconds.toString() + " sec") }
                        )
                    }
                }
            }

            item {
                val recommended = viewModel.recommendedTemplateFor(short.newsId)
                if (recommended != null) {
                    Surface(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Suggested: " + recommended.name,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    viewModel.recommendationReasonFor(short.newsId) +
                                        " - you can pick any template.",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            TextButton(onClick = { viewModel.selectTemplate(shortId, recommended.id) }) {
                                Text("Use it")
                            }
                        }
                    }
                }
            }

            item { SectionHeader("Template", subtitle = "1080 x 1920, 9:16") }
            items(templates.filter { it.isActive }, key = { it.id }) { template ->
                val selected = short.templateId == template.id
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { viewModel.selectTemplate(shortId, template.id) }
                        .border(
                            width = if (selected) 2.dp else 0.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(14.dp)
                        ),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(Modifier.padding(12.dp)) {
                        ShortThumbnail(
                            url = template.thumbnailUrl,
                            modifier = Modifier.size(width = 58.dp, height = 90.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(template.name, style = MaterialTheme.typography.titleSmall)
                                if (viewModel.recommendedTemplateFor(short.newsId)?.id == template.id) {
                                    Spacer(Modifier.width(6.dp))
                                    Pill("Recommended", MaterialTheme.colorScheme.tertiary)
                                }
                                if (template.isDefault) {
                                    Spacer(Modifier.width(6.dp))
                                    Pill("Default", MaterialTheme.colorScheme.secondary)
                                }
                            }
                            Text(
                                template.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                template.category.label + "  •  " + template.aspectRatio + "  •  " +
                                    template.sceneCount + " scenes",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            if (template.recommendedFor.isNotBlank()) {
                                Text(
                                    "Best for: " + template.recommendedFor,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        if (selected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ================================================================= 4. voice */

@Composable
fun VoiceSelectionScreen(
    viewModel: AIShortViewModel,
    shortId: String,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val short = shortOrNull(viewModel, shortId)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    WizardScaffold(
        title = "Choose Voice",
        step = "Step 3 of 4",
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        bottomBar = {
            Button(
                onClick = onContinue,
                enabled = short?.hasVoice == true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) { Text("Continue") }
        }
    ) { padding ->
        if (short == null) {
            EmptyState("Draft not found", modifier = Modifier.padding(padding))
            return@WizardScaffold
        }

        val voices = viewModel.voicesFor(short.language)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                SectionHeader(
                    short.language.label + " voices",
                    subtitle = "Mock voices - no speech provider is connected"
                )
            }
            items(voices, key = { it.id }) { voice ->
                val selected = short.voiceId == voice.id
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                        .clickable { viewModel.selectVoice(shortId, voice.id, short.voiceStyle) },
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(voice.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                voice.gender.label + "  •  " + voice.language.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(onClick = {
                            viewModel.previewVoice(voice.id, short.voiceStyle)
                            scope.launch {
                                snackbarHostState.showSnackbar("Voice preview is simulated in this build")
                            }
                        }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Preview")
                        }
                        if (selected) {
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item { SectionHeader("Speaking style") }
            item {
                Row(
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VoiceStyle.entries.forEach { style ->
                        FilterChip(
                            selected = short.voiceStyle == style,
                            onClick = {
                                val voiceId = short.voiceId ?: voices.firstOrNull()?.id
                                if (voiceId != null) viewModel.selectVoice(shortId, voiceId, style)
                            },
                            label = { Text(style.label) }
                        )
                    }
                }
            }
            item {
                Text(
                    VoiceStyle.entries.firstOrNull { it == short.voiceStyle }?.hint.orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/* ================================================================= 5. media */

@Composable
fun MediaSelectionScreen(
    viewModel: AIShortViewModel,
    shortId: String,
    onContinue: () -> Unit,
    onTrim: (sceneId: String) -> Unit,
    onBack: () -> Unit
) {
    val short = shortOrNull(viewModel, shortId)
    val busy by viewModel.busy.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var activeSceneId by remember { mutableStateOf<String?>(null) }

    WizardScaffold(
        title = "Scene Media",
        step = "Step 4 of 4",
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        bottomBar = {
            Button(
                onClick = onContinue,
                enabled = short?.hasMedia == true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) { Text("Continue") }
        }
    ) { padding ->
        if (short == null) {
            EmptyState("Draft not found", modifier = Modifier.padding(padding))
            return@WizardScaffold
        }
        val scenes = short.script?.scenes.orEmpty()
        if (scenes.isEmpty()) {
            EmptyState(
                title = "No scenes yet",
                description = "Generate the script first, then assign media to each scene.",
                modifier = Modifier.padding(padding)
            )
            return@WizardScaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                SectionHeader(
                    "Assign a visual to each scene",
                    subtitle = "Article photos first, then anything you upload"
                )
            }

            items(scenes, key = { it.id }) { scene ->
                val assigned = short.media.firstOrNull { it.id == scene.mediaId }
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Scene " + (scene.order + 1) + "  •  " + scene.timeLabel,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    scene.text,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (assigned != null) {
                                ShortThumbnail(
                                    url = assigned.displayUrl,
                                    modifier = Modifier.size(width = 40.dp, height = 62.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        if (short.media.isEmpty()) {
                            Text(
                                "This article has no photos. Upload something to use.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(short.media, key = { it.id }) { media ->
                                    val chosen = scene.mediaId == media.id
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            Modifier
                                                .border(
                                                    width = if (chosen) 2.dp else 0.dp,
                                                    color = if (chosen) MaterialTheme.colorScheme.primary
                                                    else Color.Transparent,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    viewModel.assignMedia(shortId, scene.id, media.id)
                                                }
                                        ) {
                                            ShortThumbnail(
                                                url = media.displayUrl,
                                                modifier = Modifier.size(width = 52.dp, height = 80.dp)
                                            )
                                        }
                                        Text(
                                            if (media.type == MediaType.VIDEO) "Video" else media.source.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        // A video scene can use a slice of the source clip.
                        if (assigned?.type == MediaType.VIDEO) {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Pill(scene.clipLabel, MaterialTheme.colorScheme.secondary)
                                Spacer(Modifier.weight(1f))
                                TextButton(onClick = { onTrim(scene.id) }) { Text("Trim clip") }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = {
                                    activeSceneId = scene.id
                                    viewModel.uploadMedia(shortId, "Scene " + (scene.order + 1) + " upload")
                                },
                                enabled = !busy
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Upload media")
                            }
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = { viewModel.resuggestMedia(shortId) }) {
                                Text("Re-suggest")
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Stock library and AI visuals are not enabled. If AI visuals are added later " +
                        "they will be labelled as illustrative, never presented as footage.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
        }
    }
}

/* ================================================================ 6. review */

@Composable
fun GenerationReviewScreen(
    viewModel: AIShortViewModel,
    shortId: String,
    onGenerate: () -> Unit,
    onOpenStep: (String) -> Unit,
    onBack: () -> Unit,
    routeForScript: (String) -> String,
    routeForTemplate: (String) -> String,
    routeForVoice: (String) -> String,
    routeForMedia: (String) -> String
) {
    val short = shortOrNull(viewModel, shortId)
    val snackbarHostState = remember { SnackbarHostState() }

    WizardScaffold(
        title = "Ready to Generate",
        step = "Check the configuration",
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        bottomBar = {
            Button(
                onClick = onGenerate,
                enabled = short?.setupComplete == true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) { Text("Generate Preview") }
        }
    ) { padding ->
        if (short == null) {
            EmptyState("Draft not found", modifier = Modifier.padding(padding))
            return@WizardScaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(short.newsHeadline.current(), style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(12.dp))
                    SummaryRow("Template", viewModel.templateById(short.templateId)?.name ?: "-") {
                        onOpenStep(routeForTemplate(shortId))
                    }
                    SummaryRow("Language", short.language.label) {
                        onOpenStep(routeForTemplate(shortId))
                    }
                    SummaryRow(
                        "Voice",
                        (viewModel.voiceById(short.voiceId)?.name ?: "-") + "  •  " + short.voiceStyle.label
                    ) { onOpenStep(routeForVoice(shortId)) }
                    SummaryRow("Duration", short.durationSeconds.toString() + " sec") {
                        onOpenStep(routeForTemplate(shortId))
                    }
                    SummaryRow("Scenes", short.sceneCount.toString()) {
                        onOpenStep(routeForScript(shortId))
                    }
                    SummaryRow("Media", short.mediaCount.toString() + " assets") {
                        onOpenStep(routeForMedia(shortId))
                    }
                    SummaryRow("Resolution", "1080 x 1920  •  9:16", null)
                }
            }

            if (!short.setupComplete) {
                ShortErrorCard(
                    title = "Setup is not finished",
                    message = "Complete the script, template, voice and media steps before generating."
                )
            }

            Text(
                "Captions are generated from the approved scene text; the template controls how they look.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, onEdit: (() -> Unit)?) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(96.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        if (onEdit != null) {
            TextButton(onClick = onEdit) { Text("Change") }
        }
    }
}
