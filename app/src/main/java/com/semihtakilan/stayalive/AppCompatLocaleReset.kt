package com.semihtakilan.stayalive

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Clears AndroidX / system per-app locale overrides so this app’s UI is driven only by DataStore +
 * [com.semihtakilan.stayalive.ui.theme.ProvideAppLocale] (no Activity recreate on language change).
 */
fun clearAndroidxApplicationLocaleOverride() {
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
}
