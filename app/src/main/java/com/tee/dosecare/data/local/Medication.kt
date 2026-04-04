package com.tee.dosecare.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val dosage: String,
    val unit: String, // mg, ml, tablets
    val frequency: String, // Daily, Twice daily, etc.
    val times: String, // JSON string of times e.g. ["08:00","20:00"]
    val startDate: Long,
    val endDate: Long? = null,
    val notes: String = "",
    val color: String = "#006D77",
    val isActive: Boolean = true,
    val userId: String = ""
)