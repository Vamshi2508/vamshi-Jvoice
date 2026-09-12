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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jvoice.news.components.EmptyState
import com.jvoice.news.components.LoadingState
import com.jvoice.news.components.PullToRefreshBox
import com.jvoice.news.components.SectionHeader
import com.jvoice.study.components.AccuracyBar
import com.jvoice.study.components.BandPill
import com.jvoice.study.components.MetricCard
import com.jvoice.study.components.ProgressRing
import com.jvoice.study.components.StudyDemoBar
import com.jvoice.study.components.StudyPill
import com.jvoice.study.components.accuracyColor
import com.jvoice.study.data.model.PerformanceBand
import com.jvoice.core.i18n.current

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(
    viewModel: StudentHomeViewModel,
    onStartExam: (String) -> Unit,
    onOpenGrandTest: (String) -> Unit,
    onOpenTopic: (String) -> Unit,
    onOpenSubjectAnalysis: (String) -> Unit,
    onOpenAnalysis: () -> Unit,
    onOpenWeakAreas: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenPerformance: () -> Unit,
    onOpenResult: (String) -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val unread by viewModel.unreadCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            state.greeting + ", " + state.studentName.substringBefore(" ") + " 👋",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "J Voice Study • Module 2",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = "Study streak",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            state.streakDays.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    IconButton(onClick = onOpenNotifications) {
                        if (unread > 0) {
                            BadgedBox(badge = { Badge { Text(unread.toString()) } }) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                            }
                        } else {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = bottomBar
    ) { padding ->
        if (isLoading) {
            LoadingState(Modifier.padding(padding), "Preparing your dashboard...")
            return@Scaffold
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(contentPadding = PaddingValues(bottom = 28.dp)) {

                item { StudyDemoBar() }

                // -------------------------------------------- today's progress
                item {
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProgressRing(
                                percent = state.todayProgress,
                                size = 92,
                                label = "Today",
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Today's Progress", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Syllabus coverage and today's exam combined",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    StudyPill(
                                        state.streakDays.toString() + " day streak",
                                        MaterialTheme.colorScheme.secondary
                                    )
                                    StudyPill(
                                        "Rank #" + state.rank,
                                        MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // ------------------------------------------------ today's exam
                item {
                    val exam = state.todaysExam
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Today's Exam",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (exam == null) {
                                Text(
                                    "No exam scheduled right now.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            } else {
                                Text(
                                    exam.questionCount.toString() + " Questions  •  " +
                                        exam.durationMinutes + " Minutes  •  " +
                                        (if (exam.subjectIds.size > 1) "Mixed subjects" else "Single subject"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(Modifier.height(12.dp))
                                Button(onClick = { onStartExam(exam.id) }) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Start Exam")
                                }
                            }
                        }
                    }
                }

                // ---------------------------------------------- continue study
                state.continueTopic?.let { topic ->
                    item {
                        SectionHeader("Continue Studying", subtitle = "మీ చదువును కొనసాగించండి")
                    }
                    item {
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clickable { onOpenTopic(topic.id) },
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.MenuBook,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(topic.name.en, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        state.continueSubjectName + "  •  " + topic.name.te,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                // ------------------------------------------------- grand test
                state.grandTest?.let { test ->
                    item { SectionHeader("Weekly Grand Test", subtitle = "వీక్లీ గ్రాండ్ టెస్ట్") }
                    item {
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        test.title.current(),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    test.questionCount.toString() + " Questions  •  " +
                                        test.durationMinutes + " Minutes  •  " + test.dateLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(Modifier.height(10.dp))
                                OutlinedButton(onClick = { onOpenGrandTest(test.id) }) {
                                    Text("View Test")
                                }
                            }
                        }
                    }
                }

                // ---------------------------------------------- weak subjects
                if (state.weakSubjects.isNotEmpty()) {
                    item {
                        SectionHeader(
                            "Weak Areas 🔴",
                            subtitle = "Below 50% accuracy",
                            actionLabel = "Explore",
                            onAction = onOpenWeakAreas
                        )
                    }
                    item {
                        Column(Modifier.padding(horizontal = 16.dp)) {
                            state.weakSubjects.forEach { perf ->
                                AccuracyBar(
                                    label = perf.subject.name.en,
                                    accuracy = perf.accuracy,
                                    sublabel = perf.subject.name.te,
                                    onClick = { onOpenSubjectAnalysis(perf.subject.id) }
                                )
                            }
                        }
                    }
                }

                // -------------------------------------------- recommended
                if (state.recommended.isNotEmpty()) {
                    item { SectionHeader("Recommended Topics", subtitle = "Start where it matters most") }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.recommended, key = { it.topic.id }) { perf ->
                                Card(
                                    Modifier
                                        .width(190.dp)
                                        .clickable { onOpenTopic(perf.topic.id) },
                                    shape = RoundedCornerShape(14.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        BandPill(perf.band)
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            perf.topic.name.en,
                                            style = MaterialTheme.typography.titleSmall,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            StudyBrowseNames.subjectName(perf.topic.subjectId),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            perf.accuracy.toString() + "% accuracy",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = accuracyColor(perf.accuracy),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------- strong subjects
                if (state.strongSubjects.isNotEmpty()) {
                    item {
                        SectionHeader(
                            "Strong Subjects 🟢",
                            subtitle = "80% and above",
                            actionLabel = "Full analysis",
                            onAction = onOpenAnalysis
                        )
                    }
                    item {
                        Column(Modifier.padding(horizontal = 16.dp)) {
                            state.strongSubjects.forEach { perf ->
                                AccuracyBar(
                                    label = perf.subject.name.en,
                                    accuracy = perf.accuracy,
                                    sublabel = perf.subject.name.te,
                                    onClick = { onOpenSubjectAnalysis(perf.subject.id) }
                                )
                            }
                        }
                    }
                }

                // -------------------------------------------- recent results
                if (state.recentQuizResults.isNotEmpty()) {
                    item {
                        SectionHeader(
                            "Recent Results",
                            actionLabel = "My performance",
                            onAction = onOpenPerformance
                        )
                    }
                    items(state.recentQuizResults, key = { it.id }) { result ->
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable { onOpenResult(result.id) },
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        result.examTitle.current(),
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        result.type.label + "  •  " + result.score + "/" + result.totalQuestions,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    result.accuracy.toString() + "%",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = accuracyColor(result.accuracy)
                                )
                            }
                        }
                    }
                }

                // -------------------------------------------- leaderboard
                item {
                    SectionHeader(
                        "Leaderboard",
                        subtitle = "Rank #" + state.rank + " of " + state.participants,
                        actionLabel = "See all",
                        onAction = onOpenLeaderboard
                    )
                }
                item {
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable { onOpenLeaderboard() },
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            state.leaderboardPreview.forEachIndexed { index, entry ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        when (index) {
                                            0 -> "🥇"
                                            1 -> "🥈"
                                            else -> "🥉"
                                        },
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(entry.name, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.weight(1f))
                                    Text(
                                        entry.points.toString() + " pts",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "#" + state.rank,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text("You", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.weight(1f))
                                    TextButton(onClick = onOpenLeaderboard) { Text("View") }
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(10.dp)) }

                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            "Performance",
                            "View",
                            Modifier.weight(1f),
                            caption = "Exams, trend, subjects",
                            onClick = onOpenPerformance
                        )
                        MetricCard(
                            "Analysis",
                            "Open",
                            Modifier.weight(1f),
                            accent = MaterialTheme.colorScheme.secondary,
                            caption = "Strong and weak areas",
                            onClick = onOpenAnalysis
                        )
                    }
                }
            }
        }
    }
}

/** Tiny helper so the home cards can resolve subject names without another VM. */
internal object StudyBrowseNames {
    fun subjectName(subjectId: String): String =
        com.jvoice.study.data.repository.StudyRepository.subjectName(subjectId)
}
