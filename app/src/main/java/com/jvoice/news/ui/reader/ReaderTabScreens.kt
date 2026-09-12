package com.jvoice.news.ui.reader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Translate
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.jvoice.core.i18n.LanguagePreference
import com.jvoice.core.i18n.LanguagePreferenceCard
import com.jvoice.core.i18n.Strings
import com.jvoice.core.i18n.tr
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jvoice.news.components.ConfirmDialog
import com.jvoice.news.components.EmptyState
import com.jvoice.news.components.NewsCard
import com.jvoice.news.components.Pill
import com.jvoice.news.components.SectionHeader
import com.jvoice.news.data.model.NotificationType
import com.jvoice.news.data.model.User
import com.jvoice.news.utils.toRelativeTime
import kotlinx.coroutines.launch
import com.jvoice.core.i18n.current

/* ------------------------------------------------------------------ categories */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderCategoriesScreen(
    viewModel: ReaderViewModel,
    onOpenCategory: (String) -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val enabled = categories.filter { it.isEnabled }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Categories • విభాగాలు") }
            )
        },
        bottomBar = bottomBar
    ) { padding ->
        if (enabled.isEmpty()) {
            EmptyState(
                title = "No categories enabled",
                description = "A News Admin can enable categories from the admin dashboard.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(enabled, key = { it.id }) { category ->
                    Card(
                        modifier = Modifier
                            .aspectRatio(1.35f)
                            .clickable { onOpenCategory(category.id) },
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(category.emoji, style = MaterialTheme.typography.headlineSmall)
                            Column {
                                Text(category.name.en, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    category.name.te,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    viewModel.articlesInCategory(category.id).size.toString() + " articles",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ category feed */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryNewsScreen(
    viewModel: ReaderViewModel,
    categoryId: String,
    onOpenArticle: (String) -> Unit,
    onBack: () -> Unit
) {
    val savedIds by viewModel.savedIds.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val category = categories.firstOrNull { it.id == categoryId }
    val articles = viewModel.articlesInCategory(categoryId)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category?.displayName ?: "Category") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (articles.isEmpty()) {
            EmptyState(
                title = "Nothing published here yet",
                description = "Articles approved in this category will show up here.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                items(articles, key = { it.id }) { article ->
                    NewsCard(
                        article = article,
                        categoryName = viewModel.categoryName(article.categoryId),
                        isSaved = savedIds.contains(article.id),
                        onClick = { onOpenArticle(article.id) },
                        onToggleSave = {
                            val saved = viewModel.toggleSave(article.id)
                            scope.launch {
                                snackbarHostState.showSnackbar(if (saved) "Saved" else "Removed")
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ saved */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSavedScreen(
    viewModel: ReaderViewModel,
    onOpenArticle: (String) -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val saved by viewModel.savedArticles.collectAsState()
    val savedIds by viewModel.savedIds.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var confirmClear by remember { mutableStateOf(false) }

    if (confirmClear) {
        ConfirmDialog(
            title = "Clear all bookmarks?",
            message = "This removes every saved article from this device. Demo data only.",
            confirmLabel = "Clear all",
            destructive = true,
            onConfirm = {
                viewModel.clearSaved()
                confirmClear = false
                scope.launch { snackbarHostState.showSnackbar("All bookmarks cleared") }
            },
            onDismiss = { confirmClear = false }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Saved • సేవ్ చేసినవి") },
                actions = {
                    if (saved.isNotEmpty()) {
                        IconButton(onClick = { confirmClear = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear all")
                        }
                    }
                }
            )
        },
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (saved.isEmpty()) {
            EmptyState(
                title = "No saved articles",
                description = "Tap the bookmark icon on any news card to read it later.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                items(saved, key = { it.id }) { article ->
                    NewsCard(
                        article = article,
                        categoryName = viewModel.categoryName(article.categoryId),
                        isSaved = savedIds.contains(article.id),
                        onClick = { onOpenArticle(article.id) },
                        onToggleSave = {
                            viewModel.toggleSave(article.id)
                            scope.launch {
                                snackbarHostState.showSnackbar("Removed from bookmarks")
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ notifications */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderNotificationsScreen(
    viewModel: ReaderViewModel,
    onOpenArticle: (String) -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notifications") },
                actions = {
                    if (notifications.any { !it.isRead }) {
                        IconButton(onClick = { viewModel.markAllRead() }) {
                            Icon(Icons.Default.DoneAll, contentDescription = "Mark all read")
                        }
                    }
                }
            )
        },
        bottomBar = bottomBar
    ) { padding ->
        if (notifications.isEmpty()) {
            EmptyState(
                title = "No notifications",
                description = "Breaking news alerts will appear here.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(notifications, key = { it.id }) { item ->
                    val accent = when (item.type) {
                        NotificationType.BREAKING -> MaterialTheme.colorScheme.error
                        NotificationType.APPROVAL -> MaterialTheme.colorScheme.tertiary
                        NotificationType.REJECTION -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
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
                                        if (item.type == NotificationType.BREAKING) Icons.Default.Campaign
                                        else Icons.Default.Notifications,
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
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    item.timeMillis.toRelativeTime(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        },
                        trailingContent = {
                            if (!item.isRead) {
                                Pill("NEW", MaterialTheme.colorScheme.primary)
                            }
                        },
                        modifier = Modifier.clickable {
                            viewModel.markNotificationRead(item.id)
                            item.articleId?.let(onOpenArticle)
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ profile */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderProfileScreen(
    user: User?,
    viewModel: ReaderViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onSignOut: () -> Unit,
    onOpenSaved: () -> Unit = {},
    onOpenCategories: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    bottomBar: @Composable () -> Unit
) {
    val saved by viewModel.savedArticles.collectAsState()
    val location by viewModel.selectedLocation.collectAsState()
    val language by LanguagePreference.language.collectAsState()
    var confirmSignOut by remember { mutableStateOf(false) }
    var notificationsOn by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (confirmSignOut) {
        ConfirmDialog(
            title = "Switch role?",
            message = "You will go back to the dummy role selector.",
            confirmLabel = "Switch",
            onConfirm = {
                confirmSignOut = false
                onSignOut()
            },
            onDismiss = { confirmSignOut = false }
        )
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text(tr(Strings.Common.profile)) }) },
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                    AsyncImage(
                        model = user?.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(50))
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(user?.name ?: "Guest reader", style = MaterialTheme.typography.titleLarge)
                        Text(
                            user?.email ?: "demo@jvoice.demo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Pill(user?.role?.label ?: "Reader", MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileStat("Saved", saved.size.toString(), Modifier.weight(1f))
                    ProfileStat("Location", location, Modifier.weight(1f))
                    ProfileStat("Member", user?.joinedOn ?: "-", Modifier.weight(1f))
                }
            }

            item { SectionHeader("My news") }
            item {
                ListItem(
                    leadingContent = { Icon(Icons.Default.Bookmark, contentDescription = null) },
                    headlineContent = { Text("Saved news") },
                    supportingContent = { Text(saved.size.toString() + " saved articles") },
                    modifier = Modifier.clickable(onClick = onOpenSaved)
                )
            }
            item {
                ListItem(
                    leadingContent = { Icon(Icons.Default.Category, contentDescription = null) },
                    headlineContent = { Text("Categories") },
                    supportingContent = { Text("Browse news by category") },
                    modifier = Modifier.clickable(onClick = onOpenCategories)
                )
            }
            item {
                ListItem(
                    leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    headlineContent = { Text("Notifications") },
                    supportingContent = { Text("Breaking news alerts") },
                    modifier = Modifier.clickable(onClick = onOpenNotifications)
                )
            }

            item { SectionHeader("Preferences") }
            item {
                ListItem(
                    leadingContent = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                    headlineContent = { Text("Dark mode") },
                    supportingContent = { Text("Follows the system unless changed here") },
                    trailingContent = {
                        Switch(checked = isDarkTheme, onCheckedChange = onToggleTheme)
                    }
                )
            }
            item {
                ListItem(
                    leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    headlineContent = { Text("Push alerts") },
                    supportingContent = { Text("Local demo toggle only") },
                    trailingContent = {
                        Switch(checked = notificationsOn, onCheckedChange = { notificationsOn = it })
                    }
                )
            }
            // The reader's language lives here. This replaced a cosmetic
            // "Telugu first" switch that nothing read - the choice now drives
            // every headline, body, study page and question in both modules.
            item {
                LanguagePreferenceCard(
                    selected = language,
                    onSelect = { picked ->
                        LanguagePreference.set(picked)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                Strings.Language.changedTo.get(picked) + ": " + picked.labelNative
                            )
                        }
                    }
                )
            }
            item {
                ListItem(
                    leadingContent = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    headlineContent = { Text("Preferred location") },
                    supportingContent = { Text(location) }
                )
            }

            item { SectionHeader("About") }
            item {
                ListItem(
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                    headlineContent = { Text("J Voice — Module 1 prototype") },
                    supportingContent = {
                        Text("Version 1.0 • All content is dummy demo data. No backend or Firebase.")
                    }
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

@Composable
private fun ProfileStat(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
