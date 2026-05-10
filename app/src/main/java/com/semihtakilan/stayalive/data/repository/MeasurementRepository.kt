package com.semihtakilan.stayalive.data.repository

import com.semihtakilan.stayalive.data.local.Measurement
import com.semihtakilan.stayalive.data.local.MeasurementDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.shareIn

class MeasurementRepository(
    private val dao: MeasurementDao,
    externalScope: CoroutineScope,
) {

    /**
     * Single shared Room observation for the whole app (replay = 1 so new collectors skip an empty flash).
     */
    val measurements: Flow<List<Measurement>> = dao.observeAll()
        .distinctUntilChanged()
        .shareIn(
            scope = externalScope,
            started = SharingStarted.Eagerly,
            replay = 1,
        )

    suspend fun insert(measurement: Measurement): Long = dao.insert(measurement)

    suspend fun update(measurement: Measurement) = dao.update(measurement)

    suspend fun delete(measurement: Measurement) = dao.delete(measurement)

    suspend fun getAllAscending(): List<Measurement> = dao.getAllAscending()

    suspend fun getAllDescending(): List<Measurement> = dao.getAllDescending()
}
