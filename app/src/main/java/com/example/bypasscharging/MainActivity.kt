package com.example.bypasscharging

import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import rikka.shizuku.Shizuku
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private lateinit var card: LinearLayout
    private lateinit var icon: ImageView
    private lateinit var title: TextView
    private lateinit var subtitle: TextView
    private lateinit var toggle: Switch
    private var updatingUi = false

    private val shizukuResult = Shizuku.OnRequestPermissionResultListener { _, grant ->
        if (grant == PackageManager.PERMISSION_GRANTED) refresh()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        card = findViewById(R.id.statusCard)
        icon = findViewById(R.id.statusIcon)
        title = findViewById(R.id.statusTitle)
        subtitle = findViewById(R.id.statusSubtitle)
        toggle = findViewById(R.id.bypassSwitch)

        toggle.setOnCheckedChangeListener { _, checked -> if (!updatingUi) apply(checked) }
        findViewById<TextView>(R.id.btnOn).setOnClickListener { apply(true) }
        findViewById<TextView>(R.id.btnOff).setOnClickListener { apply(false) }
        findViewById<TextView>(R.id.btnRefresh).setOnClickListener { refresh() }
        findViewById<ImageView>(R.id.btnSettings).setOnClickListener { showMethodDialog() }

        try { Shizuku.addRequestPermissionResultListener(shizukuResult) } catch (_: Throwable) {}
    }

    override fun onDestroy() {
        try { Shizuku.removeRequestPermissionResultListener(shizukuResult) } catch (_: Throwable) {}
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    /** Cogwheel menu: choose how the app runs its commands. Root is the default. */
    private fun showMethodDialog() {
        val items = arrayOf("Root (default)", "Shizuku (no root)")
        val current = if (BypassController.getMode(this) == BypassController.Mode.SHIZUKU) 1 else 0
        AlertDialog.Builder(this)
            .setTitle("Control method")
            .setSingleChoiceItems(items, current) { dialog, which ->
                chooseMode(if (which == 1) BypassController.Mode.SHIZUKU else BypassController.Mode.ROOT)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun chooseMode(mode: BypassController.Mode) {
        BypassController.setMode(this, mode)
        if (mode == BypassController.Mode.SHIZUKU &&
            BypassController.shizukuRunning() && !BypassController.shizukuGranted()
        ) {
            try { Shizuku.requestPermission(1001) } catch (_: Throwable) {}
        }
        refresh()
    }

    private fun apply(enabled: Boolean) = thread {
        val s = BypassController.write(this, enabled)
        runOnUiThread { render(s) }
    }

    private fun refresh() = thread {
        val s = BypassController.read(this)
        runOnUiThread { render(s) }
    }

    private fun render(s: BypassController.Status) {
        val on = s.enabled == true
        val (bgRes, fgRes) = when {
            s.error != null -> R.color.error_container to R.color.on_error_container
            on -> R.color.primary_container to R.color.on_primary_container
            else -> R.color.surface_variant to R.color.on_surface_variant
        }
        val fg = getColor(fgRes)
        (card.background.mutate() as GradientDrawable).setColor(getColor(bgRes))
        icon.setColorFilter(fg)
        title.setTextColor(fg)
        subtitle.setTextColor(fg)

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
