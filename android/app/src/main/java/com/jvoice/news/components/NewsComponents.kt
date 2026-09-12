package com.jvoice.news.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jvoice.news.data.model.NewsArticle
import com.jvoice.news.utils.toReadableCount
import com.jvoice.news.utils.toRelativeTime
import com.jvoice.core.i18n.current

/**
 * A placeholder-backed image. Dummy picsum URLs are used for demo imagery; if
 * the device is offline the gradient placeholder keeps the layout intact.
 */
@Composable
fun NewsImage(
    url: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                )
            )
        )
    ) {
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun MetaRow(
    article: NewsArticle,
    categoryName: String,
    modifier: Modifier = Modifier,
    showViews: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Pill(text = categoryName, color = MaterialTheme.colorScheme.primary)
        MetaItem(Icons.Default.LocationOn, article.location)
        MetaItem(Icons.Default.AccessTime, (article.publishedAt ?: article.createdAt).toRelativeTime())
        if (showViews && article.views > 0) {
            MetaItem(Icons.Default.Visibility, article.views.toReadableCount())
        }
    }
}

@Composable
private fun MetaItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(3.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Standard list card: image on the right, headline + description on the left. */
@Composable
fun NewsCard(
    article: NewsArticle,
    categoryName: String,
    isSaved: Boolean,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(12.dp)) {
            Column(Modifier.weight(1f)) {
                if (article.isBreaking) {
                    BreakingBadge()
                    Spacer(Modifier.height(6.dp))
                }
                Text(
                    article.headline.current(),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    article.shortDescription.current(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                MetaRow(article, categoryName)
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                NewsImage(
                    url = article.imageUrl,
                    contentDescription = article.headline.current(),
                    modifier = Modifier
                        .size(width = 104.dp, height = 84.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                IconButton(onClick = onToggleSave, modifier = Modifier.size(34.dp)) {
                    Icon(
                        if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isSaved) "Remove bookmark" else "Save article",
                        tint = if (isSaved) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

/** Large hero card used for breaking news and the featured slot. */
@Composable
fun FeaturedNewsCard(
    article: NewsArticle,
    categoryName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            NewsImage(
                url = article.imageUrl,
                contentDescription = article.headline.current(),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
            )
            Box(
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.45f to Color.Black.copy(alpha = 0.25f),
                            1f to Color.Black.copy(alpha = 0.85f)
                        )
                    )
            )
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (article.isBreaking) {
                        BreakingBadge()
                        Spacer(Modifier.width(8.dp))
                    }
                    Pill(text = categoryName, color = Color.White.copy(alpha = 0.9f), filled = false)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    article.headline.current(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    article.location + "  •  " + (article.publishedAt ?: article.createdAt).toRelativeTime(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

/** Compact card for horizontal rails (Trending, Related). */
@Composable
fun CompactNewsCard(
    article: NewsArticle,
    categoryName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    rank: Int? = null
) {
    Card(
        modifier = modifier
            .width(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box {
                NewsImage(
                    url = article.imageUrl,
                    contentDescription = article.headline.current(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp)
                )
                if (rank != null) {
                    Box(
                        Modifier
                            .padding(8.dp)
                            .size(24.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            rank.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            Column(Modifier.padding(10.dp)) {
                Text(
                    article.headline.current(),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    categoryName + "  •  " + (article.publishedAt ?: article.createdAt).toRelativeTime(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Row used in workflow screens (My News, Review Queue, News Management). */
@Composable
fun WorkflowNewsRow(
    article: NewsArticle,
    categoryName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusChip(article.status)
                        if (article.isBreaking) {
                            Spacer(Modifier.width(6.dp))
                            BreakingBadge()
                        }
                        if (article.isFeatured) {
                            Spacer(Modifier.width(6.dp))
                            Pill("PINNED", MaterialTheme.colorScheme.secondary)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        article.headline.current(),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (trailing != null) {
                    Spacer(Modifier.width(8.dp))
                    trailing()
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                article.reporterName + "  •  " + categoryName + "  •  " + article.location,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "Updated " + article.updatedAt.toRelativeTime(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (article.rejectionReason?.current() != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Rejected: " + article.rejectionReason?.current(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (article.editorNote?.current() != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Editor note: " + article.editorNote?.current(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
