package com.tee.dosecare.data.repository

import com.tee.dosecare.data.local.DoseLog
import com.tee.dosecare.data.local.DoseLogDao
import com.tee.dosecare.data.local.Medication
import com.tee.dosecare.data.local.MedicationDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepository @Inject constructor(
    private val medicationDao: MedicationDao,
    private val doseLogDao: DoseLogDao
) {
    // Medications
    fun getActiveMedications(userId: String): Flow<List<Medication>> =
        medicationDao.getActiveMedications(userId)

    fun getAllMedications(userId: String): Flow<List<Medication>> =
        medicationDao.getAllMedications(userId)

    fun getAllRecentDoseLogs(userId: String, fromTime: Long, toTime: Long): Flow<List<DoseLog>> =
        doseLogDao.getDoseLogsForDay(userId, fromTime, toTime)

    suspend fun getMedicationById(id: Int): Medication? =
        medicationDao.getMedicationById(id)

    suspend fun insertMedication(medication: Medication): Long =
        medicationDao.insertMedication(medication)

    suspend fun updateMedication(medication: Medication) =
        medicationDao.updateMedication(medication)

    suspend fun deleteMedication(medication: Medication) =
        medicationDao.deleteMedication(medication)

    suspend fun deactivateMedication(id: Int) =
        medicationDao.deactivateMedication(id)

    // Dose Logs
    fun getDoseLogsForMedication(medicationId: Int): Flow<List<DoseLog>> =
        doseLogDao.getDoseLogsForMedication(medicationId)

    fun getDoseLogsForDay(userId: String, startOfDay: Long, endOfDay: Long): Flow<List<DoseLog>> =
        doseLogDao.getDoseLogsForDay(userId, startOfDay, endOfDay)

    suspend fun insertDoseLog(doseLog: DoseLog): Long =
        doseLogDao.insertDoseLog(doseLog)

    suspend fun updateDoseStatus(id: Int, status: String, takenTime: Long?) =
        doseLogDao.updateDoseStatus(id, status, takenTime)

    suspend fun getTotalTakenCount(userId: String): Int =
        doseLogDao.getTotalTakenCount(userId)
}