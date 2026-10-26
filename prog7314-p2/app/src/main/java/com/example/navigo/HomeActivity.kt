package com.example.navigo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale

class HomeActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private var destinationMarker: Marker? = null
    private var selectedDestination: LatLng? = null
    private var selectedDestinationName = "Selected destination"

    private lateinit var selectedPlaceText: TextView
    private lateinit var coordinatesText: TextView
    private lateinit var planRouteButton: Button

    private val locationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            if (permissions.values.any { it }) {
                showMyLocation()
            } else {
                Toast.makeText(
                    this,
                    "Location permission denied. You can still explore the map.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private val searchLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }

            val data = result.data
                ?: return@registerForActivityResult

            if (!data.hasExtra("destination_latitude") ||
                !data.hasExtra("destination_longitude")
            ) {
                Toast.makeText(
                    this,
                    "Search did not return a destination.",
                    Toast.LENGTH_SHORT
                ).show()

                return@registerForActivityResult
            }

            val latitude = data.getDoubleExtra(
                "destination_latitude",
                0.0
            )

            val longitude = data.getDoubleExtra(
                "destination_longitude",
                0.0
            )

            val name = data.getStringExtra("destination_name")
                ?.takeIf { it.isNotBlank() }
                ?: "Selected destination"

            val position = LatLng(latitude, longitude)

            // Update Home so the destination is still selected
            // when the user returns from Route Comparison.
            selectDestination(position, name)

            // Open NAVIGO's own Route Comparison screen.
            openRouteComparison()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lightweight check using Firebase instance directly to prevent main-thread blocking
        if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Keep the status-bar fix that worked on Home.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.statusBarColor = getColor(R.color.navigo_teal)
        window.navigationBarColor = getColor(R.color.navigo_background)

        setContentView(R.layout.activity_home)

        val homeRoot = findViewById<View>(R.id.homeRoot)

        ViewCompat.setOnApplyWindowInsetsListener(homeRoot) { view, insets ->
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

        ViewCompat.requestApplyInsets(homeRoot)

        mapView = findViewById(R.id.homeMap)
        selectedPlaceText = findViewById(R.id.tvSelectedPlace)
        coordinatesText = findViewById(R.id.tvSelectedCoordinates)
        planRouteButton = findViewById(R.id.btnPlanRoute)

        // Defer map creation safely so the main thread unblocks during activity startup
        mapView.post {
            if (!isFinishing && !isDestroyed) {
                mapView.onCreate(savedInstanceState)
                mapView.getMapAsync(this)
            }
        }

        setupButtons()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Starting map position, not the user's detected location.
        val initialPosition = LatLng(-26.2041, 28.0473)

        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                initialPosition,
                11f
            )
        )

        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.isMapToolbarEnabled = false

        map.setOnMapClickListener { position ->
            selectDestination(position)
        }

        map.setOnMapLongClickListener { position ->
            selectDestination(position)
        }

        // Restore the marker if a destination was selected
        // before the map finished loading.
        selectedDestination?.let { position ->
            showDestinationMarker(position)
        }
    }

    private fun setupButtons() {

        findViewById<ImageButton>(R.id.btnMyLocation)
            .setOnClickListener {
                requestMyLocation()
            }

        findViewById<LinearLayout>(R.id.searchBar)
            .setOnClickListener {
                openPlaceSearch()
            }

        findViewById<TextView>(R.id.tvSearchDestination)
            .setOnClickListener {
                openPlaceSearch()
            }

        planRouteButton.setOnClickListener {
            openRouteComparison()
        }

        findViewById<TextView>(R.id.tvWeatherSummary)
            .setOnClickListener {
                showIntegrationMessage(
                    "Live weather will be connected to the weather API."
                )
            }

        findViewById<TextView>(R.id.tvIncidentSummary)
            .setOnClickListener {
                showIntegrationMessage(
                    "Community reports will be connected to NaviGo's REST API."
                )
            }

        findViewById<TextView>(R.id.navHome)
            .setOnClickListener {
                googleMap?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(-26.2041, 28.0473),
                        11f
                    )
                )
            }

        findViewById<TextView>(R.id.navAlerts)
            .setOnClickListener {
                startActivity(
                    Intent(this, AlertsNotificationsActivity::class.java)
                )
            }

        findViewById<TextView>(R.id.navTrips).setOnClickListener {
            startActivity(
                Intent(this, PlannedTripsActivity::class.java)
            )
        }

        findViewById<TextView>(R.id.navSettings).setOnClickListener {
            startActivity(
                Intent(this, SettingsActivity::class.java)
            )
        }
    }

    private fun openPlaceSearch() {
        searchLauncher.launch(
            Intent(this, SearchActivity::class.java)
        )
    }

    private fun selectDestination(
        position: LatLng,
        name: String = "Selected destination"
    ) {
        selectedDestination = position
        selectedDestinationName = name

        selectedPlaceText.text = name

        coordinatesText.text = String.format(
            Locale.US,
            "Latitude: %.5f  •  Longitude: %.5f",
            position.latitude,
            position.longitude
        )

        planRouteButton.isEnabled = true
        planRouteButton.text = "Get directions"

        showDestinationMarker(position)
    }

    private fun showDestinationMarker(position: LatLng) {
        destinationMarker?.remove()

        destinationMarker = googleMap?.addMarker(
            MarkerOptions()
                .position(position)
                .title(selectedDestinationName)
        )

        googleMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                position,
                14f
            )
        )
    }

    private fun openRouteComparison() {
        val position = selectedDestination

        if (position == null) {
            Toast.makeText(
                this,
                "Please select a destination first.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val routeIntent = Intent(
            this,
            RouteComparisonActivity::class.java
        ).apply {
            putExtra("destination_name", selectedDestinationName)
            putExtra("destination_latitude", position.latitude)
            putExtra("destination_longitude", position.longitude)
        }

        startActivity(routeIntent)
    }

    private fun requestMyLocation() {
        val finePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val coarsePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (
            finePermission == PackageManager.PERMISSION_GRANTED ||
            coarsePermission == PackageManager.PERMISSION_GRANTED
        ) {
            showMyLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun showMyLocation() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            return
        }

        try {
            googleMap?.isMyLocationEnabled = true

            val cancellationTokenSource = CancellationTokenSource()

            val priority = if (fineGranted) {
                Priority.PRIORITY_HIGH_ACCURACY
            } else {
                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            }

            locationClient.getCurrentLocation(
                priority,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->

                if (location != null) {
                    val myPosition = LatLng(
                        location.latitude,
                        location.longitude
                    )

                    googleMap?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            myPosition,
                            15f
                        )
                    )
                } else {
                    Toast.makeText(
                        this,
                        "Current location unavailable. Check that device location is on.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }.addOnFailureListener {
                Toast.makeText(
                    this,
                    "Could not retrieve your location.",
                    Toast.LENGTH_LONG
                ).show()
            }

        } catch (exception: SecurityException) {
            Toast.makeText(
                this,
                "Location permission is required.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showIntegrationMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    // MapView lifecycle
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