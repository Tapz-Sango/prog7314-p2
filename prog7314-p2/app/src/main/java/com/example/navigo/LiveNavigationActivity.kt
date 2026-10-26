
package com.example.navigo

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

class LiveNavigationActivity : AppCompatActivity() {

    private lateinit var mapView: MapView

    private var origin: LatLng? = null
    private var destination: LatLng? = null

    private var originName = "Starting point"
    private var destinationName = "Destination"
    private var travelMode = "driving"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = getColor(R.color.navigo_teal)
        window.navigationBarColor = getColor(R.color.navigo_background)

        setContentView(R.layout.activity_live_navigation)

        // Keep the header below the status bar and the buttons
        // above the phone's navigation bar.
        val root = findViewById<View>(R.id.liveNavigationRoot)

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

        readRouteInformation()

        mapView = findViewById(R.id.liveNavigationMap)

        findViewById<TextView>(R.id.tvNavigationJourney).text =
            "$originName  →  $destinationName\n${getModeName()}"

        findViewById<ImageButton>(R.id.btnNavigationBack)
            .setOnClickListener {
                finish()
            }

        val googleMapsButton =
            findViewById<Button>(R.id.btnOpenGoogleMaps)

        googleMapsButton.isEnabled =
            origin != null && destination != null

        if (!googleMapsButton.isEnabled) {
            findViewById<TextView>(R.id.tvNavigationStatus).text =
                "Starting point or destination is missing. " +
                        "Go back and select both locations."
        }

        googleMapsButton.setOnClickListener {
            openGoogleMapsDirections()
        }

        findViewById<Button>(R.id.btnEndJourney)
            .setOnClickListener {
                endJourney()
            }

        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync { googleMap ->
            displayMapMarkers(googleMap)
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
    }

    private fun getModeName(): String {
        return when (travelMode) {
            "walking" -> "🚶 Walking"
            "bicycling" -> "🚲 Cycling"
            "bus" -> "🚌 Bus"
            "train" -> "🚆 Train"
            else -> "🚗 Driving"
        }
    }

    private fun displayMapMarkers(googleMap: GoogleMap) {

        val start = origin
        val end = destination

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = false

        if (start != null) {
            googleMap.addMarker(
                MarkerOptions()
                    .position(start)
                    .title(originName)
            )
        }

        if (end != null) {
            googleMap.addMarker(
                MarkerOptions()
                    .position(end)
                    .title(destinationName)
            )
        }

        when {

            start != null && end != null -> {

                // Avoid fitting a zero-sized area when both points match.
                if (start == end) {
                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(start, 15f)
                    )
                    return
                }

                val bounds = LatLngBounds.Builder()
                    .include(start)
                    .include(end)
                    .build()

                mapView.post {
                    if (!isFinishing && !isDestroyed) {
                        googleMap.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(
                                bounds,
                                dp(60)
                            )
                        )
                    }
                }
            }

            end != null -> {
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(end, 13f)
                )
            }

            start != null -> {
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(start, 13f)
                )
            }

            else -> {
                // Default preview location when no route was supplied.
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(-26.2041, 28.0473),
                        11f
                    )
                )
            }
        }
    }

    private fun openGoogleMapsDirections() {

        val start = origin
        val end = destination

        if (start == null || end == null) {
            Toast.makeText(
                this,
                "Please select a starting point and destination.",
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
                uriBuilder.appendQueryParameter(
                    "travelmode",
                    "transit"
                )

                uriBuilder.appendQueryParameter(
                    "transit_mode",
                    travelMode
                )
            }

            "walking", "bicycling", "driving" -> {
                uriBuilder.appendQueryParameter(
                    "travelmode",
                    travelMode
                )
            }

            else -> {
                uriBuilder.appendQueryParameter(
                    "travelmode",
                    "driving"
                )
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
                "No app is available to open Google Maps directions.",
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

    private fun endJourney() {

        val homeIntent = Intent(
            this,
            HomeActivity::class.java
        ).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
        }

        startActivity(homeIntent)

        Toast.makeText(
            applicationContext,
            "Thanks for using NaviGo",
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mapView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }
}