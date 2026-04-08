package com.gitadarshan.app

import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var seekDuration: SeekBar
    private lateinit var tvDurationValue: TextView

    // Duration options in seconds: 10, 15, 20, 30, 45, 60
    private val durationOptions = intArrayOf(10, 15, 20, 30, 45, 60)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        seekDuration = findViewById(R.id.seekDuration)
        tvDurationValue = findViewById(R.id.tvDurationValue)

        seekDuration.max = durationOptions.size - 1

        val prefs = getSharedPreferences("gita_prefs", MODE_PRIVATE)
        val savedMs = prefs.getLong("display_duration_ms", 15000L)
        val savedSec = (savedMs / 1000).toInt()
        val idx = durationOptions.indexOfFirst { it >= savedSec }.coerceAtLeast(0)
        seekDuration.progress = idx
        updateDurationLabel(idx)

        seekDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                updateDurationLabel(progress)
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {
                val seconds = durationOptions[sb.progress]
                prefs.edit().putLong("display_duration_ms", seconds * 1000L).apply()
                VerseOverlayService.displayDurationMs = seconds * 1000L
            }
        })

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
    }

    private fun updateDurationLabel(idx: Int) {
        val sec = durationOptions[idx.coerceIn(0, durationOptions.size - 1)]
        tvDurationValue.text = "$sec seconds"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
