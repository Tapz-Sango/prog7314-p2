
package com.example.navigo

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog

class IncidentReportingActivity : AppCompatActivity() {

    private lateinit var incidentTypes: RadioGroup
    private lateinit var descriptionInput: EditText
    private lateinit var locationText: TextView
    private lateinit var locationButton: Button
    private lateinit var submitButton: Button

    private var locationLatitude: Double? = null
    private var locationLongitude: Double? = null

    private val locationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            if (permissions.values.any { it }) {
                getCurrentLocation()
            } else {
                locationText.text = "Location permission denied (optional)"
                Toast.makeText(
                    this,
                    "You can still submit without a location.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = getColor(R.color.navigo_teal)
        window.navigationBarColor = getColor(R.color.navigo_background)

        setContentView(R.layout.activity_incident_reporting)

        // Keep the header below the notification bar and
        // the bottom navigation above the phone's navigation bar.
        val root = findViewById<View>(R.id.incidentReportingRoot)

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

        incidentTypes = findViewById(R.id.rgIncidentTypes)
        descriptionInput = findViewById(R.id.etIncidentDescription)
        locationText = findViewById(R.id.tvIncidentLocation)
        locationButton = findViewById(R.id.btnIncidentLocation)
        submitButton = findViewById(R.id.btnSubmitIncident)

        findViewById<View>(R.id.btnIncidentBack).setOnClickListener {
            finish()
        }

        locationButton.setOnClickListener {
            requestCurrentLocation()
        }

        submitButton.setOnClickListener {
            submitIncident()
        }

        findViewById<View>(R.id.btnIncidentBack).setOnClickListener {
            confirmExit {
                finish()
            }
        }

        findViewById<LinearLayout>(R.id.navIncidentHome).setOnClickListener {
            confirmExit {
                openScreen("HomeActivity")
            }
        }

        findViewById<LinearLayout>(R.id.navIncidentAlerts).setOnClickListener {
            confirmExit {
                openAlerts()
            }
        }

        findViewById<LinearLayout>(R.id.navIncidentTrips).setOnClickListener {
            confirmExit {
                openScreen("PlannedTripsActivity")
            }
        }

        findViewById<LinearLayout>(R.id.navIncidentSettings).setOnClickListener {
            confirmExit {
                openScreen("SettingsActivity")
            }

            onBackPressedDispatcher.addCallback(
                this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        confirmExit {
                            finish()
                        }
                    }
                }
            )
        }
    }

    private fun confirmExit(onExit: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Exit incident report?")
            .setMessage("Are you sure you want to exit? Your report will not be submitted.")
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Exit") { _, _ ->
                onExit()
            }
            .show()
    }

    private fun requestCurrentLocation() {

        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            getCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun getCurrentLocation() {

        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            locationText.text = "Location permission required"
            return
        }

        locationButton.isEnabled = false
        locationText.text = "Finding your location..."

        val priority = if (fineGranted) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        try {
            locationClient.getCurrentLocation(
                priority,
                CancellationTokenSource().token
            ).addOnSuccessListener { location ->

                locationButton.isEnabled = true

                if (location != null) {
                    locationLatitude = location.latitude
                    locationLongitude = location.longitude

                    locationText.text = String.format(
                        Locale.US,
                        "Location: %.5f, %.5f",
                        location.latitude,
                        location.longitude
                    )
                } else {
                    locationLatitude = null
                    locationLongitude = null
                    locationText.text =
                        "Location unavailable (optional)"
                }

            }.addOnFailureListener {

                locationButton.isEnabled = true
                locationLatitude = null
                locationLongitude = null
                locationText.text =
                    "Could not get location (optional)"
            }

        } catch (exception: SecurityException) {

            locationButton.isEnabled = true
            locationLatitude = null
            locationLongitude = null
            locationText.text =
                "Location permission required"
        }
    }

    private fun submitIncident() {

        val selectedType = when (incidentTypes.checkedRadioButtonId) {
            R.id.rbIncidentAccident -> "Accident"
            R.id.rbIncidentRoadBlockage -> "Road blockage"
            R.id.rbIncidentTraffic -> "Traffic"
            R.id.rbIncidentRoadHazard -> "Road hazard"
            else -> null
        }

        if (selectedType == null) {
            Toast.makeText(
                this,
                "Please select what happened.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val description = descriptionInput.text.toString().trim()

        if (description.isEmpty()) {
            descriptionInput.error = "Please enter a short description"
            descriptionInput.requestFocus()
            return
        }

        if (description.length < 10) {
            descriptionInput.error =
                "Please enter at least 10 characters"
            descriptionInput.requestFocus()
            return
        }

        // Part 2 demo:
        // The form is validated, but no incident is sent to
        // a server or saved as a real community report yet.
        // selectedType, description, locationLatitude and
        // locationLongitude can be used by the backend later.

        openAlerts()

        Toast.makeText(
            applicationContext,
            "Demo report submitted. Thank you!",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun openAlerts() {
        finish()
    }

    private fun openScreen(activityName: String) {

        val screenIntent = Intent().setClassName(
            this,
            "$packageName.$activityName"
        ).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
        }

        try {
            startActivity(screenIntent)
            finish()
        } catch (exception: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "$activityName is not available yet.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}