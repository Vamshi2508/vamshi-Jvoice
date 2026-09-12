package com.jvoice.core.auth

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jvoice.core.firebase.FirebaseAvailability

/**
 * Ends a signed-in session the moment the desk revokes it.
 *
 * Watches `users/{uid}/forceLogoutAt`. An administrator bumping that timestamp —
 * on suspending an account, changing its password, or handing a device to someone
 * else — signs the app out immediately, rather than at the next launch, which is
 * all the [AuthGate] `isLogin` check can offer.
 *
 * ### The first snapshot is skipped, and it has to be
 *
 * A Firebase value listener fires immediately on attach with whatever is already
 * stored. `forceLogoutAt` is never cleared — it is a monotonic marker — so a user
 * who was force-logged-out last week still has a non-zero value there today. Acting
 * on that first snapshot would sign them out the instant they logged back in, an
 * unfixable-looking loop where a correct password appears to do nothing.
 *
 * So the first callback only records that the listener attached. Only a *later*
 * change — an actual bump while the app is open — triggers the logout.
 *
 * Mount this inside each signed-in graph, not at the app root: it should be
 * listening exactly as long as there is a session to end.
 */
@Composable
fun ForceLogoutGuard(onLogout: () -> Unit) {
    val uid = SessionStore.current?.uid

    DisposableEffect(uid) {
        if (uid == null || !FirebaseAvailability.isAvailable) {
            return@DisposableEffect onDispose { }
        }

        val ref = FirebaseDatabase.getInstance().reference
            .child("users").child(uid).child("forceLogoutAt")

        // See the note above: this is the whole mechanism, not an optimisation.
        var seenInitialSnapshot = false

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!seenInitialSnapshot) {
                    seenInitialSnapshot = true
                    return
                }
                val stamp = snapshot.value?.toString()?.toLongOrNull() ?: 0L
                if (stamp > 0L) {
                    Log.i("ForceLogoutGuard", "forceLogoutAt bumped for $uid - signing out")
                    onLogout()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // A cancelled listener means the rules rejected the read or the
                // connection dropped. Neither should sign the user out on its own -
                // the launch-time isLogin check is the backstop.
                Log.w("ForceLogoutGuard", "Listener cancelled: ${error.message}")
            }
        }

        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }
}
