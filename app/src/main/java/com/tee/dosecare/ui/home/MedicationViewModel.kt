package com.tee.dosecare.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.tee.dosecare.data.local.DoseLog
import com.tee.dosecare.data.local.DoseStatus
import com.tee.dosecare.data.local.Medication
import com.tee.dosecare.data.repository.MedicationRepository
import com.tee.dosecare.utils.AlarmScheduler
import com.tee.dosecare.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicationViewModel @Inject constructor(
    private val repository: MedicationRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val userId get() = auth.currentUser?.uid ?: ""

    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications

    private val _todayDoseLogs = MutableStateFlow<List<DoseLog>>(emptyList())
    val todayDoseLogs: StateFlow<List<DoseLog>> = _todayDoseLogs

    private val _operationState = MutableStateFlow<Resource<Unit>?>(null)
    val operationState: StateFlow<Resource<Unit>?> = _operationState

    init {
        loadMedications()
        loadTodayDoseLogs()
        loadAllDoseLogs() // add this line
    }

    private fun loadMedications() {
        viewModelScope.launch {
            repository.getActiveMedications(userId).collectLatest {
                _medications.value = it
            }
        }
    }

    private fun loadTodayDoseLogs() {
        viewModelScope.launch {
            val startOfDay = getStartOfDay()
            val endOfDay = startOfDay + 86400000L
            repository.getDoseLogsForDay(userId, startOfDay, endOfDay).collectLatest {
                _todayDoseLogs.value = it
            }
        }
    }
    private fun loadAllDoseLogs() {
        viewModelScope.launch {
            // Load last 30 days of history
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            val now = System.currentTimeMillis()
            repository.getDoseLogsForDay(userId, thirtyDaysAgo, now).collectLatest { logs ->
                _allDoseLogs.value = logs
                _currentStreak.value = calculateStreak(logs)
            }
        }
    }

    private fun calculateStreak(logs: List<DoseLog>): Int {
        if (logs.isEmpty()) return 0

        val calendar = Calendar.getInstance()
        var streak = 0

        // Go backwards day by day and check if all doses were taken
        for (daysBack in 0..365) {
            calendar.apply {
                time = Date(System.currentTimeMillis())
                add(Calendar.DAY_OF_YEAR, -daysBack)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = calendar.timeInMillis
            val endOfDay = startOfDay + 86_400_000L

            val dayLogs = logs.filter { it.scheduledTime in startOfDay until endOfDay }

            // If no logs for this day and we're past today, stop
            if (dayLogs.isEmpty()) {
                if (daysBack > 0) break
                continue
            }

            val allTaken = dayLogs.all { it.status == DoseStatus.TAKEN }
            if (allTaken) streak++ else if (daysBack > 0) break
        }

        return streak
    }
    private val _allDoseLogs = MutableStateFlow<List<DoseLog>>(emptyList())
    val allDoseLogs: StateFlow<List<DoseLog>> = _allDoseLogs

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak

    fun addMedication(medication: Medication, context: android.content.Context) {
        viewModelScope.launch {
            _operationState.value = Resource.Loading
            try {
                val id = repository.insertMedication(medication.copy(userId = userId))
                // Schedule alarms for the newly added medication
                val savedMedication = medication.copy(id = id.toInt(), userId = userId)
                AlarmScheduler.scheduleMedicationAlarms(context, savedMedication)
                _operationState.value = Resource.Success(Unit)
            } catch (e: Exception) {
                _operationState.value = Resource.Error(e.message ?: "Failed to add medication")
            }
        }
    }

    fun deleteMedication(medication: Medication) {
        viewModelScope.launch {
            repository.deactivateMedication(medication.id)
        }
    }

    fun markDose(doseLogId: Int, status: String) {
        viewModelScope.launch {
            val takenTime = if (status == DoseStatus.TAKEN) System.currentTimeMillis() else null
            repository.updateDoseStatus(doseLogId, status, takenTime)
        }
    }

    fun scheduleDosesForToday(medication: Medication) {
        viewModelScope.launch {
            val times = medication.times.removeSurrounding("[", "]")
                .split(",")
                .map { it.trim().removeSurrounding("\"") }

            val startOfDay = getStartOfDay()
            times.forEach { time ->
                val parts = time.split(":")
                if (parts.size == 2) {
                    val hours = parts[0].toLongOrNull() ?: 0L
                    val minutes = parts[1].toLongOrNull() ?: 0L
                    val scheduledTime = startOfDay + (hours * 3600000L) + (minutes * 60000L)
                    val doseLog = DoseLog(
                        medicationId = medication.id,
                        scheduledTime = scheduledTime,
                        userId = userId
                    )
                    repository.insertDoseLog(doseLog)
                }
            }
        }
    }

    fun resetOperationState() {
        _operationState.value = null
    }

    private fun getStartOfDay(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}