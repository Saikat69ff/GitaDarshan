package com.gitadarshan.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.gitadarshan.app.data.GitaRepository

class MainActivity : AppCompatActivity() {

    private lateinit var switchService: Switch
    private lateinit var tvStatus: TextView
    private lateinit var repository: GitaRepository

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.canDrawOverlays(this)) {
            startGitaService()
        } else {
            Toast.makeText(this, "Overlay permission is required for verse display.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = GitaRepository(applicationContext)

        switchService = findViewById(R.id.switchService)
        tvStatus = findViewById(R.id.tvStatus)

        val prefs = getSharedPreferences("gita_prefs", MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("service_enabled", false)
        switchService.isChecked = isEnabled

        updateStatusText(isEnabled)

        switchService.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("service_enabled", checked).apply()
            if (checked) {
                checkPermissionsAndStart()
            } else {
                stopGitaService()
            }
            updateStatusText(checked)
        }

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Auto-start if was enabled
        if (isEnabled) {
            checkPermissionsAndStart()
        }
    }

    private fun checkPermissionsAndStart() {
        // Check overlay permission
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Please grant 'Display over other apps' permission", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
            return
        }

        // Check notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
        }

        startGitaService()
    }

    private fun startGitaService() {
        val intent = Intent(this, VerseOverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Toast.makeText(this, "Gita Darshan is active 🙏", Toast.LENGTH_SHORT).show()
    }

    private fun stopGitaService() {
        stopService(Intent(this, VerseOverlayService::class.java))
        Toast.makeText(this, "Gita Darshan stopped", Toast.LENGTH_SHORT).show()
    }

    private fun updateStatusText(enabled: Boolean) {
        tvStatus.text = if (enabled)
            "✅ Active — Gita verses will appear after screen turns off"
        else
            "⭕ Inactive — Enable to show Gita verses on screen off"
    }
}
