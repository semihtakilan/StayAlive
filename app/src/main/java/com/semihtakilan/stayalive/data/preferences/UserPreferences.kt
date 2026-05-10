package com.semihtakilan.stayalive.data.preferences

/**
 * Canonical storage: metric (kg, cm). UI converts for imperial using [unitPref].
 */
data class UserPreferences(
    val age: Int = 0,
    val weightKg: Float = 0f,
    val heightCm: Float = 0f,
    /** SYSTEM | TR | EN */
    val languagePref: String = PREF_LANGUAGE_SYSTEM,
    /** SYSTEM | LIGHT | DARK */
    val themePref: String = PREF_THEME_SYSTEM,
    /** METRIC | IMPERIAL */
    val unitPref: String = PREF_UNIT_METRIC,
) {
    companion object {
        const val PREF_LANGUAGE_SYSTEM = "SYSTEM"
        const val PREF_LANGUAGE_TR = "TR"
        const val PREF_LANGUAGE_EN = "EN"

        const val PREF_THEME_SYSTEM = "SYSTEM"
        const val PREF_THEME_LIGHT = "LIGHT"
        const val PREF_THEME_DARK = "DARK"

        const val PREF_UNIT_METRIC = "METRIC"
        const val PREF_UNIT_IMPERIAL = "IMPERIAL"
    }
}
