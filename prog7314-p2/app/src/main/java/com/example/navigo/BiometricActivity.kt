
package com.example.navigo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class BiometricActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        window.statusBarColor = getColor(R.color.navigo_teal)
        window.navigationBarColor = getColor(R.color.navigo_background)

        setContentView(R.layout.activity_biometric)

        val btnEnableBiometrics =
            findViewById<Button>(R.id.btnEnableBiometrics)

        val tvMaybeLater =
            findViewById<TextView>(R.id.tvMaybeLater)

        // Actual biometric authentication is reserved for the final POE.
        btnEnableBiometrics.setOnClickListener {
            Toast.makeText(
                this,
                "Biometric authentication will be available in the final POE",
                Toast.LENGTH_LONG
            ).show()
        }

        // Skip biometric setup and continue to Home.
        tvMaybeLater.setOnClickListener {
            openHome()
        }
    }

    private fun openHome() {
        val intent = Intent(this, HomeActivity::class.java)

        // Clear the previous authentication/setup screens.
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}