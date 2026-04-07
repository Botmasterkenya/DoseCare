package com.tee.dosecare.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication): Long

    @Update
    suspend fun updateMedication(medication: Medication)

    @Delete
    suspend fun deleteMedication(medication: Medication)

    @Query("SELECT * FROM medications WHERE userId = :userId AND isActive = 1 ORDER BY name ASC")
    fun getActiveMedications(userId: String): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE userId = :userId ORDER BY name ASC")
    fun getAllMedications(userId: String): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: Int): Medication?

    @Query("UPDATE medications SET isActive = 0 WHERE id = :id")
    suspend fun deactivateMedication(id: Int)
    // Add this query to MedicationDao.kt
    @Query("SELECT * FROM medications WHERE isActive = 1")
    suspend fun getAllMedicationsForBoot(): List<Medication>
}