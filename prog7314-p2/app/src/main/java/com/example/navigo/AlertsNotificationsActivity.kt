
package com.example.navigo

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AlertsNotificationsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = getColor(R.color.navigo_teal)
        window.navigationBarColor = getColor(R.color.navigo_background)

        setContentView(R.layout.activity_alerts_notifications)

        // Keep the header below the status bar and the
        // bottom navigation above the phone navigation bar.
        val root = findViewById<View>(R.id.alertsRoot)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                bars.left,
                bars.top,
                bars.right,
                bars.bottom
            )

            insets
        }

        ViewCompat.requestApplyInsets(root)

        // Add the fragment only when this activity is first created.
        // Android restores the existing fragment after rotation.
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.alertsFragmentContainer,
                    AlertsNotificationsFragment()
                )
                .commit()
        }

        findViewById<LinearLayout>(R.id.navAlertsHome)
            .setOnClickListener {
                openScreen("HomeActivity")
            }

        findViewById<LinearLayout>(R.id.navAlertsTrips)
            .setOnClickListener {
                openScreen("PlannedTripsActivity")
            }

        findViewById<LinearLayout>(R.id.navAlertsSettings)
            .setOnClickListener {
                openScreen("SettingsActivity")
            }

        // The Alerts icon is already on its own screen.
        findViewById<LinearLayout>(R.id.navAlertsCurrent)
            .setOnClickListener {
                // No action needed.
            }
    }

    private fun openScreen(activityName: String) {
        // Replace these names with your actual activity class names
        // if they are different.
        val target = Intent().setClassName(
            this,
            "$packageName.$activityName"
        ).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
        }

        try {
            startActivity(target)
        } catch (exception: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "This screen is not available yet.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}