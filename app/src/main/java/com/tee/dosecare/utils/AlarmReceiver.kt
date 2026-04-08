package com.tee.dosecare.utils

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tee.dosecare.DoseCareApp
import com.tee.dosecare.MainActivity

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medicationName = intent.getStringExtra("medication_name") ?: "Medication"
        val dosage = intent.getStringExtra("dosage") ?: ""
        val notificationId = intent.getIntExtra("notification_id", System.currentTimeMillis().toInt())
        val medicationId = intent.getIntExtra("medication_id", -1)
        val timeIndex = intent.getIntExtra("time_index", 0)
        val hour = intent.getIntExtra("alarm_hour", -1)
        val minute = intent.getIntExtra("alarm_minute", 0)

        Log.d(TAG, "Alarm fired for: $medicationName (id=$medicationId, hour=$hour, min=$minute)")

        showNotification(context, medicationName, dosage, notificationId)

        // Re-schedule this alarm for the next day so it repeats daily
        if (medicationId >= 0 && hour >= 0) {
            AlarmScheduler.scheduleNextDayAlarm(
                context = context,
                medicationId = medicationId,
                medicationName = medicationName,
                dosage = dosage,
                timeIndex = timeIndex,
                hour = hour,
                minute = minute
            )
            Log.d(TAG, "Rescheduled alarm for next day: $medicationName at $hour:$minute")
        }
    }

    private fun showNotification(
        context: Context,
        medicationName: String,
        dosage: String,
        notificationId: Int
    ) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, DoseCareApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("💊 Time to take $medicationName")
            .setContentText("Dosage: $dosage — Don't miss your dose!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("It's time to take $medicationName ($dosage). Tap to open DoseCare and log your dose.")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(tapPendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setSound(defaultSound)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            Log.d(TAG, "Notification shown for $medicationName (id=$notificationId)")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException showing notification: ${e.message}")
        }
    }
}