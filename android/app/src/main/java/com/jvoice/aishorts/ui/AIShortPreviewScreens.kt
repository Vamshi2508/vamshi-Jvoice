package com.jvoice.aishorts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.jvoice.aishorts.data.model.AIShortStatus
import com.jvoice.aishorts.data.model.JobStage
import com.jvoice.aishorts.data.model.RegenerateTarget
import com.jvoice.aishorts.data.repository.AIShortRepository
import com.jvoice.aishorts.domain.service.TemplateEngine
import com.jvoice.aishorts.navigation.AIShortRoutes
import com.jvoice.news.components.ConfirmDialog
import com.jvoice.news.components.EmptyState
import com.jvoice.news.components.Pill
import com.jvoice.news.components.SectionHeader
import com.jvoice.news.data.model.UserRole
import com.jvoice.news.navigation.AdminScaffold
import kotlinx.coroutines.launch
import com.jvoice.core.i18n.current

/* ============================================================ generation run */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerationProgressScreen(
    viewModel: AIShortViewModel,
    shortId: String,
    onFinished: () -> Unit,
    onBack: () -> Unit
) {
    val all by AIShortRepository.shorts.collectAsState()
    val short = remember(shortId, all) { viewModel.shortById(shortId) }
    var started by remember { mutableStateOf(false) }

    LaunchedEffect(shortId) {
        if (!started) {
            started = true
            viewModel.startGeneration(shortId)
        }
    }

    // Hand over to the preview as soon as the render lands.
    LaunchedEffect(short?.status) {
        if (short?.status == AIShortStatus.READY) onFinished()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generating AI Short") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (short == null) {
            EmptyState("Draft not found", modifier = Modifier.padding(padding))
            return@Scaffold
        }

        val job = short.job
        val percent = job?.progress ?: 0
        val stage = job?.stage ?: JobStage.QUEUED

        val done = buildList {
            add("Script generated")
            if (stage.ordinal >= JobStage.MEDIA.ordinal) add("Voice prepared")
            if (stage.ordinal >= JobStage.RENDER.ordinal) add("Media prepared")
        }
        val currentLabel = when (stage) {
            JobStage.QUEUED -> "Queued"
            JobStage.SCRIPT -> "Preparing script"
            JobStage.VOICE -> "Preparing voice"
            JobStage.MEDIA -> "Preparing media"
            JobStage.RENDER -> "Rendering video"
            JobStage.DONE -> "Finished"
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Text(
                short.newsHeadline.current(),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(20.dp))

            if (short.status == AIShortStatus.FAILED) {
                ShortErrorCard(
                    title = "Video rendering failed",
                    message = short.errorMessage ?: "Something went wrong while rendering.",
                    primaryLabel = "Retry rendering",
                    onPrimary = {
                        viewModel.regenerate(shortId, RegenerateTarget.FULL_VIDEO)
                        viewModel.startGeneration(shortId)
                    },
                    secondaryLabel = "Change template",
                    onSecondary = onBack
                )
            } else {
                GenerationProgress(
                    percent = percent,
                    stageLabel = currentLabel,
                    doneStages = done
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    "Please wait - this runs in the background. Generation is simulated locally in this build.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/* ================================================================== preview */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIShortPreviewScreen(
    viewModel: AIShortViewModel,
    shortId: String,
    canApprove: Boolean,
    canPublish: Boolean,
    onEditStep: (String) -> Unit,
    onRegenerateScript: () -> Unit,
    onBack: () -> Unit,
    routeForScript: (String) -> String,
    routeForTemplate: (String) -> String,
    routeForVoice: (String) -> String,
    routeForMedia: (String) -> String
) {
    val all by AIShortRepository.shorts.collectAsState()
    val short = remember(shortId, all) { viewModel.shortById(shortId) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showApprove by remember { mutableStateOf(false) }
    var showRegenerate by remember { mutableStateOf(false) }
    var regenTarget by remember { mutableStateOf(RegenerateTarget.SCRIPT) }

    if (showApprove && short != null) {
        ConfirmDialog(
            title = "Approve AI Short?",
            message = "This video will be available for publishing with the news article. " +
                "The article itself is not changed.",
            confirmLabel = "Approve",
            onConfirm = {
                viewModel.approve(shortId)
                showApprove = false
                scope.launch { snackbarHostState.showSnackbar("Approved") }
            },
            onDismiss = { showApprove = false }
        )
    }

    if (showRegenerate && short != null) {
        AlertDialog(
            onDismissRequest = { showRegenerate = false },
            title = { Text("Regenerate") },
            text = {
                Column {
                    RegenerateTarget.entries.forEach { option ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { regenTarget = option }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = regenTarget == option,
                                onClick = { regenTarget = option }
                            )
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text(option.label, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    option.hint,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showRegenerate = false
                    viewModel.regenerate(shortId, regenTarget)
                    when (regenTarget) {
                        RegenerateTarget.SCRIPT -> onRegenerateScript()
                        RegenerateTarget.TEMPLATE -> onEditStep(routeForTemplate(shortId))
                        RegenerateTarget.VOICE -> onEditStep(routeForVoice(shortId))
                        RegenerateTarget.MEDIA -> onEditStep(routeForMedia(shortId))
                        RegenerateTarget.FULL_VIDEO -> onEditStep(AIShortRoutes.progress(shortId))
                    }
                }) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = { showRegenerate = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Short Preview") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Sharing opens once a real video file exists")
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (short == null) {
            EmptyState("Short not found", modifier = Modifier.padding(padding))
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                VerticalVideoFrame(thumbnailUrl = short.thumbnailUrl) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.Black.copy(alpha = 0.55f)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(14.dp)
                                    .size(36.dp)
                            )
                        }
                    }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    short.durationSeconds.toString() + " sec  •  " + short.language.label + "  •  " +
                        (viewModel.templateById(short.templateId)?.name ?: "-"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(10.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShortStatusPill(short.status)
                if (short.status == AIShortStatus.PUBLISHED) {
                    Spacer(Modifier.width(8.dp))
                    Pill("Video attached", MaterialTheme.colorScheme.tertiary)
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                "Playback is simulated - no video file is produced in this build.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            )

            Spacer(Modifier.height(14.dp))
            Text(
                short.newsHeadline.current(),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            SectionHeader("Scenes")
            short.script?.scenes?.forEach { scene ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Text(
                        scene.timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.width(74.dp)
                    )
                    Text(scene.text, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            Column(Modifier.padding(horizontal = 20.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { onEditStep(routeForScript(shortId)) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Edit") }
                    OutlinedButton(
                        onClick = { showRegenerate = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Regenerate")
                    }
                }
                Spacer(Modifier.height(10.dp))

                when {
                    short.status == AIShortStatus.READY && canApprove -> {
                        Button(
                            onClick = { showApprove = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Approve Video")
                        }
                    }
                    short.status == AIShortStatus.APPROVED && canPublish -> {
                        Button(
                            onClick = {
                                viewModel.publish(shortId)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Published and attached to the article")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Publish with the article") }
                    }
                    short.status == AIShortStatus.APPROVED -> {
                        Text(
                            "Approved. A News Admin can publish it with the article.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    short.status == AIShortStatus.PUBLISHED && canPublish -> {
                        OutlinedButton(
                            onClick = {
                                viewModel.unpublish(shortId)
                                scope.launch { snackbarHostState.showSnackbar("Removed from the article") }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Unpublish video") }
                    }
                }
            }

            // What a real renderer would receive: the template's layout with every
            // token resolved from this short.
            val template = viewModel.templateById(short.templateId)
            if (template != null && template.layout.isNotEmpty()) {
                SectionHeader("Render plan", subtitle = template.name + " with tokens resolved")
                val values = TemplateEngine.bind(
                    short = short,
                    categoryName = viewModel.categoryName(short.categoryId),
                    voiceName = viewModel.voiceById(short.voiceId)?.name ?: "-",
                    mediaUrlFor = { id -> short.media.firstOrNull { it.id == id }?.label ?: "no media" }
                )
                Surface(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        TemplateEngine.renderPlan(template, values).forEach { line ->
                            Text(
                                line,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("Performance", subtitle = "Prepared for future tracking")
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiniMetric("Views", short.analytics.views.toString(), Modifier.weight(1f))
                MiniMetric("Completion", short.analytics.completionRate.toString() + "%", Modifier.weight(1f))
                MiniMetric("Shares", short.analytics.shares.toString(), Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Generations: " + short.cost.generationCount +
                    "  •  Regenerations: " + short.cost.regenerationCount +
                    "  •  Estimated cost: $" + String.format("%.2f", short.cost.estimatedCost),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MiniMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleSmall)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/* ======================================================== template admin */

@Composable
fun TemplateManagementScreen(
    viewModel: AIShortViewModel,
    role: UserRole,
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val templates by viewModel.templates.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    AdminScaffold(
        role = role,
        title = "Video Templates",
        currentRoute = AIShortRoutes.TEMPLATE_ADMIN,
        onNavigate = onNavigate,
        onSignOut = onSignOut,
        snackbarHostState = snackbarHostState
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                SectionHeader(
                    templates.size.toString() + " templates",
                    subtitle = "Local mock templates for this phase"
                )
            }
            items(templates, key = { it.id }) { template ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        ShortThumbnail(
                            url = template.thumbnailUrl,
                            modifier = Modifier.size(width = 48.dp, height = 76.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(template.name, style = MaterialTheme.typography.titleSmall)
                                if (template.isDefault) {
                                    Spacer(Modifier.width(6.dp))
                                    Pill("Default", MaterialTheme.colorScheme.secondary)
                                }
                            }
                            Text(
                                template.category.label + "  •  " + template.sceneCount + " scenes",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!template.isDefault) {
                                TextButton(onClick = {
                                    viewModel.setDefaultTemplate(template.id)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(template.name + " is now the default")
                                    }
                                }) { Text("Set default") }
                            }
                        }
                        Switch(
                            checked = template.isActive,
                            onCheckedChange = { viewModel.setTemplateActive(template.id, it) }
                        )
                    }
                }
            }
        }
    }
}
