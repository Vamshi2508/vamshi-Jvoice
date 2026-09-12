package com.jvoice.study.ui.studyadmin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.jvoice.news.components.ConfirmDialog
import com.jvoice.news.components.EmptyState
import com.jvoice.news.components.SectionHeader
import com.jvoice.study.components.StudyDemoBar
import com.jvoice.study.components.StudyPill
import com.jvoice.study.data.model.ExamTrack
import com.jvoice.study.data.model.ExamTrackGroup
import com.jvoice.study.data.model.StudyRole
import com.jvoice.study.navigation.StudyAdminScaffold
import com.jvoice.study.navigation.StudyRoutes
import kotlinx.coroutines.launch
import com.jvoice.core.i18n.current
import com.jvoice.core.i18n.LocalizedFormHeader
import com.jvoice.core.i18n.LocalizedOutlinedTextField
import com.jvoice.core.i18n.currentLanguage
import com.jvoice.core.i18n.lt
import com.jvoice.core.i18n.rememberLocalizedFormState

/* ==================================================== exam type management */

/**
 * The admin side of the student's exam picker. Whatever is enabled here is what a
 * student can choose from, with the pattern that drives their whole study plan.
 */
@Composable
fun ExamTypeManagementScreen(
    viewModel: StudyAdminViewModel,
    role: StudyRole,
    onNavigate: (String) -> Unit,
    onCreate: () -> Unit,
    onEdit: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val tracks by viewModel.tracks.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var confirmDelete by remember { mutableStateOf<ExamTrack?>(null) }

    confirmDelete?.let { track ->
        ConfirmDialog(
            title = "Delete " + track.name.en + "?",
            message = "Students preparing for it are returned to the exam picker.",
            confirmLabel = "Delete",
            onConfirm = {
                val removed = viewModel.deleteTrack(track.id)
                confirmDelete = null
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (removed) track.name.en + " deleted"
                        else "Delete the papers tagged with this exam first"
                    )
                }
            },
            onDismiss = { confirmDelete = null }
        )
    }

    StudyAdminScaffold(
        role = role,
        title = "Exam Types",
        currentRoute = StudyRoutes.STUDY_ADMIN_EXAM_TYPES,
        onNavigate = onNavigate,
        onSignOut = onSignOut,
        snackbarHostState = snackbarHostState,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreate,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New exam type") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            item { StudyDemoBar() }
            item {
                SectionHeader(
                    tracks.size.toString() + " exam types",
                    subtitle = tracks.count { it.isEnabled }.toString() +
                        " visible in the student exam picker"
                )
            }
            if (tracks.isEmpty()) {
                item {
                    EmptyState(
                        title = "No exam types yet",
                        description = "Add Constable, Group-4, SSC or any other exam. " +
                            "Students cannot start studying until one exists.",
                        actionLabel = "New exam type",
                        onAction = onCreate
                    )
                }
            }
            items(tracks, key = { it.id }) { track ->
                ExamTypeRow(
                    track = track,
                    papers = viewModel.trackPaperCount(track.id),
                    topics = viewModel.trackSummary(track).topics,
                    onToggle = { viewModel.toggleTrack(track.id) },
                    onEdit = { onEdit(track.id) },
                    onDelete = { confirmDelete = track }
                )
            }
        }
    }
}

