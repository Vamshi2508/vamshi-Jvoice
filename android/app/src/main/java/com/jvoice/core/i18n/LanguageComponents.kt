package com.jvoice.core.i18n

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Reader-facing language controls.
 *
 * Two shapes, one source of truth ([LanguagePreference]):
 *  - [LanguageSegmentedToggle] for the profile tab - a two-up segmented control.
 *  - [FirstRunLanguageDialog] for the very first launch, which also carries the
 *    notification prompt so a new reader answers both questions once.
 */

/* ------------------------------------------------------- profile-tab toggle */

/**
 * The segmented `[ తెలుగు | English ]` control that lives in the reader and
 * student profile tabs. Shows each language in its own script, so a reader who
 * cannot read the other one can still find their own.
 */
@Composable
fun LanguageSegmentedToggle(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            AppLanguage.entries.forEach { language ->
                val isSelected = language == selected
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(enabled = !isSelected) { onSelect(language) }
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedVisibility(visible = isSelected) {
                            Row {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                            }
                        }
                        Text(
                            language.labelNative,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * The whole language block for a profile screen: a labelled row with the toggle
 * underneath and a line explaining how far the choice reaches.
 */
@Composable
fun LanguagePreferenceCard(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        ListItem(
            leadingContent = {
                Icon(Icons.Default.Translate, contentDescription = null)
            },
            headlineContent = { Text(tr(Strings.Language.title)) },
            supportingContent = {
                Text(
                    tr(Strings.Language.readingIn) + ": " + selected.labelNative,
                    style = MaterialTheme.typography.bodySmall
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
        LanguageSegmentedToggle(
            selected = selected,
            onSelect = onSelect,
            modifier = Modifier.padding(start = 56.dp, end = 16.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            tr(Strings.Language.appliesEverywhere),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 56.dp, end = 16.dp, bottom = 4.dp)
        )
    }
}

/* -------------------------------------------------------- first-run dialogue */

/**
 * First launch, both questions at once: which language, and may we notify you.
 *
 * The language list is rendered in each language's own script and is NOT
 * translated by the active language - a reader arriving with the wrong default
 * has to be able to recognise their own row.
 *
 * [onAllowNotifications] fires the real runtime permission request; the dialogue
 * itself only ever raises it once, guarded by
 * [LanguagePreference.markNotificationsAsked].
 */
@Composable
fun FirstRunLanguageDialog(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onAllowNotifications: () -> Unit,
    onSkipNotifications: () -> Unit,
    onDone: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Deliberately not dismissible - both answers are wanted. */ },
        icon = {
            Icon(
                Icons.Default.Language,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    Strings.FirstRun.welcome.get(selected),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    Strings.FirstRun.welcomeSubtitle.get(selected),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    Strings.Language.chooseTitle.get(selected),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                AppLanguage.entries.forEach { language ->
                    val isSelected = language == selected
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelect(language) }
                    ) {
                        Row(
                            Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                // Each language names itself - never translated.
                                Text(
                                    language.labelNative,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (language.labelNative != language.labelEn) {
                                    Text(
                                        language.labelEn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(2.dp))

                // ----------------------------------------- notification prompt
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                Strings.FirstRun.notificationTitle.get(selected),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                Strings.FirstRun.notificationBody.get(selected),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onAllowNotifications()
                onDone()
            }) {
                Text(Strings.FirstRun.allowNotifications.get(selected))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onSkipNotifications()
                onDone()
            }) {
                Text(Strings.FirstRun.notNow.get(selected))
            }
        }
    )
}

/* -------------------------------------------------- missing-translation badge */

/**
 * Small inline badge for the desk lists: this item exists in one language only.
 * Reader surfaces never show it - they fall back silently instead.
 */
@Composable
fun TranslationBadge(
    text: LocalizedText,
    modifier: Modifier = Modifier
) {
    if (text.isComplete || text.isBlank) return
    val missing = text.missingLanguages.firstOrNull() ?: return
    Box(
        modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            "⚠ " + missing.labelEn,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}
