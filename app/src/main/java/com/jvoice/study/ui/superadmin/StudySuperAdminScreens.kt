package com.jvoice.study.ui.superadmin

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import com.jvoice.news.components.ConfirmDialog
import com.jvoice.news.components.EmptyState
import com.jvoice.news.components.LoadingState
import com.jvoice.news.components.SectionHeader
import com.jvoice.study.components.MetricCard
import com.jvoice.study.components.StudyDemoBar
import com.jvoice.study.components.StudyPill
import com.jvoice.study.data.model.StudyRole
import com.jvoice.study.data.model.StudyUser
import com.jvoice.study.navigation.StudyAdminScaffold
import com.jvoice.study.navigation.StudyRoutes
import com.jvoice.study.navigation.StudyStatGrid
import kotlinx.coroutines.launch

/* ============================================================== dashboard */

@Composable
fun StudySuperAdminDashboardScreen(
    viewModel: StudySuperAdminViewModel,
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val counts by viewModel.counts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    StudyAdminScaffold(
        role = StudyRole.SUPER_ADMIN,
        title = "Super Admin • Study",
        currentRoute = StudyRoutes.SUPER_DASHBOARD,
        onNavigate = onNavigate,
        onSignOut = onSignOut,
        snackbarHostState = snackbarHostState
    ) { padding ->
        if (isLoading) {
            LoadingState(Modifier.padding(padding), "Loading system overview...")
            return@StudyAdminScaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { StudyDemoBar() }
            item { SectionHeader("People") }
            item {
                StudyStatGrid(
                    stats = listOf(
                        "Students" to counts.students.toString(),
                        "Content Creators" to counts.creators.toString(),
                        "Exam Admins" to counts.examAdmins.toString(),
                        "Study Admins" to counts.studyAdmins.toString()
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) { (label, value), modifier ->
                    MetricCard(label, value, modifier, onClick = { onNavigate(StudyRoutes.SUPER_USERS) })
                }
            }

            item { SectionHeader("Content & exams") }
            item {
                StudyStatGrid(
                    stats = listOf(
                        "Total Questions" to counts.questions.toString(),
                        "Total Articles" to counts.articles.toString(),
                        "Total Quizzes" to counts.quizzes.toString(),
                        "Daily Exams" to counts.dailyExams.toString(),
                        "Grand Tests" to counts.grandTests.toString(),
                        "Subjects" to counts.subjects.toString()
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) { (label, value), modifier ->
                    MetricCard(
                        label, value, modifier,
                        accent = MaterialTheme.colorScheme.secondary,
                        onClick = {
                            when (label) {
                                "Total Questions" -> onNavigate(StudyRoutes.STUDY_ADMIN_BANK)
                                "Total Articles", "Total Quizzes" -> onNavigate(StudyRoutes.STUDY_ADMIN_CONTENT)
                                "Daily Exams" -> onNavigate(StudyRoutes.EXAM_ADMIN_DAILY)
                                "Grand Tests" -> onNavigate(StudyRoutes.EXAM_ADMIN_GRAND)
                                else -> onNavigate(StudyRoutes.STUDY_ADMIN_SUBJECTS)
                            }
                        }
                    )
                }
            }

            item { SectionHeader("Management") }
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    ListItem(
                        headlineContent = { Text("User Management") },
                        supportingContent = { Text("View, add, edit, activate and change roles") },
                        trailingContent = {
                            TextButton(onClick = { onNavigate(StudyRoutes.SUPER_USERS) }) { Text("Open") }
                        }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Role Permissions") },
                        supportingContent = { Text("What each of the five roles can do") },
                        trailingContent = {
                            TextButton(onClick = { onNavigate(StudyRoutes.SUPER_ROLES) }) { Text("Open") }
                        }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Syllabus & question bank") },
                        supportingContent = { Text("Full access to subjects, topics, content and questions") },
                        trailingContent = {
                            TextButton(onClick = { onNavigate(StudyRoutes.STUDY_ADMIN_SUBJECTS) }) { Text("Open") }
                        }
                    )
                }
            }
        }
    }
}

