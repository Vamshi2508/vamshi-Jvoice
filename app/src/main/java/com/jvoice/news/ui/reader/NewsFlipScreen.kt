package com.jvoice.news.ui.reader

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import coil.compose.AsyncImage
import com.jvoice.core.flags.FeatureFlags
import com.jvoice.core.flags.flagEnabled
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jvoice.news.components.BreakingBadge
import com.jvoice.news.components.EmptyState
import com.jvoice.news.components.ReportSheet
import com.jvoice.news.components.LoadingState
import com.jvoice.news.components.NewsImage
import com.jvoice.news.components.VerticalTwoPanelFlip
import com.jvoice.news.data.model.ArticleEngagement
import com.jvoice.news.data.model.NewsArticle
import com.jvoice.news.data.model.Reaction
import com.jvoice.aishorts.data.repository.AIShortRepository
import com.jvoice.news.data.repository.EngagementRepository
import com.jvoice.news.data.repository.ReadStateRepository
import com.jvoice.news.utils.shareArticle
import com.jvoice.news.utils.toReadableCount
import com.jvoice.news.utils.toRelativeTime
import kotlinx.coroutines.launch
import com.jvoice.core.i18n.current
import com.jvoice.core.i18n.currentLanguage

/**
 * The News tab: short-news pages turned vertically, with a category chip row on top
 * for filtering.
 *
 * Each page is two hinged leaves - the image above, the story below - which turn as
 * separate sheets and reveal the next article on their reverse. Tapping either leaf
 * opens the full article.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsFlipScreen(
    viewModel: ReaderViewModel,
    onOpenArticle: (String) -> Unit,
    onOpenComments: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenClassicFeed: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val allClips by viewModel.clips.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val savedIds by viewModel.savedIds.collectAsState()
    val location by viewModel.selectedLocation.collectAsState()
    val unread by viewModel.unreadCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var categoryFilter by remember { mutableStateOf<String?>(null) }
    var locationMenuOpen by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val readIds by ReadStateRepository.readIds.collectAsState()
    val round by ReadStateRepository.round.collectAsState()

    // Once every story has been read the round ends and the feed starts over,
    // so there is always something to swipe.
    LaunchedEffect(readIds, allClips) {
        if (ReadStateRepository.hasReadAll(allClips.map { it.id })) {
            ReadStateRepository.startNewRound()
            snackbarHostState.showSnackbar("You are all caught up - starting again")
        }
    }

    // The deck: unread stories only, with the chosen category first and the rest
    // of the news after it - so a category with a single story still swipes on.
    // Rebuilt only when the filter or the round changes, so marking the current
    // card as read cannot pull it out from under the swipe.
    val cards = remember(allClips, categoryFilter, round) {
        val alreadyRead = ReadStateRepository.readIds.value
        val notYetSeen = allClips.filterNot { alreadyRead.contains(it.id) }
        val pool = if (notYetSeen.isEmpty()) allClips else notYetSeen
        if (categoryFilter == null) {
            pool
        } else {
            pool.filter { it.categoryId == categoryFilter } +
                pool.filterNot { it.categoryId == categoryFilter }
        }
    }

    // The first card counts as read as soon as the deck is shown.
    LaunchedEffect(cards) {
        cards.firstOrNull()?.let { ReadStateRepository.markRead(it.id) }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "J Voice",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (flagEnabled(FeatureFlags.Keys.NEWS_LOCATION_DROPDOWN)) {
                                Spacer(Modifier.width(6.dp))
                                Box {
                                    Row(
                                        Modifier
                                            .clip(RoundedCornerShape(50))
                                            .clickable { locationMenuOpen = true }
                                            .padding(horizontal = 6.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = "Change location",
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(location, style = MaterialTheme.typography.labelLarge)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                    DropdownMenu(
                                        expanded = locationMenuOpen,
                                        onDismissRequest = { locationMenuOpen = false }
                                    ) {
                                        viewModel.locations.forEach { loc ->
                                            DropdownMenuItem(
                                                text = { Text(loc) },
                                                onClick = {
                                                    viewModel.setLocation(loc)
                                                    locationMenuOpen = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    actions = {
                        if (flagEnabled(FeatureFlags.Keys.MODULE_ICON)) {
                            IconButton(onClick = onOpenClassicFeed) {
                                Icon(Icons.Default.ViewAgenda, contentDescription = "Classic feed")
                            }
                        }
                        IconButton(onClick = onOpenSearch) {
                            Icon(Icons.Default.Search, contentDescription = "Search news")
                        }
                        IconButton(onClick = onOpenNotifications) {
                            if (unread > 0) {
                                BadgedBox(badge = { Badge { Text(unread.toString()) } }) {
                                    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                                }
                            } else {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // ---- category filter chips
                if (flagEnabled(FeatureFlags.Keys.LOCATION_CHIPS)) {
                    Row(
                        Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = categoryFilter == null,
                            onClick = { categoryFilter = null },
                            label = { Text("All") }
                        )
                        categories.filter { it.isEnabled }.forEach { category ->
                            val count = allClips.count { it.categoryId == category.id }
                            if (count > 0) {
                                FilterChip(
                                    selected = categoryFilter == category.id,
                                    onClick = {
                                        categoryFilter = if (categoryFilter == category.id) null else category.id
                                    },
                                    label = { Text(category.emoji + " " + category.name.en) }
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (isLoading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        if (cards.isEmpty()) {
            EmptyState(
                title = "Nothing here yet",
                description = "Published articles appear here as news pages.",
                modifier = Modifier.padding(padding),
                actionLabel = if (categoryFilter != null) "Clear filter" else null,
                onAction = if (categoryFilter != null) ({ categoryFilter = null }) else null
            )
            return@Scaffold
        }

        CompositionLocalProvider(
            LocalReaderSnackbar provides snackbarHostState,
            LocalReaderName provides (viewModel.currentUserName())
        ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Two hinged leaves - the image and the story turn as separate sheets.
            // See components/VerticalPageFlip.kt
            VerticalTwoPanelFlip(
                count = cards.size,
                modifier = Modifier.fillMaxSize(),
                topFraction = 0.36f,
                resetKey = categoryFilter to round,
                onPageSettled = { index ->
                    cards.getOrNull(index)?.let { ReadStateRepository.markRead(it.id) }
                },
                topPanel = { index ->
                    ImageLeaf(
                        article = cards[index],
                        categoryName = viewModel.categoryName(cards[index].categoryId),
                        isSaved = savedIds.contains(cards[index].id),
                        onToggleSave = {
                            val saved = viewModel.toggleSave(cards[index].id)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (saved) "Saved to your bookmarks" else "Removed from bookmarks"
                                )
                            }
                        },
                        onShare = { context.shareArticle(cards[index]) },
                        onClick = { onOpenArticle(cards[index].id) }
                    )
                },
                bottomPanel = { index ->
                    StoryLeaf(
                        article = cards[index],
                        isFirstPage = index == 0,
                        onOpenComments = { onOpenComments(cards[index].id) }
                    )
                }
            )
        }
        }
    }
}

/** Lets the leaves reach the screen's snackbar and the signed-in reader's name. */
val LocalReaderSnackbar = staticCompositionLocalOf<SnackbarHostState?> { null }
val LocalReaderName = staticCompositionLocalOf { "Reader" }

