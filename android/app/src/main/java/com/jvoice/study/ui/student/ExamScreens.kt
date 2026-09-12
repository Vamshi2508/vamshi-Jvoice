package com.jvoice.study.ui.student

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jvoice.news.components.ConfirmDialog
import com.jvoice.news.components.EmptyState
import com.jvoice.news.components.SectionHeader
import com.jvoice.study.components.ExamTimer
import com.jvoice.study.components.OptionRow
import com.jvoice.study.components.PaletteCell
import com.jvoice.study.components.PaletteLegend
import com.jvoice.study.components.StudyPill
import com.jvoice.study.data.model.Exam
import com.jvoice.study.data.model.ExamType
import com.jvoice.core.i18n.current

/* ============================================================== exams hub */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsHubScreen(
    viewModel: ExamViewModel,
    onStartExam: (String) -> Unit,
    onOpenResults: () -> Unit,
    onChangeExam: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    // Only papers that belong to the exam the student picked, plus general practice.
    val exams by viewModel.scopedExams.collectAsState()
    val trackLabel by viewModel.trackLabel.collectAsState()
    var tab by remember { mutableStateOf(0) }
    val daily = exams.filter { it.type == ExamType.DAILY }
    val grand = exams.filter { it.type == ExamType.GRAND_TEST }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(trackLabel?.let { it + " • Tests" } ?: "Exams • పరీక్షలు") },
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
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Daily Exams") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Grand Tests") })
            }

            val list = if (tab == 0) daily else grand
            if (list.isEmpty()) {
                EmptyState(
                    title = "No exams here",
                    description = "An Exam Admin can create and activate exams."
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)) {
                    item {
                        SectionHeader(
                            if (tab == 0) "Daily practice" else "Weekly Grand Tests",
                            subtitle = list.count { it.isActive }.toString() + " active",
                            actionLabel = "My results",
                            onAction = onOpenResults
                        )
                    }
                    items(list, key = { it.id }) { exam ->
                        ExamCard(
                            exam = exam,
                            subjectNames = exam.subjectIds.map { viewModel.subjectName(it) },
                            forThisExam = viewModel.isTrackExam(exam),
                            onStart = { onStartExam(exam.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamCard(
    exam: Exam,
    subjectNames: List<String>,
    forThisExam: Boolean,
    onStart: () -> Unit
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (exam.type == ExamType.GRAND_TEST) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(exam.title.current(), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                if (forThisExam) {
                    StudyPill("This exam", MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                }
                StudyPill(
                    if (exam.isActive) "Active" else "Inactive",
                    if (exam.isActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                exam.questionCount.toString() + " questions  •  " + exam.durationMinutes +
                    " minutes  •  " + exam.difficulty.label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                exam.dateLabel + "  •  " + subjectNames.take(4).joinToString(", ") +
                    (if (subjectNames.size > 4) " +" + (subjectNames.size - 4) else ""),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!exam.instructions.isBlank) {
                Spacer(Modifier.height(6.dp))
                Text(
                    exam.instructions.current(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(Modifier.height(12.dp))
            if (exam.isActive) {
                Button(onClick = onStart) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (exam.type == ExamType.GRAND_TEST) "Start Grand Test" else "Start Exam")
                }
            } else {
                OutlinedButton(onClick = {}, enabled = false) { Text("Not active") }
            }
        }
    }
}

/* ============================================================ exam runner */

enum class RunnerMode { EXAM, QUIZ, PRACTICE }

/**
 * The single exam interface used by topic quizzes, daily exams, practice sets and
 * the 100-question Grand Test: timer, palette, mark-for-review and navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamRunnerScreen(
    viewModel: ExamViewModel,
    mode: RunnerMode,
    id: String,
    onFinished: (String) -> Unit,
    onExit: () -> Unit
) {
    val attempt by viewModel.attempt.collectAsState()
    val submitted by viewModel.submittedResult.collectAsState()
    val autoSubmitted by viewModel.autoSubmitted.collectAsState()
    var showPalette by remember { mutableStateOf(false) }
    var confirmSubmit by remember { mutableStateOf(false) }
    var confirmExit by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(mode, id) {
        when (mode) {
            RunnerMode.EXAM -> viewModel.startExam(id)
            RunnerMode.QUIZ -> viewModel.startTopicQuiz(id)
            RunnerMode.PRACTICE -> viewModel.startPractice(id)
        }
    }

    // When the paper is graded, hand the result id to the caller for navigation.
    LaunchedEffect(submitted?.id) {
        submitted?.let { onFinished(it.id) }
    }

    BackHandler { confirmExit = true }

    if (confirmSubmit) {
        val a = attempt
        ConfirmDialog(
            title = "Submit the paper?",
            message = if (a == null) "Submit now?" else
                a.answeredCount.toString() + " answered, " + a.notAnsweredCount +
                    " unanswered, " + a.markedCount + " marked for review.",
            confirmLabel = "Submit",
            onConfirm = {
                confirmSubmit = false
                viewModel.submit()
            },
            onDismiss = { confirmSubmit = false }
        )
    }

    if (confirmExit) {
        ConfirmDialog(
            title = "Leave the exam?",
            message = "Your answers in this attempt will be discarded.",
            confirmLabel = "Leave",
            destructive = true,
            onConfirm = {
                confirmExit = false
                viewModel.abandon()
                onExit()
            },
            onDismiss = { confirmExit = false }
        )
    }

    val current = attempt
    if (current == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Exam") },
                    navigationIcon = {
                        IconButton(onClick = onExit) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            EmptyState(
                title = "Nothing to attempt",
                description = "This exam has no questions in the demo bank, or the attempt was closed.",
                modifier = Modifier.padding(padding),
                actionLabel = "Go back",
                onAction = onExit
            )
        }
        return
    }

    val attemptQuestion = current.questions.getOrNull(current.currentIndex)
    val question = attemptQuestion?.let { viewModel.questionById(it.questionId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            current.exam.title.current(),
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1
                        )
                        Text(
                            "Question " + (current.currentIndex + 1) + " of " + current.questions.size,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { confirmExit = true }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit")
                    }
                },
                actions = {
                    ExamTimer(current.remainingSeconds)
                    IconButton(onClick = { showPalette = !showPalette }) {
                        Icon(Icons.Default.Apps, contentDescription = "Question palette")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column {
                HorizontalDivider()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.previous() },
                        enabled = current.currentIndex > 0,
                        modifier = Modifier.weight(1f)
                    ) { Text("Previous") }

                    if (current.currentIndex == current.questions.lastIndex) {
                        Button(
                            onClick = { confirmSubmit = true },
                            modifier = Modifier.weight(1f)
                        ) { Text("Submit") }
                    } else {
                        Button(
                            onClick = { viewModel.next() },
                            modifier = Modifier.weight(1f)
                        ) { Text("Next") }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LinearProgressIndicator(
                progress = { (current.currentIndex + 1).toFloat() / current.questions.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
            )

            AnimatedVisibility(visible = showPalette) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Question palette", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.weight(1f))
                            Text(
                                current.answeredCount.toString() + "/" + current.questions.size + " answered",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),
                            modifier = Modifier.heightIn(max = 220.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(current.questions.indices.toList()) { index ->
                                PaletteCell(
                                    number = index + 1,
                                    state = current.paletteState(index),
                                    onClick = {
                                        viewModel.goTo(index)
                                        showPalette = false
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        PaletteLegend()
                    }
                }
            }

            if (question == null) {
                EmptyState(
                    title = "Question unavailable",
                    description = "This item is missing from the demo question bank."
                )
                return@Column
            }

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StudyPill(
                        viewModel.subjectName(question.subjectId),
                        MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(6.dp))
                    StudyPill(question.difficulty.label, MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { viewModel.toggleMark() }) {
                        Icon(
                            if (attemptQuestion.markedForReview) Icons.Default.Bookmark
                            else Icons.Default.BookmarkBorder,
                            contentDescription = "Mark for review",
                            tint = if (attemptQuestion.markedForReview) Color7B4DFF()
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Q" + (current.currentIndex + 1) + ". " + question.text.current(),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(16.dp))

                question.options.forEachIndexed { index, option ->
                    OptionRow(
                        letter = ('A' + index).toString(),
                        text = option.current(),
                        selected = attemptQuestion.selectedIndex == index,
                        onClick = { viewModel.select(index) }
                    )
                }

                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (attemptQuestion.isAnswered) {
                        TextButton(onClick = { viewModel.clearSelection() }) { Text("Clear answer") }
                    }
                    TextButton(onClick = { viewModel.toggleMark() }) {
                        Text(
                            if (attemptQuestion.markedForReview) "Unmark review" else "Mark for review"
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "Answered " + current.answeredCount + " • Marked " + current.markedCount +
                        " • Left " + current.notAnsweredCount,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (autoSubmitted) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Time is up - the paper was submitted automatically.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun Color7B4DFF() = androidx.compose.ui.graphics.Color(0xFF7B4DFF)
