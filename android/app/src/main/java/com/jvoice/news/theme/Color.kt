package com.jvoice.news.theme

import androidx.compose.ui.graphics.Color

/**
 * J Voice brand palette, taken from the logo.
 *
 * The mark is a scarlet "J" over a royal-blue globe, with a red/blue split
 * wordmark and a silver tagline strip. So the app runs on two brand colours
 * rather than one: scarlet leads (it is the J, the wordmark's first block and
 * the breaking-news accent) and royal blue is the structural second.
 *
 * The previous single muted red (#B3261E) has been replaced by the logo's
 * scarlet - the old accent read as maroon next to the new mark.
 */

// ---------------------------------------------------------------- scarlet
val JvRed = Color(0xFFDD0B1E)
val JvRedDark = Color(0xFF9E0715)
val JvRedBright = Color(0xFFFF3B4A)
val JvRedLight = Color(0xFFFFDAD8)

// ------------------------------------------------------------- royal blue
val JvBlue = Color(0xFF17318F)
val JvBlueDark = Color(0xFF0D1F63)
val JvBlueBright = Color(0xFF2A57CC)
val JvBlueLight = Color(0xFFDCE1FF)

/** The tagline strip and the wordmark's letterforms. */
val JvSilver = Color(0xFFE9ECF1)

val JvSaffron = Color(0xFFE07B00)
val JvSaffronLight = Color(0xFFFFDDB3)
val JvGreen = Color(0xFF1B7F4B)
val JvGreenLight = Color(0xFFB8F2CE)

val JvInk = Color(0xFF1B1B1F)
val JvInkSoft = Color(0xFF44464F)
val JvSurfaceLight = Color(0xFFFDFBFF)
val JvSurfaceVariantLight = Color(0xFFF2F0F4)
val JvSurfaceDark = Color(0xFF121316)
val JvSurfaceVariantDark = Color(0xFF25262B)

// Status colours used by the workflow chips.
val StatusDraft = Color(0xFF6E7078)
val StatusSubmitted = Color(0xFF1565C0)
val StatusUnderReview = Color(0xFF7B4DFF)
val StatusApproved = Color(0xFF1B7F4B)
val StatusRejected = Color(0xFFC62828)
val StatusSentBack = Color(0xFFE07B00)
val StatusPublished = Color(0xFF00695C)
