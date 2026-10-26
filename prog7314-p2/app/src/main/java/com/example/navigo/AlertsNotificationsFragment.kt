
package com.example.navigo

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import androidx.fragment.app.Fragment

class AlertsNotificationsFragment : Fragment() {

    private data class DemoAlert(
        val category: String,
        val icon: String,
        val title: String,
        val message: String,
        val time: String
    )

    private val alerts = mutableListOf(
        DemoAlert(
            "Incidents", "⚠", "Demo accident",
            "Example location: Peter Road\nSample distance: 1.2 km",
            "Example alert"
        ),
        DemoAlert(
            "Incidents", "⚠", "Demo incident",
            "Example police activity report.\nPossible delays.",
            "Example alert"
        ),
        DemoAlert(
            "Transit", "🚌", "Demo transit update",
            "Example route disruption.\nConsider an alternative route.",
            "Example alert"
        ),
        DemoAlert(
            "Weather", "☁", "Demo weather update",
            "Example forecast: rain expected.",
            "Example alert"
        )
    )

    private var selectedFilter = "All"

    private lateinit var alertsContainer: LinearLayout
    private lateinit var emptyMessage: TextView

    private lateinit var allButton: Button
    private lateinit var weatherButton: Button
    private lateinit var transitButton: Button
    private lateinit var incidentsButton: Button

    private val teal = Color.rgb(23, 169, 157)
    private val darkTeal = Color.rgb(55, 104, 102)
    private val navy = Color.rgb(36, 59, 58)
    private val muted = Color.rgb(104, 122, 128)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            selectedFilter =
                savedInstanceState.getString("selected_filter") ?: "All"

            if (savedInstanceState.getBoolean("alerts_cleared", false)) {
                alerts.clear()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_alerts_notifications,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        alertsContainer = view.findViewById(R.id.alertsContainer)
        emptyMessage = view.findViewById(R.id.tvAlertsEmpty)

        allButton = view.findViewById(R.id.btnFilterAll)
        weatherButton = view.findViewById(R.id.btnFilterWeather)
        transitButton = view.findViewById(R.id.btnFilterTransit)
        incidentsButton = view.findViewById(R.id.btnFilterIncidents)

        allButton.setOnClickListener { selectFilter("All") }
        weatherButton.setOnClickListener { selectFilter("Weather") }
        transitButton.setOnClickListener { selectFilter("Transit") }
        incidentsButton.setOnClickListener { selectFilter("Incidents") }

        view.findViewById<TextView>(R.id.btnClearAlerts)
            .setOnClickListener {
                alerts.clear()
                renderAlerts()

                Toast.makeText(
                    requireContext(),
                    "Demo alerts cleared",
                    Toast.LENGTH_SHORT
                ).show()
            }

        view.findViewById<Button>(R.id.btnReportIncident)
            .setOnClickListener {
                startActivity(
                    Intent(requireContext(), IncidentReportingActivity::class.java)
                )
            }

        updateFilterButtons()
        renderAlerts()
    }

    private fun selectFilter(filter: String) {
        selectedFilter = filter
        updateFilterButtons()
        renderAlerts()
    }

    private fun updateFilterButtons() {
        val buttons = listOf(
            "All" to allButton,
            "Weather" to weatherButton,
            "Transit" to transitButton,
            "Incidents" to incidentsButton
        )

        for ((category, button) in buttons) {
            val selected = category == selectedFilter

            button.backgroundTintList = ColorStateList.valueOf(
                if (selected) darkTeal else teal
            )

            button.setTextColor(Color.WHITE)
            button.alpha = if (selected) 1f else 0.88f
        }
    }

    private fun renderAlerts() {
        alertsContainer.removeAllViews()

        val visibleAlerts = alerts.filter {
            selectedFilter == "All" ||
                    it.category == selectedFilter
        }

        emptyMessage.visibility =
            if (visibleAlerts.isEmpty()) View.VISIBLE else View.GONE

        emptyMessage.text =
            if (alerts.isEmpty()) {
                "No demo alerts to display."
            } else {
                "No demo alerts in this category."
            }

        for ((index, alert) in visibleAlerts.withIndex()) {
            alertsContainer.addView(
                createAlertCard(alert, index)
            )
        }
    }

    private fun createAlertCard(
        alert: DemoAlert,
        index: Int
    ): LinearLayout {

        val context = requireContext()

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.TOP
            setPadding(dp(12), dp(14), dp(12), dp(14))

            background = GradientDrawable().apply {
                setColor(
                    if (index % 2 == 0) {
                        Color.WHITE
                    } else {
                        Color.rgb(110, 149, 146)
                    }
                )
                cornerRadius = dp(2).toFloat()
            }

            elevation = dp(3).toFloat()
        }

        card.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(14)
        }

        val icon = TextView(context).apply {
            text = alert.icon
            textSize = 20f
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        }

        card.addView(
            icon,
            LinearLayout.LayoutParams(dp(50), dp(45))
        )

        val details = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        card.addView(
            details,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val titleColor = when (alert.category) {
            "Weather" -> darkTeal
            "Transit" -> Color.rgb(126, 92, 20)
            else -> Color.rgb(158, 61, 38)
        }

        details.addView(
            TextView(context).apply {
                text = alert.title
                textSize = 14f
                setTextColor(titleColor)
                setTypeface(null, Typeface.BOLD)
            }
        )

        details.addView(
            TextView(context).apply {
                text = alert.message
                textSize = 13f
                setTextColor(navy)
                setPadding(0, dp(9), 0, 0)
            }
        )

        details.addView(
            TextView(context).apply {
                text = alert.time
                textSize = 12f
                setTextColor(muted)
                setTypeface(null, Typeface.ITALIC)
            }
        )

        return card
    }

    private fun dp(value: Int): Int {
        return (
                value * resources.displayMetrics.density
                ).toInt()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("selected_filter", selectedFilter)
        outState.putBoolean("alerts_cleared", alerts.isEmpty())
        super.onSaveInstanceState(outState)
    }
}