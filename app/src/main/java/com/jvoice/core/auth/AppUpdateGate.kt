package com.jvoice.core.auth

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.jvoice.core.firebase.FirebaseAvailability
import kotlinx.coroutines.tasks.await

/**
 * The per-account update gate.
 *
 * Reads `users/{uid}/update/{force,version}` once at launch. If the installed
 * versionCode is below `version`, the app is out of date; `force` decides whether
 * that merely warns or blocks.
 *
 * ### Everything here fails open
 *
 * A missing node, an unreadable one, a malformed version — all mean "no update
 * required". This is deliberate: the gate exists to stop a known-broken client
 * from writing bad data, and the cost of a false positive is that a working
 * install is bricked until someone edits the database. Treating absence as
 * "blocked" would make an unrelated network problem look like a forced update.
 *
 * Per-account rather than global on purpose - it lets the desk pin one tester to a
 * build without gating every reporter in the state.
 */
object AppUpdateGate {

    private const val TAG = "AppUpdateGate"

    /** What the launch sequence should do about the installed version. */
    sealed interface Verdict {
        /** Up to date, or no requirement recorded. Carry on. */
        data object Ok : Verdict

        /** Out of date. [blocking] true means the app must not proceed. */
        data class UpdateRequired(val requiredVersion: Long, val blocking: Boolean) : Verdict
    }

    suspend fun check(uid: String, installedVersion: Long): Verdict {
        if (!FirebaseAvailability.isAvailable) return Verdict.Ok
        return try {
            val snapshot = FirebaseDatabase.getInstance().reference
                .child("users").child(uid).child("update").get().await()

            if (!snapshot.exists()) return Verdict.Ok

            val required = snapshot.child("version").value?.toString()?.toLongOrNull()
                ?: return Verdict.Ok
            if (installedVersion >= required) return Verdict.Ok

            val force = when (val raw = snapshot.child("force").value) {
                is Boolean -> raw
                is Number -> raw.toInt() != 0
                is String -> raw.equals("true", ignoreCase = true) || raw == "1"
                else -> false
            }
            Log.i(TAG, "Update required: installed=$installedVersion required=$required force=$force")
            Verdict.UpdateRequired(required, force)
        } catch (e: Exception) {
            Log.w(TAG, "Update check failed, treating as up to date: ${e.message}")
            Verdict.Ok
        }
    }
}
