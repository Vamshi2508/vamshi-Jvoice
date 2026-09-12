package com.jvoice.aishorts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jvoice.aishorts.data.model.AIShortStatus
import com.jvoice.news.components.Pill

/** Status colour, matching the workflow colours used elsewhere in J Voice. */
@Composable
fun statusColor(status: AIShortStatus): Color = when (status) {
    AIShortStatus.DRAFT -> Color(0xFF6E7078)
    AIShortStatus.SCRIPT_GENERATING,
    AIShortStatus.PREPARING_MEDIA,
    AIShortStatus.VOICE_GENERATING,
    AIShortStatus.RENDER_QUEUED,
    AIShortStatus.RENDERING -> Color(0xFF7B4DFF)
    AIShortStatus.SCRIPT_READY,
    AIShortStatus.WAITING_FOR_REVIEW -> Color(0xFF1565C0)
    AIShortStatus.READY -> Color(0xFFE07B00)
    AIShortStatus.APPROVED -> Color(0xFF1B7F4B)
    AIShortStatus.PUBLISHED -> Color(0xFF00695C)
    AIShortStatus.FAILED -> Color(0xFFC62828)
}

@Composable
fun ShortStatusPill(status: AIShortStatus, modifier: Modifier = Modifier) {
    Pill(status.label, statusColor(status), modifier)
}

/** The 9:16 thumbnail used across the dashboard and preview. */
@Composable
fun ShortThumbnail(url: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (url.isNotBlank()) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/** A step row in the wizard checklist. */
@Composable
fun SetupStepRow(
    number: Int,
    title: String,
    subtitle: String,
    done: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (done) MaterialTheme.colorScheme.tertiary
            else MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                number.toString() + ". " + title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (done) FontWeight.Normal else FontWeight.SemiBold
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Progress block used on the generation screen. */
@Composable
fun GenerationProgress(
    percent: Int,
    stageLabel: String,
    doneStages: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        doneStages.forEach { stage ->
            Row(
                Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(stage, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Row(
            Modifier.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⏳", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.width(8.dp))
            Text(stageLabel, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
        Spacer(Modifier.height(6.dp))
        Text(
            percent.toString() + "%",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** The banner shown when a step fails. Never shows a raw exception. */
@Composable
fun ShortErrorCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    primaryLabel: String? = null,
    onPrimary: (() -> Unit)? = null,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            if (primaryLabel != null || secondaryLabel != null) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (secondaryLabel != null && onSecondary != null) {
                        androidx.compose.material3.OutlinedButton(onClick = onSecondary) {
                            Text(secondaryLabel)
                        }
                    }
                    if (primaryLabel != null && onPrimary != null) {
                        androidx.compose.material3.Button(onClick = onPrimary) {
                            Text(primaryLabel)
                        }
                    }
                }
            }
        }
    }
}

/** The 9:16 aspect helper used by preview surfaces. */
@Composable
fun VerticalVideoFrame(
    thumbnailUrl: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    Box(
        modifier
            .fillMaxWidth(0.62f)
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.Black)
    ) {
        if (thumbnailUrl.isNotBlank()) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        content()
    }
}
