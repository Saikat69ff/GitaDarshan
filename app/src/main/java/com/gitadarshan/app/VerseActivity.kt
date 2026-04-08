package com.gitadarshan.app

import android.animation.ObjectAnimator
import android.app.KeyguardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.PowerManager
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class VerseActivity : AppCompatActivity() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var countDownTimer: CountDownTimer? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show over lock screen
        setupLockScreenFlags()

        setContentView(R.layout.activity_verse)

        // Acquire wake lock
        acquireWakeLock()

        // Populate verse content
        populateVerse()

        // Setup timer
        setupCountdownTimer()

        // Setup tap to dismiss
        setupGesture()
    }

    private fun setupLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KeyguardManager::class.java)
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(PowerManager::class.java)
        @Suppress("DEPRECATION")
        wakeLock = powerManager?.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or
            PowerManager.ACQUIRE_CAUSES_WAKEUP or
            PowerManager.ON_AFTER_RELEASE,
            "GitaDarshan::VerseWakeLock"
        )
        wakeLock?.acquire(VerseOverlayService.displayDurationMs + 5000)
    }

    private fun populateVerse() {
        val verseIdText = findViewById<TextView>(R.id.tvVerseId)
        val englishText = findViewById<TextView>(R.id.tvEnglish)
        val bengaliText = findViewById<TextView>(R.id.tvBengali)
        val cardView = findViewById<View>(R.id.cardVerse)

        verseIdText.text = VerseOverlayService.currentVerseId
        englishText.text = VerseOverlayService.currentVerseEnglish
        bengaliText.text = VerseOverlayService.currentVerseBengali

        // Fade in animation
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        fadeIn.duration = 800
        cardView.startAnimation(fadeIn)
    }

    private fun setupCountdownTimer() {
        progressBar = findViewById(R.id.progressTimer)
        val duration = VerseOverlayService.displayDurationMs
        progressBar.max = duration.toInt()
        progressBar.progress = duration.toInt()

        countDownTimer = object : CountDownTimer(duration, 50) {
            override fun onTick(millisUntilFinished: Long) {
                progressBar.progress = millisUntilFinished.toInt()
            }
            override fun onFinish() {
                dismissAndSleep()
            }
        }.start()
    }

    private fun setupGesture() {
        val rootLayout = findViewById<View>(R.id.rootLayout)
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                dismissAndSleep()
                return true
            }
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vX: Float, vY: Float): Boolean {
                dismissAndSleep()
                return true
            }
        })

        rootLayout.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun dismissAndSleep() {
        countDownTimer?.cancel()
        releaseWakeLock()

        // Put screen back to sleep via root
        try {
            Runtime.getRuntime().exec(arrayOf("su", "-c", "input keyevent 223"))
        } catch (e: Exception) { }

        // Reset flag
        VerseOverlayService.currentVerseEnglish = ""
        finish()
        overridePendingTransition(0, android.R.anim.fade_out)
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) wakeLock?.release()
        } catch (e: Exception) { }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        releaseWakeLock()
    }

    override fun onBackPressed() {
        dismissAndSleep()
    }
}
