
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

class PublicTransportDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = getColor(R.color.navigo_teal)
        window.navigationBarColor = getColor(R.color.navigo_background)

        // This screen is only for bus and train.
        val mode = intent.getStringExtra("travel_mode")
            ?.lowercase(Locale.ROOT)
            ?: "bus"

        if (mode != "bus" && mode != "train") {
            openConditions()
            finish()
            return
        }

        setContentView(R.layout.activity_public_transport_details)

        // Move the content below the notification bar and
        // keep the bottom button above the navigation bar.
        val root = findViewById<android.view.View>(R.id.publicTransportRoot)

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

        val isTrain = mode == "train"

        val originName = intent.getStringExtra("origin_name")
            ?.takeIf { it.isNotBlank() }
            ?: "Starting point"

        val destinationName = intent.getStringExtra("destination_name")
            ?.takeIf { it.isNotBlank() }
            ?: "Destination"

        // Display the selected transport mode and journey.
        findViewById<TextView>(R.id.tvTransportMode).text =
            if (isTrain) "🚆 Train" else "🚌 Bus"

        findViewById<TextView>(R.id.tvTransportJourney).text =
            "$originName  →  $destinationName"

        findViewById<TextView>(R.id.tvTransportService).text =
            if (isTrain) "🚆 Train service" else "🚌 Bus service"

        findViewById<TextView>(R.id.tvTransportArrival).text =
            "Arrival time: Not available yet"

        findViewById<TextView>(R.id.tvTransportBoarding).text =
            if (isTrain) {
                "Boarding station: Not available yet"
            } else {
                "Boarding stop: Not available yet"
            }

        // Show the correct demo journey for bus or train.
        findViewById<TextView>(R.id.tvTransportStepOne).text =
            if (isTrain) {
                "● Travel to your boarding station"
            } else {
                "● Walk to your boarding stop"
            }

        findViewById<TextView>(R.id.tvTransportStepTwo).text =
            if (isTrain) {
                "● Board the train"
            } else {
                "● Board the bus"
            }

        findViewById<TextView>(R.id.tvTransportStepThree).text =
            "● Arrive near $destinationName"

        // Back: return to Route Details.
        findViewById<ImageButton>(R.id.btnTransportBack)
            .setOnClickListener {
                finish()
            }

        // Select route: continue to Route Conditions.
        findViewById<Button>(R.id.btnSelectTransportRoute)
            .setOnClickListener {
                openConditions()
            }
    }

    private fun openConditions() {

        val conditionsIntent = Intent(
            this,
            RouteConditionsActivity::class.java
        ).apply {
            // Preserve origin, destination, coordinates and travel mode.
            putExtras(this@PublicTransportDetailsActivity.intent)
        }

        startActivity(conditionsIntent)
    }
}