@Composable
private fun ExamTypeRow(
    track: ExamTrack,
    papers: Int,
    topics: Int,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(track.emoji, style = MaterialTheme.typography.titleMedium)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(track.name.en, style = MaterialTheme.typography.titleSmall)
                    Text(
                        track.group.label + "  •  " + track.shortName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = track.isEnabled, onCheckedChange = { onToggle() })
            }
            Spacer(Modifier.height(8.dp))
            Text(track.patternLabel(currentLanguage()), style = MaterialTheme.typography.bodyMedium)
            Text(
                track.sections.size.toString() + " subjects  •  " + topics + " topics  •  " +
                    papers + " papers tagged  •  " + track.negativeMarking.current(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StudyPill(
                    if (track.isEnabled) "Visible to students" else "Hidden",
                    if (track.isEnabled) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.outline
                )
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

/* ========================================================= exam type editor */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamTypeEditorScreen(
    viewModel: StudyAdminViewModel,
    trackId: String?,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val form by viewModel.trackForm.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showErrors by remember { mutableStateOf(false) }
    // Which language the exam-type fields below are bound to.
    val formState = rememberLocalizedFormState()

    LaunchedEffect(trackId) {
        if (trackId.isNullOrBlank()) viewModel.newTrack() else viewModel.loadTrack(trackId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (trackId.isNullOrBlank()) "New exam type" else "Edit exam type") },
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
                    if (viewModel.saveTrack()) {
                        scope.launch { snackbarHostState.showSnackbar("Exam type saved") }
                        onDone()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) { Text("Save exam type") }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            SectionHeader("Exam identity")

            // This screen already had two name boxes stacked; now every
            // student-visible field on it works the same way, behind one tab.
            LocalizedFormHeader(state = formState, fields = form.localizedFields)
            Spacer(Modifier.height(10.dp))

            LocalizedOutlinedTextField(
                value = form.name,
                onValueChange = { v -> viewModel.updateTrackForm { it.copy(name = v) } },
                language = formState.language,
                label = lt("Exam name", "పరీక్ష పేరు"),
                required = true,
                singleLine = true,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            Row(
                Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = form.shortName,
                    onValueChange = { v -> viewModel.updateTrackForm { it.copy(shortName = v) } },
                    label = { Text("Short name") },
                    singleLine = true,
                    modifier = Modifier.weight(2f)
                )
                OutlinedTextField(
                    value = form.emoji,
                    onValueChange = { v -> viewModel.updateTrackForm { it.copy(emoji = v) } },
                    label = { Text("Icon") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            LocalizedOutlinedTextField(
                value = form.tagline,
                onValueChange = { v -> viewModel.updateTrackForm { it.copy(tagline = v) } },
                language = formState.language,
                label = lt("One-line description", "సంక్షిప్త వివరణ"),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            SectionHeader("Category", subtitle = "How the exam is grouped in the picker")
            Column(Modifier.padding(horizontal = 16.dp)) {
                ExamTrackGroup.values().toList().chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { group ->
                            FilterChip(
                                selected = form.group == group,
                                onClick = { viewModel.updateTrackForm { it.copy(group = group) } },
                                label = { Text(group.label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }

            SectionHeader("Notification details")
            LocalizedOutlinedTextField(
                value = form.qualification,
                onValueChange = { v -> viewModel.updateTrackForm { it.copy(qualification = v) } },
                language = formState.language,
                label = lt("Qualification", "అర్హత"),
                singleLine = true,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            Row(
                Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LocalizedOutlinedTextField(
                    value = form.ageLimit,
                    onValueChange = { v -> viewModel.updateTrackForm { it.copy(ageLimit = v) } },
                    language = formState.language,
                    label = lt("Age limit", "వయో పరిమితి"),
                    singleLine = true,
                    modifier = Modifier.weight(1f)                )
                LocalizedOutlinedTextField(
                    value = form.vacancyLabel,
                    onValueChange = { v -> viewModel.updateTrackForm { it.copy(vacancyLabel = v) } },
                    language = formState.language,
                    label = lt("Vacancies", "ఖాళీలు"),
                    singleLine = true,
                    modifier = Modifier.weight(1f)                )
            }
            LocalizedOutlinedTextField(
                value = form.examDateLabel,
                onValueChange = { v -> viewModel.updateTrackForm { it.copy(examDateLabel = v) } },
                language = formState.language,
                label = lt("Exam date label (e.g. Prelims in 3 months)", "పరీక్ష తేది (ఉదా. 3 నెలల్లో ప్రిలిమ్స్)"),
                singleLine = true,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            LocalizedOutlinedTextField(
                value = form.stagesText,
                onValueChange = { v -> viewModel.updateTrackForm { it.copy(stagesText = v) } },
                language = formState.language,
                label = lt("Selection stages, comma separated", "ఎంపిక దశలు, కామాలతో వేరు చేయండి"),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            SectionHeader(
                "Important dates",
                subtitle = "One per line: Notification | 12 Sep 2026 — shown on the student's exam home"
            )
            LocalizedOutlinedTextField(
                value = form.keyDatesText,
                onValueChange = { v -> viewModel.updateTrackForm { it.copy(keyDatesText = v) } },
                language = formState.language,
                label = lt("Label | date", "లేబెల్ | తేది"),
                minLines = 4,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            Text(
                form.keyDates.size.toString() + " dates will be listed for students",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            SectionHeader("Paper rules")
            Row(
                Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = form.durationText,
                    onValueChange = { v ->
                        viewModel.updateTrackForm { it.copy(durationText = v.filter { c -> c.isDigit() }) }
                    },
                    label = { Text("Duration (min) *") },
                    isError = showErrors && form.durationError != null,
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                LocalizedOutlinedTextField(
                    value = form.negativeMarking,
                    onValueChange = { v -> viewModel.updateTrackForm { it.copy(negativeMarking = v) } },
                    language = formState.language,
                    label = lt("Negative marking", "నెగెటివ్ మార్కింగ్"),
                    singleLine = true,
                    modifier = Modifier.weight(2f)                )
            }

            SectionHeader(
                "Exam pattern — subject wise",
                subtitle = form.totalQuestions.toString() + " questions  •  " +
                    form.totalMarks + " marks  •  " + form.sections.size + " subjects"
            )
            Text(
                "Tick the subjects on this paper, then set how many questions and marks each " +
                    "one carries. Students see exactly these subjects, in this order.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
            subjects.filter { it.isEnabled }.forEach { subject ->
                val section = form.sections.firstOrNull { it.subjectId == subject.id }
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(subject.emoji, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(subject.name.en, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    subject.name.te,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = section != null,
                                onCheckedChange = { viewModel.toggleSection(subject.id) }
                            )
                        }
                        if (section != null) {
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = section.questions.toString(),
                                    onValueChange = { v ->
                                        viewModel.setSectionQuestions(
                                            subject.id,
                                            v.filter { c -> c.isDigit() }.toIntOrNull() ?: 0
                                        )
                                    },
                                    label = { Text("Questions") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = section.marks.toString(),
                                    onValueChange = { v ->
                                        viewModel.setSectionMarks(
                                            subject.id,
                                            v.filter { c -> c.isDigit() }.toIntOrNull() ?: 0
                                        )
                                    },
                                    label = { Text("Marks") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
            if (showErrors && form.sectionError != null) {
                Text(
                    form.sectionError!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Visible to students", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Hidden exam types disappear from the student exam picker",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = form.isEnabled,
                    onCheckedChange = { v -> viewModel.updateTrackForm { it.copy(isEnabled = v) } }
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
