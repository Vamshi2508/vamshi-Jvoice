package com.jvoice.study.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Relative time helper for the study module notifications and result history. */
fun Long.toRelativeTimeStudy(): String {
    val diff = System.currentTimeMillis() - this
    val minutes = diff / 60_000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> minutes.toString() + " min ago"
        hours < 24 -> if (hours == 1L) "1 hour ago" else hours.toString() + " hours ago"
        days < 7 -> if (days == 1L) "Yesterday" else days.toString() + " days ago"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date(this))
    }
}

fun Int.secondsToClock(): String {
    val m = this / 60
    val s = this % 60
    return (if (m < 10) "0" else "") + m + ":" + (if (s < 10) "0" else "") + s
}
