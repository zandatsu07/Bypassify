package com.example.bypasscharging

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.google.android.material.color.MaterialColors
import com.google.android.material.materialswitch.MaterialSwitch
import kotlin.concurrent.thread
import com.google.android.material.R as M3

class MainActivity : AppCompatActivity() {

    private lateinit var card: MaterialCardView
    private lateinit var icon: ImageView
    private lateinit var title: TextView
    private lateinit var subtitle: TextView
    private lateinit var toggle: MaterialSwitch
    private var updatingUi = false

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivityIfAvailable(
            this,
            DynamicColorsOptions.Builder()
                .setThemeOverlay(R.style.ThemeOverlay_BypassCharging_PureBlack)
                .build()
        ) // Material You colors + pure black in dark mode
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        card = findViewById(R.id.statusCard)
        icon = findViewById(R.id.statusIcon)
        title = findViewById(R.id.statusTitle)
        subtitle = findViewById(R.id.statusSubtitle)
        toggle = findViewById(R.id.bypassSwitch)

        toggle.setOnCheckedChangeListener { _, checked -> if (!updatingUi) apply(checked) }
        findViewById<MaterialButton>(R.id.btnOn).setOnClickListener { apply(true) }
        findViewById<MaterialButton>(R.id.btnOff).setOnClickListener { apply(false) }
        findViewById<MaterialButton>(R.id.btnRefresh).setOnClickListener { refresh() }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun apply(enabled: Boolean) = thread {
        val s = BypassController.write(enabled)
        runOnUiThread { render(s) }
    }

    private fun refresh() = thread {
        val s = BypassController.read()
        runOnUiThread { render(s) }
    }

    private fun render(s: BypassController.Status) {
        val on = s.enabled == true
        val (bg, fg) = when {
            s.error != null -> M3.attr.colorErrorContainer to M3.attr.colorOnErrorContainer
            on -> M3.attr.colorPrimaryContainer to M3.attr.colorOnPrimaryContainer
            else -> M3.attr.colorSurfaceVariant to M3.attr.colorOnSurfaceVariant
        }
        val bgColor = MaterialColors.getColor(card, bg)
        val fgColor = MaterialColors.getColor(card, fg)
        card.setCardBackgroundColor(bgColor)
        icon.setColorFilter(fgColor)
        title.setTextColor(fgColor)
        subtitle.setTextColor(fgColor)

        when {
            s.error != null -> { title.text = "Error"; subtitle.text = s.error }
            s.enabled == true -> { title.text = "Bypass ON"; subtitle.text = "Power goes straight to the phone" }
            s.enabled == false -> { title.text = "Bypass OFF"; subtitle.text = "Battery charges normally" }
            else -> { title.text = "Not set"; subtitle.text = "pass_through value: ${s.raw.ifEmpty { "null" }}" }
        }

        updatingUi = true
        toggle.isChecked = on
        updatingUi = false
    }
}
