package com.jvoice.news.ui.reader

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.jvoice.news.components.EmptyState
import com.jvoice.news.components.NewsCard
import com.jvoice.news.components.SectionHeader
import com.jvoice.news.data.repository.NewsRepository
import kotlinx.coroutines.launch

/**
 * Fully local dummy search: matches on title, category (English or Telugu),
 * location, tags and reporter name. No search API involved.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: ReaderViewModel,
    onOpenArticle: (String) -> Unit,
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var categoryFilter by remember { mutableStateOf<String?>(null) }
    var locationFilter by remember { mutableStateOf<String?>(null) }

    val categories by viewModel.categories.collectAsState()
    val savedIds by viewModel.savedIds.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Recompute whenever the query, a filter, or the shared article list changes.
    val allArticles by NewsRepository.articles.collectAsState()
    val results = remember(query, categoryFilter, locationFilter, allArticles) {
        NewsRepository.search(query, categoryFilter, locationFilter)
    }
    val hasFilters = query.isNotBlank() || categoryFilter != null || locationFilter != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search news") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .focusRequester(focusRequester),
                placeholder = { Text("Title, category or location...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true
            )

            Text(
                "Filter by category",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = categoryFilter == null,
                    onClick = { categoryFilter = null },
                    label = { Text("All") }
                )
                categories.filter { it.isEnabled }.forEach { category ->
                    FilterChip(
                        selected = categoryFilter == category.id,
                        onClick = {
                            categoryFilter = if (categoryFilter == category.id) null else category.id
                        },
                        label = { Text(category.name.en) }
                    )
                }
            }

            Text(
                "Filter by location",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = locationFilter == null,
                    onClick = { locationFilter = null },
                    label = { Text("Anywhere") }
                )
                viewModel.locations.forEach { loc ->
                    FilterChip(
                        selected = locationFilter == loc,
                        onClick = { locationFilter = if (locationFilter == loc) null else loc },
                        label = { Text(loc) }
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            when {
                !hasFilters -> EmptyState(
                    title = "Search the demo archive",
                    description = "Try \"Hyderabad\", \"Education\", \"క్రికెట్\" or a reporter name."
                )
                results.isEmpty() -> EmptyState(
                    title = "No results",
                    description = "No demo article matches that search. Try a different keyword or clear the filters.",
                    actionLabel = "Clear filters",
                    onAction = {
                        query = ""
                        categoryFilter = null
                        locationFilter = null
                    }
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(bottom = 20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        SectionHeader(
                            results.size.toString() + " result" + (if (results.size == 1) "" else "s")
                        )
                    }
                    items(results, key = { it.id }) { article ->
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
}
