package com.jvoice.study.ui.creator

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jvoice.news.components.ConfirmDialog
import com.jvoice.news.components.EmptyState
import com.jvoice.news.components.LoadingState
import com.jvoice.news.components.SectionHeader
import com.jvoice.study.components.MetricCard
import com.jvoice.study.components.OptionRow
import com.jvoice.study.components.StudyDemoBar
import com.jvoice.study.components.StudyPill
import com.jvoice.study.data.model.ContentStatus
import com.jvoice.study.data.model.Difficulty
import com.jvoice.study.data.model.QuestionType
import com.jvoice.study.data.model.StudyRole
import com.jvoice.study.navigation.StudyAdminScaffold
import com.jvoice.study.navigation.StudyRoutes
import com.jvoice.study.navigation.StudyStatGrid
import kotlinx.coroutines.launch
import com.jvoice.core.i18n.current
import com.jvoice.core.i18n.AppLanguage
import com.jvoice.core.i18n.LocalizedFormHeader
import com.jvoice.core.i18n.LocalizedOutlinedTextField
import com.jvoice.core.i18n.LocalizedText
import com.jvoice.core.i18n.currentLanguage
import com.jvoice.core.i18n.lt
import com.jvoice.core.i18n.rememberLocalizedFormState

private fun statusColor(status: ContentStatus) = when (status) {
    ContentStatus.DRAFT -> androidx.compose.ui.graphics.Color(0xFF6E7078)
    ContentStatus.PENDING_REVIEW -> androidx.compose.ui.graphics.Color(0xFFE07B00)
    ContentStatus.PUBLISHED -> androidx.compose.ui.graphics.Color(0xFF1B7F4B)
}

/* ============================================================== dashboard */

