package com.semihtakilan.stayalive.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.semihtakilan.stayalive.AppContainer
import com.semihtakilan.stayalive.data.preferences.UserPreferences

@Suppress("UNCHECKED_CAST")
class StayAliveViewModelFactory(
    private val container: AppContainer,
    private val bootstrapPreferences: UserPreferences,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) ->
                MainViewModel(container.userPreferencesRepository, bootstrapPreferences) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(container.measurementRepository) as T
            modelClass.isAssignableFrom(InsightsViewModel::class.java) ->
                InsightsViewModel(container.measurementRepository) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(container.userPreferencesRepository, bootstrapPreferences) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
