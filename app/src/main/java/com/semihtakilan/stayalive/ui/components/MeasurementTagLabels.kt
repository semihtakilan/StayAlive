package com.semihtakilan.stayalive.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.semihtakilan.stayalive.R
import com.semihtakilan.stayalive.domain.MeasurementTagKeys

/**
 * Maps persisted tag keys to localized labels. Never use this for database writes.
 */
@Composable
fun measurementTagLabel(key: String): String = when (key) {
    MeasurementTagKeys.POST_MEDICATION -> stringResource(R.string.tag_post_medication)
    MeasurementTagKeys.HEADACHE -> stringResource(R.string.tag_headache)
    MeasurementTagKeys.EXERCISE -> stringResource(R.string.tag_exercise)
    MeasurementTagKeys.RESTING -> stringResource(R.string.tag_resting)
    MeasurementTagKeys.STRESS -> stringResource(R.string.tag_stress)
    MeasurementTagKeys.AFTER_MEAL -> stringResource(R.string.tag_after_meal)
    MeasurementTagKeys.MORNING -> stringResource(R.string.tag_morning)
    MeasurementTagKeys.EVENING -> stringResource(R.string.tag_evening)
    else -> key
}