/* ========================================================= user management */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyUserManagementScreen(
    viewModel: StudySuperAdminViewModel,
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val users by viewModel.users.collectAsState()
    val roleFilter by viewModel.roleFilter.collectAsState()
    val query by viewModel.query.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var editing by remember { mutableStateOf<StudyUser?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    var pendingDeactivate by remember { mutableStateOf<StudyUser?>(null) }

    if (showAdd || editing != null) {
        UserDialog(
            user = editing,
            onDismiss = { showAdd = false; editing = null },
            onSave = { name, email, role ->
                val target = editing
                if (target == null) {
                    viewModel.addUser(name, email, role)
                    scope.launch { snackbarHostState.showSnackbar("User added") }
                } else {
                    viewModel.updateUser(target.id, name, email)
                    if (role != target.role) viewModel.changeRole(target.id, role)
                    scope.launch { snackbarHostState.showSnackbar("User updated") }
                }
                showAdd = false
                editing = null
            }
        )
    }

    pendingDeactivate?.let { user ->
        ConfirmDialog(
            title = "Deactivate " + user.name + "?",
            message = "The account stays in the demo data but is marked inactive.",
            confirmLabel = "Deactivate",
            destructive = true,
            onConfirm = {
                viewModel.setActive(user.id, false)
                pendingDeactivate = null
                scope.launch { snackbarHostState.showSnackbar("Account deactivated") }
            },
            onDismiss = { pendingDeactivate = null }
        )
    }

    StudyAdminScaffold(
        role = StudyRole.SUPER_ADMIN,
        title = "User Management",
        currentRoute = StudyRoutes.SUPER_USERS,
        onNavigate = onNavigate,
        onSignOut = onSignOut,
        snackbarHostState = snackbarHostState,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add user") }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setQuery,
                placeholder = { Text("Search name or email") },
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
                FilterChip(
                    selected = roleFilter == null,
                    onClick = { viewModel.setRoleFilter(null) },
                    label = { Text("All roles") }
                )
                StudyRole.entries.forEach { role ->
                    FilterChip(
                        selected = roleFilter == role,
                        onClick = { viewModel.setRoleFilter(if (roleFilter == role) null else role) },
                        label = { Text(role.label) }
                    )
                }
            }

            if (users.isEmpty()) {
                EmptyState(
                    title = "No users match",
                    description = "Clear the search or the role filter.",
                    actionLabel = "Clear",
                    onAction = {
                        viewModel.setQuery("")
                        viewModel.setRoleFilter(null)
                    }
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
                    item { SectionHeader(users.size.toString() + " users") }
                    items(users, key = { it.id }) { user ->
                        var menuOpen by remember { mutableStateOf(false) }
                        var roleMenuOpen by remember { mutableStateOf(false) }

                        ListItem(
                            leadingContent = {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            user.name.take(1),
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                }
                            },
                            headlineContent = { Text(user.name) },
                            supportingContent = {
                                Column {
                                    Text(user.email, style = MaterialTheme.typography.labelSmall)
                                    Spacer(Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        StudyPill(user.role.label, MaterialTheme.colorScheme.primary)
                                        StudyPill(
                                            if (user.isActive) "Active" else "Inactive",
                                            if (user.isActive) MaterialTheme.colorScheme.tertiary
                                            else MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            },
                            trailingContent = {
                                Box {
                                    IconButton(onClick = { menuOpen = true }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Actions")
                                    }
                                    DropdownMenu(
                                        expanded = menuOpen,
                                        onDismissRequest = { menuOpen = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("View / edit") },
                                            onClick = {
                                                menuOpen = false
                                                editing = user
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(if (user.isActive) "Deactivate" else "Activate") },
                                            onClick = {
                                                menuOpen = false
                                                if (user.isActive) {
                                                    pendingDeactivate = user
                                                } else {
                                                    viewModel.setActive(user.id, true)
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Account activated")
                                                    }
                                                }
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Change role") },
                                            onClick = {
                                                menuOpen = false
                                                roleMenuOpen = true
                                            }
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = roleMenuOpen,
                                        onDismissRequest = { roleMenuOpen = false }
                                    ) {
                                        StudyRole.entries.forEach { role ->
                                            DropdownMenuItem(
                                                text = { Text(role.label) },
                                                trailingIcon = {
                                                    if (role == user.role) {
                                                        Icon(Icons.Default.Check, contentDescription = null)
                                                    }
                                                },
                                                onClick = {
                                                    viewModel.changeRole(user.id, role)
                                                    roleMenuOpen = false
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            user.name + " is now a " + role.label
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun UserDialog(
    user: StudyUser?,
    onDismiss: () -> Unit,
    onSave: (String, String, StudyRole) -> Unit
) {
    var name by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var role by remember { mutableStateOf(user?.role ?: StudyRole.STUDENT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (user == null) "Add user" else "Edit user") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Text("Role", style = MaterialTheme.typography.labelMedium)
                Row(
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StudyRole.entries.forEach { value ->
                        FilterChip(
                            selected = role == value,
                            onClick = { role = value },
                            label = { Text(value.label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onSave(name.trim(), email.trim(), role) }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/* ========================================================= role permissions */

@Composable
fun StudyRolePermissionsScreen(
    viewModel: StudySuperAdminViewModel,
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    StudyAdminScaffold(
        role = StudyRole.SUPER_ADMIN,
        title = "Role Permissions",
        currentRoute = StudyRoutes.SUPER_ROLES,
        onNavigate = onNavigate,
        onSignOut = onSignOut,
        snackbarHostState = snackbarHostState
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                SectionHeader(
                    "Module 2 permissions",
                    subtitle = "Defined in the mock data - read-only in this phase"
                )
            }
            items(viewModel.rolePermissions, key = { it.role.name }) { row ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(row.role.label, style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.width(8.dp))
                            StudyPill(row.role.teluguLabel, MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.weight(1f))
                            Text(
                                viewModel.countFor(row.role).toString() + " users",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        row.permissions.forEach { permission ->
                            Row(
                                Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(permission, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
