package com.semihtakilan.stayalive.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semihtakilan.stayalive.data.preferences.UserPreferences
import com.semihtakilan.stayalive.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Activity-scoped preferences for theme and locale wiring.
 */
class MainViewModel(
    userPreferencesRepository: UserPreferencesRepository,
    bootstrapPreferences: UserPreferences,
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> =
        userPreferencesRepository.userPreferencesFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = bootstrapPreferences,
        )
}
