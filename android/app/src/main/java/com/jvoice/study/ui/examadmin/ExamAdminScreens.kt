package com.jvoice.study.ui.examadmin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jvoice.news.components.ConfirmDialog
import com.jvoice.news.components.EmptyState
import com.jvoice.news.components.LoadingState
import com.jvoice.news.components.SectionHeader
import com.jvoice.study.components.MetricCard
import com.jvoice.study.components.StudyDemoBar
import com.jvoice.study.components.StudyPill
import com.jvoice.study.components.accuracyColor
import com.jvoice.study.data.model.Difficulty
import com.jvoice.study.data.model.Exam
import com.jvoice.study.data.model.ExamType
import com.jvoice.study.data.model.StudyRole
import com.jvoice.study.navigation.StudyAdminScaffold
import com.jvoice.study.navigation.StudyRoutes
import com.jvoice.study.navigation.StudyStatGrid
import com.jvoice.study.utils.toRelativeTimeStudy
import kotlinx.coroutines.launch
import com.jvoice.core.i18n.current
import com.jvoice.core.i18n.LocalizedFormHeader
import com.jvoice.core.i18n.LocalizedOutlinedTextField
import com.jvoice.core.i18n.lt
import com.jvoice.core.i18n.rememberLocalizedFormState

/* ============================================================== dashboard */

