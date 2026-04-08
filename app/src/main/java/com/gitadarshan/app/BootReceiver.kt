package com.gitadarshan.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs: SharedPreferences = context.getSharedPreferences("gita_prefs", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("service_enabled", true)

        if (isEnabled && (
            intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.LOCKED_BOOT_COMPLETED" ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        )) {
            val serviceIntent = Intent(context, VerseOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}
