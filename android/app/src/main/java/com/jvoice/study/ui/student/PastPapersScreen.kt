package com.jvoice.study.ui.student

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jvoice.news.components.EmptyState
import com.jvoice.news.components.SectionHeader
import com.jvoice.study.components.StudyPill

/**
 * Everything real papers have already asked on this exam, grouped by the
 * subject it belongs to. Each row opens the topic it came from, where the
 * Past papers tab holds the full set.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastPapersScreen(
    viewModel: StudyBrowseViewModel,
    onOpenTopic: (String) -> Unit,
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val subjects = viewModel.enabledSubjects()

    // subject -> the topics under it that carry past-paper questions
    val sections = subjects.mapNotNull { subject ->
        val topics = viewModel.topicsOf(subject.id)
            .map { topic -> topic to viewModel.previousQuestions(topic.id) }
            .filter { it.second.isNotEmpty() }
        if (topics.isEmpty()) null else subject to topics
    }

    val total = sections.sumOf { section -> section.second.sumOf { it.second.size } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Previous papers") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = bottomBar
    ) { padding ->
        if (sections.isEmpty()) {
            EmptyState(
                title = "No past questions yet",
                description = "Nothing from a real paper has been recorded for this exam.",
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                SectionHeader(
                    total.toString() + " questions asked before",
                    subtitle = "Across " + sections.size + " subjects on this paper"
                )
            }

            sections.forEach { (subject, topics) ->
                item(key = "h_" + subject.id) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(subject.emoji, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            subject.name.en,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.weight(1f))
                        StudyPill(
                            topics.sumOf { it.second.size }.toString() + " Q",
                            MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                items(topics, key = { it.first.id }) { (topic, questions) ->
                    // the papers this topic has shown up in, newest label first
                    val papers = questions
                        .mapNotNull { it.paperLabel.takeIf { label -> label.isNotBlank() } }
                        .distinct()

                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { onOpenTopic(topic.id) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        questions.size.toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(topic.name.en, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    papers.joinToString("  •  "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open topic",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}
