
package com.example.navigo

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.Executors
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SearchActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var statusText: TextView
    private lateinit var recentContainer: LinearLayout

    private val searchExecutor = Executors.newSingleThreadExecutor()

    private val preferences by lazy {
        getSharedPreferences("navigo_search", MODE_PRIVATE)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.statusBarColor = getColor(R.color.navigo_teal)
        window.navigationBarColor = getColor(R.color.navigo_background)

        setContentView(R.layout.activity_search)

        val searchRoot = findViewById<View>(R.id.main)

        ViewCompat.setOnApplyWindowInsetsListener(searchRoot) { view, insets ->
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

        ViewCompat.requestApplyInsets(searchRoot)

        searchInput = findViewById(R.id.etSearchPlace)
        statusText = findViewById(R.id.tvSearchStatus)
        recentContainer = findViewById(R.id.recentPlacesContainer)

        findViewById<ImageButton>(R.id.btnSearchBack)
            .setOnClickListener { finish() }

        findViewById<ImageButton>(R.id.btnSubmitSearch)
            .setOnClickListener { searchForPlace() }

        searchInput.setOnEditorActionListener { _, actionId, event ->
            val enterPressed =
                event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                        event.action == KeyEvent.ACTION_DOWN

            if (actionId == EditorInfo.IME_ACTION_SEARCH || enterPressed) {
                searchForPlace()
                true
            } else {
                false
            }
        }

        findViewById<TextView>(R.id.searchNavHome)
            .setOnClickListener { finish() }

        findViewById<TextView>(R.id.searchNavAlerts)
            .setOnClickListener {
                showNotReady("Alerts screen")
            }

        findViewById<TextView>(R.id.searchNavTrips)
            .setOnClickListener {
                showNotReady("Planned Trips screen")
            }

        findViewById<TextView>(R.id.searchNavSettings)
            .setOnClickListener {
                showNotReady("Settings screen")
            }

        displayRecents()
    }

    private fun searchForPlace() {
        val query = searchInput.text.toString().trim()

        if (query.isEmpty()) {
            searchInput.error = "Enter a place to search"
            return
        }

        statusText.text = "Searching for \"$query\"..."
        findViewById<ImageButton>(R.id.btnSubmitSearch).isEnabled = false

        // Geocoding may involve network access, so never run it on the UI thread.
        searchExecutor.execute {
            val address = try {
                @Suppress("DEPRECATION")
                Geocoder(this, Locale.getDefault())
                    .getFromLocationName(query, 1)
                    ?.firstOrNull()
            } catch (exception: Exception) {
                null
            }

            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread

                findViewById<ImageButton>(R.id.btnSubmitSearch)
                    .isEnabled = true

                if (address == null) {
                    statusText.text =
                        "Place not found. Try a more specific name or address."
                    return@runOnUiThread
                }

                val name = address.getAddressLine(0)
                    ?.takeIf { it.isNotBlank() }
                    ?: query

                val latitude = address.latitude
                val longitude = address.longitude

                saveRecent(name, latitude, longitude)
                returnDestination(name, latitude, longitude)
            }
        }
    }

    private fun returnDestination(
        name: String,
        latitude: Double,
        longitude: Double
    ) {
        val result = Intent().apply {
            putExtra("destination_name", name)
            putExtra("destination_latitude", latitude)
            putExtra("destination_longitude", longitude)
        }

        setResult(Activity.RESULT_OK, result)
        finish()
    }

    private fun saveRecent(
        name: String,
        latitude: Double,
        longitude: Double
    ) {
        val updated = JSONArray()

        updated.put(
            JSONObject().apply {
                put("name", name)
                put("latitude", latitude)
                put("longitude", longitude)
            }
        )

        val previous = readRecents()

        for (index in 0 until previous.length()) {
            val item = previous.optJSONObject(index) ?: continue

            if (item.optString("name") == name) continue
            if (updated.length() >= 5) break

            updated.put(item)
        }

        preferences.edit()
            .putString("recent_places", updated.toString())
            .apply()
    }

    private fun readRecents(): JSONArray {
        val saved = preferences.getString("recent_places", "[]") ?: "[]"

        return try {
            JSONArray(saved)
        } catch (exception: Exception) {
            JSONArray()
        }
    }

    private fun displayRecents() {
        recentContainer.removeAllViews()

        val recents = readRecents()

        if (recents.length() == 0) {
            statusText.text = "No recent searches yet."
            return
        }

        statusText.text = "Tap a recent place to view it on the map."

        for (index in 0 until recents.length()) {
            val place = recents.optJSONObject(index) ?: continue

            val name = place.optString("name")
            val latitude = place.optDouble("latitude", Double.NaN)
            val longitude = place.optDouble("longitude", Double.NaN)

            if (name.isBlank() ||
                latitude.isNaN() ||
                longitude.isNaN()
            ) {
                continue
            }

            val row = TextView(this).apply {
                text = "◷    $name"
                textSize = 14f
                setTextColor(getColor(R.color.navigo_navy))
                setPadding(12, 24, 12, 24)
                isClickable = true
                isFocusable = true

                setOnClickListener {
                    returnDestination(name, latitude, longitude)
                }
            }

            recentContainer.addView(row)

            val divider = android.view.View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                )
                setBackgroundColor(
                    getColor(R.color.navigo_light_grey)
                )
            }

            recentContainer.addView(divider)
        }
    }

    private fun showNotReady(screen: String) {
        Toast.makeText(
            this,
            "$screen will open once we build it.",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroy() {
        searchExecutor.shutdown()
        super.onDestroy()
    }
}