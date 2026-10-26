
package com.example.navigo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AlertsHostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = getColor(R.color.navigo_teal)
        window.navigationBarColor = Color.WHITE

        val container = FrameLayout(this).apply {
            id = R.id.alertsHostContainer
            setBackgroundColor(getColor(R.color.navigo_background))
        }

        setContentView(container)

        ViewCompat.setOnApplyWindowInsetsListener(container) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        ViewCompat.requestApplyInsets(container)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(container.id, AlertsNotificationsFragment())
                .commitNow()
        }

        // These IDs come from the Alerts layout shared earlier.
        findViewById<View?>(R.id.navAlertsHome)?.setOnClickListener {
            openScreen(HomeActivity::class.java)
        }

        findViewById<View?>(R.id.navAlertsTrips)?.setOnClickListener {
            openScreen(PlannedTripsActivity::class.java)
        }

        findViewById<View?>(R.id.navAlertsSettings)?.setOnClickListener {
            openScreen(SettingsActivity::class.java)
        }
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