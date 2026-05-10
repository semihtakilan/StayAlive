@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.semihtakilan.stayalive.ui.home

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.view.ContextThemeWrapper
import android.text.format.DateFormat as AndroidDateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.semihtakilan.stayalive.R
import java.util.Calendar

/** Preserves hour/minute/second from [draftMillis], applies Y/M/D from selected date millis. */
internal fun mergeSelectedDateIntoDraft(draftMillis: Long, selectedDateMillis: Long): Long {
    val preserved = Calendar.getInstance().apply { timeInMillis = draftMillis }
    val picked = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    preserved.set(Calendar.YEAR, picked.get(Calendar.YEAR))
    preserved.set(Calendar.MONTH, picked.get(Calendar.MONTH))
    preserved.set(Calendar.DAY_OF_MONTH, picked.get(Calendar.DAY_OF_MONTH))
    return preserved.timeInMillis
}

@Composable
internal fun MedicalDatePickerDialog(
    draftMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    val context = LocalContext.current
    val latestDismiss = rememberUpdatedState(onDismiss)
    val latestConfirm = rememberUpdatedState(onConfirm)
    DisposableEffect(context, draftMillis) {
        val themedContext = ContextThemeWrapper(
            context.findHostActivity(),
            R.style.ThemeOverlay_StayAlive_DatePicker,
        )
        val initial = Calendar.getInstance().apply { timeInMillis = draftMillis }
        val dialog = DatePickerDialog(
            themedContext,
            { _, year, monthOfYear, dayOfMonth ->
                val picked = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, monthOfYear)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                latestConfirm.value(mergeSelectedDateIntoDraft(draftMillis, picked.timeInMillis))
            },
            initial.get(Calendar.YEAR),
            initial.get(Calendar.MONTH),
            initial.get(Calendar.DAY_OF_MONTH),
        )
        dialog.setOnDismissListener { latestDismiss.value() }
        dialog.show()
        onDispose {
            dialog.setOnDismissListener(null)
            if (dialog.isShowing) dialog.dismiss()
        }
    }
}

@Composable
internal fun MedicalTimePickerDialog(
    draftMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    val cal = Calendar.getInstance().apply { timeInMillis = draftMillis }
    val timePickerState = rememberTimePickerState(
        initialHour = cal.get(Calendar.HOUR_OF_DAY),
        initialMinute = cal.get(Calendar.MINUTE),
        is24Hour = AndroidDateFormat.is24HourFormat(ctx),
    )
    val timeColors = medicalTimePickerColors(scheme)

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = scheme.surfaceContainerHigh,
            tonalElevation = 2.dp,
            shadowElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = timeColors,
                    layoutType = TimePickerDefaults.layoutType(),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = scheme.primary),
                    ) {
                        Text(stringResource(android.R.string.cancel))
                    }
                    TextButton(
                        onClick = {
                            onConfirm(timePickerState.hour, timePickerState.minute)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = scheme.primary),
                    ) {
                        Text(stringResource(android.R.string.ok))
                    }
                }
            }
        }
    }
}

@Composable
private fun medicalTimePickerColors(scheme: ColorScheme) =
    TimePickerDefaults.colors(
        clockDialColor = scheme.surfaceVariant.copy(alpha = 0.5f),
        clockDialSelectedContentColor = scheme.onPrimary,
        clockDialUnselectedContentColor = scheme.onSurfaceVariant,
        selectorColor = scheme.primary,
        containerColor = Color.Transparent,
        periodSelectorBorderColor = scheme.outline,
        periodSelectorSelectedContainerColor = scheme.primary.copy(alpha = 0.22f),
        periodSelectorUnselectedContainerColor = scheme.surfaceVariant,
        periodSelectorSelectedContentColor = scheme.primary,
        periodSelectorUnselectedContentColor = scheme.onSurfaceVariant,
        timeSelectorSelectedContainerColor = scheme.primary.copy(alpha = 0.22f),
        timeSelectorUnselectedContainerColor = scheme.surfaceVariant,
        timeSelectorSelectedContentColor = scheme.primary,
        timeSelectorUnselectedContentColor = scheme.onSurfaceVariant,
    )

private fun Context.findHostActivity(): Activity {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    error("Date picker requires an Activity context")
}
