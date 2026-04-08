package com.gitadarshan.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.gitadarshan.app.data.GitaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VerseOverlayService : Service() {

    companion object {
        const val CHANNEL_ID = "gita_darshan_channel"
        const val NOTIFICATION_ID = 1001
        const val TAG = "GitaDarshan"

        // Static current verse so VerseActivity can access it
        var currentVerseEnglish: String = ""
        var currentVerseBengali: String = ""
        var currentVerseId: String = ""
        var displayDurationMs: Long = 15000L
    }

    private lateinit var repository: GitaRepository
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var isShowingVerse = false

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                Log.d(TAG, "Screen OFF detected")
                handleScreenOff()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = GitaRepository(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())

        // Load settings
        val prefs = getSharedPreferences("gita_prefs", MODE_PRIVATE)
        displayDurationMs = prefs.getLong("display_duration_ms", 15000L)

        // Register screen off receiver
        try {
            val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(screenOffReceiver, filter, RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(screenOffReceiver, filter)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register receiver: ${e.message}")
        }

        Log.d(TAG, "GitaDarshan Service started. Verses loaded: ${repository.getVerseCount()}")
        return START_STICKY
    }

    private fun handleScreenOff() {
        if (isShowingVerse) return

        // Reload duration setting each time (in case user changed it)
        val prefs = getSharedPreferences("gita_prefs", MODE_PRIVATE)
        displayDurationMs = prefs.getLong("display_duration_ms", 15000L)

        // Pick a random verse
        val verse = repository.getRandomVerse()
        currentVerseEnglish = verse.english
        currentVerseBengali = verse.bengali
        currentVerseId = "Chapter ${verse.chapter}, Verse ${verse.verseNumber}  •  ${verse.verseId}"

        serviceScope.launch {
            delay(400) // Brief pause to let screen go off fully
            wakeScreenWithRoot()
            delay(300) // Let screen wake up
            showVerseActivity()
        }
    }

    private fun wakeScreenWithRoot() {
        try {
            // KernelSU / Magisk compatible root wake
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "input keyevent 224"))
            process.waitFor()
            Log.d(TAG, "Screen wake command sent")
        } catch (e: Exception) {
            Log.e(TAG, "Root wake failed: ${e.message}")
        }
    }

    private fun showVerseActivity() {
        isShowingVerse = true
        val intent = Intent(this, VerseActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_NO_HISTORY
        }
        startActivity(intent)

        // Reset flag after display duration + buffer
        serviceScope.launch {
            delay(displayDurationMs + 3000)
            isShowingVerse = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(screenOffReceiver)
        } catch (e: Exception) { }
        serviceScope.launch {}.cancel()

        // Restart if killed
        val intent = Intent(this, VerseOverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Gita Darshan",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Gita Darshan background service"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Gita Darshan")
            .setContentText("Bhagavad Gita verses are active 🙏")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentIntent(pi)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
