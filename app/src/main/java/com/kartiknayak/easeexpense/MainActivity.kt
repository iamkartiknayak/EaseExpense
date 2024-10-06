package com.kartiknayak.easeexpense

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.kartiknayak.easeexpense.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedFunctions = SharedFunctions()
        val (introDone, authEnabled) = sharedFunctions.getAppBootData(baseContext)

        if (!introDone) {
            val intent = Intent(this, IntroActivity::class.java)
            this.startActivity(intent)
        } else if (!authEnabled) {
            sharedFunctions.loadMainScreen(this)
        } else {
            val biometricHelper = BiometricHelper()
            biometricHelper.initializeBiometricPrompt(this) { result: BiometricAuthResult ->
                when (result) {
                    BiometricAuthResult.SUCCESS -> sharedFunctions.loadMainScreen(this)
                    BiometricAuthResult.ERROR -> finishAffinity()
                    BiometricAuthResult.CANCELED -> Log.e("AUTH_ERROR", "Error Authenticating")
                }
            }
        }
    }
}