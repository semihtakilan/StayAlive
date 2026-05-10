package com.semihtakilan.stayalive.ui.theme

import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.semihtakilan.stayalive.data.preferences.UserPreferences
import java.util.Locale

/** Locale reflected by [ProvideAppLocale] (matches [LocalConfiguration]). */
@Composable
fun currentAppLocale(): Locale {
    val locales = LocalConfiguration.current.locales
    return if (locales.size() == 0) Locale.getDefault() else locales[0]
}

/**
 * Applies in-app language without [AppCompatDelegate.setApplicationLocales] (no Activity recreate).
 *
 * Compose [androidx.compose.ui.res.stringResource] resolves strings from [LocalContext], not only
 * [LocalConfiguration], so we must provide both a wrapped context and matching configuration.
 */
@Composable
fun ProvideAppLocale(languagePref: String, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val base = LocalConfiguration.current
    val overrideLocale = when (languagePref) {
        UserPreferences.PREF_LANGUAGE_TR -> Locale.forLanguageTag("tr")
        UserPreferences.PREF_LANGUAGE_EN -> Locale.forLanguageTag("en")
        else -> null
    }
    if (overrideLocale == null) {
        content()
        return
    }
    val localizedConfiguration = remember(base, overrideLocale) {
        Configuration(base).apply { setLocales(LocaleList(overrideLocale)) }
    }
    val localizedContext = remember(context, localizedConfiguration) {
        context.createConfigurationContext(localizedConfiguration)
    }
    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfiguration,
    ) {
        content()
    }
}
