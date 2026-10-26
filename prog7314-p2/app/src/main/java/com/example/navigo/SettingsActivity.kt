package com.example.navigo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.navigo.repository.AuthRepository


class SettingsActivity : AppCompatActivity() {

    private val preferences by lazy {
        getSharedPreferences("navigo_settings", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = getColor(R.color.navigo_teal)
        window.navigationBarColor = Color.WHITE

        setContentView(R.layout.activity_settings)

        val root = findViewById<View>(R.id.settingsRoot)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        ViewCompat.requestApplyInsets(root)

        // Launch ProfileActivity when clicking profile management
        findViewById<View>(R.id.settingProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<View>(R.id.settingBiometrics).setOnClickListener {
            Toast.makeText(
                this,
                "Biometrics will be implemented in Part 3",
                Toast.LENGTH_SHORT
            ).show()
        }

        findViewById<View>(R.id.settingLanguage).setOnClickListener {
            showComingLater("Language switching")
        }

        findViewById<View>(R.id.settingNotifications).setOnClickListener {
            showComingLater("Notification delivery")
        }

        findViewById<View>(R.id.settingPreferredTransports).setOnClickListener {
            showTransportOptions()
        }

        findViewById<View>(R.id.settingWeatherAlerts).setOnClickListener {
            showWeatherOptions()
        }

        //Handle logout sign out flow
        findViewById<View>(R.id.settingLogout).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out of NaviGo?")
                .setPositiveButton("Sign Out") { _, _ ->
                    val authRepository = AuthRepository()

                    //Call logout with context and the web client ID string
                    authRepository.logoutGoogle(this, "YOUR_WEB_CLIENT_ID_HERE.apps.googleusercontent.com")

                    val intent = Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        findViewById<View>(R.id.settingPrivacy).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Privacy Policy")
                .setMessage(
                    "Your privacy matters to us. NaviGo is designed to help you " +
                            "plan journeys and manage your travel preferences. " +
                            "We aim to handle your information responsibly and give you " +
                            "control over your personal settings."
                )
                .setPositiveButton("OK", null)
                .show()
        }

        findViewById<View>(R.id.navSettingsHome).setOnClickListener {
            openScreen(HomeActivity::class.java)
        }

        findViewById<View>(R.id.navSettingsAlerts).setOnClickListener {
            openScreen(AlertsHostActivity::class.java)
        }

        findViewById<View>(R.id.navSettingsTrips).setOnClickListener {
            openScreen(PlannedTripsActivity::class.java)
        }

        updatePreferenceLabels()
    }


    private fun showTransportOptions() {
        val options = arrayOf(
            "Driving",
            "Walking",
            "Cycling",
            "Bus",
            "Train"
        )

        val savedTransport = preferences.getString(
            "preferred_transport",
            "Bus"
        ) ?: "Bus"

        val selectedIndex = options.indexOf(savedTransport)
            .coerceAtLeast(0)

        AlertDialog.Builder(this)
            .setTitle("Choose preferred transport")
            .setSingleChoiceItems(
                options,
                selectedIndex
            ) { dialog, which ->
                val selectedTransport = options[which]

                preferences.edit()
                    .putString(
                        "preferred_transport",
                        selectedTransport
                    )
                    .apply()

                updatePreferenceLabels()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showWeatherOptions() {
        val weatherEnabled = preferences.getBoolean(
            "weather_alerts",
            false
        )

        AlertDialog.Builder(this)
            .setTitle("Weather alerts")
            .setSingleChoiceItems(
                arrayOf("On", "Off"),
                if (weatherEnabled) 0 else 1
            ) { dialog, which ->
                preferences.edit()
                    .putBoolean(
                        "weather_alerts",
                        which == 0
                    )
                    .apply()

                updatePreferenceLabels()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updatePreferenceLabels() {
        val mode = preferences.getString(
            "preferred_transport",
            "Bus"
        ) ?: "Bus"

        val weatherEnabled = preferences.getBoolean(
            "weather_alerts",
            false
        )

        findViewById<TextView>(R.id.tvPreferredTransport).text =
            "Preferred Transport: $mode"

        findViewById<TextView>(R.id.tvWeatherPreference).text =
            "Weather Alerts: ${if (weatherEnabled) "On" else "Off"}"
    }

    private fun showComingLater(feature: String) {
        Toast.makeText(
            this,
            "$feature will be available in a later part.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun openScreen(target: Class<*>) {
        startActivity(
            Intent(this, target).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }
        )
    }
}