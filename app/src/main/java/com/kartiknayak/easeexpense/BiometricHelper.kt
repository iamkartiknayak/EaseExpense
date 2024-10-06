package com.kartiknayak.easeexpense

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class BiometricHelper {
    private lateinit var biometricManager: BiometricManager
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var authResult: BiometricAuthResult

    fun initializeBiometricPrompt(
        context: MainActivity,
        callback: (BiometricAuthResult) -> Unit,
    ): BiometricAuthResult {
        authResult = BiometricAuthResult.CANCELED
        biometricManager = BiometricManager.from(context)
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Ease Expense")
            .setDescription("Authenticate to access")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        val executor = ContextCompat.getMainExecutor(context)
        val canAuthenticate =
            biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL)

        // if authentication is not set-up on the device
        if (canAuthenticate == 11) SharedFunctions().loadMainScreen(context)

        biometricPrompt = BiometricPrompt(
            context,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    authResult = BiometricAuthResult.ERROR
                    callback(authResult)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    authResult = BiometricAuthResult.SUCCESS
                    callback(authResult)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    authResult = BiometricAuthResult.CANCELED
                    callback(authResult)
                }
            })

        biometricPrompt.authenticate(promptInfo)
        return authResult
    }
}