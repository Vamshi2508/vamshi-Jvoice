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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jvoice.news.components.ConfirmDialog
import com.jvoice.news.components.EmptyState
import com.jvoice.news.components.LoadingState
import com.jvoice.news.components.SectionHeader
import com.jvoice.core.i18n.LanguagePreference
import com.jvoice.core.i18n.LanguagePreferenceCard
import com.jvoice.core.i18n.Strings
import com.jvoice.core.i18n.tr
import com.jvoice.shell.LocalModuleSwitcher
import com.jvoice.study.components.AccuracyBar
import com.jvoice.study.components.MetricCard
import com.jvoice.study.components.ProgressRing
import com.jvoice.study.components.StudyPill
import com.jvoice.study.components.TrendBarChart
import com.jvoice.study.components.accuracyColor
import com.jvoice.study.data.model.LeaderboardEntry
import com.jvoice.study.data.model.LeaderboardPeriod
import com.jvoice.study.data.model.StudyNotificationType
import com.jvoice.study.data.model.StudyUser
import com.jvoice.study.utils.toRelativeTimeStudy
import com.jvoice.core.i18n.current

/* =========================================================== leaderboard */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel,
    onChangeExam: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val period by viewModel.period.collectAsState()
    val entries by viewModel.entries.collectAsState()
    val me by viewModel.currentUser.collectAsState()
    val neighbours by viewModel.neighbourhood.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val trackLabel by viewModel.trackLabel.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("🏆 " + (trackLabel ?: "") + " Leaderboard") },
                actions = {
                    IconButton(onClick = onChangeExam) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Change exam")
                    }
                }
            )
        },
        bottomBar = bottomBar
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = LeaderboardPeriod.entries.indexOf(period),
                edgePadding = 12.dp
            ) {
                LeaderboardPeriod.entries.forEach { value ->
                    Tab(
                        selected = period == value,
                        onClick = { viewModel.setPeriod(value) },
                        text = { Text(value.label) }
                    )
                }
            }

            if (isLoading) {
                LoadingState(message = "Loading ranks...")
                return@Column
            }

            // your rank card
            me?.let { entry ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Your rank",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "#" + entry.rank,
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "of " + viewModel.participants() + " students",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                entry.points.toString() + " pts",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                entry.testsCompleted.toString() + " tests  •  " + entry.accuracy + "%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                item { SectionHeader("Top " + entries.size, subtitle = period.label + " ranking") }
                items(entries, key = { "top_" + it.rank }) { entry ->
                    LeaderboardRow(entry)
                }
                if (neighbours.isNotEmpty() && (me?.rank ?: 0) > entries.size) {
                    item { SectionHeader("Around you") }
                    items(neighbours, key = { "near_" + it.rank }) { entry ->
                        LeaderboardRow(entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(entry: LeaderboardEntry) {
    val highlight = entry.isCurrentUser
    Surface(
        color = if (highlight) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        else MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.width(38.dp)) {
                Text(
                    when (entry.rank) {
                        1 -> "🥇"
                        2 -> "🥈"
                        3 -> "🥉"
                        else -> "#" + entry.rank
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = if (highlight) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    entry.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    entry.testsCompleted.toString() + " tests  •  " + entry.accuracy + "% accuracy",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                entry.points.toString() + " pts",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
    HorizontalDivider()
}

/* ========================================================== performance */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceHistoryScreen(
    viewModel: PerformanceViewModel,
    onOpenResult: (String) -> Unit,
    onOpenSubject: (String) -> Unit,
    onBack: () -> Unit
) {
    val stats by viewModel.stats.collectAsState()
    val trend by viewModel.trend.collectAsState()
    val subjects by viewModel.subjectPerformance.collectAsState()
    val results by viewModel.results.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Performance") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard("Exams", stats.examsCompleted.toString(), Modifier.weight(1f))
                    MetricCard(
                        "Quizzes",
                        stats.quizzesCompleted.toString(),
                        Modifier.weight(1f),
                        accent = MaterialTheme.colorScheme.secondary
                    )
                    MetricCard(
                        "Streak",
                        stats.studyStreakDays.toString() + "d",
                        Modifier.weight(1f),
                        accent = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        "Average score",
                        stats.averageScorePercent.toString() + "%",
                        Modifier.weight(1f),
                        accent = accuracyColor(stats.averageScorePercent)
                    )
                    MetricCard(
                        "Best score",
                        stats.bestScorePercent.toString() + "%",
                        Modifier.weight(1f),
                        accent = accuracyColor(stats.bestScorePercent)
                    )
                }
            }
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        "Current rank",
                        "#" + stats.currentRank,
                        Modifier.weight(1f),
                        caption = "of " + stats.participants
                    )
                    MetricCard(
                        "Topics done",
                        stats.topicsCompleted.toString(),
                        Modifier.weight(1f),
                        accent = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            if (trend.isNotEmpty()) {
                item { SectionHeader("Last " + trend.size + " exams", subtitle = "Accuracy trend") }
                item {
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            TrendBarChart(values = trend)
                        }
                    }
                }
            }

            if (subjects.isNotEmpty()) {
                item { SectionHeader("Subject performance") }
                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        subjects.forEach { perf ->
                            AccuracyBar(
                                label = perf.subject.name.en,
                                accuracy = perf.accuracy,
                                sublabel = perf.correct.toString() + "/" + perf.total,
                                onClick = { onOpenSubject(perf.subject.id) }
                            )
                        }
                    }
                }
            }

            item { SectionHeader("Attempt history") }
            items(results.sortedByDescending { it.takenAt }, key = { it.id }) { result ->
                ListItem(
                    headlineContent = {
                        Text(
                            result.examTitle.current(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    supportingContent = {
                        Text(
                            result.type.label + "  •  " + result.score + "/" + result.totalQuestions +
                                "  •  " + result.timeTakenLabel +
                                (if (result.rank != null) "  •  Rank #" + result.rank else "")
                        )
                    },
                    trailingContent = {
                        Text(
                            result.accuracy.toString() + "%",
                            style = MaterialTheme.typography.titleSmall,
                            color = accuracyColor(result.accuracy)
                        )
                    },
                    modifier = Modifier.clickable { onOpenResult(result.id) }
                )
                HorizontalDivider()
            }
        }
    }
}

/* ========================================================= notifications */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyNotificationsScreen(
    viewModel: StudyNotificationsViewModel,
    onOpenExam: (String) -> Unit,
    onBack: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (notifications.any { !it.isRead }) {
                        IconButton(onClick = { viewModel.markAllRead() }) {
                            Icon(Icons.Default.DoneAll, contentDescription = "Mark all read")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            EmptyState(
                title = "No notifications",
                description = "Exam and result alerts appear here.",
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(notifications, key = { it.id }) { item ->
                val accent = when (item.type) {
                    StudyNotificationType.DAILY_EXAM -> MaterialTheme.colorScheme.primary
                    StudyNotificationType.GRAND_TEST -> MaterialTheme.colorScheme.secondary
                    StudyNotificationType.RESULT -> MaterialTheme.colorScheme.tertiary
                    StudyNotificationType.RANK -> MaterialTheme.colorScheme.tertiary
                    StudyNotificationType.RECOMMENDATION -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.outline
                }
                ListItem(
                    leadingContent = {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = accent.copy(alpha = 0.12f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    when (item.type) {
                                        StudyNotificationType.GRAND_TEST -> Icons.Default.EmojiEvents
                                        StudyNotificationType.DAILY_EXAM -> Icons.AutoMirrored.Filled.Assignment
                                        StudyNotificationType.RESULT,
                                        StudyNotificationType.RANK -> Icons.Default.Insights
                                        else -> Icons.Default.Notifications
                                    },
                                    contentDescription = null,
                                    tint = accent
                                )
                            }
                        }
                    },
                    headlineContent = {
                        Text(
                            item.title.current(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.Bold
                        )
                    },
                    supportingContent = {
                        Column {
                            Text(item.message.current(), style = MaterialTheme.typography.bodySmall)
                            Text(
                                item.timeMillis.toRelativeTimeStudy(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    },
                    trailingContent = {
                        if (!item.isRead) StudyPill("NEW", MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier.clickable {
                        viewModel.markRead(item.id)
                        if (item.type == StudyNotificationType.DAILY_EXAM ||
                            item.type == StudyNotificationType.GRAND_TEST
                        ) {
                            onOpenExam("")
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

/* =============================================================== profile */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    user: StudyUser?,
    performanceViewModel: PerformanceViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onOpenPerformance: () -> Unit,
    onOpenAnalysis: () -> Unit,
    onChangeExam: () -> Unit,
    onSignOut: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val stats by performanceViewModel.stats.collectAsState()
    val trackLabel by performanceViewModel.trackLabel.collectAsState()
    var confirmSignOut by remember { mutableStateOf(false) }
    var reminders by remember { mutableStateOf(true) }
    val switcher = LocalModuleSwitcher.current
    val language by LanguagePreference.language.collectAsState()

    if (confirmSignOut) {
        ConfirmDialog(
            title = "Switch role?",
            message = "You will return to the landing page.",
            confirmLabel = "Switch",
            onConfirm = { confirmSignOut = false; onSignOut() },
            onDismiss = { confirmSignOut = false }
        )
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Profile") }) },
        bottomBar = bottomBar
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                (user?.name ?: "S").take(1),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            user?.name ?: "Student",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            user?.email ?: "student@demo.in",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StudyPill("Student", MaterialTheme.colorScheme.primary)
                            StudyPill("Rank #" + stats.currentRank, MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }

            // The exam being prepared for, changeable at any time.
            item {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { onChangeExam() },
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Target exam", style = MaterialTheme.typography.labelMedium)
                            Text(
                                trackLabel ?: "Not chosen yet",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text("Change", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProgressRing(
                        percent = stats.averageAccuracy,
                        size = 90,
                        label = "Avg accuracy",
                        color = accuracyColor(stats.averageAccuracy)
                    )
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                stats.studyStreakDays.toString() + " day study streak",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stats.examsCompleted.toString() + " exams  •  " +
                                stats.quizzesCompleted + " quizzes  •  " +
                                stats.topicsCompleted + " topics done",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { SectionHeader("My progress") }
            item {
                ListItem(
                    leadingContent = { Icon(Icons.Default.Insights, contentDescription = null) },
                    headlineContent = { Text("My Performance") },
                    supportingContent = { Text("Trend, subject accuracy and attempt history") },
                    modifier = Modifier.clickable { onOpenPerformance() }
                )
            }
            item {
                ListItem(
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null) },
                    headlineContent = { Text("Strong & Weak Analysis") },
                    supportingContent = { Text("Subject and topic classification") },
                    modifier = Modifier.clickable { onOpenAnalysis() }
                )
            }

            item { SectionHeader("J Voice") }
            item {
                ListItem(
                    leadingContent = { Icon(Icons.Default.Newspaper, contentDescription = null) },
                    headlineContent = { Text("📰  News module") },
                    supportingContent = { Text("Switch back to the news experience") },
                    modifier = Modifier.clickable { switcher?.openNewsAsReader?.invoke() }
                )
            }

            item { SectionHeader(tr(Strings.Common.preferences)) }
            // Same control as the reader profile, same stored preference - a
            // student who set Telugu in the news module finds it already set here.
            item {
                LanguagePreferenceCard(
                    selected = language,
                    onSelect = { picked -> LanguagePreference.set(picked) }
                )
            }
            item {
                ListItem(
                    leadingContent = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                    headlineContent = { Text(tr(Strings.Common.darkMode)) },
                    trailingContent = { Switch(checked = isDarkTheme, onCheckedChange = onToggleTheme) }
                )
            }
            item {
                ListItem(
                    leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    headlineContent = { Text("Study reminders") },
                    supportingContent = { Text("Local demo toggle only") },
                    trailingContent = { Switch(checked = reminders, onCheckedChange = { reminders = it }) }
                )
            }
            item {
                ListItem(
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                    headlineContent = { Text("J Voice Study — Module 2 prototype") },
                    supportingContent = { Text("All questions, ranks and results are local mock data.") }
                )
            }

            item {
                OutlinedButton(
                    onClick = { confirmSignOut = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Switch role")
                }
            }
        }
    }
}
