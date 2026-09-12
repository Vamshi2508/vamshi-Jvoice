package com.jvoice.study.ui.student

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Quiz
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jvoice.news.components.EmptyState
import com.jvoice.news.components.LoadingState
import com.jvoice.news.components.SectionHeader
import com.jvoice.study.components.AccuracyBar
import com.jvoice.study.components.BandPill
import com.jvoice.study.components.ProgressRing
import com.jvoice.study.components.StudyPill
import com.jvoice.study.components.accuracyColor
import com.jvoice.study.components.bandColor
import com.jvoice.study.data.model.PerformanceBand
import com.jvoice.core.i18n.current
import com.jvoice.core.i18n.Strings
import com.jvoice.core.i18n.tr

/* ================================================= strong & weak analysis */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    viewModel: PerformanceViewModel,
    onOpenSubject: (String) -> Unit,
    onOpenWeakAreas: () -> Unit,
    onBack: () -> Unit
) {
    val performance by viewModel.subjectPerformance.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val strong = performance.filter { it.band == PerformanceBand.STRONG }
    val needsPractice = performance.filter { it.band == PerformanceBand.NEEDS_PRACTICE }
    val weak = performance.filter { it.band == PerformanceBand.WEAK }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Performance") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            LoadingState(Modifier.padding(padding), "Analysing your attempts...")
            return@Scaffold
        }
        if (performance.isEmpty()) {
            EmptyState(
                title = "No attempts yet",
                description = "Take a quiz or a daily exam and your subject analysis appears here.",
                modifier = Modifier.padding(padding)
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
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val overall = if (performance.isEmpty()) 0 else
                            performance.sumOf { it.correct } * 100 / performance.sumOf { it.total }.coerceAtLeast(1)
                        ProgressRing(
                            percent = overall,
                            size = 88,
                            label = "Overall",
                            color = accuracyColor(overall)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Classification rule", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "80-100% Strong 🟢",
                                style = MaterialTheme.typography.labelMedium,
                                color = bandColor(PerformanceBand.STRONG)
                            )
                            Text(
                                "50-79% Needs Practice 🟡",
                                style = MaterialTheme.typography.labelMedium,
                                color = bandColor(PerformanceBand.NEEDS_PRACTICE)
                            )
                            Text(
                                "0-49% Weak 🔴",
                                style = MaterialTheme.typography.labelMedium,
                                color = bandColor(PerformanceBand.WEAK)
                            )
                        }
                    }
                }
            }

            if (strong.isNotEmpty()) {
                item { SectionHeader("Strong Subjects 🟢", subtitle = "బలమైన సబ్జెక్టులు") }
                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        strong.forEach { perf ->
                            AccuracyBar(
                                label = perf.subject.name.en,
                                accuracy = perf.accuracy,
                                sublabel = perf.correct.toString() + "/" + perf.total + " correct",
                                onClick = { onOpenSubject(perf.subject.id) }
                            )
                        }
                    }
                }
            }

            if (needsPractice.isNotEmpty()) {
                item { SectionHeader("Needs Practice 🟡", subtitle = "మెరుగుపడాల్సినవి") }
                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        needsPractice.forEach { perf ->
                            AccuracyBar(
                                label = perf.subject.name.en,
                                accuracy = perf.accuracy,
                                sublabel = perf.correct.toString() + "/" + perf.total + " correct",
                                onClick = { onOpenSubject(perf.subject.id) }
                            )
                        }
                    }
                }
            }

            if (weak.isNotEmpty()) {
                item {
                    SectionHeader(
                        "Weak Subjects 🔴",
                        subtitle = "బలహీన సబ్జెక్టులు",
                        actionLabel = "Explore",
                        onAction = onOpenWeakAreas
                    )
                }
                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        weak.forEach { perf ->
                            AccuracyBar(
                                label = perf.subject.name.en,
                                accuracy = perf.accuracy,
                                sublabel = perf.correct.toString() + "/" + perf.total + " correct",
                                onClick = { onOpenSubject(perf.subject.id) }
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = onOpenWeakAreas,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) { Text("Explore Your Weak Areas") }
            }
        }
    }
}

