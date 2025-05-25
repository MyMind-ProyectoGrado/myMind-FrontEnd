package com.example.mymindv2.adapters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.mymindv2.services.NotificationService

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) {
            Log.e("NotificationReceiver", "Context is null, cannot show notification")
            return
        }
        Log.d("NotificationReceiver", "Alarm received, triggering notification...")
        NotificationService.showNotification(context)
    }
}
