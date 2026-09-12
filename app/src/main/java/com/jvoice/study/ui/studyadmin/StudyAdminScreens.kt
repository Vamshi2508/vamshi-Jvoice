package com.jvoice.study.ui.studyadmin

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.jvoice.study.data.model.ContentStatus
import com.jvoice.study.data.model.Difficulty
import com.jvoice.study.data.model.StudyRole
import com.jvoice.study.data.model.Subject
import com.jvoice.study.data.model.Topic
import com.jvoice.study.navigation.StudyAdminScaffold
import com.jvoice.study.navigation.StudyRoutes
import com.jvoice.study.navigation.StudyStatGrid
import kotlinx.coroutines.launch
import com.jvoice.core.i18n.current
import com.jvoice.core.i18n.ContentLanguageTabs
import com.jvoice.core.i18n.LocalizedOutlinedTextField
import com.jvoice.core.i18n.LocalizedText
import com.jvoice.core.i18n.Strings
import com.jvoice.core.i18n.lt
import com.jvoice.core.i18n.rememberLocalizedFormState
import com.jvoice.core.i18n.tr

private fun statusColor(status: ContentStatus) = when (status) {
    ContentStatus.DRAFT -> androidx.compose.ui.graphics.Color(0xFF6E7078)
    ContentStatus.PENDING_REVIEW -> androidx.compose.ui.graphics.Color(0xFFE07B00)
    ContentStatus.PUBLISHED -> androidx.compose.ui.graphics.Color(0xFF1B7F4B)
}

/* ============================================================== dashboard */

