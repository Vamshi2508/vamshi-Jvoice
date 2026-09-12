package com.jvoice.news.utils

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import com.jvoice.news.data.model.NewsArticle
import com.jvoice.news.data.model.NewsStatus
import com.jvoice.news.theme.StatusApproved
import com.jvoice.news.theme.StatusDraft
import com.jvoice.news.theme.StatusPublished
import com.jvoice.news.theme.StatusRejected
import com.jvoice.news.theme.StatusSentBack
import com.jvoice.news.theme.StatusSubmitted
import com.jvoice.news.theme.StatusUnderReview
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.jvoice.core.i18n.AppLanguage
import com.jvoice.core.i18n.LanguagePreference

/** "2 hours ago" style relative time, with a Telugu-friendly short form. */
fun Long.toRelativeTime(): String {
    val diff = System.currentTimeMillis() - this
    val minutes = diff / 60_000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> if (hours == 1L) "1 hour ago" else "$hours hours ago"
        days < 7 -> if (days == 1L) "Yesterday" else "$days days ago"
        else -> toFullDate()
    }
}

fun Long.toFullDate(): String =
    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date(this))

fun Long.toShortDate(): String =
    SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH).format(Date(this))

fun Int.toReadableCount(): String = when {
    this >= 100_000 -> String.format(Locale.ENGLISH, "%.1fL", this / 100_000f)
    this >= 1_000 -> String.format(Locale.ENGLISH, "%.1fK", this / 1_000f)
    else -> toString()
}

fun NewsStatus.color(): Color = when (this) {
    NewsStatus.DRAFT -> StatusDraft
    NewsStatus.SUBMITTED -> StatusSubmitted
    NewsStatus.UNDER_REVIEW -> StatusUnderReview
    NewsStatus.APPROVED -> StatusApproved
    NewsStatus.REJECTED -> StatusRejected
    NewsStatus.SENT_BACK -> StatusSentBack
    NewsStatus.PUBLISHED -> StatusPublished
}

/**
 * Local share sheet - uses the OS chooser, no backend involved.
 *
 * Shares in the language the reader is reading in, since that is the version
 * they chose to read and presumably the one their contacts read too.
 */
fun Context.shareArticle(
    article: NewsArticle,
    language: AppLanguage = LanguagePreference.current
) {
    val headline = article.headline.get(language)
    val text = buildString {
        append(headline)
        append("\n\n")
        append(article.shortDescription.get(language))
        append("\n\nvia J Voice (demo content)")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, headline)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(intent, "Share via"))
}

fun String.toTagList(): List<String> =
    split(",", " ")
        .map { it.trim().removePrefix("#") }
        .filter { it.isNotBlank() }
        .distinct()
