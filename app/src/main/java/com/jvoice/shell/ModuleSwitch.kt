package com.jvoice.shell

import androidx.compose.runtime.staticCompositionLocalOf

/** The two modules of the J Voice app. */
enum class AppModule(val label: String, val emoji: String) {
    NEWS("News", "📰"),
    STUDY("Study", "📚")
}

/**
 * Lets a screen cross between Module 1 and Module 2 without going back to the
 * landing page.
 *
 * Supplied once by the app shell and read through [LocalModuleSwitcher], so no
 * screen signature has to change to carry it - which keeps the existing News
 * screens untouched.
 *
 * Three entry points, because the two crossings mean different things:
 *  - [switchToOtherAsSuperAdmin] keeps the Super Admin's rank across modules.
 *  - [openStudyAsStudent] backs the Reader's "Study" bottom-navigation tab.
 *  - [openNewsAsReader] is the way back from the Student experience.
 */
data class ModuleSwitcher(
    val current: AppModule,
    val switchToOtherAsSuperAdmin: () -> Unit,
    val openStudyAsStudent: () -> Unit,
    val openNewsAsReader: () -> Unit
) {
    val other: AppModule
        get() = if (current == AppModule.NEWS) AppModule.STUDY else AppModule.NEWS
}

val LocalModuleSwitcher = staticCompositionLocalOf<ModuleSwitcher?> { null }
