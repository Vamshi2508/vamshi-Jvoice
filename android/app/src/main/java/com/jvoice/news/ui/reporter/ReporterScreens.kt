package com.jvoice.news.ui.reporter

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jvoice.news.components.ConfirmDialog
import com.jvoice.news.components.DemoDisclaimerBar
import com.jvoice.news.components.EmptyState
import com.jvoice.news.components.LoadingState
import com.jvoice.news.components.PullToRefreshBox
import com.jvoice.news.components.SectionHeader
import com.jvoice.news.components.StatCard
import com.jvoice.news.components.WorkflowNewsRow
import com.jvoice.news.data.model.NewsStatus
import com.jvoice.news.data.model.User
import com.jvoice.news.navigation.StatGrid
import com.jvoice.news.theme.StatusApproved
import com.jvoice.news.theme.StatusDraft
import com.jvoice.news.theme.StatusPublished
import com.jvoice.news.theme.StatusRejected
import com.jvoice.news.theme.StatusSubmitted
import com.jvoice.news.utils.toRelativeTime
import kotlinx.coroutines.launch
import com.jvoice.core.i18n.current

@Composable
fun ReporterBottomBar(currentRoute: String, onNavigate: (String) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute.contains("dashboard"),
            onClick = { onNavigate("dashboard") },
            icon = { Icon(Icons.Default.Edit, contentDescription = null) },
            label = { Text("Dashboard") }
        )
        NavigationBarItem(
            selected = currentRoute.contains("my_news"),
            onClick = { onNavigate("my_news") },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            label = { Text("My News") }
        )
    }
}

/* ------------------------------------------------------------------ dashboard */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReporterDashboardScreen(
    user: User?,
    viewModel: ReporterViewModel,
    onCreateNews: () -> Unit,
    onOpenMyNews: () -> Unit,
    onEditArticle: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val stats by viewModel.stats.collectAsState()
    val articles by viewModel.myArticles.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var confirmSignOut by remember { mutableStateOf(false) }

    if (confirmSignOut) {
        ConfirmDialog(
            title = "Switch role?",
            message = "You will return to the role selector.",
            confirmLabel = "Switch",
            onConfirm = { confirmSignOut = false; onSignOut() },
            onDismiss = { confirmSignOut = false }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Text("Reporter Dashboard", style = MaterialTheme.typography.titleMedium)
                        Text(
                            user?.name ?: "Reporter",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { confirmSignOut = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Switch role")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateNews,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Create News") }
            )
        }
    ) { padding ->
        if (isLoading) {
            LoadingState(Modifier.padding(padding), "Loading your desk...")
            return@Scaffold
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
                item { DemoDisclaimerBar() }
                item { SectionHeader("My statistics", subtitle = "నా గణాంకాలు") }
                item {
                    StatGrid(
                        stats = listOf(
                            "Total submitted" to stats.total.toString(),
                            "Pending review" to stats.pending.toString(),
                            "Approved" to stats.approved.toString(),
                            "Rejected" to stats.rejected.toString()
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) { (label, value), modifier ->
                        val accent = when (label) {
                            "Pending review" -> StatusSubmitted
                            "Approved" -> StatusApproved
                            "Rejected" -> StatusRejected
                            else -> MaterialTheme.colorScheme.primary
                        }
                        StatCard(label, value, modifier, accent)
                    }
                }
                item { Spacer(Modifier.height(6.dp)) }
                item {
                    StatGrid(
                        stats = listOf(
                            "Drafts" to stats.drafts.toString(),
                            "Published live" to stats.published.toString(),
                            "Sent back" to stats.sentBack.toString()
                        ),
                        columns = 3,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) { (label, value), modifier ->
                        val accent = when (label) {
                            "Drafts" -> StatusDraft
                            "Published live" -> StatusPublished
                            else -> MaterialTheme.colorScheme.secondary
                        }
                        StatCard(label, value, modifier, accent)
                    }
                }

                item {
                    SectionHeader(
                        "Recent articles",
                        actionLabel = "View all",
                        onAction = onOpenMyNews
                    )
                }

                if (articles.isEmpty()) {
                    item {
                        EmptyState(
                            title = "No articles yet",
                            description = "Tap Create News to file your first demo story.",
                            actionLabel = "Create News",
                            onAction = onCreateNews,
                            modifier = Modifier.height(240.dp)
                        )
                    }
                } else {
                    items(articles.take(5), key = { it.id }) { article ->
                        WorkflowNewsRow(
                            article = article,
                            categoryName = viewModel.categoryName(article.categoryId),
                            onClick = {
                                if (viewModel.canEdit(article)) onEditArticle(article.id) else onOpenMyNews()
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                        )
                    }
                }

                if (notifications.isNotEmpty()) {
                    item { SectionHeader("Desk updates") }
                    items(notifications.take(4), key = { "n_" + it.id }) { item ->
                        Column(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                            Text(item.title.current(), style = MaterialTheme.typography.titleSmall)
                            Text(
                                item.message.current(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                item.timeMillis.toRelativeTime(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ my news */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyNewsScreen(
    viewModel: ReporterViewModel,
    onEditArticle: (String) -> Unit,
    onCreateNews: () -> Unit
) {
    val articles by viewModel.myArticles.collectAsState()
    var filter by remember { mutableStateOf<NewsStatus?>(null) }
    var pendingDelete by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val filtered = if (filter == null) articles else articles.filter { it.status == filter }

    pendingDelete?.let { id ->
        ConfirmDialog(
            title = "Delete draft?",
            message = "This removes the draft from the local demo data.",
            confirmLabel = "Delete",
            destructive = true,
            onConfirm = {
                viewModel.deleteDraft(id)
                pendingDelete = null
                scope.launch { snackbarHostState.showSnackbar("Draft deleted") }
            },
            onDismiss = { pendingDelete = null }
        )
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("My News") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateNews,
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
                    onClick = { filter = null },
                    label = { Text("All (" + articles.size + ")") }
                )
                listOf(
                    NewsStatus.DRAFT,
                    NewsStatus.SUBMITTED,
                    NewsStatus.UNDER_REVIEW,
                    NewsStatus.APPROVED,
                    NewsStatus.PUBLISHED,
                    NewsStatus.REJECTED,
                    NewsStatus.SENT_BACK
                ).forEach { status ->
                    val count = articles.count { it.status == status }
                    FilterChip(
                        selected = filter == status,
                        onClick = { filter = if (filter == status) null else status },
                        label = { Text(status.label + " (" + count + ")") }
                    )
                }
            }

            if (filtered.isEmpty()) {
                EmptyState(
                    title = "Nothing here",
                    description = "No articles with this status yet.",
                    actionLabel = "Create News",
                    onAction = onCreateNews
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
                    items(filtered, key = { it.id }) { article ->
                        WorkflowNewsRow(
                            article = article,
                            categoryName = viewModel.categoryName(article.categoryId),
                            onClick = {
                                if (viewModel.canEdit(article)) {
                                    onEditArticle(article.id)
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Only drafts, rejected and sent-back articles can be edited"
                                        )
                                    }
                                }
                            },
                            trailing = {
                                if (article.status == NewsStatus.DRAFT) {
                                    IconButton(onClick = { pendingDelete = article.id }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete draft")
                                    }
                                }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}
