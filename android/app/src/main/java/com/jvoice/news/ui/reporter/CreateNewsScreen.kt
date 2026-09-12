package com.jvoice.news.ui.reporter

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.jvoice.news.components.ConfirmDialog
import com.jvoice.news.components.NewsImage
import com.jvoice.news.components.SectionHeader
import kotlinx.coroutines.launch
import com.jvoice.core.i18n.AppLanguage
import com.jvoice.core.i18n.LocalizedFormHeader
import com.jvoice.core.i18n.LocalizedOutlinedTextField
import com.jvoice.core.i18n.LocalizedText
import com.jvoice.core.i18n.Places
import com.jvoice.core.i18n.current
import com.jvoice.core.i18n.currentLanguage
import com.jvoice.core.i18n.lt
import com.jvoice.core.i18n.rememberLocalizedFormState

/**
 * Create News / Edit News. Reporters may edit drafts, rejected and sent-back
 * articles; everything is stored in the local mock repository.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNewsScreen(
    viewModel: ReporterViewModel,
    articleId: String?,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val form by viewModel.form.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showErrors by remember { mutableStateOf(false) }
    // Which language the copy fields below are bound to.
    val formState = rememberLocalizedFormState()
    var confirmSubmit by remember { mutableStateOf(false) }

    LaunchedEffect(articleId) {
        if (articleId.isNullOrBlank()) viewModel.startNewArticle() else viewModel.loadForEdit(articleId)
    }

    if (confirmSubmit) {
        ConfirmDialog(
            title = "Submit for review?",
            message = "The article moves to the editor's review queue and can no longer be edited by you until it comes back.",
            confirmLabel = "Submit",
            onConfirm = {
                confirmSubmit = false
                if (viewModel.save(submit = true)) onDone()
            },
            onDismiss = { confirmSubmit = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (articleId.isNullOrBlank()) "Create News" else "Edit News") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (viewModel.save(submit = false)) {
                            scope.launch { snackbarHostState.showSnackbar("Saved as draft") }
                            onDone()
                        } else {
                            showErrors = true
                            scope.launch { snackbarHostState.showSnackbar("Add a headline before saving") }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Save Draft") }

                Button(
                    onClick = {
                        showErrors = true
                        if (form.isValid) {
                            confirmSubmit = true
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please fix the highlighted fields")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.height(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Submit")
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            SectionHeader("Article content")

            // One set of boxes for both languages; the tab above chooses which
            // side they are bound to. Filing in one language is enough - the tab
            // badge shows what is still missing.
            LocalizedFormHeader(state = formState, fields = form.localizedFields)
            Spacer(Modifier.height(10.dp))

            LocalizedOutlinedTextField(
                value = form.headline,
                onValueChange = { value -> viewModel.updateForm { it.copy(headline = value) } },
                language = formState.language,
                label = lt("Headline", "శీర్షిక"),
                placeholder = lt("Headline in this language", "ఈ భాషలో శీర్షిక రాయండి"),
                required = true,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            if (showErrors && form.headlineError != null) {
                FieldError(form.headlineError!!)
            }

            LocalizedOutlinedTextField(
                value = form.shortDescription,
                onValueChange = { value -> viewModel.updateForm { it.copy(shortDescription = value) } },
                language = formState.language,
                label = lt("Short description", "సంక్షిప్త వివరణ"),
                required = true,
                minLines = 2,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            if (showErrors && form.descriptionError != null) {
                FieldError(form.descriptionError!!)
            }

            LocalizedOutlinedTextField(
                value = form.content,
                onValueChange = { value -> viewModel.updateForm { it.copy(content = value) } },
                language = formState.language,
                label = lt("Full article", "పూర్తి కథనం"),
                required = true,
                minLines = 8,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            if (showErrors && form.contentError != null) {
                FieldError(form.contentError!!)
            } else {
                // Counts the language on screen, so the reporter sees the length
                // of what they are actually typing.
                Text(
                    form.content.rawFor(formState.language).length.toString() +
                        (if (currentLanguage() == AppLanguage.TELUGU) " అక్షరాలు" else " characters"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            HorizontalDivider(Modifier.padding(16.dp))
            SectionHeader("Classification")

            Text(
                "Category *",
                style = MaterialTheme.typography.labelMedium,
                color = if (showErrors && form.categoryError != null) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.filter { it.isEnabled }.forEach { category ->
                    FilterChip(
                        selected = form.categoryId == category.id,
                        onClick = { viewModel.updateForm { it.copy(categoryId = category.id) } },
                        label = { Text(category.emoji + " " + category.name.current()) }
                    )
                }
            }

            Text(
                "Location",
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
                viewModel.locations.forEach { loc ->
                    FilterChip(
                        selected = form.location == loc,
                        onClick = { viewModel.updateForm { it.copy(location = loc) } },
                        // The chip shows the Telugu name but `loc` stays the
                        // English key the article is filtered on.
                        label = { Text(Places.render(loc, currentLanguage())) }
                    )
                }
            }

            // Tags are paired up by position across the two boxes on save, so
            // keeping the same order in both gives each tag its translation.
            LocalizedOutlinedTextField(
                value = form.tagsText,
                onValueChange = { value -> viewModel.updateForm { it.copy(tagsText = value) } },
                language = formState.language,
                label = lt("Tags (comma separated)", "ట్యాగ్లు (కామాలతో వేరు చేయండి)"),
                placeholder = lt("education, telangana", "విద్య, తెలంగాణ"),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            HorizontalDivider(Modifier.padding(16.dp))
            SectionHeader("Image", subtitle = "Paste a demo image URL - no upload service in Module 1")

            OutlinedTextField(
                value = form.imageUrl,
                onValueChange = { value -> viewModel.updateForm { it.copy(imageUrl = value) } },
                label = { Text("Image URL") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            OutlinedTextField(
                value = form.photosText,
                onValueChange = { value -> viewModel.updateForm { it.copy(photosText = value) } },
                label = { Text("More photos (one URL per line)") },
                placeholder = { Text("Extra stills for this story") },
                minLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            OutlinedTextField(
                value = form.videosText,
                onValueChange = { value -> viewModel.updateForm { it.copy(videosText = value) } },
                label = { Text("Videos (one URL per line)") },
                placeholder = { Text("Clips shot for this story") },
                minLines = 2,
                supportingText = {
                    Text("Photos and videos here become the visual source for an AI Short.")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            if (form.imageUrl.isNotBlank()) {
                NewsImage(
                    url = form.imageUrl,
                    contentDescription = "Preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(14.dp))
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Mark as Breaking News", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Editors can still change this during review",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = form.isBreaking,
                    onCheckedChange = { value -> viewModel.updateForm { it.copy(isBreaking = value) } }
                )
            }

            Box(Modifier.height(12.dp))
        }
    }
}

/**
 * Validation message for a bilingual field. The message itself is bilingual too -
 * the reporter reads it in whichever language they set the app to, regardless of
 * which content tab they are typing in.
 */
@Composable
private fun FieldError(message: LocalizedText) {
    Text(
        message.current(),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
    )
}
