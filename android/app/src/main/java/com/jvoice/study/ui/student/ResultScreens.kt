package com.jvoice.study.ui.student

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jvoice.news.components.EmptyState
import com.jvoice.news.components.SectionHeader
import com.jvoice.study.components.AccuracyBar
import com.jvoice.study.components.BandPill
import com.jvoice.study.components.MetricCard
import com.jvoice.study.components.OptionRow
import com.jvoice.study.components.ScoreCard
import com.jvoice.study.components.StudyPill
import com.jvoice.study.components.accuracyColor
import com.jvoice.study.data.model.ExamType
import com.jvoice.study.data.model.PerformanceBand
import com.jvoice.core.i18n.current

/* =============================================================== result */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamResultScreen(
    viewModel: PerformanceViewModel,
    resultId: String,
    onReviewAnswers: (String) -> Unit,
    onStudyWeakTopics: () -> Unit,
    onOpenSubjectAnalysis: (String) -> Unit,
    onBackHome: () -> Unit,
    onBack: () -> Unit
) {
    val allResults by viewModel.results.collectAsState()
    val result = remember(resultId, allResults) { viewModel.resultById(resultId) }
    val improvement = if (result?.type == ExamType.GRAND_TEST) viewModel.grandTestImprovement() else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (result?.type == ExamType.GRAND_TEST) "Grand Test Result" else "Result") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (result == null) {
            EmptyState(
                title = "Result not found",
                description = "This attempt is not in the local demo data.",
                modifier = Modifier.padding(padding),
                actionLabel = "Back",
                onAction = onBack
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        if (result.type == ExamType.TOPIC_QUIZ) "Quiz Completed 🎉" else "Exam Submitted 🎉",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        result.examTitle.current(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                ScoreCard(
                    score = result.score.toString() + " / " + result.totalQuestions,
                    accuracy = result.accuracy,
                    correct = result.correct,
                    wrong = result.wrong,
                    skipped = result.skipped,
                    timeLabel = result.timeTakenLabel,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // ------------------------------------------------ rank + improvement
            if (result.rank != null && result.participants != null) {
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            "Your Rank",
                            "#" + result.rank,
                            Modifier.weight(1f),
                            caption = "of " + result.participants + " participants"
                        )
                        MetricCard(
                            "Accuracy",
                            result.accuracy.toString() + "%",
                            Modifier.weight(1f),
                            accent = accuracyColor(result.accuracy),
                            caption = PerformanceBand.of(result.accuracy).label
                        )
                    }
                }
            }

            if (improvement != null) {
                val (previous, current, delta) = improvement
                item {
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (delta >= 0)
                                MaterialTheme.colorScheme.tertiaryContainer
                            else MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (delta >= 0) Icons.AutoMirrored.Filled.TrendingUp
                                else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Compared with your previous Grand Test",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    "Previous " + previous + "  →  Current " + current,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Text(
                                (if (delta >= 0) "+" else "") + delta,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ---------------------------------------------- subject performance
            if (result.subjectScores.isNotEmpty()) {
                item { SectionHeader("Subject Performance", subtitle = "సబ్జెక్ట్ వారీ ప్రదర్శన") }
                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        result.subjectScores.forEach { score ->
                            AccuracyBar(
                                label = viewModel.subjectName(score.subjectId),
                                accuracy = score.accuracy,
                                trailing = score.correct.toString() + "/" + score.total,
                                onClick = { onOpenSubjectAnalysis(score.subjectId) }
                            )
                        }
                    }
                }
            }

            // ------------------------------------------------ weak in this paper
            val weakInPaper = result.subjectScores.filter { it.accuracy < 50 }
            if (weakInPaper.isNotEmpty()) {
                item {
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text("Detected in this paper", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(6.dp))
                            weakInPaper.forEach { score ->
                                Row(
                                    Modifier.padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        viewModel.subjectName(score.subjectId),
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        "Accuracy " + score.accuracy + "%",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    BandPill(PerformanceBand.of(score.accuracy))
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Recommendation: explore these subjects from Weak Areas and take a practice set.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ----------------------------------------------- topic performance
            if (result.topicScores.isNotEmpty()) {
                item { SectionHeader("Topic Performance") }
                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        result.topicScores.sortedBy { it.accuracy }.forEach { score ->
                            AccuracyBar(
                                label = viewModel.topicName(score.topicId),
                                accuracy = score.accuracy,
                                sublabel = viewModel.subjectName(score.subjectId),
                                trailing = score.correct.toString() + "/" + score.total
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }

            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    if (result.answers.isNotEmpty()) {
                        Button(
                            onClick = { onReviewAnswers(result.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Review Answers") }
                        Spacer(Modifier.height(8.dp))
                    }
                    OutlinedButton(
                        onClick = onStudyWeakTopics,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Study Weak Topics") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onBackHome,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Back to Home") }
                }
            }
        }
    }
}

/* =============================================================== review */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewAnswersScreen(
    viewModel: PerformanceViewModel,
    resultId: String,
    onBack: () -> Unit
) {
    val allResults by viewModel.results.collectAsState()
    val result = remember(resultId, allResults) { viewModel.resultById(resultId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Answers") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (result == null || result.answers.isEmpty()) {
            EmptyState(
                title = "Nothing to review",
                description = "This attempt did not store per-question answers.",
                modifier = Modifier.padding(padding),
                actionLabel = "Back",
                onAction = onBack
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LegendChip("Correct " + result.correct, Icons.Default.CheckCircle, MaterialTheme.colorScheme.tertiary)
                    LegendChip("Wrong " + result.wrong, Icons.Default.Cancel, MaterialTheme.colorScheme.error)
                    LegendChip("Skipped " + result.skipped, Icons.Default.RemoveCircle, MaterialTheme.colorScheme.outline)
                }
            }

            items(result.answers.size) { index ->
                val answer = result.answers[index]
                val question = viewModel.questionById(answer.questionId)
                if (question == null) return@items

                Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                answer.isSkipped -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                answer.isCorrect -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                                Text((index + 1).toString(), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        StudyPill(
                            viewModel.subjectName(question.subjectId),
                            MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(6.dp))
                        StudyPill(
                            when {
                                answer.isSkipped -> "Skipped"
                                answer.isCorrect -> "Correct"
                                else -> "Wrong"
                            },
                            when {
                                answer.isSkipped -> MaterialTheme.colorScheme.outline
                                answer.isCorrect -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                        if (answer.markedForReview) {
                            Spacer(Modifier.width(6.dp))
                            StudyPill("Marked", MaterialTheme.colorScheme.secondary)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(question.text.current(), style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))

                    question.options.forEachIndexed { optionIndex, option ->
                        val isCorrectOption = optionIndex == answer.correctIndex
                        val isChosen = optionIndex == answer.selectedIndex
                        OptionRow(
                            letter = ('A' + optionIndex).toString(),
                            text = option.current(),
                            selected = isChosen,
                            correct = when {
                                isCorrectOption -> true
                                isChosen -> false
                                else -> null
                            }
                        )
                    }

                    if (!question.explanation.isBlank) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Explanation", style = MaterialTheme.typography.labelMedium)
                                Text(question.explanation.current(), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun LegendChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(5.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}