/** Upper leaf: the image, carrying category, time and the save / share actions. */
@Composable
private fun ImageLeaf(
    article: NewsArticle,
    categoryName: String,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    onShare: () -> Unit,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
    ) {
        NewsImage(
            url = article.imageUrl,
            contentDescription = article.headline.current(),
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.42f),
                        0.45f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.72f)
                    )
                )
        )

        Row(
            Modifier
                .align(Alignment.TopStart)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (article.isBreaking) {
                BreakingBadge()
                Spacer(Modifier.width(8.dp))
            }
            // Minimum addition for AI Shorts: a badge when a video is attached.
            val shorts by AIShortRepository.shorts.collectAsState()
            val hasVideo = remember(shorts, article.id) {
                AIShortRepository.publishedShortFor(article.id) != null
            }
            if (hasVideo) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.55f)
                ) {
                    Row(
                        Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            "Video",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Row(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 14.dp, end = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.20f)
            ) {
                Text(
                    categoryName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                (article.publishedAt ?: article.createdAt).toRelativeTime(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onToggleSave, modifier = Modifier.size(38.dp)) {
                Icon(
                    if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = if (isSaved) "Remove bookmark" else "Save",
                    tint = Color.White,
                    modifier = Modifier.size(21.dp)
                )
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onShare, modifier = Modifier.size(38.dp)) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/** Lower leaf: the story. */
@Composable
private fun StoryLeaf(
    article: NewsArticle,
    isFirstPage: Boolean,
    onOpenComments: () -> Unit
) {
    val engagementMap by EngagementRepository.engagement.collectAsState()
    val allComments by EngagementRepository.comments.collectAsState()
    val engagement = engagementMap[article.id] ?: ArticleEngagement(article.id)
    val commentCount = remember(allComments, article.id) {
        allComments.count { it.articleId == article.id }
    }
    var showReport by remember { mutableStateOf(false) }
    val snackbar = LocalReaderSnackbar.current
    val scope = rememberCoroutineScope()
    val reporterName = LocalReaderName.current

    if (showReport) {
        ReportSheet(
            headline = article.headline.current(),
            onDismiss = { showReport = false },
            onSubmit = { reason, suggestion ->
                EngagementRepository.submitReport(article.id, reason, suggestion, reporterName)
                showReport = false
                scope.launch {
                    snackbar?.showSnackbar("Thanks - your report was sent to the desk")
                }
            }
        )
    }

    // Only the image opens the article; the story panel is not tappable.
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            article.headline.current(),
            style = MaterialTheme.typography.titleLarge,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(10.dp))
        Text(
            article.shortDescription.current(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        // The excerpt has to be recomputed when the language changes, not just
        // when the story does - keying only on the id would leave a Telugu
        // excerpt under an English headline after a toggle.
        val language = currentLanguage()
        val body = article.content.get(language)
        val excerpt = remember(article.id, language) {
            body.lineSequence()
                .map { it.trim() }
                .firstOrNull { it.isNotBlank() && !it.startsWith("(") }
                .orEmpty()
        }
        if (excerpt.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                excerpt,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 9,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(12.dp))
        Text(
            article.location + "  •  " + article.views.toReadableCount() + " views",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // ------------------------------------------- who filed this story
        // Sits in the slack between the excerpt and the swipe hint, so it fills
        // space that was empty rather than pushing the story text up the page.
        Spacer(Modifier.weight(1f))
        ReporterCredit(
            name = article.reporterName,
            avatarUrl = article.reporterAvatarUrl,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.weight(1f))

        if (isFirstPage) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Swipe to turn the page  •  tap the photo to read",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // ---------------------------------------- like / dislike / comment
        HorizontalDivider()
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReactionButton(
                icon = if (engagement.myReaction == Reaction.LIKE) Icons.Filled.ThumbUp
                else Icons.Outlined.ThumbUp,
                label = engagement.likes.toReadableCount(),
                active = engagement.myReaction == Reaction.LIKE,
                activeColor = MaterialTheme.colorScheme.primary,
                onClick = { EngagementRepository.toggleLike(article.id) }
            )
            Spacer(Modifier.width(6.dp))
            ReactionButton(
                icon = if (engagement.myReaction == Reaction.DISLIKE) Icons.Filled.ThumbDown
                else Icons.Outlined.ThumbDown,
                label = engagement.dislikes.toReadableCount(),
                active = engagement.myReaction == Reaction.DISLIKE,
                activeColor = MaterialTheme.colorScheme.error,
                onClick = { EngagementRepository.toggleDislike(article.id) }
            )
            Spacer(Modifier.width(6.dp))
            ReactionButton(
                icon = Icons.AutoMirrored.Outlined.Comment,
                label = if (commentCount == 0) "Comment" else commentCount.toString(),
                active = false,
                activeColor = MaterialTheme.colorScheme.primary,
                onClick = onOpenComments
            )
            Spacer(Modifier.weight(1f))
            ReactionButton(
                icon = Icons.Outlined.Flag,
                label = "Report",
                active = false,
                activeColor = MaterialTheme.colorScheme.error,
                onClick = { showReport = true }
            )
        }
    }
}

/** One pill in the engagement bar. */
/**
 * The reporter's photo with their name beneath it.
 *
 * Falls back to their initials on a tinted circle when there is no photo, which is
 * the common case today - the desk accounts were seeded without pictures. An empty
 * circle would read as a broken image; initials read as a person.
 */
@Composable
private fun ReporterCredit(
    name: String,
    avatarUrl: String,
    modifier: Modifier = Modifier
) {
    if (name.isBlank()) return
    Column(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl.isNotBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    reporterInitials(name),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** First letters of the first two words, e.g. "Kiran Kumar" -> "KK". */
private fun reporterInitials(name: String): String = name.trim()
    .split(' ', '\t', '\n')
    .filter { it.isNotBlank() }
    .take(2)
    .map { it.first().uppercaseChar() }
    .joinToString("")

@Composable
private fun ReactionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val tint = if (active) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        shape = RoundedCornerShape(50),
        color = if (active) activeColor.copy(alpha = 0.12f) else Color.Transparent,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = tint,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}
