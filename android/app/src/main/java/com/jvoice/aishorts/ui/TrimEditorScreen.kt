package com.jvoice.aishorts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.jvoice.aishorts.data.model.MediaType
import com.jvoice.aishorts.data.repository.AIShortRepository
import com.jvoice.news.components.EmptyState
import com.jvoice.news.components.Pill
import com.jvoice.news.components.SectionHeader
import kotlinx.coroutines.launch

/**
 * Clip picker for a scene's video.
 *
 * Only a start/end window is stored. The source video is never trimmed, copied
 * or re-encoded - the short holds a reference plus the window, so the original
 * asset on the article stays exactly as the Editor supplied it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrimEditorScreen(
    viewModel: AIShortViewModel,
    shortId: String,
    sceneId: String,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val all by AIShortRepository.shorts.collectAsState()
    val short = remember(shortId, all) { viewModel.shortById(shortId) }
    val scene = short?.script?.scenes?.firstOrNull { it.id == sceneId }
    val media = short?.media?.firstOrNull { it.id == scene?.mediaId }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val sourceMs = (media?.durationMs ?: 0).coerceAtLeast(1)
    var range by remember(sceneId, media?.id) {
        val start = scene?.clipStartMs?.toFloat() ?: 0f
        val end = if ((scene?.clipEndMs ?: 0) > 0) scene!!.clipEndMs.toFloat()
        else minOf(sourceMs.toFloat(), (scene?.durationSeconds ?: 5) * 1000f)
        mutableStateOf(start..end)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trim clip") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.setClipWindow(shortId, sceneId, 0, 0)
                        scope.launch { snackbarHostState.showSnackbar("Using the full asset") }
                        onDone()
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Use full clip") }
                Button(
                    onClick = {
                        viewModel.setClipWindow(
                            shortId, sceneId,
                            range.start.toInt(), range.endInclusive.toInt()
                        )
                        onDone()
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Save clip") }
            }
        }
    ) { padding ->
        if (short == null || scene == null) {
            EmptyState("Scene not found", modifier = Modifier.padding(padding))
            return@Scaffold
        }
        if (media == null || media.type != MediaType.VIDEO) {
            EmptyState(
                title = "This scene has no video",
                description = "Trimming applies to video assets. Assign a video to this scene first.",
                modifier = Modifier.padding(padding),
                actionLabel = "Back",
                onAction = onBack
            )
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                VerticalVideoFrame(thumbnailUrl = media.displayUrl) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(shape = RoundedCornerShape(50), color = Color.Black.copy(alpha = 0.55f)) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Preview clip",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .size(30.dp)
                            )
                        }
                    }
                }
            }

            Text(
                media.label + "  •  source " + msLabel(sourceMs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            SectionHeader("Selected clip", subtitle = "Scene " + (scene.order + 1) + " needs " + scene.durationSeconds + " sec")

            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Start", style = MaterialTheme.typography.labelSmall)
                            Text(msLabel(range.start.toInt()), style = MaterialTheme.typography.titleMedium)
                        }
                        Column(Modifier.weight(1f)) {
                            Text("End", style = MaterialTheme.typography.labelSmall)
                            Text(msLabel(range.endInclusive.toInt()), style = MaterialTheme.typography.titleMedium)
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Length", style = MaterialTheme.typography.labelSmall)
                            Text(
                                ((range.endInclusive - range.start).toInt() / 1000).toString() + " sec",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    RangeSlider(
                        value = range,
                        onValueChange = { range = it },
                        valueRange = 0f..sourceMs.toFloat()
                    )

                    Spacer(Modifier.height(4.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("00:00", style = MaterialTheme.typography.labelSmall)
                        Text(msLabel(sourceMs), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Pill("Original never modified", MaterialTheme.colorScheme.tertiary)
                Pill("Metadata only", MaterialTheme.colorScheme.outline)
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "J Voice stores the source id plus the start and end time. The video file on the " +
                    "article is left untouched.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

private fun msLabel(ms: Int): String {
    val total = ms / 1000
    val m = total / 60
    val s = total % 60
    return (if (m < 10) "0" else "") + m + ":" + (if (s < 10) "0" else "") + s
}
