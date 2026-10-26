package com.example.navigo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PlannedTripsActivity : AppCompatActivity() {

    private val trips = mutableListOf<PlannedTrip>()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private lateinit var tripsContainer: LinearLayout
    private lateinit var tripForm: LinearLayout
    private lateinit var nameInput: EditText
    private lateinit var destinationInput: EditText
    private lateinit var modeSpinner: Spinner
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var emptyMessage: TextView

    private var selectedDate = ""
    private var selectedTime = ""

    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    //'id' was added field with a default value so Firestore and local storage match
    data class PlannedTrip(
        val id: String = System.currentTimeMillis().toString(),
        val name: String = "",
        val destination: String = "",
        val date: String = "",
        val time: String = "",
        val mode: String = ""
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = getColor(R.color.navigo_teal)
        window.navigationBarColor = Color.WHITE

        setContentView(R.layout.activity_planned_trips)

        val root = findViewById<View>(R.id.plannedTripsRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(root)

        tripsContainer = findViewById(R.id.tripsContainer)
        tripForm = findViewById(R.id.tripForm)
        nameInput = findViewById(R.id.etTripName)
        destinationInput = findViewById(R.id.etTripDestination)
        modeSpinner = findViewById(R.id.spinnerTripMode)
        dateButton = findViewById(R.id.btnTripDate)
        timeButton = findViewById(R.id.btnTripTime)
        emptyMessage = findViewById(R.id.tvTripsEmpty)

        val modes = listOf("Driving", "Walking", "Cycling", "Bus", "Train")
        modeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            modes
        )

        loadTrips()
        renderTrips()
        fetchTripsFromCloud() //Fetch remote updates on startup

        findViewById<Button>(R.id.btnPlanNewTrip).setOnClickListener {
            tripForm.visibility =
                if (tripForm.visibility == View.VISIBLE) View.GONE
                else View.VISIBLE
        }

        findViewById<Button>(R.id.btnCancelTrip).setOnClickListener {
            tripForm.visibility = View.GONE
        }

        dateButton.setOnClickListener {
            val now = Calendar.getInstance()

            val picker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    val selected = Calendar.getInstance().apply {
                        set(year, month, day)
                    }
                    selectedDate = dateFormat.format(selected.time)
                    dateButton.text = selectedDate
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
            )

            picker.datePicker.minDate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            picker.show()
        }

        timeButton.setOnClickListener {
            val now = Calendar.getInstance()

            TimePickerDialog(
                this,
                { _, hour, minute ->
                    selectedTime = String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        hour,
                        minute
                    )
                    timeButton.text = selectedTime
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
            ).show()
        }

        findViewById<Button>(R.id.btnAddTrip).setOnClickListener {
            addTrip()
        }

        findViewById<View>(R.id.navTripsHome).setOnClickListener {
            openScreen(HomeActivity::class.java)
        }

        findViewById<View>(R.id.navTripsAlerts).setOnClickListener {
            openAlerts()
        }

        findViewById<View>(R.id.navTripsSettings).setOnClickListener {
            openScreen(SettingsActivity::class.java)
        }
    }

    private fun addTrip() {
        val name = nameInput.text.toString().trim()
        val destination = destinationInput.text.toString().trim()

        if (name.isEmpty()) {
            nameInput.error = "Enter a trip name"
            nameInput.requestFocus()
            return
        }

        if (destination.isEmpty()) {
            destinationInput.error = "Enter a destination"
            destinationInput.requestFocus()
            return
        }

        if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(
                this,
                "Please choose a date and time.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val chosenDateTime = Calendar.getInstance()
        try {
            val date = dateFormat.parse(selectedDate) ?: return
            chosenDateTime.time = date
            val parts = selectedTime.split(":")
            chosenDateTime.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
            chosenDateTime.set(Calendar.MINUTE, parts[1].toInt())
            chosenDateTime.set(Calendar.SECOND, 0)

            if (chosenDateTime.before(Calendar.getInstance())) {
                Toast.makeText(
                    this,
                    "Choose a future date and time.",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        } catch (exception: Exception) {
            Toast.makeText(
                this,
                "Please select a valid date and time.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val newTrip = PlannedTrip(
            id = System.currentTimeMillis().toString(),
            name = name,
            destination = destination,
            date = selectedDate,
            time = selectedTime,
            mode = modeSpinner.selectedItem.toString()
        )

        trips.add(newTrip)

        saveTrips()
        syncTripToCloud(newTrip)
        renderTrips()

        nameInput.text.clear()
        destinationInput.text.clear()
        selectedDate = ""
        selectedTime = ""
        dateButton.text = "Choose date"
        timeButton.text = "Choose time"
        modeSpinner.setSelection(0)
        tripForm.visibility = View.GONE

        Toast.makeText(this, "Trip added!", Toast.LENGTH_SHORT).show()
    }

    private fun renderTrips() {
        tripsContainer.removeAllViews()
        emptyMessage.visibility =
            if (trips.isEmpty()) View.VISIBLE else View.GONE

        trips.forEachIndexed { index, trip ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(14), dp(16), dp(14))
                setBackgroundResource(R.drawable.bg_home_card)
                elevation = dp(2).toFloat()
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }

            val heading = TextView(this).apply {
                text = trip.name
                textSize = 16f
                setTextColor(getColor(R.color.navigo_navy))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val details = TextView(this).apply {
                text = "Destination: ${trip.destination}\n" +
                        "${trip.date}  •  ${trip.time}\n" +
                        "Transport: ${trip.mode}"
                textSize = 13f
                setTextColor(getColor(R.color.navigo_text))
                setPadding(0, dp(8), 0, dp(8))
            }

            val removeButton = Button(this).apply {
                text = "Remove"
                isAllCaps = false
                setTextColor(getColor(R.color.navigo_teal))
                setOnClickListener {
                    androidx.appcompat.app.AlertDialog.Builder(
                        this@PlannedTripsActivity
                    )
                        .setMessage("Remove this planned trip?")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Remove") { _, _ ->
                            val tripToRemove = trips[index]
                            trips.removeAt(index)
                            saveTrips()
                            deleteTripFromCloud(tripToRemove.id)
                            renderTrips()
                        }
                        .show()
                }
            }

            card.addView(heading)
            card.addView(details)
            card.addView(removeButton)
            tripsContainer.addView(card, params)
        }
    }

    private fun saveTrips() {
        val array = JSONArray()
        trips.forEach { trip ->
            array.put(
                JSONObject().apply {
                    put("id", trip.id)
                    put("name", trip.name)
                    put("destination", trip.destination)
                    put("date", trip.date)
                    put("time", trip.time)
                    put("mode", trip.mode)
                }
            )
        }

        getSharedPreferences("navigo_trips", MODE_PRIVATE)
            .edit()
            .putString("saved_trips", array.toString())
            .apply()
    }

    private fun loadTrips() {
        val stored = getSharedPreferences("navigo_trips", MODE_PRIVATE)
            .getString("saved_trips", "[]") ?: "[]"

        try {
            val array = JSONArray(stored)
            trips.clear()
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)

                trips.add(
                    PlannedTrip(
                        id = item.optString("id", System.currentTimeMillis().toString()),
                        name = item.optString("name"),
                        destination = item.optString("destination"),
                        date = item.optString("date"),
                        time = item.optString("time"),
                        mode = item.optString("mode")
                    )
                )
            }
        } catch (exception: Exception) {
            Toast.makeText(
                this,
                "Saved trips could not be loaded.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun syncTripToCloud(trip: PlannedTrip) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .collection("planned_trips")
            .document(trip.id)
            .set(trip)
    }

    private fun deleteTripFromCloud(tripId: String) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .collection("planned_trips")
            .document(tripId)
            .delete()
    }

    private fun fetchTripsFromCloud() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .collection("planned_trips")
            .get()
            .addOnSuccessListener { result ->
                val cloudTrips = mutableListOf<PlannedTrip>()
                for (document in result) {
                    val trip = document.toObject(PlannedTrip::class.java)
                    cloudTrips.add(trip)
                }
                if (cloudTrips.isNotEmpty()) {
                    trips.clear()
                    trips.addAll(cloudTrips)
                    saveTrips()
                    renderTrips()
                }
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

    private fun openAlerts() {
        openScreen(AlertsHostActivity::class.java)
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}