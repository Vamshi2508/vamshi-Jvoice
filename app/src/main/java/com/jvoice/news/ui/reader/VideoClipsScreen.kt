package com.jvoice.news.ui.reader

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.jvoice.news.components.BreakingBadge
import com.jvoice.news.components.EmptyState
import com.jvoice.news.components.Pill
import com.jvoice.news.data.model.NewsClip
import com.jvoice.news.data.repository.ClipsRepository
import com.jvoice.news.utils.toReadableCount
import com.jvoice.news.utils.toRelativeTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Clips: short-form vertical video, one clip per full-screen page.
 *
 * No media engine is wired up in this build - there is no backend and no bundled
 * video, so the transport (progress, play/pause, mute) is simulated over the
 * clip's real duration. Swapping in ExoPlayer later only replaces the surface
 * behind the thumbnail; the chrome, gestures and data stay as they are.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoClipsScreen(
    viewModel: ReaderViewModel,
    onOpenArticle: (String) -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val allClips by ClipsRepository.clips.collectAsState()
    val liked by ClipsRepository.likedIds.collectAsState()
    val saved by ClipsRepository.savedIds.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var categoryFilter by remember { mutableStateOf<String?>(null) }
    var muted by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val clips = remember(allClips, categoryFilter) { ClipsRepository.feed(categoryFilter) }

    Scaffold(
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black
    ) { padding ->
        if (clips.isEmpty()) {
            EmptyState(
                title = "No clips here",
                description = "No video clips in this category yet.",
                modifier = Modifier.padding(padding),
                actionLabel = "Clear filter",
                onAction = { categoryFilter = null }
            )
            return@Scaffold
        }

        val pager = rememberPagerState(pageCount = { clips.size })

        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            VerticalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
                val clip = clips[page]
                ClipPlayerPage(
                    clip = clip,
                    categoryName = viewModel.categoryName(clip.categoryId),
                    isActive = pager.currentPage == page,
                    isLiked = liked.contains(clip.id),
                    isSaved = saved.contains(clip.id),
                    muted = muted,
                    onToggleMute = { muted = !muted },
                    onToggleLike = { ClipsRepository.toggleLike(clip.id) },
                    onToggleSave = {
                        val nowSaved = ClipsRepository.toggleSave(clip.id)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (nowSaved) "Clip saved" else "Removed from saved clips"
                            )
                        }
                    },
                    onShare = {
                        scope.launch { snackbarHostState.showSnackbar("Sharing is disabled for demo clips") }
                    },
                    onReadStory = { clip.relatedArticleId?.let(onOpenArticle) }
                )
            }

            // ---- filter chips over the video
            Row(
                Modifier
                    .align(Alignment.TopStart)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClipChip("All", categoryFilter == null) { categoryFilter = null }
                categories.filter { c -> allClips.any { it.categoryId == c.id } }.forEach { category ->
                    ClipChip(
                        label = category.emoji + " " + category.name.en,
                        selected = categoryFilter == category.id
                    ) {
                        categoryFilter = if (categoryFilter == category.id) null else category.id
                    }
                }
            }
        }
    }
}

@Composable
private fun ClipChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) Color.White else Color.Black.copy(alpha = 0.45f),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.Black else Color.White,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun ClipPlayerPage(
    clip: NewsClip,
    categoryName: String,
    isActive: Boolean,
    isLiked: Boolean,
    isSaved: Boolean,
    muted: Boolean,
    onToggleMute: () -> Unit,
    onToggleLike: () -> Unit,
    onToggleSave: () -> Unit,
    onShare: () -> Unit,
    onReadStory: () -> Unit
) {
    var playing by remember(clip.id) { mutableStateOf(true) }
    var progress by remember(clip.id) { mutableFloatStateOf(0f) }

    // Simulated transport: advances in real time over the clip's duration.
    LaunchedEffect(clip.id, isActive, playing) {
        if (!isActive) {
            progress = 0f
            return@LaunchedEffect
        }
        ClipsRepository.registerView(clip.id)
        while (playing) {
            delay(200)
            progress += 0.2f / clip.durationSeconds
            if (progress >= 1f) progress = 0f
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AsyncImage(
            model = clip.thumbnailUrl,
            contentDescription = clip.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // legibility scrims
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.45f),
                        0.35f to Color.Transparent,
                        0.72f to Color.Black.copy(alpha = 0.55f),
                        1f to Color.Black.copy(alpha = 0.88f)
                    )
                )
        )

        // tap anywhere to pause / resume
        Box(
            Modifier
                .fillMaxSize()
                .clickable { playing = !playing }
        )

        if (!playing) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.55f),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(18.dp)
                        .size(40.dp)
                )
            }
        }

        // ---- right-hand action rail
        Column(
            Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ActionButton(
                icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                label = clip.likes.toReadableCount(),
                tint = if (isLiked) MaterialTheme.colorScheme.primary else Color.White,
                onClick = onToggleLike
            )
            ActionButton(
                icon = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                label = "Save",
                tint = if (isSaved) MaterialTheme.colorScheme.primary else Color.White,
                onClick = onToggleSave
            )
            ActionButton(
                icon = Icons.Default.Share,
                label = "Share",
                tint = Color.White,
                onClick = onShare
            )
            ActionButton(
                icon = if (muted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                label = if (muted) "Muted" else "Sound",
                tint = Color.White,
                onClick = onToggleMute
            )
        }

        // ---- caption block
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.82f)
                .padding(start = 16.dp, bottom = 18.dp, end = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (clip.isBreaking) {
                    BreakingBadge()
                    Spacer(Modifier.width(8.dp))
                }
                Pill(categoryName, Color.White)
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.45f)
                ) {
                    Text(
                        clip.durationLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                clip.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                clip.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Text(
                clip.reporterName + "  •  " + clip.location + "  •  " +
                    clip.views.toReadableCount() + " views  •  " +
                    clip.publishedAt.toRelativeTime(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (clip.relatedArticleId != null) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.16f),
                    modifier = Modifier.clickable(onClick = onReadStory)
                ) {
                    Text(
                        "Read the full story",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                "Demo build — playback simulated, no media stream",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.45f)
            )
        }

        // ---- transport bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(3.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color.White.copy(alpha = 0.25f)
        )
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(28.dp))
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}
