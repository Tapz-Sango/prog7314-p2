
package com.example.navigo

import android.content.ActivityNotFoundException
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale
import java.util.concurrent.Executors

class RouteComparisonActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var originInput: EditText
    private lateinit var originStatus: TextView
    private lateinit var originButton: Button
    private lateinit var selectedRouteText: TextView
    private lateinit var directionsButton: Button

    private val geocodingExecutor = Executors.newSingleThreadExecutor()

    private var googleMap: GoogleMap? = null
    private var origin: LatLng? = null
    private var destination: LatLng? = null

    private var originName = ""
    private var destinationName = "Selected destination"

    private var travelMode = "driving"
    private var selectedModeName = "Driving"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.statusBarColor = getColor(R.color.navigo_teal)
        window.navigationBarColor = getColor(R.color.navigo_background)

        setContentView(R.layout.activity_route_comparison)

        val routeRoot = findViewById<View>(R.id.main)

        ViewCompat.setOnApplyWindowInsetsListener(routeRoot) { view, insets ->
            val statusBars = insets.getInsets(
                WindowInsetsCompat.Type.statusBars()
            )
            val navigationBars = insets.getInsets(
                WindowInsetsCompat.Type.navigationBars()
            )
            val cutout = insets.getInsets(
                WindowInsetsCompat.Type.displayCutout()
            )

            view.setPadding(
                0,
                maxOf(statusBars.top, cutout.top),
                0,
                navigationBars.bottom
            )

            insets
        }

        ViewCompat.requestApplyInsets(routeRoot)

        mapView = findViewById(R.id.routeMap)
        originInput = findViewById(R.id.etRouteOrigin)
        originStatus = findViewById(R.id.tvOriginStatus)
        originButton = findViewById(R.id.btnSetOrigin)
        selectedRouteText = findViewById(R.id.tvSelectedRoute)
        directionsButton = findViewById(R.id.btnRouteDirections)

        val hasDestination =
            intent.hasExtra("destination_latitude") &&
                    intent.hasExtra("destination_longitude")

        if (hasDestination) {
            destination = LatLng(
                intent.getDoubleExtra("destination_latitude", 0.0),
                intent.getDoubleExtra("destination_longitude", 0.0)
            )

            destinationName = intent.getStringExtra("destination_name")
                ?.takeIf { it.isNotBlank() }
                ?: "Selected destination"
        }

        findViewById<TextView>(R.id.tvRouteDestination).text =
            if (hasDestination) {
                "Destination: $destinationName"
            } else {
                "No destination selected. Return to Home and search for a place."
            }

        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync { map ->
            googleMap = map
            map.uiSettings.isZoomControlsEnabled = true
            map.uiSettings.isMapToolbarEnabled = false
            updateMapMarkers()
        }

        findViewById<ImageButton>(R.id.btnRouteBack)
            .setOnClickListener { finish() }

        originButton.setOnClickListener {
            searchForOrigin()
        }

        findViewById<LinearLayout>(R.id.cardDriving)
            .setOnClickListener {
                selectMode("Driving", "driving")
            }

        findViewById<LinearLayout>(R.id.cardWalking)
            .setOnClickListener {
                selectMode("Walking", "walking")
            }

        findViewById<LinearLayout>(R.id.cardCycling)
            .setOnClickListener {
                selectMode("Cycling", "bicycling")
            }

        findViewById<LinearLayout>(R.id.cardBus)
            .setOnClickListener {
                selectMode("Bus", "bus")
            }

        findViewById<LinearLayout>(R.id.cardTrain)
            .setOnClickListener {
                selectMode("Train", "train")
            }

        updateDirectionsButton()

        directionsButton.setOnClickListener {
            openExternalDirections()
        }
    }

    private fun searchForOrigin() {
        val query = originInput.text.toString().trim()

        if (query.isEmpty()) {
            originInput.error = "Enter a starting point"
            return
        }

        originButton.isEnabled = false
        originStatus.text = "Finding starting point..."

        geocodingExecutor.execute {
            val address = try {
                @Suppress("DEPRECATION")
                Geocoder(this, Locale.getDefault())
                    .getFromLocationName(query, 1)
                    ?.firstOrNull()
            } catch (exception: Exception) {
                null
            }

            runOnUiThread {
                if (isFinishing || isDestroyed) {
                    return@runOnUiThread
                }

                originButton.isEnabled = true

                if (address == null) {
                    originStatus.text =
                        "Starting point not found. Try a more specific address."
                    return@runOnUiThread
                }

                origin = LatLng(
                    address.latitude,
                    address.longitude
                )

                originName = address.getAddressLine(0)
                    ?.takeIf { it.isNotBlank() }
                    ?: query

                originStatus.text = "Starting point: $originName"

                val keyboard = getSystemService(
                    INPUT_METHOD_SERVICE
                ) as InputMethodManager

                keyboard.hideSoftInputFromWindow(
                    originInput.windowToken,
                    0
                )

                originInput.clearFocus()

                updateMapMarkers()
                updateDirectionsButton()
            }
        }
    }

    private fun updateMapMarkers() {
        val map = googleMap ?: return

        map.clear()

        val start = origin
        val end = destination

        if (start != null) {
            map.addMarker(
                MarkerOptions()
                    .position(start)
                    .title("Starting point: $originName")
                    .icon(
                        BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_AZURE
                        )
                    )
            )
        }

        if (end != null) {
            map.addMarker(
                MarkerOptions()
                    .position(end)
                    .title("Destination: $destinationName")
            )
        }

        when {
            start != null && end != null -> {
                val bounds = LatLngBounds.Builder()
                    .include(start)
                    .include(end)
                    .build()

                // Wait until the MapView has a measured size before
                // fitting both markers into the visible map.
                mapView.post {
                    if (!isFinishing && !isDestroyed) {
                        map.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(
                                bounds,
                                70
                            )
                        )
                    }
                }
            }

            end != null -> {
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(end, 13f)
                )
            }

            start != null -> {
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(start, 13f)
                )
            }

            else -> {
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(-26.2041, 28.0473),
                        11f
                    )
                )
            }
        }
    }


    private fun selectMode(name: String, mode: String) {
        selectedModeName = name
        travelMode = mode

        selectedRouteText.text =
            "Selected: $selectedModeName (preview — route not calculated)"

        val start = origin
        val end = destination

        if (start == null) {
            Toast.makeText(
                this,
                "Please enter and set your starting point first.",
                Toast.LENGTH_LONG
            ).show()

            originInput.requestFocus()
            return
        }

        if (end == null) {
            Toast.makeText(
                this,
                "Please select a destination first.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val detailsIntent = Intent(
            this,
            RouteDetailsActivity::class.java
        ).apply {
            putExtra("origin_latitude", start.latitude)
            putExtra("origin_longitude", start.longitude)
            putExtra("origin_name", originName)

            putExtra("destination_latitude", end.latitude)
            putExtra("destination_longitude", end.longitude)
            putExtra("destination_name", destinationName)

            putExtra("travel_mode", travelMode)
        }

        startActivity(detailsIntent)
    }

    private fun updateDirectionsButton() {
        directionsButton.isEnabled =
            origin != null && destination != null
    }

    private fun openExternalDirections() {
        val start = origin ?: return
        val end = destination ?: return

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

            else -> {
                uriBuilder.appendQueryParameter(
                    "travelmode",
                    travelMode
                )
            }
        }

        val uri = uriBuilder.build()

        try {
            startActivity(
                Intent(Intent.ACTION_VIEW, uri)
            )
        } catch (exception: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "No app is available to open directions.",
                Toast.LENGTH_LONG
            ).show()
        }
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
        geocodingExecutor.shutdown()
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