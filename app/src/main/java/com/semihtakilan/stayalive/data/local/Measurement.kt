package com.semihtakilan.stayalive.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class Measurement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sys: Int,
    val dia: Int,
    val pulse: Int,
    val timestamp: Long,
    /** Stored as universal keys (e.g. [MeasurementTagKeys.HEADACHE]). */
    val tags: List<String>,
)
