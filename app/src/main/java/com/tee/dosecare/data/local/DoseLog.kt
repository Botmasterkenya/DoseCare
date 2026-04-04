package com.tee.dosecare.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dose_logs",
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicationId")]
)
data class DoseLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val medicationId: Int,
    val scheduledTime: Long,
    val takenTime: Long? = null,
    val status: String = DoseStatus.PENDING,
    val notes: String = "",
    val userId: String = ""
)

object DoseStatus {
    const val PENDING = "PENDING"
    const val TAKEN = "TAKEN"
    const val SKIPPED = "SKIPPED"
    const val MISSED = "MISSED"
}