@Composable
fun CreatorDashboardScreen(
    viewModel: CreatorViewModel,
    onNavigate: (String) -> Unit,
    onNewArticle: () -> Unit,
    onEditArticle: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val counts by viewModel.counts.collectAsState()
    val articles by viewModel.articles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    StudyAdminScaffold(
        role = StudyRole.CONTENT_CREATOR,
        title = "Content Creator",
        currentRoute = StudyRoutes.CREATOR_DASHBOARD,
        onNavigate = onNavigate,
        onSignOut = onSignOut,
        snackbarHostState = snackbarHostState,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewArticle,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New article") }
            )
        }
    ) { padding ->
        if (isLoading) {
            LoadingState(Modifier.padding(padding), "Loading your content...")
            return@StudyAdminScaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item { StudyDemoBar() }
            item { SectionHeader("Content library") }
            item {
                StudyStatGrid(
                    stats = listOf(
                        "Articles" to counts.articles.toString(),
                        "Quizzes" to counts.quizzes.toString(),
                        "Questions" to counts.questions.toString()
                    ),
                    columns = 3,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) { (label, value), modifier -> MetricCard(label, value, modifier) }
            }
            item { SectionHeader("Article pipeline") }
            item {
                StudyStatGrid(
                    stats = listOf(
                        "Draft" to counts.drafts.toString(),
                        "Pending Review" to counts.pending.toString(),
                        "Published" to counts.published.toString()
                    ),
                    columns = 3,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) { (label, value), modifier ->
                    MetricCard(
                        label, value, modifier,
                        accent = statusColor(
                            when (label) {
                                "Draft" -> ContentStatus.DRAFT
                                "Pending Review" -> ContentStatus.PENDING_REVIEW
                                else -> ContentStatus.PUBLISHED
                            }
                        )
                    )
                }
            }

            item {
                SectionHeader(
                    "Recent articles",
                    actionLabel = "All articles",
                    onAction = { onNavigate(StudyRoutes.CREATOR_ARTICLES) }
                )
            }
            items(articles.take(6), key = { it.id }) { article ->
                ListItem(
                    headlineContent = {
                        Text(article.title.current(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    supportingContent = {
                        Text(
                            viewModel.subjectName(article.subjectId) + " • " +
                                viewModel.topicName(article.topicId)
                        )
                    },
                    trailingContent = { StudyPill(article.status.label, statusColor(article.status)) },
                    modifier = Modifier.clickable { onEditArticle(article.id) }
                )
                HorizontalDivider()
            }
        }
    }
}

/* =========================================================== article list */

@Composable
fun CreatorArticlesScreen(
    viewModel: CreatorViewModel,
    onNavigate: (String) -> Unit,
    onNewArticle: () -> Unit,
    onEditArticle: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val articles by viewModel.articles.collectAsState()
    val filter by viewModel.statusFilter.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var pendingDelete by remember { mutableStateOf<String?>(null) }

    pendingDelete?.let { id ->
        ConfirmDialog(
            title = "Delete article?",
            message = "This removes the article from the local demo content.",
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
        role = StudyRole.CONTENT_CREATOR,
        title = "Articles",
        currentRoute = StudyRoutes.CREATOR_ARTICLES,
        onNavigate = onNavigate,
        onSignOut = onSignOut,
        snackbarHostState = snackbarHostState,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewArticle,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New") }
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
                    onClick = { viewModel.setStatusFilter(null) },
                    label = { Text("All") }
                )
                ContentStatus.entries.forEach { status ->
                    FilterChip(
                        selected = filter == status,
                        onClick = { viewModel.setStatusFilter(if (filter == status) null else status) },
                        label = { Text(status.label) }
                    )
                }
            }

            if (articles.isEmpty()) {
                EmptyState(
                    title = "No articles",
                    description = "Create your first study article.",
                    actionLabel = "New article",
                    onAction = onNewArticle
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
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
                                            article.readingMinutes + " min"
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
                                            scope.launch { snackbarHostState.showSnackbar("Published") }
                                        }) {
                                            Icon(Icons.Default.Publish, contentDescription = "Publish")
                                        }
                                    }
                                    IconButton(onClick = { onEditArticle(article.id) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
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

/* ========================================================= article editor */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleEditorScreen(
    viewModel: CreatorViewModel,
    articleId: String?,
    authorName: String,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val form by viewModel.articleForm.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showErrors by remember { mutableStateOf(false) }
    // Which language the content fields below are bound to.
    val formState = rememberLocalizedFormState()

    LaunchedEffect(articleId) {
        if (articleId.isNullOrBlank()) viewModel.newArticle() else viewModel.loadArticle(articleId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (articleId.isNullOrBlank()) "Create Article" else "Edit Article") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (viewModel.saveArticle(ContentStatus.DRAFT, authorName)) {
                            scope.launch { snackbarHostState.showSnackbar("Saved as draft") }
                            onDone()
                        } else {
                            showErrors = true
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Save Draft") }
                OutlinedButton(
                    onClick = {
                        showErrors = true
                        if (viewModel.saveArticle(ContentStatus.PENDING_REVIEW, authorName)) {
                            scope.launch { snackbarHostState.showSnackbar("Submitted for review") }
                            onDone()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Submit") }
                Button(
                    onClick = {
                        showErrors = true
                        if (viewModel.saveArticle(ContentStatus.PUBLISHED, authorName)) {
                            scope.launch { snackbarHostState.showSnackbar("Published") }
                            onDone()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Publish") }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            SectionHeader("Subject & topic")
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                subjects.filter { it.isEnabled }.forEach { subject ->
                    FilterChip(
                        selected = form.subjectId == subject.id,
                        onClick = {
                            viewModel.updateArticleForm {
                                it.copy(
                                    subjectId = subject.id,
                                    topicId = viewModel.topicsOf(subject.id).firstOrNull()?.id.orEmpty()
                                )
                            }
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
                viewModel.topicsOf(form.subjectId).forEach { topic ->
                    FilterChip(
                        selected = form.topicId == topic.id,
                        onClick = { viewModel.updateArticleForm { it.copy(topicId = topic.id) } },
                        label = { Text(topic.name.current()) }
                    )
                }
            }
            if (showErrors && form.topicError != null) {
                CreatorFieldError(form.topicError!!)
            }

            SectionHeader("Content")

            // One set of boxes for both languages; the tab decides which side of
            // each field they are bound to. Writing one language is enough to
            // publish - the tab badge shows what is still outstanding.
            LocalizedFormHeader(state = formState, fields = form.localizedFields)
            Spacer(Modifier.height(10.dp))

            LocalizedOutlinedTextField(
                value = form.title,
                onValueChange = { v -> viewModel.updateArticleForm { it.copy(title = v) } },
                language = formState.language,
                label = lt("Title", "శీర్షిక"),
                required = true,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            if (showErrors && form.titleError != null) {
                CreatorFieldError(form.titleError!!)
            }
            LocalizedOutlinedTextField(
                value = form.description,
                onValueChange = { v -> viewModel.updateArticleForm { it.copy(description = v) } },
                language = formState.language,
                label = lt("Short description", "సంక్షిప్త వివరణ"),
                minLines = 2,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            LocalizedOutlinedTextField(
                value = form.content,
                onValueChange = { v -> viewModel.updateArticleForm { it.copy(content = v) } },
                language = formState.language,
                label = lt("Article content", "కథనం కంటెంట్"),
                required = true,
                minLines = 8,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            if (showErrors && form.contentError != null) {
                CreatorFieldError(form.contentError!!)
            } else {
                // Counts the language on screen, not the fallback.
                Text(
                    form.content.rawFor(formState.language).length.toString() +
                        (if (currentLanguage() == AppLanguage.TELUGU) " అక్షరాలు" else " characters"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            // Bullets are paired up by line position on save, so keeping the same
            // order in both languages gives each bullet its translation.
            LocalizedOutlinedTextField(
                value = form.pointsText,
                onValueChange = { v -> viewModel.updateArticleForm { it.copy(pointsText = v) } },
                language = formState.language,
                label = lt("Important points (one per line)", "ముఖ్యాంశాలు (ఒక్క లెంకి ఒక్కటి)"),
                minLines = 4,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            LocalizedOutlinedTextField(
                value = form.examplesText,
                onValueChange = { v -> viewModel.updateArticleForm { it.copy(examplesText = v) } },
                language = formState.language,
                label = lt("Examples (one per line)", "ఉదాహరణలు (ఒక్క లెంకి ఒక్కటి)"),
                minLines = 3,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            OutlinedTextField(
                value = form.readingMinutes,
                onValueChange = { v ->
                    viewModel.updateArticleForm { it.copy(readingMinutes = v.filter { c -> c.isDigit() }) }
                },
                label = { Text("Estimated reading time (minutes)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

/* ======================================================== questions list */

@Composable
fun CreatorQuestionsScreen(
    viewModel: CreatorViewModel,
    onNavigate: (String) -> Unit,
    onNewQuestion: () -> Unit,
    onEditQuestion: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val questions by viewModel.questions.collectAsState()
    val quizzes by viewModel.quizzes.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var pendingDelete by remember { mutableStateOf<String?>(null) }

    pendingDelete?.let { id ->
        ConfirmDialog(
            title = "Delete question?",
            message = "The question is removed from the local bank.",
            confirmLabel = "Delete",
            destructive = true,
            onConfirm = {
                viewModel.deleteQuestion(id)
                pendingDelete = null
                scope.launch { snackbarHostState.showSnackbar("Question deleted") }
            },
            onDismiss = { pendingDelete = null }
        )
    }

    StudyAdminScaffold(
        role = StudyRole.CONTENT_CREATOR,
        title = "Questions & Quizzes",
        currentRoute = StudyRoutes.CREATOR_QUESTIONS,
        onNavigate = onNavigate,
        onSignOut = onSignOut,
        snackbarHostState = snackbarHostState,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewQuestion,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New question") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                SectionHeader(
                    quizzes.size.toString() + " quizzes",
                    subtitle = "One quiz is generated per topic with at least four questions"
                )
            }
            item { SectionHeader(questions.size.toString() + " questions (showing latest)") }
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
                                StudyPill("Ans: " + question.correctOption.current().take(14), MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { onEditQuestion(question.id) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { pendingDelete = question.id }) {
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

/* ======================================================== question editor */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionEditorScreen(
    viewModel: CreatorViewModel,
    questionId: String?,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val form by viewModel.questionForm.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showErrors by remember { mutableStateOf(false) }
    // Which language the content fields below are bound to.
    val formState = rememberLocalizedFormState()

    LaunchedEffect(questionId) {
        if (questionId.isNullOrBlank()) viewModel.newQuestion() else viewModel.loadQuestion(questionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (questionId.isNullOrBlank()) "Add Question" else "Edit Question") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        showErrors = true
                        if (viewModel.saveQuestion(ContentStatus.DRAFT)) {
                            scope.launch { snackbarHostState.showSnackbar("Saved as draft") }
                            onDone()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Save Draft") }
                Button(
                    onClick = {
                        showErrors = true
                        if (viewModel.saveQuestion(ContentStatus.PUBLISHED)) {
                            scope.launch { snackbarHostState.showSnackbar("Question published") }
                            onDone()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Publish") }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            SectionHeader("Subject & topic")
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                subjects.filter { it.isEnabled }.forEach { subject ->
                    FilterChip(
                        selected = form.subjectId == subject.id,
                        onClick = {
                            viewModel.updateQuestionForm {
                                it.copy(
                                    subjectId = subject.id,
                                    topicId = viewModel.topicsOf(subject.id).firstOrNull()?.id.orEmpty()
                                )
                            }
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
                viewModel.topicsOf(form.subjectId).forEach { topic ->
                    FilterChip(
                        selected = form.topicId == topic.id,
                        onClick = { viewModel.updateQuestionForm { it.copy(topicId = topic.id) } },
                        label = { Text(topic.name.current()) }
                    )
                }
            }

            SectionHeader("Question")

            LocalizedFormHeader(state = formState, fields = form.localizedFields)
            Spacer(Modifier.height(10.dp))

            LocalizedOutlinedTextField(
                value = form.text,
                onValueChange = { v -> viewModel.updateQuestionForm { it.copy(text = v) } },
                language = formState.language,
                label = lt("Question text", "ప్రశ్న"),
                required = true,
                minLines = 3,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            if (showErrors && form.textError != null) {
                CreatorFieldError(form.textError!!)
            }

            SectionHeader("Options", subtitle = "Tap an option to mark it as the correct answer")
            // Option slots are shared across languages: slot B is the same option
            // in Telugu and English, which is why the correct answer is one index
            // rather than one per language. Editing only touches the active side.
            form.options.forEachIndexed { index, option ->
                Column(Modifier.padding(horizontal = 16.dp)) {
                    LocalizedOutlinedTextField(
                        value = option,
                        onValueChange = { v ->
                            viewModel.updateQuestionForm {
                                it.copy(options = it.options.toMutableList().also { list -> list[index] = v })
                            }
                        },
                        language = formState.language,
                        label = lt("Option " + ('A' + index), "ఆప్షన్ " + ('A' + index)),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    OptionRow(
                        letter = ('A' + index).toString(),
                        text = option.rawFor(formState.language).ifBlank {
                            if (currentLanguage() == AppLanguage.TELUGU) "(ఖాళీ)" else "(empty)"
                        },
                        selected = form.correctIndex == index,
                        correct = if (form.correctIndex == index) true else null,
                        onClick = { viewModel.updateQuestionForm { it.copy(correctIndex = index) } }
                    )
                }
            }
            if (showErrors && form.optionsError != null) {
                CreatorFieldError(form.optionsError!!)
            }

            SectionHeader("Explanation & metadata")
            LocalizedOutlinedTextField(
                value = form.explanation,
                onValueChange = { v -> viewModel.updateQuestionForm { it.copy(explanation = v) } },
                language = formState.language,
                label = lt("Explanation", "వివరణ"),
                minLines = 2,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Difficulty.entries.forEach { level ->
                    FilterChip(
                        selected = form.difficulty == level,
                        onClick = { viewModel.updateQuestionForm { it.copy(difficulty = level) } },
                        label = { Text(level.label) }
                    )
                }
            }
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuestionType.entries.forEach { type ->
                    FilterChip(
                        selected = form.type == type,
                        onClick = { viewModel.updateQuestionForm { it.copy(type = type) } },
                        label = { Text(type.label) }
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

/**
 * Validation message for a bilingual field. The message is itself bilingual - the
 * creator reads it in the app language they chose, whichever content tab they
 * happen to be typing in.
 */
@Composable
private fun CreatorFieldError(message: LocalizedText) {
    Text(
        message.current(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
    )
}
