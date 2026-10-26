
package com.example.navigo

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class RouteDetailsActivity : AppCompatActivity() {

    private var origin: LatLng? = null
    private var destination: LatLng? = null

    private var originName = "Starting point"
    private var destinationName = "Destination"
    private var travelMode = "driving"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        window.statusBarColor = getColor(R.color.navigo_teal)
        window.navigationBarColor = getColor(R.color.navigo_background)

        setContentView(R.layout.activity_route_details)


        readRouteInformation()
        displayRouteDetails()

        findViewById<ImageButton>(R.id.btnDetailsBack).setOnClickListener {
            finish()
        }


        findViewById<Button>(R.id.btnStartRoute).setOnClickListener {

            val nextScreen = when (travelMode) {
                "bus", "train" -> PublicTransportDetailsActivity::class.java
                else -> RouteConditionsActivity::class.java
            }

            startActivity(
                Intent(this, nextScreen).apply {
                    putExtras(this@RouteDetailsActivity.intent)
                }
            )
        }
    }

    private fun readRouteInformation() {
        if (
            intent.hasExtra("origin_latitude") &&
            intent.hasExtra("origin_longitude")
        ) {
            origin = LatLng(
                intent.getDoubleExtra("origin_latitude", 0.0),
                intent.getDoubleExtra("origin_longitude", 0.0)
            )
        }

        if (
            intent.hasExtra("destination_latitude") &&
            intent.hasExtra("destination_longitude")
        ) {
            destination = LatLng(
                intent.getDoubleExtra("destination_latitude", 0.0),
                intent.getDoubleExtra("destination_longitude", 0.0)
            )
        }

        originName = intent.getStringExtra("origin_name")
            ?.takeIf { it.isNotBlank() }
            ?: "Starting point"

        destinationName = intent.getStringExtra("destination_name")
            ?.takeIf { it.isNotBlank() }
            ?: "Destination"

        travelMode = intent.getStringExtra("travel_mode")
            ?.lowercase(Locale.ROOT)
            ?: "driving"

        if (travelMode !in listOf(
                "driving", "walking", "bicycling", "bus", "train"
            )
        ) {
            travelMode = "driving"
        }
    }

    private fun displayRouteDetails() {
        val modeName: String
        val modeIcon: String
        val previewTime: String
        val firstStep: String
        val firstStepTime: String
        val secondStep: String
        val secondStepTime: String
        val note: String

        when (travelMode) {
            "walking" -> {
                modeName = "Walking"
                modeIcon = "🚶"
                previewTime = "48 min"
                firstStep = "Walk towards your destination"
                firstStepTime = "Follow pedestrian paths"
                secondStep = "Arrive at your destination"
                secondStepTime = "Walking route"
                note = "Use pedestrian crossings and follow local signs."
            }

            "bicycling" -> {
                modeName = "Cycling"
                modeIcon = "🚲"
                previewTime = "20 min"
                firstStep = "Cycle towards your destination"
                firstStepTime = "Use cycle-friendly roads where available"
                secondStep = "Arrive at your destination"
                secondStepTime = "Cycling route"
                note = "Wear a helmet and follow road rules."
            }

            "bus" -> {
                modeName = "Bus"
                modeIcon = "🚌"
                previewTime = "30 min"
                firstStep = "Walk to a bus stop"
                firstStepTime = "Stop and walking time not yet available"
                secondStep = "Take a bus towards your destination"
                secondStepTime = "Service and departure time not yet available"
                note = "Bus stops, fares and timetables require live transit data."
            }

            "train" -> {
                modeName = "Train"
                modeIcon = "🚆"
                previewTime = "35 min"
                firstStep = "Travel to a train station"
                firstStepTime = "Station and walking time not yet available"
                secondStep = "Take a train towards your destination"
                secondStepTime = "Service and departure time not yet available"
                note = "Train stations, fares and timetables require live transit data."
            }

            else -> {
                modeName = "Driving"
                modeIcon = "🚗"
                previewTime = "12 min"
                firstStep = "Drive towards your destination"
                firstStepTime = "Follow the suggested roads"
                secondStep = "Arrive at your destination"
                secondStepTime = "Driving route"
                note = "Follow road signs and do not use your phone while driving."
            }
        }

        findViewById<TextView>(R.id.tvDetailsJourney).text =
            "$originName  →  $destinationName"

        findViewById<TextView>(R.id.tvDetailsMode).text =
            "$modeIcon  $modeName"

        findViewById<TextView>(R.id.tvDetailsDuration).text =
            "$previewTime • demo estimate"

        findViewById<TextView>(R.id.tvDetailsStepOne).text =
            firstStep

        findViewById<TextView>(R.id.tvDetailsStepOneTime).text =
            firstStepTime

        findViewById<TextView>(R.id.tvDetailsStepTwo).text =
            secondStep

        findViewById<TextView>(R.id.tvDetailsStepTwoTime).text =
            secondStepTime

        findViewById<TextView>(R.id.tvDetailsConditions).text =
            "☁ Weather: not connected\n" +
                    "⚠ Incident reports: not connected"

        findViewById<TextView>(R.id.tvDetailsNote).text =
            "$note\n\nJourney steps and travel time are a demo preview."

        val startButton = findViewById<Button>(R.id.btnStartRoute)


        startButton.text = when (travelMode) {
            "bus", "train" -> "View Transport Details  →"
            else -> "View Route Conditions  →"
        }

        startButton.isEnabled = origin != null && destination != null

        if (!startButton.isEnabled) {
            findViewById<TextView>(R.id.tvDetailsNote).text =
                "Starting point or destination is missing. " +
                        "Go back to Route Comparison and set both locations."
        }
    }

    private fun startDirections() {
        val start = origin
        val end = destination

        if (start == null || end == null) {
            Toast.makeText(
                this,
                "Please set a starting point and destination first.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val originCoordinates = String.format(
            Locale.US,
            "%f,%f",
            start.latitude,
            start.longitude
        )

        val destinationCoordinates = String.format(
            Locale.US,
            "%f,%f",
            end.latitude,
            end.longitude
        )

        val uriBuilder = Uri.parse(
            "https://www.google.com/maps/dir/"
        ).buildUpon()
            .appendQueryParameter("api", "1")
            .appendQueryParameter("origin", originCoordinates)
            .appendQueryParameter("destination", destinationCoordinates)

        when (travelMode) {
            "bus", "train" -> {
                uriBuilder.appendQueryParameter("travelmode", "transit")
                uriBuilder.appendQueryParameter("transit_mode", travelMode)
            }

            else -> {
                uriBuilder.appendQueryParameter("travelmode", travelMode)
            }
        }

        val directionsIntent = Intent(
            Intent.ACTION_VIEW,
            uriBuilder.build()
        )

        try {
            startActivity(directionsIntent)
        } catch (exception: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "No app is available to open directions.",
                Toast.LENGTH_LONG
            ).show()
        } catch (exception: SecurityException) {
            Toast.makeText(
                this,
                "Unable to open directions on this device.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}