package com.semihtakilan.stayalive.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {

    @Query("SELECT * FROM measurements ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<Measurement>>

    @Query("SELECT * FROM measurements ORDER BY timestamp ASC")
    suspend fun getAllAscending(): List<Measurement>

    @Query("SELECT * FROM measurements ORDER BY timestamp DESC")
    suspend fun getAllDescending(): List<Measurement>

    @Insert
    suspend fun insert(entity: Measurement): Long

    @Update
    suspend fun update(entity: Measurement)

    @Delete
    suspend fun delete(entity: Measurement)
}
