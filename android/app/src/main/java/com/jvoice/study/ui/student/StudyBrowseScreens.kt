package com.jvoice.study.ui.student

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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import com.jvoice.study.data.model.Quiz
import com.jvoice.study.data.model.Subject
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.jvoice.study.data.model.QuestionSource
import com.jvoice.study.data.model.Question
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jvoice.news.components.EmptyState
import com.jvoice.news.components.SectionHeader
import com.jvoice.study.components.StudyPill
import com.jvoice.study.components.accuracyColor
import kotlinx.coroutines.launch
import com.jvoice.core.i18n.current
import com.jvoice.core.i18n.Strings
import com.jvoice.core.i18n.tr

/* =============================================== subject list (Study tab) */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyBrowseScreen(
    viewModel: StudyBrowseViewModel,
    onOpenSubject: (String) -> Unit,
    onOpenTopic: (String) -> Unit,
    onChangeExam: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val subjects by viewModel.subjects.collectAsState()
    val query by viewModel.query.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val completed by viewModel.completed.collectAsState()
    val trackLabel by viewModel.trackLabel.collectAsState()
    val enabled = subjects.filter { it.isEnabled }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(trackLabel?.let { it + " • Study" } ?: "Study • చదువు") },
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
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setQuery,
                placeholder = { Text("Search topics or articles") },
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

            if (query.isNotBlank()) {
                if (searchResults.isEmpty()) {
                    EmptyState(
                        title = "No matches",
                        description = "No article or topic matches that search."
                    )
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 20.dp)) {
                        item { SectionHeader(searchResults.size.toString() + " results") }
                        items(searchResults, key = { it.id }) { article ->
                            Card(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clickable { onOpenTopic(article.topicId) },
                                shape = RoundedCornerShape(14.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(Modifier.padding(14.dp)) {
                                    Text(article.title.current(), style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        viewModel.subjectName(article.subjectId) + "  •  " +
                                            article.readingMinutes + " min read",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                return@Column
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(span = { GridItemSpan(2) }) {
                    SectionHeader(
                        enabled.size.toString() + " subjects on this paper",
                        subtitle = completed.size.toString() + " topics completed"
                    )
                }
                items(enabled, key = { it.id }) { subject ->
                    SubjectTile(
                        subject = subject,
                        progress = viewModel.subjectProgress(subject.id),
                        topics = viewModel.topicsOf(subject.id).size,
                        articles = viewModel.articleCount(subject.id),
                        questions = viewModel.questionCountForSubject(subject.id),
                        onClick = { onOpenSubject(subject.id) }
                    )
                }
            }
        }
    }
}

/* --------------------------------------------------------- category tile */

/**
 * Two of these sit in a row. The top half is the subject's image band — a
 * tinted gradient carrying its glyph — so the grid reads as picture cards
 * rather than a list squeezed into two columns.
 */
private val subjectTints = listOf(
    Color(0xFF7B4DFF) to Color(0xFFB08CFF),
    Color(0xFF1462C4) to Color(0xFF5FA8F5),
    Color(0xFF1B7F4B) to Color(0xFF5FC98F),
    Color(0xFFB96B00) to Color(0xFFF0B45C),
    Color(0xFFC02B2B) to Color(0xFFF08080),
    Color(0xFF0F8A8A) to Color(0xFF5FD0D0)
)

@Composable
private fun SubjectTile(
    subject: Subject,
    progress: Int,
    topics: Int,
    articles: Int,
    questions: Int,
    onClick: () -> Unit
) {
    // stable per-subject colour so a tile always looks the same
    val tint = subjectTints[(subject.id.hashCode().let { if (it < 0) -it else it }) % subjectTints.size]

    Card(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // ---- image band
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.7f)
                    .background(Brush.linearGradient(listOf(tint.first, tint.second))),
                contentAlignment = Alignment.Center
            ) {
                Text(subject.emoji, style = MaterialTheme.typography.displaySmall)

                Surface(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(topStart = 10.dp, bottomEnd = 18.dp),
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text(
                        progress.toString() + "%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = tint.first,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // ---- label + counts
            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    subject.name.en,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    subject.name.te,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    topics.toString() + " topics • " + articles + " articles",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    questions.toString() + " questions",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/* ================================================== topics inside a subject */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectTopicsScreen(
    viewModel: StudyBrowseViewModel,
    subjectId: String,
    onOpenTopic: (String) -> Unit,
    onStartQuiz: (String) -> Unit,
    onBack: () -> Unit
) {
    val subject = viewModel.subjectById(subjectId)
    val completed by viewModel.completed.collectAsState()
    val topics = viewModel.topicsOf(subjectId)

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
        if (topics.isEmpty()) {
            EmptyState(
                title = "No topics yet",
                description = "A Study Admin can add topics to this subject.",
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(2) }) {
                SectionHeader(
                    topics.size.toString() + " topics",
                    subtitle = subject?.name?.current().orEmpty() + "  •  " +
                        viewModel.subjectProgress(subjectId) + "% complete"
                )
            }
            items(topics, key = { it.id }) { topic ->
                val perf = viewModel.topicAccuracy(topic.id)
                TopicTile(
                    topic = topic,
                    subjectEmoji = subject?.emoji ?: "📘",
                    isDone = completed.contains(topic.id),
                    articles = viewModel.articleCountForTopic(topic.id),
                    questions = viewModel.questionCount(topic.id),
                    accuracy = if (perf != null && perf.attempted) perf.accuracy else null,
                    onOpen = { onOpenTopic(topic.id) },
                    onQuiz = { onStartQuiz(topic.id) }
                )
            }
        }
    }
}

/* ------------------------------------------------------------ topic tile */

/**
 * Same picture-card treatment as the category grid, one level down. The band
 * carries the chapter number and a done tick; the body carries what is inside
 * the topic and a shortcut straight into its quiz.
 */
@Composable
private fun TopicTile(
    topic: com.jvoice.study.data.model.Topic,
    subjectEmoji: String,
    isDone: Boolean,
    articles: Int,
    questions: Int,
    accuracy: Int?,
    onOpen: () -> Unit,
    onQuiz: () -> Unit
) {
    val tint = subjectTints[(topic.id.hashCode().let { if (it < 0) -it else it }) % subjectTints.size]

    Card(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.7f)
                    .background(Brush.linearGradient(listOf(tint.first, tint.second))),
                contentAlignment = Alignment.Center
            ) {
                Text(subjectEmoji, style = MaterialTheme.typography.displaySmall)

                Surface(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(bottomStart = 10.dp, topEnd = 18.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        "Ch " + topic.order,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = tint.first,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (isDone) {
                    Surface(
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(topStart = 10.dp, bottomEnd = 18.dp),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = tint.first,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Done",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = tint.first
                            )
                        }
                    }
                } else if (accuracy != null) {
                    Surface(
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(topStart = 10.dp, bottomEnd = 18.dp),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Text(
                            accuracy.toString() + "%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = tint.first,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Column(Modifier.padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 4.dp)) {
                Text(
                    topic.name.en,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    topic.name.te,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    articles.toString() + " material • " + questions + " questions",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudyPill(topic.difficulty.label, MaterialTheme.colorScheme.outline)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onQuiz) {
                    Icon(
                        Icons.Default.Quiz,
                        contentDescription = "Start quiz",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyArticleScreen(
    viewModel: StudyBrowseViewModel,
    topicId: String,
    onStartQuiz: (String) -> Unit,
    onOpenTopic: (String) -> Unit,
    onBack: () -> Unit
) {
    val topic = viewModel.topicById(topicId)
    val articles = viewModel.articlesForTopic(topicId)
    val completed by viewModel.completed.collectAsState()
    val isDone = completed.contains(topicId)
    val (previous, next) = viewModel.neighbours(topicId)
    val quiz = viewModel.quizForTopic(topicId)
    val sampleQuestions = viewModel.sampleQuestions(topicId)
    val pastQuestions = viewModel.previousQuestions(topicId)
    val allSets = viewModel.quizSets(topicId)
    // Split on the id, not the title: titles are translated now, so matching
    // "Previously asked" as text would classify every Telugu set as practice.
    val practiceSets = allSets.filterNot { it.isPastPaperSet }
    val pastSets = allSets.filter { it.isPastPaperSet }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Study material and Quizzes are separate tabs inside the topic.
    var tab by remember(topicId) { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topic?.name?.current() ?: tr(Strings.Study.topic)) },
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
                        val nowDone = viewModel.toggleCompleted(topicId)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (nowDone) "Marked as completed" else "Marked as not completed"
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (isDone) "Completed" else "Mark done")
                }
                Button(
                    onClick = {
                        if (quiz == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar("No quiz available for this topic yet")
                            }
                        } else {
                            onStartQuiz(topicId)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Take Quiz")
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    text = { Text("Material (" + articles.size + ")") }
                )
                Tab(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    text = { Text("Quizzes (" + practiceSets.size + ")") }
                )
                Tab(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    text = { Text("Past papers (" + pastQuestions.size + ")") }
                )
            }

            when (tab) {
                0 -> TopicMaterialTab(
                    viewModel = viewModel,
                    topicId = topicId,
                    articles = articles,
                    isDone = isDone,
                    previous = previous,
                    next = next,
                    hasQuiz = quiz != null,
                    onStartQuiz = onStartQuiz,
                    onOpenTopic = onOpenTopic,
                    onSeeQuizzes = { tab = 1 }
                )
                1 -> TopicQuizzesTab(
                    viewModel = viewModel,
                    quizSets = practiceSets,
                    totalQuestions = sampleQuestions.size,
                    emptyTitle = "No quiz sets yet",
                    emptyBody = "Nothing has been added to this topic's practice bank.",
                    headerNoun = "quiz sets"
                )
                else -> TopicQuizzesTab(
                    viewModel = viewModel,
                    quizSets = pastSets,
                    totalQuestions = pastQuestions.size,
                    emptyTitle = "No past paper questions",
                    emptyBody = "Nothing this topic has been asked in a real exam has been recorded yet.",
                    headerNoun = "past paper sets"
                )
            }
        }
    }
}

/* ------------------------------------------------------------ material tab */

@Composable
private fun TopicMaterialTab(
    viewModel: StudyBrowseViewModel,
    topicId: String,
    articles: List<com.jvoice.study.data.model.StudyArticle>,
    isDone: Boolean,
    previous: com.jvoice.study.data.model.Topic?,
    next: com.jvoice.study.data.model.Topic?,
    hasQuiz: Boolean,
    onStartQuiz: (String) -> Unit,
    onOpenTopic: (String) -> Unit,
    onSeeQuizzes: () -> Unit
) {
    if (articles.isEmpty()) {
        EmptyState(
            title = "No study material yet",
            description = "Nothing has been published for this topic. The Quizzes tab may still have questions.",
            actionLabel = if (hasQuiz) "Take Quiz" else null,
            onAction = if (hasQuiz) ({ onStartQuiz(topicId) }) else null
        )
        return
    }

    // a topic can carry several pieces — list them, then read one
    var openId by remember(topicId) { mutableStateOf<String?>(null) }
    val article = articles.firstOrNull { it.id == openId }

    if (article == null) {
        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                SectionHeader(
                    articles.size.toString() + (if (articles.size == 1) " article" else " articles"),
                    subtitle = "Published for this topic"
                )
            }
            itemsIndexed(articles) { index, item ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                        .clickable { openId = item.id },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                ) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    (index + 1).toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                item.title.current(),
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                item.authorName + "  •  " + item.readingMinutes + " min read",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Read",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onSeeQuizzes, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("See questions for this topic")
                }
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                TopicNeighbours(previous = previous, next = next, onOpenTopic = onOpenTopic)
            }
        }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TextButton(onClick = { openId = null }, modifier = Modifier.padding(start = 8.dp)) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("All material (" + articles.size + ")")
        }

        Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            Text(article.title.current(), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StudyPill(viewModel.subjectName(article.subjectId), MaterialTheme.colorScheme.primary)
                StudyPill(article.readingMinutes.toString() + " min read", MaterialTheme.colorScheme.outline)
                if (isDone) {
                    StudyPill("Completed", MaterialTheme.colorScheme.tertiary)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                article.description.current(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider(Modifier.padding(horizontal = 16.dp))

        Text(
            article.content.current(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )

        if (article.importantPoints.isNotEmpty()) {
            SectionHeader("Important Points", subtitle = "ముఖ్యాంశాలు")
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(Modifier.padding(14.dp)) {
                    article.importantPoints.forEach { point ->
                        Row(Modifier.padding(vertical = 4.dp)) {
                            Text("•  ", style = MaterialTheme.typography.bodyLarge)
                            Text(point.current(), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        if (article.formulas.isNotEmpty()) {
            SectionHeader("Key Formulas", subtitle = "సూత్రాలు")
            Column(Modifier.padding(horizontal = 16.dp)) {
                article.formulas.forEach { formula ->
                    Surface(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ) {
                        Text(
                            formula.current(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        if (article.examples.isNotEmpty()) {
            SectionHeader("Examples", subtitle = "ఉదాహరణలు")
            Column(Modifier.padding(horizontal = 16.dp)) {
                article.examples.forEachIndexed { index, example ->
                    Row(Modifier.padding(vertical = 5.dp)) {
                        Text(
                            (index + 1).toString() + ".  ",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(example.current(), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onSeeQuizzes, modifier = Modifier.padding(horizontal = 8.dp)) {
            Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("See questions for this topic")
        }

        HorizontalDivider(Modifier.padding(horizontal = 16.dp))

        TopicNeighbours(previous = previous, next = next, onOpenTopic = onOpenTopic)

        Text(
            "Author: " + article.authorName + " • demo content",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

/** Walk to the chapter before or after this one. */
@Composable
private fun TopicNeighbours(
    previous: com.jvoice.study.data.model.Topic?,
    next: com.jvoice.study.data.model.Topic?,
    onOpenTopic: (String) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (previous != null) {
            TextButton(onClick = { onOpenTopic(previous.id) }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    previous.name.en,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(110.dp)
                )
            }
        } else {
            Spacer(Modifier.width(8.dp))
        }
        if (next != null) {
            TextButton(onClick = { onOpenTopic(next.id) }) {
                Text(
                    next.name.en,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(110.dp)
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/* ------------------------------------------------------------- quizzes tab */

/**
 * A topic holds several practice sets, not one quiz. Sets are laid out two to
 * a row; picking one opens it in place and answers are marked as you tap.
 */
@Composable
private fun TopicQuizzesTab(
    viewModel: StudyBrowseViewModel,
    quizSets: List<Quiz>,
    totalQuestions: Int,
    emptyTitle: String,
    emptyBody: String,
    headerNoun: String
) {
    var active by remember { mutableStateOf<Quiz?>(null) }

    val current = active
    if (current != null) {
        QuizSetPlayer(
            quiz = current,
            questions = viewModel.questionsOfQuiz(current),
            onBack = { active = null }
        )
        return
    }

    if (quizSets.isEmpty()) {
        EmptyState(title = emptyTitle, description = emptyBody)
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            SectionHeader(
                quizSets.size.toString() + " " + headerNoun,
                subtitle = totalQuestions.toString() + " questions"
            )
        }
        items(quizSets, key = { it.id }) { set ->
            QuizSetTile(set = set, onClick = { active = set })
        }
    }
}

@Composable
private fun QuizSetTile(set: Quiz, onClick: () -> Unit) {
    val past = set.isPastPaperSet
    val tint = if (past) {
        Color(0xFF0F8A8A) to Color(0xFF5FD0D0)
    } else {
        subjectTints[(set.id.hashCode().let { if (it < 0) -it else it }) % subjectTints.size]
    }

    Card(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.9f)
                    .background(Brush.linearGradient(listOf(tint.first, tint.second))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    set.questionIds.size.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Surface(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(topStart = 10.dp, bottomEnd = 18.dp),
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text(
                        if (past) "past paper" else "questions",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = tint.first,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(set.title.current(), style = MaterialTheme.typography.titleSmall)
                Text(
                    "about " + set.durationMinutes + " min",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/* ----------------------------------------------------------- quiz player */

private val CorrectGreen = Color(0xFF1B7F4B)
private val CorrectGreenBg = Color(0x1A1B7F4B)
private val WrongRed = Color(0xFFC02B2B)
private val WrongRedBg = Color(0x1AC02B2B)

/**
 * Answer-as-you-go practice. Tapping an option locks that question in: the
 * chosen option turns green when right and red when wrong, and the right
 * answer is revealed either way.
 */
@Composable
private fun QuizSetPlayer(quiz: Quiz, questions: List<Question>, onBack: () -> Unit) {
    // questionId -> the option index the student picked
    val picked = remember(quiz.id) { mutableStateMapOf<String, Int>() }
    val answered = picked.size
    val score = picked.count { (id, choice) ->
        questions.firstOrNull { it.id == id }?.correctIndex == choice
    }

    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "All sets")
                }
                Column(Modifier.weight(1f)) {
                    Text(quiz.title.current(), style = MaterialTheme.typography.titleMedium)
                    Text(
                        answered.toString() + " of " + questions.size + " answered",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (answered > 0) {
                    StudyPill(
                        score.toString() + " / " + answered,
                        if (score * 2 >= answered) CorrectGreen else WrongRed
                    )
                }
            }
            if (answered > 0) {
                LinearProgressIndicator(
                    progress = { answered.toFloat() / questions.size.coerceAtLeast(1) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }
            Spacer(Modifier.height(6.dp))
        }

        itemsIndexed(questions) { index, question ->
            PlayableQuestion(
                number = index + 1,
                question = question,
                chosen = picked[question.id],
                onPick = { choice -> if (!picked.containsKey(question.id)) picked[question.id] = choice }
            )
        }

        if (questions.isNotEmpty()) {
            item {
                if (answered == questions.size) {
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (score * 2 >= questions.size) CorrectGreenBg else WrongRedBg
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Set complete — " + score + " of " + questions.size + " correct",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (score * 2 >= questions.size) CorrectGreen else WrongRed
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Try another set from this topic.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { picked.clear() }) { Text("Retry set") }
                                Button(onClick = onBack) { Text("All sets") }
                            }
                        }
                    }
                } else {
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) { Text("Back to all sets") }
                }
            }
        }
    }
}

/** One question. Untouched it is neutral; once tapped it shows green / red. */
@Composable
private fun PlayableQuestion(
    number: Int,
    question: Question,
    chosen: Int?,
    onPick: (Int) -> Unit
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(Modifier.padding(14.dp)) {
            if (question.source == QuestionSource.PREVIOUS && question.paperLabel.isNotBlank()) {
                StudyPill(question.paperLabel, MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.height(8.dp))
            }

            Text(
                number.toString() + ".  " + question.text.current(),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(10.dp))

            question.options.forEachIndexed { index, option ->
                val isCorrect = index == question.correctIndex
                val isChosen = chosen == index
                val settled = chosen != null

                // green for the right answer once settled, red only for a wrong pick
                val border = when {
                    settled && isCorrect -> CorrectGreen
                    settled && isChosen -> WrongRed
                    else -> MaterialTheme.colorScheme.outlineVariant
                }
                val fill = when {
                    settled && isCorrect -> CorrectGreenBg
                    settled && isChosen -> WrongRedBg
                    else -> Color.Transparent
                }
                val labelColor = when {
                    settled && isCorrect -> CorrectGreen
                    settled && isChosen -> WrongRed
                    else -> MaterialTheme.colorScheme.onSurface
                }

                Surface(
                    color = fill,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.5.dp, border, RoundedCornerShape(12.dp))
                        .clickable(enabled = !settled) { onPick(index) }
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            ('A' + index).toString() + ".",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = labelColor
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            option.current(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = labelColor,
                            modifier = Modifier.weight(1f)
                        )
                        if (settled && isCorrect) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Correct",
                                tint = CorrectGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        } else if (settled && isChosen) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = "Wrong",
                                tint = WrongRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (chosen != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    if (chosen == question.correctIndex) "Correct" else
                        "Answer: " + question.correctOption.current(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (chosen == question.correctIndex) CorrectGreen else WrongRed
                )
                if (!question.explanation.isBlank) {
                    Text(
                        question.explanation.current(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }
        }
    }
}

/** One question with its answer and, for past papers, where it was asked. */
@Composable
private fun QuestionCard(question: Question) {
    var revealed by remember(question.id) { mutableStateOf(false) }

    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(Modifier.padding(14.dp)) {
            if (question.source == QuestionSource.PREVIOUS && question.paperLabel.isNotBlank()) {
                StudyPill(question.paperLabel, MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.height(8.dp))
            }

            Text(question.text.current(), style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))

            question.options.forEachIndexed { index, option ->
                val correct = revealed && index == question.correctIndex
                Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        ('A' + index).toString() + ".  ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (correct) FontWeight.Bold else FontWeight.Normal,
                        color = if (correct) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        option.current(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (correct) FontWeight.Bold else FontWeight.Normal,
                        color = if (correct) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { revealed = !revealed }) {
                    Text(if (revealed) "Hide answer" else "Show answer")
                }
                Spacer(Modifier.weight(1f))
                StudyPill(question.difficulty.label, MaterialTheme.colorScheme.outline)
            }

            if (revealed && !question.explanation.isBlank) {
                Text(
                    question.explanation.current(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
