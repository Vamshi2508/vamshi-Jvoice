package com.jvoice.study.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.jvoice.core.i18n.Strings
import com.jvoice.core.i18n.tr
import com.jvoice.shell.AppModule
import com.jvoice.shell.LocalModuleSwitcher
import com.jvoice.study.data.model.StudyRole
import kotlinx.coroutines.launch
import com.jvoice.core.i18n.current

data class StudyNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

/* ------------------------------------------------------------- student shell */

/**
 * Route sentinel for the News tab.
 *
 * The News tab is not a Study destination - it leaves the module - so it needs an
 * identity that no real route can equal. That keeps it permanently unselected,
 * which is correct: you are never "on" it, you only ever leave through it.
 */
private const val ROUTE_LEAVE_TO_NEWS = "__leave_to_news__"

@Composable
fun StudentBottomBar(
    currentRoute: String?,
    unreadCount: Int,
    onNavigate: (String) -> Unit,
    /**
     * The way back to the reader.
     *
     * Study used to be a one-way door: the Reader's "Study" tab handed over to
     * this bar, which had no News tab, so the only exit was a row buried in
     * Profile. Now it is the first tab.
     *
     * How it gets back depends on who is hosting. Inside the News shell it is a
     * plain tab switch to the reader feed in the same NavHost, so the reader's
     * scroll position and back stack survive. In the standalone Study module
     * there is no reader feed to switch to, so it crosses modules through
     * [com.jvoice.shell.ModuleSwitcher]. Null hides the tab.
     */
    onOpenNews: (() -> Unit)? = null
) {
    val items = buildList {
        if (onOpenNews != null) {
            add(StudyNavItem(ROUTE_LEAVE_TO_NEWS, tr(Strings.Study.tabNews), Icons.Default.Newspaper))
        }
        add(StudyNavItem(StudyRoutes.HOME, tr(Strings.Study.tabHome), Icons.Default.Home))
        add(StudyNavItem(StudyRoutes.BROWSE, tr(Strings.Study.tabStudy), Icons.AutoMirrored.Filled.MenuBook))
        add(StudyNavItem(StudyRoutes.EXAMS, tr(Strings.Study.tabExams), Icons.AutoMirrored.Filled.Assignment, unreadCount))
        add(StudyNavItem(StudyRoutes.LEADERBOARD, tr(Strings.Study.tabRanks), Icons.Default.EmojiEvents))
        add(StudyNavItem(StudyRoutes.PROFILE, tr(Strings.Study.tabProfile), Icons.Default.AccountCircle))
    }
    NavigationBar {
        items.forEach { item ->
            val isLeaving = item.route == ROUTE_LEAVE_TO_NEWS
            NavigationBarItem(
                selected = !isLeaving && currentRoute == item.route,
                onClick = {
                    if (isLeaving) onOpenNews?.invoke()
                    else if (currentRoute != item.route) onNavigate(item.route)
                },
                icon = {
                    if (item.badgeCount > 0) {
                        BadgedBox(badge = { Badge { Text(item.badgeCount.toString()) } }) {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.label)
                    }
                },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}

/* --------------------------------------------------------------- admin shell */

private fun drawerItemsFor(role: StudyRole): List<StudyNavItem> = when (role) {
    StudyRole.CONTENT_CREATOR -> listOf(
        StudyNavItem(StudyRoutes.CREATOR_DASHBOARD, "Dashboard", Icons.Default.Dashboard),
        StudyNavItem(StudyRoutes.CREATOR_ARTICLES, "Articles", Icons.AutoMirrored.Filled.Article),
        StudyNavItem(StudyRoutes.CREATOR_QUESTIONS, "Questions & Quizzes", Icons.Default.QuestionAnswer)
    )
    StudyRole.EXAM_ADMIN -> listOf(
        StudyNavItem(StudyRoutes.EXAM_ADMIN_DASHBOARD, "Dashboard", Icons.Default.Dashboard),
        StudyNavItem(StudyRoutes.EXAM_ADMIN_DAILY, "Daily Exams", Icons.AutoMirrored.Filled.Assignment),
        StudyNavItem(StudyRoutes.EXAM_ADMIN_GRAND, "Grand Tests", Icons.Default.EmojiEvents),
        StudyNavItem(StudyRoutes.EXAM_ADMIN_BANK, "Question Bank", Icons.Default.QuestionAnswer),
        StudyNavItem(StudyRoutes.EXAM_ADMIN_RESULTS, "Exam Results", Icons.Default.Insights)
    )
    StudyRole.STUDY_ADMIN -> listOf(
        StudyNavItem(StudyRoutes.STUDY_ADMIN_DASHBOARD, "Dashboard", Icons.Default.Dashboard),
        StudyNavItem(StudyRoutes.STUDY_ADMIN_EXAM_TYPES, "Exam Types", Icons.Default.School),
        StudyNavItem(StudyRoutes.STUDY_ADMIN_SUBJECTS, "Subjects", Icons.Default.Category),
        StudyNavItem(StudyRoutes.STUDY_ADMIN_TOPICS, "Topics", Icons.Default.Topic),
        StudyNavItem(StudyRoutes.STUDY_ADMIN_CONTENT, "Content", Icons.AutoMirrored.Filled.Article),
        StudyNavItem(StudyRoutes.STUDY_ADMIN_BANK, "Question Bank", Icons.Default.QuestionAnswer)
    )
    StudyRole.SUPER_ADMIN -> listOf(
        StudyNavItem(StudyRoutes.SUPER_DASHBOARD, "Dashboard", Icons.Default.Dashboard),
        StudyNavItem(StudyRoutes.SUPER_USERS, "User Management", Icons.Default.Groups),
        StudyNavItem(StudyRoutes.SUPER_ROLES, "Role Permissions", Icons.Default.Shield),
        StudyNavItem(StudyRoutes.STUDY_ADMIN_EXAM_TYPES, "Exam Types", Icons.Default.School),
        StudyNavItem(StudyRoutes.STUDY_ADMIN_SUBJECTS, "Subjects", Icons.Default.Category),
        StudyNavItem(StudyRoutes.STUDY_ADMIN_TOPICS, "Topics", Icons.Default.Topic),
        StudyNavItem(StudyRoutes.STUDY_ADMIN_CONTENT, "Content", Icons.AutoMirrored.Filled.Article),
        StudyNavItem(StudyRoutes.STUDY_ADMIN_BANK, "Question Bank", Icons.Default.QuestionAnswer),
        StudyNavItem(StudyRoutes.EXAM_ADMIN_DAILY, "Daily Exams", Icons.AutoMirrored.Filled.Assignment),
        StudyNavItem(StudyRoutes.EXAM_ADMIN_GRAND, "Grand Tests", Icons.Default.EmojiEvents),
        StudyNavItem(StudyRoutes.EXAM_ADMIN_RESULTS, "Exam Results", Icons.Default.Insights)
    )
    StudyRole.STUDENT -> emptyList()
}

/**
 * Drawer shell for the four management roles. Menu items are filtered by role,
 * so a Content Creator never sees exam or user administration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyAdminScaffold(
    role: StudyRole,
    title: String,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit,
    snackbarHostState: SnackbarHostState,
    actions: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val items = drawerItemsFor(role)
    // A Super Admin owns both modules, so offer a direct hop to the other one.
    val moduleSwitcher = LocalModuleSwitcher.current
        ?.takeIf { role == StudyRole.SUPER_ADMIN }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("J Voice Study", style = MaterialTheme.typography.titleLarge)
                            Text(
                                role.label + " • " + role.teluguLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                items.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.label) },
                        icon = { Icon(item.icon, contentDescription = null) },
                        selected = item.route == currentRoute,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (item.route != currentRoute) onNavigate(item.route)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                if (moduleSwitcher != null) {
                    NavigationDrawerItem(
                        label = {
                            Text(moduleSwitcher.other.emoji + "  " + moduleSwitcher.other.label + " module")
                        },
                        icon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            moduleSwitcher.switchToOtherAsSuperAdmin()
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                NavigationDrawerItem(
                    label = { Text("Switch role") },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onSignOut()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(title, style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open menu")
                        }
                    },
                    actions = { actions() }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = floatingActionButton,
            content = content
        )
    }
}

/* --------------------------------------------------------------- stat grid */

@Composable
fun StudyStatGrid(
    stats: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    columns: Int = 2,
    cell: @Composable (Pair<String, String>, Modifier) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        stats.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { stat -> cell(stat, Modifier.weight(1f)) }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
