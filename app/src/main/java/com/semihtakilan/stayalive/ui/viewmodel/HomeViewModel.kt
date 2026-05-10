package com.semihtakilan.stayalive.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semihtakilan.stayalive.R
import com.semihtakilan.stayalive.data.local.Measurement
import com.semihtakilan.stayalive.data.repository.MeasurementRepository
import com.semihtakilan.stayalive.domain.WhoCategory
import com.semihtakilan.stayalive.domain.WhoHypertensionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class BpEntryField {
    SYS,
    DIA,
    PULSE,
}

data class HomeUiState(
    val measurements: List<Measurement> = emptyList(),
    /** Average of last 7 days if any; otherwise latest reading. */
    val summarySys: Int? = null,
    val summaryDia: Int? = null,
    val summaryPulse: Int? = null,
    /** True when the summary averages readings from the last 7 days. */
    val summaryFromSevenDayAverage: Boolean = false,
    val summaryCategory: WhoCategory? = null,
    val addSheetVisible: Boolean = false,
    val activeField: BpEntryField = BpEntryField.SYS,
    val draftSys: String = "",
    val draftDia: String = "",
    val draftPulse: String = "",
    val selectedTagKeys: Set<String> = emptySet(),
    /** Local wall-clock time stored for the draft measurement (persisted on save). */
    val draftTimestampMillis: Long = System.currentTimeMillis(),
    /** One-shot error after a failed DB save; cleared when consumed or sheet reopens. */
    val saveErrorMessageRes: Int? = null,
    val editMode: Boolean = false,
)

private data class AddSheetDraft(
    val editingMeasurementId: Long? = null,
    val visible: Boolean = false,
    val field: BpEntryField = BpEntryField.SYS,
    val sys: String = "",
    val dia: String = "",
    val pulse: String = "",
    val tags: Set<String> = emptySet(),
    val timestampMillis: Long = System.currentTimeMillis(),
)

