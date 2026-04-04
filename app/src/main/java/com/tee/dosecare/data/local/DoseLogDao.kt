package com.tee.dosecare.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseLog(doseLog: DoseLog): Long

    @Update
    suspend fun updateDoseLog(doseLog: DoseLog)

    @Query("SELECT * FROM dose_logs WHERE medicationId = :medicationId ORDER BY scheduledTime DESC")
    fun getDoseLogsForMedication(medicationId: Int): Flow<List<DoseLog>>

    @Query("SELECT * FROM dose_logs WHERE userId = :userId AND scheduledTime BETWEEN :startOfDay AND :endOfDay")
    fun getDoseLogsForDay(userId: String, startOfDay: Long, endOfDay: Long): Flow<List<DoseLog>>

    @Query("SELECT * FROM dose_logs WHERE userId = :userId AND status = :status")
    fun getDoseLogsByStatus(userId: String, status: String): Flow<List<DoseLog>>

    @Query("UPDATE dose_logs SET status = :status, takenTime = :takenTime WHERE id = :id")
    suspend fun updateDoseStatus(id: Int, status: String, takenTime: Long?)

    @Query("SELECT COUNT(*) FROM dose_logs WHERE userId = :userId AND status = 'TAKEN'")
    suspend fun getTotalTakenCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM dose_logs WHERE userId = :userId AND scheduledTime BETWEEN :start AND :end")
    suspend fun getTotalScheduledInRange(userId: String, start: Long, end: Long): Int
}