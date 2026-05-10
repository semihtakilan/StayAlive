package com.semihtakilan.stayalive.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semihtakilan.stayalive.data.preferences.UserPreferences
import com.semihtakilan.stayalive.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    bootstrapPreferences: UserPreferences,
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> =
        userPreferencesRepository.userPreferencesFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = bootstrapPreferences,
        )

    fun setAge(age: Int) {
        viewModelScope.launch { userPreferencesRepository.updateAge(age) }
    }

    fun setWeightKg(weightKg: Float) {
        viewModelScope.launch { userPreferencesRepository.updateWeightKg(weightKg) }
    }

    fun setHeightCm(heightCm: Float) {
        viewModelScope.launch { userPreferencesRepository.updateHeightCm(heightCm) }
    }

    fun setLanguagePref(value: String) {
        viewModelScope.launch { userPreferencesRepository.updateLanguagePref(value) }
    }

    fun setThemePref(value: String) {
        viewModelScope.launch { userPreferencesRepository.updateThemePref(value) }
    }

    fun setUnitPref(value: String) {
        viewModelScope.launch { userPreferencesRepository.updateUnitPref(value) }
    }
}
