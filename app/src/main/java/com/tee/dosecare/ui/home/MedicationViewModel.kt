package com.tee.dosecare.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.tee.dosecare.data.local.DoseLog
import com.tee.dosecare.data.local.DoseStatus
import com.tee.dosecare.data.local.Medication
import com.tee.dosecare.data.repository.MedicationRepository
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

    fun addMedication(medication: Medication) {
        viewModelScope.launch {
            _operationState.value = Resource.Loading
            try {
                repository.insertMedication(medication.copy(userId = userId))
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