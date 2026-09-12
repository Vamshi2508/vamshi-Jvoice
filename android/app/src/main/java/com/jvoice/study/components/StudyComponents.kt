package com.jvoice.study.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jvoice.study.data.model.PaletteState
import com.jvoice.study.data.model.PerformanceBand

/* ------------------------------------------------------------------ colours */

@Composable
fun bandColor(band: PerformanceBand): Color = when (band) {
    PerformanceBand.STRONG -> Color(0xFF1B7F4B)
    PerformanceBand.NEEDS_PRACTICE -> Color(0xFFE07B00)
    PerformanceBand.WEAK -> Color(0xFFC62828)
}

@Composable
fun accuracyColor(accuracy: Int): Color = bandColor(PerformanceBand.of(accuracy))

/* ------------------------------------------------------------------ pills */

@Composable
fun StudyPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    filled: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = if (filled) color else color.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (filled) Color.White else color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun BandPill(band: PerformanceBand, modifier: Modifier = Modifier) {
    StudyPill(band.emoji + " " + band.label, bandColor(band), modifier)
}

/* ------------------------------------------------------------------ progress */

/** Circular progress ring used for "Today's Progress" and score summaries. */
@Composable
fun ProgressRing(
    percent: Int,
    modifier: Modifier = Modifier,
    size: Int = 108,
    stroke: Int = 10,
    color: Color = MaterialTheme.colorScheme.primary,
    label: String? = null,
    centerText: String? = null
) {
    val animated by animateFloatAsState(
        targetValue = (percent.coerceIn(0, 100)) / 100f,
        label = "ring"
    )
    val track = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val strokePx = stroke.dp.toPx()
            val inset = strokePx / 2
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                centerText ?: (percent.toString() + "%"),
                style = MaterialTheme.typography.titleLarge,
                color = color
            )
            if (label != null) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** Labelled horizontal accuracy bar - the workhorse of the analysis screens. */
@Composable
fun AccuracyBar(
    label: String,
    accuracy: Int,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    sublabel: String? = null,
    onClick: (() -> Unit)? = null
) {
    val color = accuracyColor(accuracy)
    Column(
        modifier = (if (onClick != null) modifier.clickable { onClick() } else modifier)
            .fillMaxWidth()
            .padding(vertical = 7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyMedium)
                if (sublabel != null) {
                    Text(
                        sublabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                trailing ?: (accuracy.toString() + "%"),
                style = MaterialTheme.typography.titleSmall,
                color = color
            )
        }
        Spacer(Modifier.height(5.dp))
        LinearProgressIndicator(
            progress = { accuracy.coerceIn(0, 100) / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/** Simple vertical bar chart for the exam trend. No external chart library. */
@Composable
fun TrendBarChart(
    values: List<Pair<String, Int>>,
    modifier: Modifier = Modifier,
    barHeight: Int = 120
) {
    if (values.isEmpty()) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height((barHeight + 42).dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEach { (label, value) ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    value.toString() + "%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(3.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height((barHeight * value.coerceIn(0, 100) / 100).coerceAtLeast(4).dp)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(accuracyColor(value))
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    label.removePrefix("Exam ").let { "E" + it },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

/* ------------------------------------------------------------------ cards */

@Composable
fun ScoreCard(
    score: String,
    accuracy: Int,
    correct: Int,
    wrong: Int,
    skipped: Int,
    timeLabel: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(
                    percent = accuracy,
                    size = 96,
                    centerText = accuracy.toString() + "%",
                    label = "Accuracy",
                    color = accuracyColor(accuracy)
                )
                Spacer(Modifier.width(18.dp))
                Column {
                    Text(
                        "Score",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(score, style = MaterialTheme.typography.displaySmall)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            timeLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CountTile("Correct", correct.toString(), Color(0xFF1B7F4B), Modifier.weight(1f))
                CountTile("Wrong", wrong.toString(), Color(0xFFC62828), Modifier.weight(1f))
                CountTile("Skipped", skipped.toString(), MaterialTheme.colorScheme.outline, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun CountTile(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.10f)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, color = color)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Compact metric tile used across dashboards. */
@Composable
fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    caption: String? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Box(
                Modifier
                    .size(width = 26.dp, height = 4.dp)
                    .background(accent, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.height(10.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, color = accent)
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (caption != null) {
                Text(
                    caption,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/* ------------------------------------------------------------------ exam bits */

/** Countdown chip. Turns amber under five minutes and red under one. */
@Composable
fun ExamTimer(remainingSeconds: Int, modifier: Modifier = Modifier) {
    val color = when {
        remainingSeconds <= 60 -> MaterialTheme.colorScheme.error
        remainingSeconds <= 300 -> Color(0xFFE07B00)
        else -> MaterialTheme.colorScheme.primary
    }
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val text = (if (minutes < 10) "0" else "") + minutes + ":" + (if (seconds < 10) "0" else "") + seconds

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Timer,
                contentDescription = "Time left",
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Spacer(Modifier.width(6.dp))
            Text(text, style = MaterialTheme.typography.titleSmall, color = color)
        }
    }
}

@Composable
fun paletteColors(state: PaletteState): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when (state) {
        PaletteState.CURRENT -> scheme.primary to Color.White
        PaletteState.ANSWERED -> Color(0xFF1B7F4B) to Color.White
        PaletteState.MARKED -> Color(0xFF7B4DFF) to Color.White
        PaletteState.ANSWERED_MARKED -> Color(0xFF00695C) to Color.White
        PaletteState.NOT_ANSWERED -> Color(0xFFC62828).copy(alpha = 0.15f) to Color(0xFFC62828)
        PaletteState.NOT_VISITED -> scheme.surfaceVariant to scheme.onSurfaceVariant
    }
}

@Composable
fun PaletteCell(
    number: Int,
    state: PaletteState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = paletteColors(state)
    Surface(
        modifier = modifier
            .size(40.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = bg,
        border = if (state == PaletteState.NOT_VISITED) {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        } else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                number.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = fg
            )
        }
    }
}

@Composable
fun PaletteLegend(modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LegendDot("Answered", Color(0xFF1B7F4B))
            LegendDot("Marked", Color(0xFF7B4DFF))
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LegendDot("Not answered", Color(0xFFC62828))
            LegendDot("Not visited", MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** A single answer option row with selected / correct / wrong styling. */
@Composable
fun OptionRow(
    letter: String,
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    correct: Boolean? = null,
    onClick: (() -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    val borderColor = when {
        correct == true -> Color(0xFF1B7F4B)
        correct == false && selected -> scheme.error
        selected -> scheme.primary
        else -> scheme.outlineVariant
    }
    val bg = when {
        correct == true -> Color(0xFF1B7F4B).copy(alpha = 0.08f)
        correct == false && selected -> scheme.error.copy(alpha = 0.08f)
        selected -> scheme.primary.copy(alpha = 0.08f)
        else -> scheme.surface
    }

    Surface(
        modifier = (if (onClick != null) modifier.clickable { onClick() } else modifier)
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .border(
                width = if (selected || correct == true) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        color = bg
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = borderColor.copy(alpha = if (selected || correct == true) 1f else 0.12f),
                modifier = Modifier.size(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        letter,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected || correct == true) Color.White else borderColor
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/* ------------------------------------------------------------------ misc */

@Composable
fun StudyDemoBar(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Text(
            "Module 2 demo — all subjects, questions and ranks are local mock data.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}
