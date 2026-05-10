@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.semihtakilan.stayalive.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedTextField as M3OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.semihtakilan.stayalive.R
import com.semihtakilan.stayalive.data.preferences.UserPreferences
import com.semihtakilan.stayalive.ui.components.WrapRow
import com.semihtakilan.stayalive.ui.theme.StayAliveTheme

private const val LBS_PER_KG = 2.20462f
private const val CM_PER_INCH = 2.54f

@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    onAgeChange: (Int) -> Unit,
    onWeightKgChange: (Float) -> Unit,
    onHeightCmChange: (Float) -> Unit,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
) {
    val imperial = preferences.unitPref == UserPreferences.PREF_UNIT_IMPERIAL
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    var ageText by remember(preferences.age) {
        mutableStateOf(if (preferences.age > 0) preferences.age.toString() else "")
    }
    var weightText by remember(preferences.weightKg, preferences.unitPref) {
        mutableStateOf(formatWeightForDisplay(preferences.weightKg, imperial))
    }
    var heightText by remember(preferences.heightCm, preferences.unitPref) {
        mutableStateOf(formatHeightForDisplay(preferences.heightCm, imperial))
    }

    LaunchedEffect(preferences.age, preferences.weightKg, preferences.heightCm, preferences.unitPref) {
        ageText = if (preferences.age > 0) preferences.age.toString() else ""
        weightText = formatWeightForDisplay(preferences.weightKg, imperial)
        heightText = formatHeightForDisplay(preferences.heightCm, imperial)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = stringResource(R.string.settings_profile_card),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CommittingOutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it.filter { ch -> ch.isDigit() }.take(3) },
                    label = { Text(stringResource(R.string.settings_age_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    onBlurOrDone = {
                        onAgeChange(ageText.toIntOrNull()?.coerceIn(0, 130) ?: 0)
                    },
                )
                CommittingOutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = {
                        Text(
                            if (imperial) stringResource(R.string.settings_weight_label_imperial)
                            else stringResource(R.string.settings_weight_label_metric),
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    onBlurOrDone = {
                        val parsed = weightText.replace(',', '.').toFloatOrNull() ?: return@CommittingOutlinedTextField
                        val kg = if (imperial) parsed / LBS_PER_KG else parsed
                        onWeightKgChange(kg.coerceAtLeast(0f))
                    },
                )
                CommittingOutlinedTextField(
                    value = heightText,
                    onValueChange = { heightText = it },
                    label = {
                        Text(
                            if (imperial) stringResource(R.string.settings_height_label_imperial)
                            else stringResource(R.string.settings_height_label_metric),
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    onBlurOrDone = {
                        val parsed = heightText.replace(',', '.').toFloatOrNull() ?: return@CommittingOutlinedTextField
                        val cm = if (imperial) parsed * CM_PER_INCH else parsed
                        onHeightCmChange(cm.coerceAtLeast(0f))
                    },
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = stringResource(R.string.settings_prefs_card),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = stringResource(R.string.settings_language_label),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                PreferenceRow(
                    title = stringResource(R.string.settings_language_label),
                    value = stringResource(languageLabelRes(preferences.languagePref)),
                    onClick = { showLanguageDialog = true },
                )

                Text(
                    text = stringResource(R.string.settings_theme_label),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                PreferenceRow(
                    title = stringResource(R.string.settings_theme_label),
                    value = stringResource(themeLabelRes(preferences.themePref)),
                    onClick = { showThemeDialog = true },
                )

                Text(
                    text = stringResource(R.string.settings_units_label),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                WrapRow(horizontalSpacing = 8.dp, verticalSpacing = 8.dp) {
                    unitOptions().forEach { opt ->
                        FilterChip(
                            selected = preferences.unitPref == opt.value,
                            onClick = { onUnitChange(opt.value) },
                            label = { Text(stringResource(opt.labelRes)) },
                        )
                    }
                }
            }
        }
    }

    if (showLanguageDialog) {
        OptionDialog(
            title = stringResource(R.string.settings_language_label),
            options = languageOptions(),
            selectedValue = sanitizeLanguagePref(preferences.languagePref),
            onDismiss = { showLanguageDialog = false },
            onSelect = {
                onLanguageChange(it)
                showLanguageDialog = false
            },
        )
    }
    if (showThemeDialog) {
        OptionDialog(
            title = stringResource(R.string.settings_theme_label),
            options = themeOptions(),
            selectedValue = sanitizeThemePref(preferences.themePref),
            onDismiss = { showThemeDialog = false },
            onSelect = {
                onThemeChange(it)
                showThemeDialog = false
            },
        )
    }
}

private data class PrefOption(val value: String, val labelRes: Int)

private fun languageOptions() = listOf(
    PrefOption(UserPreferences.PREF_LANGUAGE_TR, R.string.option_language_tr),
    PrefOption(UserPreferences.PREF_LANGUAGE_EN, R.string.option_language_en),
)

private fun themeOptions() = listOf(
    PrefOption(UserPreferences.PREF_THEME_LIGHT, R.string.option_theme_light),
    PrefOption(UserPreferences.PREF_THEME_DARK, R.string.option_theme_dark),
)

private fun unitOptions() = listOf(
    PrefOption(UserPreferences.PREF_UNIT_METRIC, R.string.option_unit_metric),
    PrefOption(UserPreferences.PREF_UNIT_IMPERIAL, R.string.option_unit_imperial),
)

private fun formatWeightForDisplay(weightKg: Float, imperial: Boolean): String {
    if (weightKg <= 0f) return ""
    return if (imperial) {
        "%.1f".format(weightKg * LBS_PER_KG)
    } else {
        "%.1f".format(weightKg)
    }
}

private fun formatHeightForDisplay(heightCm: Float, imperial: Boolean): String {
    if (heightCm <= 0f) return ""
    return if (imperial) {
        "%.1f".format(heightCm / CM_PER_INCH)
    } else {
        "%.1f".format(heightCm)
    }
}

private fun sanitizeLanguagePref(value: String): String {
    return when (value) {
        UserPreferences.PREF_LANGUAGE_TR,
        UserPreferences.PREF_LANGUAGE_EN,
        -> value

        else -> UserPreferences.PREF_LANGUAGE_EN
    }
}

private fun sanitizeThemePref(value: String): String {
    return when (value) {
        UserPreferences.PREF_THEME_LIGHT,
        UserPreferences.PREF_THEME_DARK,
        -> value

        else -> UserPreferences.PREF_THEME_LIGHT
    }
}

private fun languageLabelRes(value: String): Int {
    return when (sanitizeLanguagePref(value)) {
        UserPreferences.PREF_LANGUAGE_TR -> R.string.option_language_tr
        else -> R.string.option_language_en
    }
}

private fun themeLabelRes(value: String): Int {
    return when (sanitizeThemePref(value)) {
        UserPreferences.PREF_THEME_DARK -> R.string.option_theme_dark
        else -> R.string.option_theme_light
    }
}

@Composable
private fun PreferenceRow(
    title: String,
    value: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "$value  >",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun OptionDialog(
    title: String,
    options: List<PrefOption>,
    selectedValue: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 2.dp,
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                options.forEach { opt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(opt.value) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedValue == opt.value,
                            onClick = { onSelect(opt.value) },
                        )
                        Text(
                            text = stringResource(opt.labelRes),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

/** Commits [onBlurOrDone] when focus leaves the field (simple alternative to an explicit Save action). */
@Composable
private fun CommittingOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    keyboardOptions: KeyboardOptions,
    singleLine: Boolean,
    modifier: Modifier = Modifier,
    onBlurOrDone: () -> Unit,
) {
    var hadFocus by remember { mutableStateOf(false) }
    M3OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        modifier = modifier.onFocusChanged { state ->
            if (state.isFocused) {
                hadFocus = true
            } else if (hadFocus) {
                hadFocus = false
                onBlurOrDone()
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() {
    StayAliveTheme {
        SettingsScreen(
            preferences = UserPreferences(age = 42, weightKg = 72f, heightCm = 178f),
            onAgeChange = {},
            onWeightKgChange = {},
            onHeightCmChange = {},
            onLanguageChange = {},
            onThemeChange = {},
            onUnitChange = {},
        )
    }
}