@Composable
fun StudyAdminDashboardScreen(
    viewModel: StudyAdminViewModel,
    role: StudyRole,
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val counts by viewModel.counts.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    StudyAdminScaffold(
        role = role,
        title = "Study Admin",
        currentRoute = StudyRoutes.STUDY_ADMIN_DASHBOARD,
        onNavigate = onNavigate,
        onSignOut = onSignOut,
        snackbarHostState = snackbarHostState
    ) { padding ->
        if (isLoading) {
            LoadingState(Modifier.padding(padding), "Loading syllabus...")
            return@StudyAdminScaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { StudyDemoBar() }
            item {
                SectionHeader(
                    "Exam types",
                    subtitle = "What students can choose to prepare for",
                    actionLabel = "Manage",
                    onAction = { onNavigate(StudyRoutes.STUDY_ADMIN_EXAM_TYPES) }
                )
            }
            item {
                StudyStatGrid(
                    stats = listOf(
                        "Exam types" to tracks.size.toString(),
                        "Visible" to tracks.count { it.isEnabled }.toString()
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) { (label, value), modifier ->
                    MetricCard(
                        label, value, modifier,
                        accent = MaterialTheme.colorScheme.tertiary,
                        caption = if (label == "Exam types") "Constable, Group-4, SSC…" else "In the student picker",
                        onClick = { onNavigate(StudyRoutes.STUDY_ADMIN_EXAM_TYPES) }
                    )
                }
            }
            item { SectionHeader("Syllabus") }
            item {
                StudyStatGrid(
                    stats = listOf(
                        "Subjects" to counts.subjects.toString(),
                        "Topics" to counts.topics.toString()
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) { (label, value), modifier ->
                    MetricCard(
                        label, value, modifier,
                        caption = if (label == "Subjects") counts.enabledSubjects.toString() + " enabled" else null,
                        onClick = {
                            onNavigate(
                                if (label == "Subjects") StudyRoutes.STUDY_ADMIN_SUBJECTS
                                else StudyRoutes.STUDY_ADMIN_TOPICS
                            )
                        }
                    )
                }
            }
            item { SectionHeader("Content") }
            item {
                StudyStatGrid(
                    stats = listOf(
                        "Articles" to counts.articles.toString(),
                        "Published" to counts.published.toString(),
                        "Pending review" to counts.pending.toString(),
                        "Questions" to counts.questions.toString()
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) { (label, value), modifier ->
                    MetricCard(
                        label, value, modifier,
                        accent = when (label) {
                            "Published" -> statusColor(ContentStatus.PUBLISHED)
                            "Pending review" -> statusColor(ContentStatus.PENDING_REVIEW)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        onClick = {
                            onNavigate(
                                if (label == "Questions") StudyRoutes.STUDY_ADMIN_BANK
                                else StudyRoutes.STUDY_ADMIN_CONTENT
                            )
                        }
                    )
                }
            }
            item { SectionHeader("Quizzes") }
            item {
                MetricCard(
                    "Topic quizzes",
                    counts.quizzes.toString(),
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    accent = MaterialTheme.colorScheme.secondary,
                    caption = "Generated from topics with four or more questions"
                )
            }
        }
    }
}

/* =============================================================== subjects */

@Composable
fun SubjectManagementScreen(
    viewModel: StudyAdminViewModel,
    role: StudyRole,
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val subjects by viewModel.subjects.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var editing by remember { mutableStateOf<Subject?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Subject?>(null) }

    if (showAdd || editing != null) {
        SubjectDialog(
            subject = editing,
            onDismiss = { showAdd = false; editing = null },
            onSave = { name, emoji ->
                val target = editing
                if (target == null) {
                    viewModel.addSubject(name, emoji)
                    scope.launch { snackbarHostState.showSnackbar("Subject added") }
                } else {
                    viewModel.updateSubject(target.id, name, emoji)
                    scope.launch { snackbarHostState.showSnackbar("Subject updated") }
                }
                showAdd = false
                editing = null
            }
        )
    }

    pendingDelete?.let { subject ->
        ConfirmDialog(
            title = "Delete " + subject.name.en + "?",
            message = "Subjects that still contain topics cannot be deleted.",
            confirmLabel = "Delete",
            destructive = true,
            onConfirm = {
                val deleted = viewModel.deleteSubject(subject.id)
                pendingDelete = null
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (deleted) "Subject deleted" else "Cannot delete - topics still assigned"
                    )
                }
            },
            onDismiss = { pendingDelete = null }
        )
    }

    StudyAdminScaffold(
        role = role,
        title = "Subjects",
        currentRoute = StudyRoutes.STUDY_ADMIN_SUBJECTS,
        onNavigate = onNavigate,
        onSignOut = onSignOut,
        snackbarHostState = snackbarHostState,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add subject") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item { SectionHeader(subjects.size.toString() + " subjects") }
            items(subjects, key = { it.id }) { subject ->
                ListItem(
                    leadingContent = {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(subject.emoji, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    },
                    headlineContent = { Text(subject.name.en) },
                    supportingContent = {
                        Column {
                            Text(subject.name.te, style = MaterialTheme.typography.labelMedium)
                            Text(
                                viewModel.topicCountFor(subject.id).toString() + " topics • " +
                                    viewModel.questionCountFor(subject.id) + " questions",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = subject.isEnabled,
                                onCheckedChange = { viewModel.toggleSubject(subject.id) }
                            )
                            IconButton(onClick = { editing = subject }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { pendingDelete = subject }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun SubjectDialog(
    subject: Subject?,
    onDismiss: () -> Unit,
    onSave: (LocalizedText, String) -> Unit
) {
    var name by remember { mutableStateOf(subject?.name ?: LocalizedText.EMPTY) }
    var emoji by remember { mutableStateOf(subject?.emoji ?: "📘") }
    val formState = rememberLocalizedFormState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (subject == null) "Add subject" else "Edit subject") },
        text = {
            Column {
                // Was two stacked name boxes; now one field behind a language
                // tab, same as every other content form in the app.
                ContentLanguageTabs(
                    state = formState,
                    fields = listOf(name),
                    showHint = false
                )
                Spacer(Modifier.height(10.dp))
                LocalizedOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    language = formState.language,
                    label = lt("Name", "పేరు"),
                    required = true,
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it },
                    label = { Text("Icon") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                // One language is enough to save; the other can follow later.
                enabled = !name.isBlank,
                onClick = { onSave(name.trimmed(), emoji.trim()) }
            ) { Text(tr(Strings.Common.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/* ================================================================= topics */

@Composable
fun TopicManagementScreen(
    viewModel: StudyAdminViewModel,
    role: StudyRole,
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val topics by viewModel.topics.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val filter by viewModel.topicSubjectFilter.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var editing by remember { mutableStateOf<Topic?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Topic?>(null) }

    if (showAdd || editing != null) {
        TopicDialog(
            topic = editing,
            subjects = subjects,
            defaultSubjectId = filter ?: subjects.firstOrNull()?.id.orEmpty(),
            onDismiss = { showAdd = false; editing = null },
            onSave = { subjectId, name, difficulty ->
                val target = editing
                if (target == null) {
                    viewModel.addTopic(subjectId, name, difficulty)
                    scope.launch { snackbarHostState.showSnackbar("Topic added") }
                } else {
                    viewModel.updateTopic(target.id, subjectId, name, difficulty)
                    scope.launch { snackbarHostState.showSnackbar("Topic updated") }
                }
                showAdd = false
                editing = null
            }
        )
    }

    pendingDelete?.let { topic ->
        ConfirmDialog(
            title = "Delete " + topic.name.en + "?",
            message = "Topics with articles or questions attached cannot be deleted.",
            confirmLabel = "Delete",
            destructive = true,
            onConfirm = {
                val deleted = viewModel.deleteTopic(topic.id)
                pendingDelete = null
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (deleted) "Topic deleted" else "Cannot delete - content still references this topic"
                    )
                }
            },
            onDismiss = { pendingDelete = null }
        )
    }

    StudyAdminScaffold(
        role = role,
        title = "Topics",
        currentRoute = StudyRoutes.STUDY_ADMIN_TOPICS,
        onNavigate = onNavigate,
        onSignOut = onSignOut,
        snackbarHostState = snackbarHostState,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add topic") }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter == null,
                    onClick = { viewModel.setTopicSubjectFilter(null) },
                    label = { Text("All subjects") }
                )
                subjects.forEach { subject ->
                    FilterChip(
                        selected = filter == subject.id,
                        onClick = {
                            viewModel.setTopicSubjectFilter(if (filter == subject.id) null else subject.id)
                        },
                        label = { Text(subject.name.current()) }
                    )
                }
            }

            if (topics.isEmpty()) {
                EmptyState(title = "No topics", description = "Add the first topic for this subject.")
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
                    item { SectionHeader(topics.size.toString() + " topics") }
                    items(topics, key = { it.id }) { topic ->
                        ListItem(
                            headlineContent = { Text(topic.name.en) },
                            supportingContent = {
                                Column {
                                    Text(topic.name.te, style = MaterialTheme.typography.labelMedium)
                                    Spacer(Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        StudyPill(
                                            viewModel.subjectName(topic.subjectId),
                                            MaterialTheme.colorScheme.primary
                                        )
                                        StudyPill(topic.difficulty.label, MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = topic.isEnabled,
                                        onCheckedChange = { viewModel.toggleTopic(topic.id) }
                                    )
                                    IconButton(onClick = { editing = topic }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = { pendingDelete = topic }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete")
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

@Composable
private fun TopicDialog(
    topic: Topic?,
    subjects: List<Subject>,
    defaultSubjectId: String,
    onDismiss: () -> Unit,
    onSave: (String, LocalizedText, Difficulty) -> Unit
) {
    var subjectId by remember { mutableStateOf(topic?.subjectId ?: defaultSubjectId) }
    var name by remember { mutableStateOf(topic?.name ?: LocalizedText.EMPTY) }
    var difficulty by remember { mutableStateOf(topic?.difficulty ?: Difficulty.MEDIUM) }
    val formState = rememberLocalizedFormState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (topic == null) "Add topic" else "Edit topic") },
        text = {
            Column {
                Text("Subject", style = MaterialTheme.typography.labelMedium)
                Row(
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subjects.forEach { subject ->
                        FilterChip(
                            selected = subjectId == subject.id,
                            onClick = { subjectId = subject.id },
                            label = { Text(subject.name.current()) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                ContentLanguageTabs(
                    state = formState,
                    fields = listOf(name),
                    showHint = false
                )
                Spacer(Modifier.height(10.dp))
                LocalizedOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    language = formState.language,
                    label = lt("Topic name", "టాపిక్ పేరు"),
                    required = true,
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Difficulty.entries.forEach { level ->
                        FilterChip(
                            selected = difficulty == level,
                            onClick = { difficulty = level },
                            label = { Text(level.localizedLabel.current()) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !name.isBlank && subjectId.isNotBlank(),
                onClick = { onSave(subjectId, name.trimmed(), difficulty) }
            ) { Text(tr(Strings.Common.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/* ================================================================ content */

@Composable
fun ContentManagementScreen(
    viewModel: StudyAdminViewModel,
    role: StudyRole,
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val articles by viewModel.articles.collectAsState()
    val filter by viewModel.contentStatusFilter.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var pendingDelete by remember { mutableStateOf<String?>(null) }

    pendingDelete?.let { id ->
        ConfirmDialog(
            title = "Delete article?",
            message = "The article is removed from the local content library.",
            confirmLabel = "Delete",
            destructive = true,
            onConfirm = {
                viewModel.deleteArticle(id)
                pendingDelete = null
                scope.launch { snackbarHostState.showSnackbar("Article deleted") }
            },
            onDismiss = { pendingDelete = null }
        )
    }

    StudyAdminScaffold(
        role = role,
        title = "Content",
        currentRoute = StudyRoutes.STUDY_ADMIN_CONTENT,
        onNavigate = onNavigate,
        onSignOut = onSignOut,
        snackbarHostState = snackbarHostState
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter == null,
                    onClick = { viewModel.setContentStatusFilter(null) },
                    label = { Text("All") }
                )
                ContentStatus.entries.forEach { status ->
                    FilterChip(
                        selected = filter == status,
                        onClick = {
                            viewModel.setContentStatusFilter(if (filter == status) null else status)
                        },
                        label = { Text(status.label) }
                    )
                }
            }

            if (articles.isEmpty()) {
                EmptyState(title = "No articles", description = "Nothing matches this filter.")
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                    item { SectionHeader(articles.size.toString() + " articles") }
                    items(articles, key = { it.id }) { article ->
                        ListItem(
                            headlineContent = {
                                Text(article.title.current(), maxLines = 2, overflow = TextOverflow.Ellipsis)
                            },
                            supportingContent = {
                                Column {
                                    Text(
                                        viewModel.subjectName(article.subjectId) + " • " +
                                            viewModel.topicName(article.topicId) + " • " +
                                            article.authorName
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    StudyPill(article.status.label, statusColor(article.status))
                                }
                            },
                            trailingContent = {
                                Row {
                                    if (article.status != ContentStatus.PUBLISHED) {
                                        IconButton(onClick = {
                                            viewModel.publishArticle(article.id)
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Published to students")
                                            }
                                        }) {
                                            Icon(Icons.Default.Publish, contentDescription = "Publish")
                                        }
                                    } else {
                                        IconButton(onClick = {
                                            viewModel.sendBackArticle(article.id)
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Moved back to draft")
                                            }
                                        }) {
                                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Unpublish")
                                        }
                                    }
                                    IconButton(onClick = { pendingDelete = article.id }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete")
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
