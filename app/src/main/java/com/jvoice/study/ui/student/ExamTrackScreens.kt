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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jvoice.news.components.EmptyState
import com.jvoice.news.components.SectionHeader
import com.jvoice.study.components.CountTile
import com.jvoice.study.components.ProgressRing
import com.jvoice.study.components.StudyDemoBar
import com.jvoice.study.components.StudyPill
import com.jvoice.study.components.accuracyColor
import com.jvoice.study.data.model.Exam
import com.jvoice.study.data.model.ExamKeyDate
import com.jvoice.study.data.model.ExamResult
import com.jvoice.study.data.model.ExamSectionRow
import com.jvoice.study.data.model.ExamTrack
import com.jvoice.study.data.model.ExamTrackSummary
import com.jvoice.study.data.model.ExamType
import com.jvoice.study.utils.toRelativeTimeStudy
import com.jvoice.core.i18n.current
import com.jvoice.core.i18n.Strings
import com.jvoice.core.i18n.tr

/* ========================================================== exam picker */

/**
 * The first screen of the Study tab. Nothing is studied until an exam is chosen:
 * Constable, Group-4, SSC and so on. The list is whatever a Study Admin has
 * configured, so it changes without an app update.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamPickerScreen(
    viewModel: ExamTrackViewModel,
    onSelected: (String) -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val grouped by viewModel.grouped.collectAsState()
    val query by viewModel.query.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Choose your exam • మీ పరీక్ష") }
            )
        },
        bottomBar = bottomBar
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setQuery,
                placeholder = { Text("Search Constable, Group-4, SSC…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (grouped.isEmpty()) {
                EmptyState(
                    title = "No exams configured",
                    description = "A Study Admin adds exam types from the admin login. " +
                        "Nothing to prepare for yet."
                )
                return@Column
            }

            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                item {
                    Text(
                        "Pick the exam you are preparing for. Syllabus, topic quizzes, daily " +
                            "tests, grand tests, results and ranks are all shown for that exam only.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                grouped.forEach { (group, tracks) ->
                    item(key = "hdr_" + group.name) {
                        SectionHeader(
                            group.label,
                            subtitle = group.teluguLabel + "  •  " + tracks.size + " exams"
                        )
                    }
                    items(tracks, key = { it.id }) { track ->
                        ExamTrackCard(
                            summary = viewModel.summary(track),
                            onSelect = {
                                viewModel.select(track.id)
                                onSelected(track.id)
                            }
                        )
                    }
                }
                item { Spacer(Modifier.height(6.dp)); StudyDemoBar() }
            }
        }
    }
}

@Composable
private fun ExamTrackCard(summary: ExamTrackSummary, onSelect: () -> Unit) {
    val track = summary.track
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        // One line of identity, one line of numbers. The full pattern, eligibility
        // and stages live on the exam hub after the student picks this exam.
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(track.emoji, style = MaterialTheme.typography.titleLarge)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(track.name.en, style = MaterialTheme.typography.titleSmall)
                Text(
                    track.name.te,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    summary.subjects.toString() + " subjects  •  " + track.totalQuestions +
                        " Q  •  " + track.durationMinutes + " min",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/* ============================================================== exam hub */

/**
 * The chosen exam's home. Deliberately short: what is coming up, where to go, and
 * how far along the student is. Everything on it is dynamic - the dates come from
 * what an admin entered, the quick links carry live counts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamTrackHubScreen(
    viewModel: ExamTrackViewModel,
    onChangeExam: () -> Unit,
    onOpenSubject: (String) -> Unit,
    onOpenTopic: (String) -> Unit,
    onStartQuiz: (String) -> Unit,
    onStartExam: (String) -> Unit,
    onOpenExams: () -> Unit,
    onOpenBrowse: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenResult: (String) -> Unit,
    onOpenPerformance: () -> Unit,
    onOpenWeakAreas: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenKeyDates: () -> Unit,
    onOpenPastPapers: () -> Unit,
    onOpenDashboard: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val state by viewModel.hub.collectAsState()
    val unread by viewModel.unreadCount.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val track = state.track

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    ExamSwitcherTitle(
                        current = track,
                        tracks = tracks.filter { it.isEnabled },
                        onSelect = viewModel::select,
                        onBrowseAll = onChangeExam
                    )
                },
                actions = {
                    IconButton(onClick = onOpenNotifications) {
                        if (unread > 0) {
                            BadgedBox(badge = { Badge { Text(unread.toString()) } }) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                            }
                        } else {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                    IconButton(onClick = onChangeExam) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Change exam")
                    }
                }
            )
        },
        bottomBar = bottomBar
    ) { padding ->
        if (track == null) {
            EmptyState(
                title = "No exam selected",
                description = "Choose the exam you are preparing for.",
                actionLabel = "Choose exam",
                onAction = onChangeExam,
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { TrackHeaderCard(track, onChangeExam) }

            // ---- where to go. Notifications live behind the toolbar bell and the
            // dates behind their own quick link, so the hub stays a list of doors.
            item { SectionHeader("Quick links", subtitle = "Everything for " + track.shortName) }
            item {
                QuickLinks(
                    state = state,
                    onStartExam = onStartExam,
                    onOpenExams = onOpenExams,
                    onOpenBrowse = onOpenBrowse,
                    onOpenPerformance = onOpenPerformance,
                    onOpenLeaderboard = onOpenLeaderboard,
                    onOpenWeakAreas = onOpenWeakAreas,
                    onOpenKeyDates = onOpenKeyDates,
                    onOpenPastPapers = onOpenPastPapers
                )
            }

            // ---- how far along
            item { SectionHeader("Your progress", subtitle = "On this exam only") }
            item { TrackProgressCard(state, onOpenDashboard, onOpenTopic) }

            if (state.recentResults.isNotEmpty()) {
                item {
                    SectionHeader(
                        "Latest result",
                        actionLabel = "All results",
                        onAction = onOpenPerformance
                    )
                }
                item {
                    val result = state.recentResults.first()
                    ResultRow(result) { onOpenResult(result.id) }
                }
            }

            item { Spacer(Modifier.height(8.dp)); StudyDemoBar() }
        }
    }
}

/* ------------------------------------------------------------- hub pieces */

