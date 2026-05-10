package com.semihtakilan.stayalive

import android.content.Context
import com.semihtakilan.stayalive.data.local.AppDatabase
import com.semihtakilan.stayalive.data.preferences.UserPreferencesRepository
import com.semihtakilan.stayalive.data.repository.MeasurementRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Simple service locator for repositories (no DI framework).
 */
class AppContainer(
    context: Context,
    private val applicationScope: CoroutineScope,
) {

    private val appContext = context.applicationContext

    private val database: AppDatabase by lazy { AppDatabase.build(appContext) }

    val measurementRepository: MeasurementRepository by lazy {
        MeasurementRepository(database.measurementDao(), applicationScope)
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(appContext, applicationScope)
    }

    /** Opens the DB file on a background thread so the first UI frame does not pay cold-open cost. */
    fun warmUpDatabase() {
        applicationScope.launch(Dispatchers.IO) {
            runCatching {
                measurementRepository.measurements
            }
        }
    }
}
