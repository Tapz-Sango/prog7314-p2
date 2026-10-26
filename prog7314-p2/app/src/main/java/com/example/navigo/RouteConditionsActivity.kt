
package com.example.navigo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class RouteConditionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = getColor(R.color.navigo_teal)
        window.navigationBarColor = getColor(R.color.navigo_background)

        setContentView(R.layout.activity_route_conditions)

        // Keep the screen below the status bar and above the navigation bar.
        val root = findViewById<android.view.View>(R.id.routeConditionsRoot)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        ViewCompat.requestApplyInsets(root)

        // Read the route information passed from the previous screen.
        val originName = intent.getStringExtra("origin_name")
            ?.takeIf { it.isNotBlank() }
            ?: "Starting point"

        val destinationName = intent.getStringExtra("destination_name")
            ?.takeIf { it.isNotBlank() }
            ?: "Destination"

        val travelMode = intent.getStringExtra("travel_mode")
            ?.lowercase(Locale.ROOT)
            ?: "driving"

        val modeName = when (travelMode) {
            "walking" -> "Walking"
            "bicycling" -> "Cycling"
            "bus" -> "Bus"
            "train" -> "Train"
            else -> "Driving"
        }

        // Display the selected journey and transport mode.
        findViewById<TextView>(R.id.tvConditionsJourney).text =
            "$originName  →  $destinationName"

        findViewById<TextView>(R.id.tvConditionsMode).text =
            "Selected transport: $modeName"

        // Back: return to the previous screen.
        findViewById<ImageButton>(R.id.btnConditionsBack)
            .setOnClickListener {
                finish()
            }

        // Continue: open Live Navigation and preserve route information.
        findViewById<Button>(R.id.btnContinueNavigation)
            .setOnClickListener {

                val navigationIntent = Intent(
                    this,
                    LiveNavigationActivity::class.java
                ).apply {
                    putExtras(this@RouteConditionsActivity.intent)
                }

                startActivity(navigationIntent)
            }

        // Route options: return to Route Comparison.
        findViewById<Button>(R.id.btnViewRouteOptions)
            .setOnClickListener {

                val comparisonIntent = Intent(
                    this,
                    RouteComparisonActivity::class.java
                ).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
                }

                startActivity(comparisonIntent)
                finish()
            }
    }
}