/**
 * The toolbar title doubles as the exam switcher: it shows the exam the student
 * picked and opens a dropdown of every enabled exam so they can move to another
 * one without walking back through the picker. "All exams" at the bottom still
 * opens the full picker, which has search and the group headers.
 */
@Composable
private fun ExamSwitcherTitle(
    current: ExamTrack?,
    tracks: List<ExamTrack>,
    onSelect: (String) -> Unit,
    onBrowseAll: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            Modifier
                .clip(RoundedCornerShape(50))
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                (current?.emoji ?: "📚") + "  " + (current?.shortName ?: "Study"),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = "Switch exam",
                modifier = Modifier.size(22.dp)
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            tracks.forEach { option ->
                val isCurrent = option.id == current?.id
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                option.shortName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal
                            )
                            Text(
                                option.name.en,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    leadingIcon = { Text(option.emoji) },
                    trailingIcon = {
                        if (isCurrent) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Current exam",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        if (!isCurrent) onSelect(option.id)
                    }
                )
            }

            if (tracks.isNotEmpty()) HorizontalDivider()

            DropdownMenuItem(
                text = { Text("All exams", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                onClick = {
                    expanded = false
                    onBrowseAll()
                }
            )
        }
    }
}

@Composable
private fun TrackHeaderCard(track: ExamTrack, onChangeExam: () -> Unit) {
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
            Text(track.emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(track.name.en, style = MaterialTheme.typography.titleMedium)
                Text(
                    track.name.te,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            TextButton(onClick = onChangeExam) { Text("Change") }
        }
    }
}

