package com.jvoice.core.auth

import com.jvoice.core.i18n.LocalizedText
import com.jvoice.core.i18n.lt
import com.jvoice.news.data.model.UserRole
import com.jvoice.study.data.model.StudyRole

/**
 * The single role string stored at `users/{uid}/role`, and how it maps onto the
 * app's two existing role enums.
 *
 * ### Why this type exists
 *
 * J Voice already had two unrelated role enums — [UserRole] for the news module
 * and [StudyRole] for study — and no shared notion of "who is this person". A
 * Firebase account has exactly one identity, so something has to reconcile them.
 * Rather than store two fields and risk them disagreeing, the database stores one
 * [code] and this enum resolves it to whichever module enums apply.
 *
 * [SUPER_ADMIN] is the case that makes the mapping non-trivial: it maps to *both*
 * module enums, which is what lets that account cross between modules.
 *
 * ### Readers and students are absent on purpose
 *
 * Only desk roles sign in. Readers and students use the app without an account —
 * they enter from the landing screen and their state lives on the device. So there
 * is no `READER` or `STUDENT` code here, and [fromCode] rejects them: an account
 * whose role is "reader" is a data error, not a login, and failing loudly beats
 * silently granting a desk session.
 */
enum class JvRole(
    /** Exactly the string stored in the database. Lower snake case, never renamed. */
    val code: String,
    val label: LocalizedText,
    /** The news-module role this grants, or null if it grants none. */
    val newsRole: UserRole?,
    /** The study-module role this grants, or null if it grants none. */
    val studyRole: StudyRole?
) {
    REPORTER(
        code = "reporter",
        label = lt("Reporter", "రిపోర్టర్"),
        newsRole = UserRole.REPORTER,
        studyRole = null
    ),
    EDITOR(
        code = "editor",
        label = lt("Editor", "ఎడిటర్"),
        newsRole = UserRole.EDITOR,
        studyRole = null
    ),
    NEWS_ADMIN(
        code = "news_admin",
        label = lt("News Admin", "న్యూస్ అడ్మిన్"),
        newsRole = UserRole.NEWS_ADMIN,
        studyRole = null
    ),
    CONTENT_CREATOR(
        code = "content_creator",
        label = lt("Content Creator", "కంటెంట్ క్రియేటర్"),
        newsRole = null,
        studyRole = StudyRole.CONTENT_CREATOR
    ),
    EXAM_ADMIN(
        code = "exam_admin",
        label = lt("Exam Admin", "ఎగ్జామ్ అడ్మిన్"),
        newsRole = null,
        studyRole = StudyRole.EXAM_ADMIN
    ),
    STUDY_ADMIN(
        code = "study_admin",
        label = lt("Study Admin", "స్టడీ అడ్మిన్"),
        newsRole = null,
        studyRole = StudyRole.STUDY_ADMIN
    ),

    /** Owns both modules, which is why it carries both enums. */
    SUPER_ADMIN(
        code = "super_admin",
        label = lt("Super Admin", "సూపర్ అడ్మిన్"),
        newsRole = UserRole.SUPER_ADMIN,
        studyRole = StudyRole.SUPER_ADMIN
    );

    /** True when this role's landing surface is the news module. */
    val startsInNews: Boolean get() = newsRole != null

    companion object {
        /**
         * Resolves a stored role string, or null when it is missing, unknown, or a
         * non-desk role.
         *
         * Null is a hard sign-in failure by design — see the class note. Matching is
         * case- and separator-insensitive so "News Admin", "news-admin" and
         * "news_admin" all resolve; the database should hold [code], but a human
         * typing into the console should not lock an account out.
         */
        fun fromCode(raw: String?): JvRole? {
            val normalised = raw?.trim()?.lowercase()?.replace('-', '_')?.replace(' ', '_')
                ?: return null
            return entries.firstOrNull { it.code == normalised }
        }

        /** Roles that may sign in, in the order the desk thinks about them. */
        val signInRoles: List<JvRole> get() = entries.toList()
    }
}
