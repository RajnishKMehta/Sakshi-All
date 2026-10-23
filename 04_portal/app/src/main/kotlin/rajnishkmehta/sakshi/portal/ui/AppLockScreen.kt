package rajnishkmehta.sakshi.portal.ui

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import rajnishkmehta.sakshi.portal.R
import rajnishkmehta.sakshi.portal.data.SettingsRepository
import rajnishkmehta.sakshi.portal.debug.DebugLogger as Log

@Composable
fun AppLockScreen(
    appLockManager: AppLockManager,
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val biometricAuthEnabled by settingsRepository.biometricAuthEnabledFlow.collectAsState(initial = true)

    var showPrompt by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var actionRequired by remember { mutableStateOf<Boolean>(false) }

    val promptTitle = stringResource(R.string.app_lock_prompt_title)
    val noCredentialsMsg = stringResource(R.string.app_lock_no_credentials)

    val promptInfo = remember(biometricAuthEnabled, promptTitle) {
        val allowedAuthenticators = if (biometricAuthEnabled) {
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        }

        BiometricPrompt.PromptInfo.Builder()
            .setTitle(promptTitle)
            .setAllowedAuthenticators(allowedAuthenticators)
            .build()
    }

    LaunchedEffect(showPrompt, promptInfo) {
        if (showPrompt && activity != null) {
            val executor = ContextCompat.getMainExecutor(activity)
            val biometricPrompt = BiometricPrompt(activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Log.e("AppLockScreen", "Auth error: \$errorCode, \$errString")

                        if (errorCode == BiometricPrompt.ERROR_CANCELED || errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                            showPrompt = false
                        } else if (errorCode == BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL) {
                            errorMsg = noCredentialsMsg
                            actionRequired = true
                            showPrompt = false
                        } else {
                            errorMsg = errString.toString()
                            showPrompt = false
                        }
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        Log.i("AppLockScreen", "Auth succeeded.")
                        appLockManager.unlock()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Log.w("AppLockScreen", "Auth failed.")
                    }
                })

            biometricPrompt.authenticate(promptInfo)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (errorMsg != null) {
                Text(
                    text = errorMsg!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (actionRequired) {
                Button(onClick = {
                    val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                    context.startActivity(intent)
                }) {
                    Text(stringResource(R.string.app_lock_open_settings))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!showPrompt) {
                Button(onClick = {
                    errorMsg = null
                    actionRequired = false
                    showPrompt = true
                }) {
                    Text(stringResource(R.string.app_lock_retry_auth))
                }
            }
        }
    }
}
