package com.jvoice.core.i18n

/**
 * The two languages J Voice ships content in.
 *
 * [TELUGU] is the default: the audience is Telugu-first, so a reader who never
 * opens the picker gets Telugu. Nothing here is derived from the device locale -
 * the choice is explicit and remembered, because a Telugu reader on an English
 * phone is the common case, not the exception.
 */
enum class AppLanguage(
    val code: String,
    /** Name of this language written in English - for admin/desk screens. */
    val labelEn: String,
    /** Name of this language written in itself - for the reader-facing picker. */
    val labelNative: String,
    /** Two-letter chip used in compact toggles. */
    val shortLabel: String
) {
    TELUGU("te", "Telugu", "తెలుగు", "తె"),
    ENGLISH("en", "English", "English", "EN");

    val other: AppLanguage get() = if (this == TELUGU) ENGLISH else TELUGU

    companion object {
        val DEFAULT = TELUGU
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: DEFAULT
    }
}

/**
 * One piece of content in both languages.
 *
 * This is the spine of the bilingual build: every user-visible string that is
 * *content* (a headline, a body, a question, an option, an explanation) is a
 * [LocalizedText] rather than a [String], so the language can be chosen at read
 * time instead of at authoring time.
 *
 * Either side may be blank. The desk requires one language and treats the second
 * as optional, so [get] falls back to whichever side is filled - a reader never
 * sees an empty screen because a translation has not landed yet. Use
 * [rawFor] when you need to know what is actually stored (the authoring forms
 * do, so an empty English box stays empty instead of echoing the Telugu).
 */
data class LocalizedText(
    val en: String,
    val te: String
) {
    /** Reader-facing text: the requested language, or the other one if it is blank. */
    fun get(language: AppLanguage): String = when (language) {
        AppLanguage.TELUGU -> te.ifBlank { en }
        AppLanguage.ENGLISH -> en.ifBlank { te }
    }

    /** What is actually stored for [language] - blank stays blank. Authoring forms use this. */
    fun rawFor(language: AppLanguage): String = when (language) {
        AppLanguage.TELUGU -> te
        AppLanguage.ENGLISH -> en
    }

    fun hasVersionFor(language: AppLanguage): Boolean = rawFor(language).isNotBlank()

    /** Both languages written. Drives the "needs translation" badges on the desk. */
    val isComplete: Boolean get() = en.isNotBlank() && te.isNotBlank()

    /** Nothing written at all - an untouched field. */
    val isBlank: Boolean get() = en.isBlank() && te.isBlank()

    /** The languages still waiting on a translation, in picker order. */
    val missingLanguages: List<AppLanguage>
        get() = AppLanguage.entries.filter { !hasVersionFor(it) }

    fun with(language: AppLanguage, value: String): LocalizedText = when (language) {
        AppLanguage.TELUGU -> copy(te = value)
        AppLanguage.ENGLISH -> copy(en = value)
    }

    /**
     * Search matches against *both* languages regardless of the active one, so a
     * reader browsing in Telugu still finds a story by typing its English name.
     */
    fun matches(query: String): Boolean {
        val q = query.trim()
        if (q.isBlank()) return true
        return en.contains(q, ignoreCase = true) || te.contains(q, ignoreCase = true)
    }

    /** Trims both languages. */
    fun trimmed(): LocalizedText = LocalizedText(en = en.trim(), te = te.trim())

    /**
     * Truncates both languages independently, for notification previews and
     * one-line summaries. Trims per language rather than per byte because the
     * two scripts do not agree on how much text a given count is worth.
     */
    fun trimmedTo(maxChars: Int): LocalizedText = LocalizedText(
        en = if (en.length > maxChars) en.take(maxChars).trimEnd() + "…" else en,
        te = if (te.length > maxChars) te.take(maxChars).trimEnd() + "…" else te
    )

    /** Both versions stacked - used by the desk when it deliberately shows both. */
    fun bothLines(primary: AppLanguage = AppLanguage.DEFAULT): String {
        if (!isComplete) return get(primary)
        val first = rawFor(primary)
        val second = rawFor(primary.other)
        return "$first\n$second"
    }

    /** Both versions on one line - the old `"English / తెలుగు"` display style. */
    fun inline(primary: AppLanguage = AppLanguage.DEFAULT): String {
        if (!isComplete) return get(primary)
        return rawFor(primary) + " / " + rawFor(primary.other)
    }

    override fun toString(): String = if (isComplete) "$en / $te" else get(AppLanguage.DEFAULT)

    companion object {
        val EMPTY = LocalizedText("", "")

        /** One string that needs no translation - a number, a name, a code. */
        fun both(value: String) = LocalizedText(value, value)

        fun en(value: String) = LocalizedText(en = value, te = "")
        fun te(value: String) = LocalizedText(en = "", te = value)
    }
}

/** Terse constructor so the mock data and string table stay readable. */
fun lt(en: String, te: String) = LocalizedText(en = en, te = te)

/* ------------------------------------------------------------------ helpers */

fun List<LocalizedText>.get(language: AppLanguage): List<String> =
    map { it.get(language) }.filter { it.isNotBlank() }

/** True when every entry carries both languages. */
val List<LocalizedText>.isComplete: Boolean get() = all { it.isComplete }

/** Any entry in the list matching the query in either language. */
fun List<LocalizedText>.anyMatches(query: String): Boolean = any { it.matches(query) }

/** Joins a bilingual list for a single-language render. */
fun List<LocalizedText>.joinFor(language: AppLanguage, separator: String = ", "): String =
    get(language).joinToString(separator)
