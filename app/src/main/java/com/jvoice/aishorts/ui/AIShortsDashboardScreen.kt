package com.jvoice.aishorts.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
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
import com.jvoice.aishorts.data.model.AIShortStatus
import com.jvoice.aishorts.data.model.ShortsFilter
import com.jvoice.aishorts.navigation.AIShortRoutes
import com.jvoice.news.components.ConfirmDialog
import com.jvoice.news.components.EmptyState
import com.jvoice.news.components.Pill
import com.jvoice.news.components.SectionHeader
import com.jvoice.news.data.model.UserRole
import com.jvoice.news.navigation.AdminScaffold
import com.jvoice.news.utils.toRelativeTime
import kotlinx.coroutines.launch
import com.jvoice.core.i18n.current

/**
 * AI Shorts dashboard - every short generated from a news article, filterable by
 * status and searchable by headline, category, location or status.
 *
 * Uses the existing AdminScaffold so it sits in the Editor / Admin drawer exactly
 * like the other management screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIShortsDashboardScreen(
    viewModel: AIShortViewModel,
    role: UserRole,
    onNavigate: (String) -> Unit,
    onOpenShort: (String) -> Unit,
    onContinueSetup: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val shorts by viewModel.shorts.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val query by viewModel.query.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var pendingDelete by remember { mutableStateOf<String?>(null) }

    pendingDelete?.let { id ->
        ConfirmDialog(
            title = "Delete this AI Short?",
            message = "The news article is not affected - only the generated short is removed.",
            confirmLabel = "Delete",
            destructive = true,
            onConfirm = {
                viewModel.deleteShort(id)
                pendingDelete = null
                scope.launch { snackbarHostState.showSnackbar("AI Short deleted") }
            },
            onDismiss = { pendingDelete = null }
        )
    }

    AdminScaffold(
        role = role,
        title = "AI Shorts",
        currentRoute = AIShortRoutes.DASHBOARD,
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
                onValueChange = viewModel::setQuery,
                placeholder = { Text("Search headline, category, location") },
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
                ShortsFilter.entries.forEach { option ->
                    val count = viewModel.countFor(option)
                    FilterChip(
                        selected = filter == option,
                        onClick = { viewModel.setFilter(option) },
                        label = { Text(option.label + " (" + count + ")") }
                    )
                }
            }

            if (shorts.isEmpty()) {
                EmptyState(
                    title = "No AI Shorts here",
                    description = "Open a news article in review and choose Create AI Short.",
                    actionLabel = if (filter != ShortsFilter.ALL) "Show all" else null,
                    onAction = if (filter != ShortsFilter.ALL) ({ viewModel.setFilter(ShortsFilter.ALL) }) else null
                )
                return@AdminScaffold
            }

            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                item { SectionHeader(shorts.size.toString() + " shorts") }
                items(shorts, key = { it.id }) { short ->
                    val template = viewModel.templateById(short.templateId)
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp)
                            .clickable {
                                if (short.status.isPlayable) onOpenShort(short.id)
                                else onContinueSetup(short.id)
                            },
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(Modifier.padding(12.dp)) {
                            ShortThumbnail(
                                url = short.thumbnailUrl,
                                modifier = Modifier.size(width = 62.dp, height = 96.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    short.newsHeadline.current(),
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Template: " + (template?.name ?: "Not chosen"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Language: " + short.language.label + "  •  " +
                                        short.durationSeconds + " sec  •  " +
                                        short.sceneCount + " scenes",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    ShortStatusPill(short.status)
                                    if (short.status == AIShortStatus.PUBLISHED) {
                                        Pill("On the article", MaterialTheme.colorScheme.tertiary)
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Updated " + short.updatedAt.toRelativeTime() +
                                        "  •  " + short.createdBy,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )

                                if (short.status == AIShortStatus.FAILED && short.errorMessage != null) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        short.errorMessage,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (short.status.isPlayable) {
                                        OutlinedButton(
                                            onClick = { onOpenShort(short.id) },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text("Preview")
                                        }
                                    }
                                    TextButton(onClick = { onContinueSetup(short.id) }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(if (short.setupComplete) "Edit" else "Continue setup")
                                    }
                                    Spacer(Modifier.weight(1f))
                                    IconButton(onClick = { pendingDelete = short.id }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete short")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
