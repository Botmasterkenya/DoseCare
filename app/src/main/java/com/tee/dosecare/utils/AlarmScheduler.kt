package com.tee.dosecare.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.tee.dosecare.data.local.Medication
import java.util.Calendar

object AlarmScheduler {

    private const val TAG = "AlarmScheduler"

    fun scheduleMedicationAlarms(context: Context, medication: Medication) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Check exact alarm permission on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Cannot schedule exact alarms — user needs to grant permission in settings")
                // Schedule inexact alarm as fallback
                scheduleInexactAlarms(context, alarmManager, medication)
                return
            }
        }

        val times = parseTimes(medication.times)

        times.forEachIndexed { index, (hour, minute) ->
            val triggerTime = getNextTriggerTime(hour, minute)
            val requestCode = getRequestCode(medication.id, index)

            val intent = buildAlarmIntent(context, medication, requestCode)

            try {
                // setAlarmClock is the most reliable — shows in system clock, survives doze
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerTime, intent),
                    intent
                )
                Log.d(TAG, "Scheduled alarm for ${medication.name} at $hour:$minute (triggerTime=$triggerTime)")
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException scheduling alarm: ${e.message}")
                // Fallback to setExact
                try {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, intent)
                } catch (e2: Exception) {
                    Log.e(TAG, "Failed to schedule alarm: ${e2.message}")
                }
            }
        }
    }

    private fun scheduleInexactAlarms(context: Context, alarmManager: AlarmManager, medication: Medication) {
        val times = parseTimes(medication.times)
        times.forEachIndexed { index, (hour, minute) ->
            val triggerTime = getNextTriggerTime(hour, minute)
            val requestCode = getRequestCode(medication.id, index)
            val intent = buildAlarmIntent(context, medication, requestCode)
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, intent)
            Log.d(TAG, "Scheduled inexact alarm for ${medication.name} at $hour:$minute")
        }
    }

    fun cancelMedicationAlarms(context: Context, medication: Medication) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val times = parseTimes(medication.times)

        times.forEachIndexed { index, _ ->
            val requestCode = getRequestCode(medication.id, index)
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let {
                alarmManager.cancel(it)
                Log.d(TAG, "Cancelled alarm for ${medication.name}")
            }
        }
    }

    private fun buildAlarmIntent(context: Context, medication: Medication, requestCode: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("medication_name", medication.name)
            putExtra("dosage", "${medication.dosage} ${medication.unit}")
            putExtra("notification_id", requestCode)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getNextTriggerTime(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If this time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return calendar.timeInMillis
    }

    private fun parseTimes(timesJson: String): List<Pair<Int, Int>> {
        return timesJson
            .removeSurrounding("[", "]")
            .split(",")
            .mapNotNull { token ->
                val time = token.trim().removeSurrounding("\"")
                val parts = time.split(":")
                if (parts.size == 2) {
                    val h = parts[0].toIntOrNull()
                    val m = parts[1].toIntOrNull()
                    if (h != null && m != null) Pair(h, m) else null
                } else null
            }
    }

    private fun getRequestCode(medicationId: Int, timeIndex: Int): Int {
        return (medicationId * 100) + timeIndex
    }
}