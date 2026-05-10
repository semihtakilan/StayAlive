package com.semihtakilan.stayalive

import android.app.Application
import com.semihtakilan.stayalive.data.preferences.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking

class StayAliveApplication : Application() {

    /** Long-lived scope for shared flows (Room + DataStore); avoids duplicate collectors. */
    val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var container: AppContainer
        private set

    /** First DataStore read before any UI; keeps theme/locale from flashing defaults. */
    lateinit var bootstrapPreferences: UserPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        clearAndroidxApplicationLocaleOverride()
        container = AppContainer(this, applicationScope)
        bootstrapPreferences = runBlocking(Dispatchers.IO) {
            container.userPreferencesRepository.readFirstSnapshot()
        }
        container.warmUpDatabase()
    }

    override fun onTerminate() {
        applicationScope.cancel()
        super.onTerminate()
    }
}