/**
 * "Important dates", opened from the hub's quick links. Notification, hall ticket
 * and exam dates for the selected exam, exactly as the Study Admin entered them.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamKeyDatesScreen(
    viewModel: ExamTrackViewModel,
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit = {}
) {
    val state by viewModel.hub.collectAsState()
    val track = state.track

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Important dates") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = bottomBar
    ) { padding ->
        if (track == null) {
            EmptyState(
                title = "No exam selected",
                description = "Choose an exam to see its dates.",
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                SectionHeader(
                    track.emoji + "  " + track.name.current(),
                    subtitle = track.examDateLabel.current().ifBlank { tr(Strings.Study.announcedByDesk) }
                )
            }
            if (state.keyDates.isEmpty()) {
                item { HubHintCard("No dates announced yet for " + track.shortName + ".") }
            } else {
                items(state.keyDates, key = { "date_" + it.label.en }) { date -> KeyDateRow(date) }
            }
            item { Spacer(Modifier.height(8.dp)); StudyDemoBar() }
        }
    }
}

@Composable
private fun KeyDateRow(date: ExamKeyDate) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(date.label.current(), style = MaterialTheme.typography.bodyMedium)
                if (!date.note.isBlank) {
                    Text(
                        date.note.current(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            StudyPill(date.dateLabel, MaterialTheme.colorScheme.primary)
        }
    }
}

/** Six tiles that cover everything a student does for this exam. */
@Composable
private fun QuickLinks(
    state: TrackHubState,
    onStartExam: (String) -> Unit,
    onOpenExams: () -> Unit,
    onOpenBrowse: () -> Unit,
    onOpenPerformance: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenWeakAreas: () -> Unit,
    onOpenKeyDates: () -> Unit,
    onOpenPastPapers: () -> Unit
) {
    val daily = state.dailyExam
    val grand = state.grandTest
    val links = listOf(
        QuickLink(
            "Daily Exam",
            daily?.let { it.questionCount.toString() + " Q • " + it.durationMinutes + " min" } ?: "None active",
            Icons.AutoMirrored.Filled.Assignment
        ) { if (daily != null) onStartExam(daily.id) else onOpenExams() },
        QuickLink(
            "Grand Test",
            grand?.let { it.questionCount.toString() + " Q • " + it.dateLabel } ?: "Not scheduled",
            Icons.Default.EmojiEvents
        ) { if (grand != null) onStartExam(grand.id) else onOpenExams() },
        // material and quizzes live together now — one card into the same browser
        QuickLink(
            "Study & quizzes",
            state.articleCount.toString() + " articles • " + state.quizCount + " quizzes",
            Icons.AutoMirrored.Filled.MenuBook,
            onOpenBrowse
        ),
        QuickLink(
            "Previous papers",
            state.pastPaperCount.toString() + " asked before",
            Icons.Default.HistoryEdu,
            onOpenPastPapers
        ),
        QuickLink(
            "My results",
            state.attemptCount.toString() + " attempts",
            Icons.Default.Insights,
            onOpenPerformance
        ),
        QuickLink(
            "My rank",
            "#" + state.rank + " of " + state.participants,
            Icons.Default.Leaderboard,
            onOpenLeaderboard
        ),
        QuickLink(
            "Important dates",
            if (state.keyDates.isEmpty()) "None announced"
            else state.keyDates.size.toString() + " announced",
            Icons.Default.CalendarMonth,
            onOpenKeyDates
        )
    )

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        links.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { link -> QuickLinkTile(link, Modifier.weight(1f)) }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        if (state.weakAreas.isNotEmpty()) {
            QuickLinkTile(
                QuickLink(
                    "Weak areas",
                    state.weakAreas.joinToString(", ") { it.subject.name.en },
                    Icons.Default.TrendingDown,
                    onOpenWeakAreas
                ),
                Modifier.fillMaxWidth()
            )
        }
    }
}

private data class QuickLink(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun QuickLinkTile(link: QuickLink, modifier: Modifier = Modifier) {
    Card(
        modifier.clickable { link.onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Icon(
                link.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(link.title, style = MaterialTheme.typography.titleSmall)
            Text(
                link.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun TrackProgressCard(
    state: TrackHubState,
    onOpenDashboard: () -> Unit,
    onOpenTopic: (String) -> Unit
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(
                    percent = state.progress,
                    size = 92,
                    centerText = state.progress.toString() + "%",
                    label = "Syllabus"
                )
                Spacer(Modifier.width(18.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        state.topicsCompleted.toString() + " of " + state.totalTopics + " topics done",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            state.streakDays.toString() + "-day streak",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Per-subject progress, the detail that used to crowd the top of this screen.
            state.sections.take(4).forEach { row ->
                Column(Modifier.padding(vertical = 5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            row.subject.emoji + "  " + row.subject.name.en,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            row.completedTopics.toString() + "/" + row.topicCount,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { row.progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                state.continueTopic?.let { topic ->
                    Button(
                        onClick = { onOpenTopic(topic.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Continue " + topic.name.en, maxLines = 1)
                    }
                }
                OutlinedButton(onClick = onOpenDashboard, modifier = Modifier.weight(1f)) {
                    Text("Full dashboard")
                }
            }
        }
    }
}

@Composable
private fun ResultRow(result: ExamResult, onOpen: () -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onOpen() },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(result.examTitle.current(), style = MaterialTheme.typography.titleSmall)
                Text(
                    result.score.toString() + "/" + result.totalQuestions + "  •  " +
                        result.takenAt.toRelativeTimeStudy() +
                        (result.rank?.let { "  •  Rank #" + it } ?: ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StudyPill(result.accuracy.toString() + "%", accuracyColor(result.accuracy))
        }
    }
}

@Composable
private fun HubHintCard(text: String) {
    Surface(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(14.dp)
        )
    }
}

/* ------------------------------------------------------- no-exam-yet guard */

/**
 * Shown on the Study, Exams and Ranks tabs while no exam has been chosen, so the
 * app never presents a syllabus that belongs to nobody's paper.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseExamPrompt(
    title: String,
    onChooseExam: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text(title) }) },
        bottomBar = bottomBar
    ) { padding ->
        EmptyState(
            title = "Choose your exam first",
            description = "Pick Constable, Group-4, SSC or any other exam. Everything here is " +
                "then shown for that exam only.",
            actionLabel = "Choose exam",
            onAction = onChooseExam,
            modifier = Modifier.padding(padding)
        )
    }
}
