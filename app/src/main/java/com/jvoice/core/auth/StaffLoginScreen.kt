package com.jvoice.core.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jvoice.core.i18n.Strings
import com.jvoice.core.i18n.current
import com.jvoice.core.i18n.tr
import com.jvoice.news.components.JVoiceLogo
import com.jvoice.news.theme.JvBlueDark
import com.jvoice.news.theme.JvInk

/**
 * The staff sign-in screen.
 *
 * Bilingual throughout, including the error messages — a district reporter filing
 * in Telugu should not have to read an English failure to understand that their
 * password was wrong.
 *
 * On the same navy-to-ink ground the landing hero uses, because that is what the
 * logo is designed to sit on.
 */
@Composable
fun StaffLoginScreen(
    onSignedIn: (SessionStore.DeskSession) -> Unit,
    onBack: () -> Unit
) {
    val viewModel: LoginViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()

    var loginId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetPhone by remember { mutableStateOf("") }
    var resetResult by remember { mutableStateOf<String?>(null) }

    val isLoading = state is LoginUiState.Loading

    // Routing on success belongs to the caller, so the screen reports and forgets.
    LaunchedEffect(state) {
        (state as? LoginUiState.Success)?.let { onSignedIn(it.session) }
    }

    val submit = {
        if (!isLoading) viewModel.signIn(loginId, password)
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(JvBlueDark, JvInk)))
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = tr(Strings.Common.back),
                tint = Color.White
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(56.dp))
            JVoiceLogo(width = 220.dp)
            Spacer(Modifier.height(22.dp))

            Text(
                tr(Strings.Auth.staffSignIn),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            Spacer(Modifier.height(6.dp))
            Text(
                tr(Strings.Auth.subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(26.dp))

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {
                    OutlinedTextField(
                        value = loginId,
                        onValueChange = { loginId = it.trim() },
                        label = { Text(tr(Strings.Auth.loginId)) },
                        placeholder = { Text(tr(Strings.Auth.loginIdHint)) },
                        // The domain is shown rather than typed. Staff enter a bare
                        // username and LoginViewModel expands it - see toEmail().
                        suffix = { Text("@" + LoginViewModel.LOGIN_DOMAIN) },
                        singleLine = true,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        isError = state is LoginUiState.Error,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(tr(Strings.Auth.password)) },
                        singleLine = true,
                        enabled = !isLoading,
                        visualTransformation =
                            if (passwordVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                        isError = state is LoginUiState.Error,
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    contentDescription = tr(
                                        if (passwordVisible) Strings.Auth.hidePassword
                                        else Strings.Auth.showPassword
                                    )
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    (state as? LoginUiState.Error)?.let { error ->
                        Spacer(Modifier.height(10.dp))
                        Text(
                            error.message.current(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    Button(
                        onClick = submit,
                        enabled = !isLoading && loginId.isNotBlank() && password.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(17.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(tr(Strings.Auth.signingIn))
                        } else {
                            Text(tr(Strings.Auth.signIn))
                        }
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = { showResetDialog = true },
                            enabled = !isLoading
                        ) {
                            Text(tr(Strings.Auth.forgotPassword))
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            TextButton(onClick = onBack) {
                Text(
                    tr(Strings.Auth.continueAsReader) + "  ·  " +
                        tr(Strings.Auth.noAccountNeeded),
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false; resetResult = null },
            title = { Text(tr(Strings.Auth.resetTitle)) },
            text = {
                Column {
                    Text(tr(Strings.Auth.resetBody), style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resetPhone,
                        onValueChange = { resetPhone = it.filter(Char::isDigit).take(10) },
                        label = { Text(tr(Strings.Study.mobileNumber)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    resetResult?.let {
                        Spacer(Modifier.height(10.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = resetPhone.length == 10,
                    onClick = {
                        viewModel.requestPasswordReset(resetPhone) { message ->
                            resetResult = message.get(
                                com.jvoice.core.i18n.LanguagePreference.current
                            )
                        }
                    }
                ) { Text(tr(Strings.Auth.sendRequest)) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false; resetResult = null }) {
                    Text(tr(Strings.Common.cancel))
                }
            }
        )
    }
}
