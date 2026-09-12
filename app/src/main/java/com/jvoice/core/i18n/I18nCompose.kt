package com.jvoice.core.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Reads a bilingual string in the language the reader has chosen.
 *
 * `Text(tr(Strings.Common.save))` is the whole idiom - short on purpose, because
 * it replaces several hundred literal strings and anything longer would bury the
 * screens it appears in.
 */
@Composable
@ReadOnlyComposable
fun tr(text: LocalizedText): String = text.get(LocalAppLanguage.current)

/** Same thing as an extension, for chaining off a model field. */
@Composable
@ReadOnlyComposable
fun LocalizedText.current(): String = get(LocalAppLanguage.current)

/** A bilingual list in the active language. */
@Composable
@ReadOnlyComposable
fun trList(items: List<LocalizedText>): List<String> = items.get(LocalAppLanguage.current)

/** The active language itself, when a screen needs to branch on it. */
@Composable
@ReadOnlyComposable
fun currentLanguage(): AppLanguage = LocalAppLanguage.current