class HomeViewModel(
    private val measurementRepository: MeasurementRepository,
) : ViewModel() {

    private val addSheet = MutableStateFlow(AddSheetDraft())
    private val saveErrorRes = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        measurementRepository.measurements,
        addSheet,
        saveErrorRes,
    ) { list, sheet, errRes ->
        // DAO already returns ORDER BY timestamp DESC
        val summary = summarizeForCard(list)
        HomeUiState(
            measurements = list,
            summarySys = summary?.sys,
            summaryDia = summary?.dia,
            summaryPulse = summary?.pulse,
            summaryFromSevenDayAverage = summary?.fromSevenDayAverage == true,
            summaryCategory = summary?.let { s ->
                WhoHypertensionHelper.category(s.sys, s.dia)
            },
            addSheetVisible = sheet.visible,
            activeField = sheet.field,
            draftSys = sheet.sys,
            draftDia = sheet.dia,
            draftPulse = sheet.pulse,
            selectedTagKeys = sheet.tags,
            draftTimestampMillis = sheet.timestampMillis,
            saveErrorMessageRes = errRes,
            editMode = sheet.editingMeasurementId != null,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HomeUiState(),
    )

    fun clearSaveError() {
        saveErrorRes.value = null
    }

    fun openAddSheet() {
        saveErrorRes.value = null
        addSheet.value = AddSheetDraft(visible = true, timestampMillis = System.currentTimeMillis())
    }

    fun openEditSheet(measurement: Measurement) {
        saveErrorRes.value = null
        addSheet.value = AddSheetDraft(
            editingMeasurementId = measurement.id,
            visible = true,
            field = BpEntryField.SYS,
            sys = measurement.sys.toString(),
            dia = measurement.dia.toString(),
            pulse = measurement.pulse.toString(),
            tags = measurement.tags.toSet(),
            timestampMillis = measurement.timestamp,
        )
    }

    fun dismissAddSheet() {
        addSheet.update { it.copy(visible = false) }
    }

    fun setActiveField(field: BpEntryField) {
        addSheet.update { it.copy(field = field) }
    }

    fun setDraftTimestampMillis(millis: Long) {
        addSheet.update { it.copy(timestampMillis = millis) }
    }

    fun appendDigit(digit: Int) {
        if (digit !in 0..9) return
        val ch = digit.toString()
        addSheet.update { s ->
            when (s.field) {
                BpEntryField.SYS -> s.copy(sys = appendLimited(s.sys, ch, maxLen = 3))
                BpEntryField.DIA -> s.copy(dia = appendLimited(s.dia, ch, maxLen = 3))
                BpEntryField.PULSE -> s.copy(pulse = appendLimited(s.pulse, ch, maxLen = 3))
            }
        }
    }

    fun deleteLastDigit() {
        addSheet.update { s ->
            when (s.field) {
                BpEntryField.SYS -> s.copy(sys = s.sys.dropLast(1))
                BpEntryField.DIA -> s.copy(dia = s.dia.dropLast(1))
                BpEntryField.PULSE -> s.copy(pulse = s.pulse.dropLast(1))
            }
        }
    }

    fun toggleTagKey(key: String) {
        addSheet.update { s ->
            val next = if (s.tags.contains(key)) s.tags - key else s.tags + key
            s.copy(tags = next)
        }
    }

    /**
     * @return false if validation failed (show a localized error in UI). On success, persists asynchronously and closes the sheet when done.
     */
    fun trySaveDraft(): Boolean {
        val sheet = addSheet.value
        val sys = sheet.sys.toIntOrNull() ?: return false
        val dia = sheet.dia.toIntOrNull() ?: return false
        val pulse = sheet.pulse.toIntOrNull() ?: return false
        if (sys !in 50..300 || dia !in 30..200 || pulse !in 30..220) return false

        viewModelScope.launch {
            try {
                val entity = Measurement(
                    id = sheet.editingMeasurementId ?: 0L,
                    sys = sys,
                    dia = dia,
                    pulse = pulse,
                    timestamp = sheet.timestampMillis,
                    tags = sheet.tags.toList(),
                )
                if (sheet.editingMeasurementId == null) {
                    measurementRepository.insert(entity)
                } else {
                    measurementRepository.update(entity)
                }
                dismissAddSheet()
            } catch (t: Throwable) {
                Log.e(TAG, "Measurement insert failed", t)
                saveErrorRes.value = R.string.error_save_measurement
            }
        }
        return true
    }

    fun deleteEditedMeasurement() {
        val sheet = addSheet.value
        val editId = sheet.editingMeasurementId ?: return
        viewModelScope.launch {
            try {
                measurementRepository.delete(
                    Measurement(
                        id = editId,
                        sys = sheet.sys.toIntOrNull() ?: 0,
                        dia = sheet.dia.toIntOrNull() ?: 0,
                        pulse = sheet.pulse.toIntOrNull() ?: 0,
                        timestamp = sheet.timestampMillis,
                        tags = sheet.tags.toList(),
                    ),
                )
                dismissAddSheet()
            } catch (t: Throwable) {
                Log.e(TAG, "Measurement delete failed", t)
                saveErrorRes.value = R.string.error_save_measurement
            }
        }
    }

    private fun appendLimited(current: String, ch: String, maxLen: Int): String {
        if (current.length >= maxLen) return current
        return current + ch
    }

    private data class CardSummary(
        val sys: Int,
        val dia: Int,
        val pulse: Int,
        val fromSevenDayAverage: Boolean,
    )

    /** [sortedNewestFirst] — list ordered by timestamp descending (newest first). */
    private fun summarizeForCard(sortedNewestFirst: List<Measurement>): CardSummary? {
        if (sortedNewestFirst.isEmpty()) return null
        val now = System.currentTimeMillis()
        val windowMs = 7L * 24 * 60 * 60 * 1000
        val recent = sortedNewestFirst.filter { now - it.timestamp <= windowMs }
        val fromSeven = recent.isNotEmpty()
        val pool = if (fromSeven) recent else listOf(sortedNewestFirst.first())
        val avgSys = pool.map { it.sys }.average().roundToInt()
        val avgDia = pool.map { it.dia }.average().roundToInt()
        val avgPulse = pool.map { it.pulse }.average().roundToInt()
        return CardSummary(avgSys, avgDia, avgPulse, fromSeven)
    }

    private companion object {
        private const val TAG = "StayAlive"
    }
}
