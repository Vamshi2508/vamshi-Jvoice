package com.jvoice.core.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.jvoice.news.BuildConfig

/**
 * Whether this build can actually talk to Firebase.
 *
 * ### Why a runtime guard is needed at all
 *
 * `google-services.json` is not in version control and may be absent — a fresh
 * clone, a contributor without console access, a CI job. The Gradle build handles
 * that by applying the google-services plugin conditionally (see
 * `app/build.gradle.kts`), which means a build without the file produces an APK
 * with the Firebase SDKs present but no configuration behind them.
 *
 * In that APK `FirebaseApp.initializeApp` returns null and every later Firebase
 * call throws. Rather than let that surface as a crash inside a login button, the
 * app asks this object once at startup and falls back to the local demo logins.
 *
 * [BuildConfig.HAS_FIREBASE_CONFIG] carries the Gradle-time answer, so the build
 * and the runtime cannot disagree about which mode the app is in. The actual
 * [FirebaseApp] initialisation is still attempted, because the flag only says the
 * file existed — it does not prove the contents are valid.
 */
object FirebaseAvailability {

    private const val TAG = "FirebaseAvailability"

    @Volatile
    private var initialised = false

    @Volatile
    private var available = false

    /**
     * Attempts initialisation once. Safe to call repeatedly and from any thread;
     * later calls return the first answer.
     */
    fun init(context: Context): Boolean {
        if (initialised) return available
        synchronized(this) {
            if (initialised) return available
            available = try {
                if (!BuildConfig.HAS_FIREBASE_CONFIG) {
                    Log.w(
                        TAG,
                        "Built without google-services.json - running on local demo logins. " +
                            "See firebase/README.md to connect the jvoice project."
                    )
                    false
                } else {
                    // Returns null rather than throwing when there is no usable config.
                    val app = FirebaseApp.initializeApp(context.applicationContext)
                    if (app == null) {
                        Log.e(TAG, "google-services.json present but FirebaseApp init returned null.")
                        false
                    } else {
                        Log.i(TAG, "Firebase ready: project=${app.options.projectId}")
                        true
                    }
                }
            } catch (e: Exception) {
                // Deliberately broad: a malformed config throws several different
                // exception types out of the SDK, and none of them should take the
                // app down at startup.
                Log.e(TAG, "Firebase initialisation failed, falling back to demo logins: ${e.message}", e)
                false
            }
            initialised = true
            return available
        }
    }

    /**
     * True when real Firebase auth is usable.
     *
     * Reads the cached answer; [init] must have run first (MainActivity does it in
     * `onCreate`). Returns false rather than throwing if it has not, because a
     * false answer degrades to demo mode while an exception would not.
     */
    val isAvailable: Boolean get() = initialised && available

    /** True when the app is deliberately running on local mock logins. */
    val isDemoMode: Boolean get() = !isAvailable
}
