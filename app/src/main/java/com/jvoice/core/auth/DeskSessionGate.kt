package com.jvoice.core.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jvoice.core.i18n.LocalizedText
import com.jvoice.core.i18n.Strings
import com.jvoice.core.i18n.current
import com.jvoice.core.i18n.tr

/**
 * The launch sequence for a cached desk session — the equivalent of SafeTrack's
 * splash screen, minus the screen.
 *
 * On every launch where a session is stored on the device, exactly two server
 * checks run before the app routes anywhere:
 *
 *  1. `isLogin` — the kill-switch ([AuthGate.isAccountEnabled])
 *  2. `update/{force,version}` — the version gate ([AppUpdateGate])
 *
 * Credentials are **not** re-validated; see [SessionStore] for why. Both checks
 * fail open, so a network problem does not lock out a valid session.
 *
 * This is a controller with no UI of its own beyond a spinner and two dialogs: it
 * decides, then calls [onReady] or [onRejected] and gets out of the way.
 */
@Composable
fun DeskSessionGate(
    session: SessionStore.DeskSession,
    installedVersion: Long,
    onReady: () -> Unit,
    onRejected: (LocalizedText) -> Unit
) {
    var updateVerdict by remember {
        mutableStateOf<AppUpdateGate.Verdict?>(null)
    }
    var checking by remember { mutableStateOf(true) }

    LaunchedEffect(session.uid) {
        // 1. Kill-switch. A disabled account is signed out here, not later.
        if (!AuthGate.isAccountEnabled(session.uid)) {
            AuthGate.logout()
            onRejected(Strings.Auth.accountDisabled)
            return@LaunchedEffect
        }

        // 2. Update gate. A non-blocking verdict still shows the dialogue, but the
        // user can dismiss it and continue.
        when (val verdict = AppUpdateGate.check(session.uid, installedVersion)) {
            AppUpdateGate.Verdict.Ok -> {
                checking = false
                onReady()
            }

            is AppUpdateGate.Verdict.UpdateRequired -> {
                checking = false
                updateVerdict = verdict
            }
        }
    }

    if (checking) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    (updateVerdict as? AppUpdateGate.Verdict.UpdateRequired)?.let { verdict ->
        AlertDialog(
            // A forced update must not be dismissable, which is the only thing
            // `force` actually changes.
            onDismissRequest = {
                if (!verdict.blocking) {
                    updateVerdict = null
                    onReady()
                }
            },
            title = { Text(tr(Strings.Auth.updateTitle)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(tr(Strings.Auth.updateBody))
                    Text(
                        "Required version: ${verdict.requiredVersion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                if (!verdict.blocking) {
                    TextButton(onClick = {
                        updateVerdict = null
                        onReady()
                    }) { Text(tr(Strings.Auth.later)) }
                }
            }
        )
    }
}
