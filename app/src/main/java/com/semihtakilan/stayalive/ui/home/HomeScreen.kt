@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.semihtakilan.stayalive.ui.home

import android.text.format.DateFormat as AndroidDateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.semihtakilan.stayalive.R
import com.semihtakilan.stayalive.data.local.Measurement
import com.semihtakilan.stayalive.domain.MeasurementTagKeys
import com.semihtakilan.stayalive.domain.WhoCategory
import com.semihtakilan.stayalive.domain.WhoHypertensionHelper
import com.semihtakilan.stayalive.ui.components.WrapRow
import com.semihtakilan.stayalive.ui.components.measurementTagLabel
import com.semihtakilan.stayalive.ui.theme.StayAliveTheme
import com.semihtakilan.stayalive.ui.theme.currentAppLocale
import com.semihtakilan.stayalive.ui.viewmodel.BpEntryField
import com.semihtakilan.stayalive.ui.viewmodel.HomeUiState
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

@Composable
fun HomeScreen(
    state: HomeUiState,
    onOpenAdd: () -> Unit,
    onOpenEdit: (Measurement) -> Unit,
    onDismissSheet: () -> Unit,
    onFieldChange: (BpEntryField) -> Unit,
    onDigit: (Int) -> Unit,
    onDeleteDigit: () -> Unit,
    onDeleteMeasurement: () -> Unit,
    onToggleTag: (String) -> Unit,
    onDraftTimestampChange: (Long) -> Unit,
    onClearSaveError: () -> Unit,
    onSave: () -> Boolean,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val invalidMeasurementMessage = stringResource(R.string.error_invalid_measurement)
    val saveErrorRes = state.saveErrorMessageRes
    val saveErrorText = saveErrorRes?.let { stringResource(it) }
    LaunchedEffect(saveErrorRes, saveErrorText) {
        if (saveErrorRes != null && saveErrorText != null) {
            snackbarHostState.showSnackbar(saveErrorText)
            onClearSaveError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenAdd,
                shape = RoundedCornerShape(28.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.home_add_measurement),
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.home_add_measurement),
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 8.dp),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SummaryCard(state = state)
            }
            item {
                Text(
                    text = stringResource(R.string.home_history_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            if (state.measurements.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.home_empty_history),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                items(state.measurements, key = { it.id }) { row ->
                    MeasurementHistoryRow(
                        measurement = row,
                        onClick = { onOpenEdit(row) },
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(88.dp)) }
        }
    }

    if (state.addSheetVisible) {
        Dialog(
            onDismissRequest = onDismissSheet,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
            ) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .heightIn(max = 720.dp),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    shadowElevation = 12.dp,
                ) {
                    AddMeasurementSheet(
                        state = state,
                        onFieldChange = onFieldChange,
                        onDigit = onDigit,
                        onDeleteDigit = onDeleteDigit,
                        onDeleteMeasurement = onDeleteMeasurement,
                        onToggleTag = onToggleTag,
                        onDraftTimestampChange = onDraftTimestampChange,
                        onCancel = onDismissSheet,
                        onSave = {
                            val ok = onSave()
                            if (!ok) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(message = invalidMeasurementMessage)
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(state: HomeUiState) {
    val category = state.summaryCategory
    val accent = category?.let { WhoHypertensionHelper.categoryColor(it) }
    val lightCards = MaterialTheme.colorScheme.surface == Color.White

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (lightCards) 3.dp else 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.home_summary_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (state.summarySys != null && state.summaryDia != null && state.summaryPulse != null) {
                val subtitle = if (state.summaryFromSevenDayAverage) {
                    stringResource(R.string.home_summary_avg_7d)
                } else {
                    stringResource(R.string.home_summary_latest)
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(
                            R.string.bp_sys_dia_only,
                            state.summarySys,
                            state.summaryDia,
                        ),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (accent != null) {
                        WhoStatusDot(color = accent)
                    }
                }
                Text(
                    text = stringResource(
                        R.string.field_value_pair,
                        stringResource(R.string.bp_pulse_short),
                        state.summaryPulse.toString(),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = stringResource(R.string.home_summary_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun WhoStatusDot(color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .shadow(8.dp, CircleShape, ambientColor = color.copy(alpha = 0.45f), spotColor = color)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun MeasurementHistoryRow(
    measurement: Measurement,
    onClick: () -> Unit,
) {
    val category = WhoHypertensionHelper.category(measurement.sys, measurement.dia)
    val strip = WhoHypertensionHelper.categoryColor(category)
    val locale = currentAppLocale()
    val dateFmt = remember(locale) { DateFormat.getDateInstance(DateFormat.MEDIUM, locale) }
    val timeFmt = remember(locale) { DateFormat.getTimeInstance(DateFormat.SHORT, locale) }
    val date = Date(measurement.timestamp)
    val lightCards = MaterialTheme.colorScheme.surface == Color.White

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (lightCards) 3.dp else 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val w = 6.dp.toPx()
                    val r = 3.dp.toPx()
                    drawRoundRect(
                        color = strip,
                        topLeft = Offset.Zero,
                        size = Size(w, size.height),
                        cornerRadius = CornerRadius(r, r),
                    )
                }
                .padding(start = 24.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(
                    R.string.insights_datetime_join,
                    dateFmt.format(date),
                    timeFmt.format(date),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    R.string.bp_sys_dia_only,
                    measurement.sys,
                    measurement.dia,
                ),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(
                    R.string.field_value_pair,
                    stringResource(R.string.bp_pulse_short),
                    measurement.pulse.toString(),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (measurement.tags.isNotEmpty()) {
                WrapRow(
                    horizontalSpacing = 8.dp,
                    verticalSpacing = 8.dp,
                ) {
                    measurement.tags.forEach { key ->
                        TagCapsule(text = measurementTagLabel(key))
                    }
                }
            }
        }
    }
}

@Composable
private fun TagCapsule(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AddMeasurementSheet(
    state: HomeUiState,
    onFieldChange: (BpEntryField) -> Unit,
    onDigit: (Int) -> Unit,
    onDeleteDigit: () -> Unit,
    onDeleteMeasurement: () -> Unit,
    onToggleTag: (String) -> Unit,
    onDraftTimestampChange: (Long) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val locale = currentAppLocale()
    val dateFmt = remember(locale) { DateFormat.getDateInstance(DateFormat.MEDIUM, locale) }
    val timeFmt = remember(locale) { DateFormat.getTimeInstance(DateFormat.SHORT, locale) }
    val draftDate = remember(state.draftTimestampMillis) { Date(state.draftTimestampMillis) }
    val timestampPillLabel = remember(draftDate, dateFmt, timeFmt) {
        "${dateFmt.format(draftDate)} • ${timeFmt.format(draftDate)}"
    }

    if (showDatePicker) {
        MedicalDatePickerDialog(
            draftMillis = state.draftTimestampMillis,
            onDismiss = { showDatePicker = false },
            onConfirm = { mergedMillis ->
                onDraftTimestampChange(mergedMillis)
                showDatePicker = false
                showTimePicker = true
            },
        )
    }

    if (showTimePicker) {
        MedicalTimePickerDialog(
            draftMillis = state.draftTimestampMillis,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                onDraftTimestampChange(
                    mergeTimeOfDay(state.draftTimestampMillis, hour, minute),
                )
                showTimePicker = false
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.align(Alignment.CenterStart),
            ) {
                Text(stringResource(R.string.action_cancel))
            }
            Text(
                text = if (state.editMode) {
                    stringResource(R.string.sheet_edit_title)
                } else {
                    stringResource(R.string.sheet_add_title)
                },
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        val dash = stringResource(R.string.placeholder_empty)
        val measurementTimeContentDescription = stringResource(R.string.sheet_measurement_time)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BpValueFieldCard(
                label = stringResource(R.string.bp_sys_short),
                value = state.draftSys.ifEmpty { dash },
                selected = state.activeField == BpEntryField.SYS,
                onClick = { onFieldChange(BpEntryField.SYS) },
                modifier = Modifier.weight(1f),
            )
            BpValueFieldCard(
                label = stringResource(R.string.bp_dia_short),
                value = state.draftDia.ifEmpty { dash },
                selected = state.activeField == BpEntryField.DIA,
                onClick = { onFieldChange(BpEntryField.DIA) },
                modifier = Modifier.weight(1f),
            )
            BpValueFieldCard(
                label = stringResource(R.string.bp_pulse_short),
                value = state.draftPulse.ifEmpty { dash },
                selected = state.activeField == BpEntryField.PULSE,
                onClick = { onFieldChange(BpEntryField.PULSE) },
                modifier = Modifier.weight(1f),
            )
        }

        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .semantics {
                    contentDescription = "$measurementTimeContentDescription: $timestampPillLabel"
                },
            shape = RoundedCornerShape(50),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.EditCalendar,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = timestampPillLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }

        Numpad(
            onDigit = onDigit,
            onDelete = onDeleteDigit,
        )

        Text(
            text = stringResource(R.string.insights_tags_label),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(MeasurementTagKeys.ALL, key = { it }) { key ->
                val selected = state.selectedTagKeys.contains(key)
                FilterChip(
                    selected = selected,
                    onClick = { onToggleTag(key) },
                    label = { Text(measurementTagLabel(key)) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                    ),
                    border = null,
                )
            }
        }

        if (state.editMode) {
            OutlinedButton(
                onClick = onDeleteMeasurement,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.06f),
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(
                    text = stringResource(R.string.action_delete),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(
                text = if (state.editMode) {
                    stringResource(R.string.action_update)
                } else {
                    stringResource(R.string.action_save)
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun BpValueFieldCard(
    label: String,
    value: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val borderColor = if (selected) scheme.primary else scheme.outline.copy(alpha = 0.25f)
    val fill = if (selected) scheme.primary.copy(alpha = 0.14f) else scheme.surfaceVariant.copy(alpha = 0.65f)
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 96.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = fill),
        border = BorderStroke(width = if (selected) 2.dp else 1.dp, color = borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 2.dp else 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) scheme.primary else scheme.onSurfaceVariant,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (selected) scheme.onSurface else scheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

private fun mergeTimeOfDay(draftMs: Long, hour: Int, minute: Int): Long {
    return Calendar.getInstance().apply {
        timeInMillis = draftMs
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

@Composable
private fun Numpad(
    onDigit: (Int) -> Unit,
    onDelete: () -> Unit,
) {
    val deleteDescription = stringResource(R.string.content_desc_numpad_delete)
    val zeroDescription = stringResource(R.string.content_desc_numpad_digit, 0)
    val rows = listOf(
        listOf(1, 2, 3),
        listOf(4, 5, 6),
        listOf(7, 8, 9),
    )
    val gap = 8.dp
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap),
            ) {
                row.forEach { d ->
                    val desc = stringResource(R.string.content_desc_numpad_digit, d)
                    NumpadKey(
                        label = stringResource(R.string.fmt_numpad_digit, d),
                        onClick = { onDigit(d) },
                        contentDescription = desc,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            NumpadKey(
                label = stringResource(R.string.fmt_numpad_digit, 0),
                onClick = { onDigit(0) },
                contentDescription = zeroDescription,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
            )
            NumpadKey(
                label = stringResource(R.string.numpad_delete),
                onClick = onDelete,
                contentDescription = deleteDescription,
                isDelete = true,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
            )
        }
    }
}

@Composable
private fun NumpadKey(
    label: String,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    isDelete: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.semantics { this.contentDescription = contentDescription },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = if (isDelete) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.displaySmall
                },
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    StayAliveTheme {
        HomeScreen(
            state = HomeUiState(
                measurements = listOf(
                    Measurement(
                        id = 1,
                        sys = 128,
                        dia = 82,
                        pulse = 72,
                        timestamp = System.currentTimeMillis(),
                        tags = listOf(MeasurementTagKeys.MORNING),
                    ),
                ),
                summarySys = 125,
                summaryDia = 80,
                summaryPulse = 70,
                summaryFromSevenDayAverage = true,
                summaryCategory = WhoCategory.Elevated,
            ),
            onOpenAdd = {},
            onOpenEdit = {},
            onDismissSheet = {},
            onFieldChange = {},
            onDigit = {},
            onDeleteDigit = {},
            onDeleteMeasurement = {},
            onToggleTag = {},
            onDraftTimestampChange = {},
            onClearSaveError = {},
            onSave = { true },
        )
    }
}