@Composable
fun ExamAdminDashboardScreen(
    viewModel: ExamAdminViewModel,
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val counts by viewModel.counts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val exams by viewModel.exams.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    StudyAdminScaffold(
        role = StudyRole.EXAM_ADMIN,
        title = "Exam Admin",
        currentRoute = StudyRoutes.EXAM_ADMIN_DASHBOARD,
        onNavigate = onNavigate,
        onSignOut = onSignOut,
        snackbarHostState = snackbarHostState
    ) { padding ->
        if (isLoading) {
            LoadingState(Modifier.padding(padding), "Loading exam schedule...")
            return@StudyAdminScaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { StudyDemoBar() }
            item { SectionHeader("Overview") }
            item {
                StudyStatGrid(
                    stats = listOf(
                        "Daily Exams" to counts.dailyExams.toString(),
                        "Grand Tests" to counts.grandTests.toString(),
                        "Question Bank" to counts.questions.toString(),
                        "Results" to counts.results.toString()
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) { (label, value), modifier ->
                    MetricCard(
                        label, value, modifier,
                        caption = when (label) {
                            "Daily Exams" -> counts.activeDaily.toString() + " active"
                            "Grand Tests" -> counts.activeGrand.toString() + " active"
                            else -> null
                        },
                        onClick = {
                            when (label) {
                                "Daily Exams" -> onNavigate(StudyRoutes.EXAM_ADMIN_DAILY)
                                "Grand Tests" -> onNavigate(StudyRoutes.EXAM_ADMIN_GRAND)
                                "Question Bank" -> onNavigate(StudyRoutes.EXAM_ADMIN_BANK)
                                else -> onNavigate(StudyRoutes.EXAM_ADMIN_RESULTS)
                            }
                        }
                    )
                }
            }

            item { SectionHeader("Upcoming and recent") }
            items(exams.take(6), key = { it.id }) { exam ->
                ListItem(
                    headlineContent = { Text(exam.title.current(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    supportingContent = {
                        Text(
                            exam.type.label + " • " + exam.questionCount + " questions • " +
                                exam.durationMinutes + " min • " + exam.dateLabel
                        )
                    },
                    trailingContent = {
                        StudyPill(
                            if (exam.isActive) "Active" else "Inactive",
                            if (exam.isActive) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.outline
                        )
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

/* ============================================================= exam lists */

@Composable
fun ExamListScreen(
    viewModel: ExamAdminViewModel,
    type: ExamType,
    onNavigate: (String) -> Unit,
    onCreate: () -> Unit,
    onEdit: (String) -> Unit,
    onViewResults: () -> Unit,
    onSignOut: () -> Unit
) {
    val exams by viewModel.exams.collectAsState()
    val list = exams.filter { it.type == type }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var pendingDelete by remember { mutableStateOf<String?>(null) }

    pendingDelete?.let { id ->
        ConfirmDialog(
            title = "Delete exam?",
            message = "The exam is removed from the local demo schedule.",
            confirmLabel = "Delete",
            destructive = true,
            onConfirm = {
                viewModel.deleteExam(id)
                pendingDelete = null
                scope.launch { snackbarHostState.showSnackbar("Exam deleted") }
            },
            onDismiss = { pendingDelete = null }
        )
    }

    StudyAdminScaffold(
        role = StudyRole.EXAM_ADMIN,
        title = if (type == ExamType.GRAND_TEST) "Grand Tests" else "Daily Exams",
        currentRoute = if (type == ExamType.GRAND_TEST) StudyRoutes.EXAM_ADMIN_GRAND
        else StudyRoutes.EXAM_ADMIN_DAILY,
        onNavigate = onNavigate,
        onSignOut = onSignOut,
        snackbarHostState = snackbarHostState,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreate,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(if (type == ExamType.GRAND_TEST) "New Grand Test" else "New Daily Exam") }
            )
        }
    ) { padding ->
        if (list.isEmpty()) {
            EmptyState(
                title = "No exams yet",
                description = "Create the first one with the button below.",
                modifier = Modifier.padding(padding),
                actionLabel = "Create",
                onAction = onCreate
            )
            return@StudyAdminScaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                SectionHeader(
                    list.size.toString() + " " + (if (type == ExamType.GRAND_TEST) "grand tests" else "daily exams"),
                    actionLabel = "Results",
                    onAction = onViewResults
                )
            }
            items(list, key = { it.id }) { exam ->
                AdminExamCard(
                    exam = exam,
                    subjectNames = exam.subjectIds.map { viewModel.subjectName(it) },
                    onEdit = { onEdit(exam.id) },
                    onToggleActive = {
                        viewModel.setActive(exam.id, !exam.isActive)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (exam.isActive) "Exam deactivated" else "Exam activated and students notified"
                            )
                        }
                    },
                    onDelete = { pendingDelete = exam.id }
                )
            }
        }
    }
}

@Composable
private fun AdminExamCard(
    exam: Exam,
    subjectNames: List<String>,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
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
                Text(exam.title.current(), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                StudyPill(
                    if (exam.isActive) "Active" else "Inactive",
                    if (exam.isActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                exam.questionCount.toString() + " questions • " + exam.durationMinutes +
                    " min • " + exam.difficulty.label + " • " + exam.dateLabel,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                subjectNames.take(5).joinToString(", ") +
                    (if (subjectNames.size > 5) " +" + (subjectNames.size - 5) else ""),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (exam.isActive) "Live for students" else "Hidden from students",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Switch(checked = exam.isActive, onCheckedChange = { onToggleActive() })
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

/* ============================================================ exam editor */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamEditorScreen(
    viewModel: ExamAdminViewModel,
    examId: String?,
    type: ExamType,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val form by viewModel.form.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showErrors by remember { mutableStateOf(false) }
    // Which language the exam title and instructions are bound to.
    val formState = rememberLocalizedFormState()

    LaunchedEffect(examId, type) {
        if (examId.isNullOrBlank()) viewModel.newExam(type) else viewModel.loadExam(examId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (examId.isNullOrBlank()) {
                            if (type == ExamType.GRAND_TEST) "Create Grand Test" else "Create Daily Exam"
                        } else "Edit Exam"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Button(
                onClick = {
                    showErrors = true
                    if (viewModel.save()) {
                        scope.launch { snackbarHostState.showSnackbar("Exam saved") }
                        onDone()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) { Text("Save exam") }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            SectionHeader("Exam details")

            // The title and the pre-exam instructions are both read by students,
            // so both are authored in the two languages behind this tab.
            LocalizedFormHeader(state = formState, fields = form.localizedFields)
            Spacer(Modifier.height(10.dp))

            LocalizedOutlinedTextField(
                value = form.title,
                onValueChange = { v -> viewModel.updateForm { it.copy(title = v) } },
                language = formState.language,
                label = lt("Exam title", "పరీక్ష శీర్షిక"),
                required = true,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            OutlinedTextField(
                value = form.dateLabel,
                onValueChange = { v -> viewModel.updateForm { it.copy(dateLabel = v) } },
                label = { Text("Date label (e.g. Today, This Sunday)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
            Row(
                Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = form.durationText,
                    onValueChange = { v ->
                        viewModel.updateForm { it.copy(durationText = v.filter { c -> c.isDigit() }) }
                    },
                    label = { Text("Duration (min) *") },
                    isError = showErrors && form.durationError != null,
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = form.questionCountText,
                    onValueChange = { v ->
                        viewModel.updateForm { it.copy(questionCountText = v.filter { c -> c.isDigit() }) }
                    },
                    label = { Text("Questions *") },
                    isError = showErrors && form.countError != null,
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            SectionHeader(
                "Exam type",
                subtitle = "Who sits this paper. None selected = every exam type sees it."
            )
            Column(Modifier.padding(horizontal = 16.dp)) {
                tracks.filter { it.isEnabled }.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { track ->
                            FilterChip(
                                selected = form.trackIds.contains(track.id),
                                onClick = { viewModel.toggleTrack(track.id) },
                                label = { Text(track.emoji + " " + track.shortName) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(6.dp))
                }
                if (form.trackIds.size == 1) {
                    TextButton(onClick = { viewModel.applyTrackPattern(form.trackIds.first()) }) {
                        Text("Use " + viewModel.trackName(form.trackIds.first()) + " pattern")
                    }
                }
            }

            SectionHeader("Subjects", subtitle = "Questions are drawn from the bank across these subjects")
            Column(Modifier.padding(horizontal = 16.dp)) {
                subjects.filter { it.isEnabled }.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { subject ->
                            FilterChip(
                                selected = form.subjectIds.contains(subject.id),
                                onClick = { viewModel.toggleSubject(subject.id) },
                                label = { Text(subject.name.en) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
            if (showErrors && form.subjectError != null) {
                Text(
                    form.subjectError!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            SectionHeader("Difficulty")
            Row(
                Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Difficulty.entries.forEach { level ->
                    FilterChip(
                        selected = form.difficulty == level,
                        onClick = { viewModel.updateForm { it.copy(difficulty = level) } },
                        label = { Text(level.label) }
                    )
                }
            }

            SectionHeader("Instructions")
            LocalizedOutlinedTextField(
                value = form.instructions,
                onValueChange = { v -> viewModel.updateForm { it.copy(instructions = v) } },
                language = formState.language,
                label = lt(
                    "Instructions shown before the exam",
                    "పరీక్షకు ముందు చూపే సూచనలు"
                ),
                minLines = 3,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Activate immediately", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Active exams appear in the student's Exams tab",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = form.isActive,
                    onCheckedChange = { v -> viewModel.updateForm { it.copy(isActive = v) } }
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

/* =========================================================== question bank */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionBankScreen(
    viewModel: ExamAdminViewModel,
    role: StudyRole,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val questions by viewModel.bankQuestions.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val subjectFilter by viewModel.bankSubject.collectAsState()
    val difficultyFilter by viewModel.bankDifficulty.collectAsState()
    val query by viewModel.bankQuery.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    StudyAdminScaffold(
        role = role,
        title = "Question Bank",
        currentRoute = currentRoute,
        onNavigate = onNavigate,
        onSignOut = onSignOut,
        snackbarHostState = snackbarHostState
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setBankQuery,
                placeholder = { Text("Search question text") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = subjectFilter == null,
                    onClick = { viewModel.setBankSubject(null) },
                    label = { Text("All subjects") }
                )
                subjects.forEach { subject ->
                    FilterChip(
                        selected = subjectFilter == subject.id,
                        onClick = {
                            viewModel.setBankSubject(if (subjectFilter == subject.id) null else subject.id)
                        },
                        label = { Text(subject.name.en) }
                    )
                }
            }
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = difficultyFilter == null,
                    onClick = { viewModel.setBankDifficulty(null) },
                    label = { Text("Any difficulty") }
                )
                Difficulty.entries.forEach { level ->
                    FilterChip(
                        selected = difficultyFilter == level,
                        onClick = {
                            viewModel.setBankDifficulty(if (difficultyFilter == level) null else level)
                        },
                        label = { Text(level.label) }
                    )
                }
            }

            if (questions.isEmpty()) {
                EmptyState(
                    title = "No questions match",
                    description = "Clear the filters or search for something else.",
                    actionLabel = "Clear filters",
                    onAction = {
                        viewModel.setBankQuery("")
                        viewModel.setBankSubject(null)
                        viewModel.setBankDifficulty(null)
                    }
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                    item { SectionHeader(questions.size.toString() + " questions") }
                    items(questions, key = { it.id }) { question ->
                        ListItem(
                            headlineContent = {
                                Text(question.text.current(), maxLines = 2, overflow = TextOverflow.Ellipsis)
                            },
                            supportingContent = {
                                Column {
                                    Text(
                                        viewModel.subjectName(question.subjectId) + " • " +
                                            viewModel.topicName(question.topicId)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        StudyPill(question.difficulty.label, MaterialTheme.colorScheme.secondary)
                                        StudyPill(question.type.label, MaterialTheme.colorScheme.outline)
                                        StudyPill(question.status.label, MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

/* ============================================================ exam results */

@Composable
fun ExamResultsAdminScreen(
    viewModel: ExamAdminViewModel,
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val results by viewModel.results.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    StudyAdminScaffold(
        role = StudyRole.EXAM_ADMIN,
        title = "Exam Results",
        currentRoute = StudyRoutes.EXAM_ADMIN_RESULTS,
        onNavigate = onNavigate,
        onSignOut = onSignOut,
        snackbarHostState = snackbarHostState
    ) { padding ->
        if (results.isEmpty()) {
            EmptyState(
                title = "No attempts recorded",
                description = "Student attempts appear here once they submit an exam.",
                modifier = Modifier.padding(padding)
            )
            return@StudyAdminScaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                SectionHeader(
                    results.size.toString() + " attempts",
                    subtitle = "Demo student: Sai Charan"
                )
            }
            items(results, key = { it.id }) { result ->
                ListItem(
                    headlineContent = {
                        Text(result.examTitle.current(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    supportingContent = {
                        Text(
                            result.type.label + " • " + result.score + "/" + result.totalQuestions +
                                " • " + result.timeTakenLabel + " • " + result.takenAt.toRelativeTimeStudy() +
                                (if (result.rank != null) " • Rank #" + result.rank else "")
                        )
                    },
                    trailingContent = {
                        Text(
                            result.accuracy.toString() + "%",
                            style = MaterialTheme.typography.titleSmall,
                            color = accuracyColor(result.accuracy)
                        )
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