/* ============================================== subject-level topic analysis */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectAnalysisScreen(
    viewModel: PerformanceViewModel,
    subjectId: String,
    onOpenTopic: (String) -> Unit,
    onBack: () -> Unit
) {
    // Recomposes when the shared tally changes.
    viewModel.subjectPerformance.collectAsState()
    val subject = viewModel.subjectById(subjectId)
    val perf = viewModel.subjectPerformanceById(subjectId)
    val topics = viewModel.topicsFor(subjectId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(subject?.name?.current() ?: tr(Strings.Study.subject)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (perf == null || topics.isEmpty()) {
            EmptyState(
                title = "No data for this subject",
                description = "Attempt some questions from this subject to build the analysis.",
                modifier = Modifier.padding(padding)
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
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProgressRing(
                            percent = perf.accuracy,
                            size = 88,
                            label = "Accuracy",
                            color = accuracyColor(perf.accuracy)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                subject?.displayName ?: "",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(Modifier.height(4.dp))
                            BandPill(perf.band)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                perf.correct.toString() + " correct out of " + perf.total + " attempted",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item { SectionHeader("Topics", subtitle = "Weakest first") }
            items(topics, key = { it.topic.id }) { topicPerf ->
                Column(
                    Modifier
                        .clickable { onOpenTopic(topicPerf.topic.id) }
                        .padding(horizontal = 16.dp)
                ) {
                    AccuracyBar(
                        label = topicPerf.topic.name.en + "  " + topicPerf.band.emoji,
                        accuracy = topicPerf.accuracy,
                        sublabel = topicPerf.topic.name.te + "  •  " +
                            topicPerf.correct + "/" + topicPerf.total,
                        onClick = { onOpenTopic(topicPerf.topic.id) }
                    )
                }
            }
        }
    }
}

/* ============================================== explore your weak areas */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeakAreasScreen(
    viewModel: PerformanceViewModel,
    onOpenTopic: (String) -> Unit,
    onStartPractice: (String) -> Unit,
    onBack: () -> Unit
) {
    val groups by viewModel.weakAreas.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Explore Your Weak Areas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (groups.isEmpty()) {
            EmptyState(
                title = "Nothing weak right now 🎉",
                description = "Every attempted subject is at 80% or above. Keep it up.",
                modifier = Modifier.padding(padding)
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
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "We found areas that need improvement.",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Based on " + groups.sumOf { it.topics.size } + " topics across " +
                            groups.size + " subjects.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(groups, key = { it.subject.id }) { group ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(group.band.emoji, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(group.subject.name.en, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    group.subject.name.te,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                group.accuracy.toString() + "%",
                                style = MaterialTheme.typography.titleMedium,
                                color = bandColor(group.band)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        group.topics.forEach { topicPerf ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenTopic(topicPerf.topic.id) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier
                                        .size(6.dp)
                                        .background(bandColor(topicPerf.band), RoundedCornerShape(3.dp))
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    topicPerf.topic.name.en,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                StudyPill(
                                    topicPerf.accuracy.toString() + "%",
                                    bandColor(topicPerf.band)
                                )
                            }
                            HorizontalDivider()
                        }
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { onStartPractice(group.topics.first().topic.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Start Practice")
                        }
                    }
                }
            }
        }
    }
}

/* ============================================== weak topic learning plan */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeakTopicScreen(
    viewModel: PerformanceViewModel,
    topicId: String,
    onReadArticle: (String) -> Unit,
    onTakeQuiz: (String) -> Unit,
    onStartPractice: (String) -> Unit,
    onBack: () -> Unit
) {
    val perf = viewModel.topicPerformance(topicId)
    val plan = viewModel.studyPlan(topicId)
    val topicName = viewModel.topicName(topicId)
    val hasArticle = viewModel.articleForTopic(topicId) != null
    val hasQuiz = viewModel.quizForTopic(topicId) != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topicName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    if (hasArticle) onReadArticle(topicId) else onStartPractice(topicId)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) { Text("Start Learning") }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProgressRing(
                        percent = perf?.accuracy ?: 0,
                        size = 84,
                        label = "Your accuracy",
                        color = accuracyColor(perf?.accuracy ?: 0)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(topicName, style = MaterialTheme.typography.titleSmall)
                        if (perf != null) {
                            Text(
                                viewModel.subjectName(perf.topic.subjectId),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            BandPill(perf.band)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                perf.correct.toString() + "/" + perf.total + " correct so far",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            SectionHeader("Recommended", subtitle = "Follow these three steps in order")

            plan.forEach { step ->
                val enabled = when (step.order) {
                    1 -> hasArticle
                    2 -> hasQuiz
                    else -> true
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(enabled = enabled) {
                            when (step.order) {
                                1 -> onReadArticle(topicId)
                                2 -> onTakeQuiz(topicId)
                                else -> onStartPractice(topicId)
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (step.done) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (step.done) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text(
                                    step.order.toString(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            step.title.current(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            step.subtitle.current(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        when (step.order) {
                            1 -> Icons.AutoMirrored.Filled.MenuBook
                            2 -> Icons.Default.Quiz
                            else -> Icons.Default.FitnessCenter
                        },
                        contentDescription = null,
                        tint = if (enabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
                }
                HorizontalDivider(Modifier.padding(start = 62.dp))
            }

            Spacer(Modifier.height(12.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onTakeQuiz(topicId) },
                    enabled = hasQuiz,
                    modifier = Modifier.weight(1f)
                ) { Text("Topic Quiz") }
                OutlinedButton(
                    onClick = { onStartPractice(topicId) },
                    modifier = Modifier.weight(1f)
                ) { Text("Practice 20") }
            }
        }
    }
}
