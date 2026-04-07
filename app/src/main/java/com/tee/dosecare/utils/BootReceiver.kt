package com.tee.dosecare.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tee.dosecare.data.local.DoseCareDatabase
import com.tee.dosecare.utils.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            // Reschedule all active medication alarms after reboot
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = DoseCareDatabase.getDatabase(context)
                    // Get all active medications for all users and reschedule
                    val allMeds = db.medicationDao().getAllMedicationsForBoot()
                    allMeds.forEach { medication ->
                        AlarmScheduler.scheduleMedicationAlarms(context, medication)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}