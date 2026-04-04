package com.tee.dosecare.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Medication::class, DoseLog::class],
    version = 1,
    exportSchema = false
)
abstract class DoseCareDatabase : RoomDatabase() {

    abstract fun medicationDao(): MedicationDao
    abstract fun doseLogDao(): DoseLogDao

    companion object {
        @Volatile
        private var INSTANCE: DoseCareDatabase? = null

        fun getDatabase(context: Context): DoseCareDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DoseCareDatabase::class.java,
                    "dosecare_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}