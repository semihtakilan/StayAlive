package com.semihtakilan.stayalive.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semihtakilan.stayalive.R
import com.semihtakilan.stayalive.data.local.Measurement
import com.semihtakilan.stayalive.data.repository.MeasurementRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class InsightsUiState(
    /** Oldest → newest for chart X order. */
    val measurementsAscending: List<Measurement> = emptyList(),
)

class InsightsViewModel(
    private val measurementRepository: MeasurementRepository,
) : ViewModel() {

    val uiState: StateFlow<InsightsUiState> =
        measurementRepository.measurements
            .map { list -> InsightsUiState(measurementsAscending = list.asReversed()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = InsightsUiState(),
            )

    /**
     * Builds a UTF-8 CSV and opens the system share sheet. Must be called with an Activity [context].
     */
    fun exportCsvAndShare(context: Context) {
        viewModelScope.launch {
            val rows = withContext(Dispatchers.IO) {
                measurementRepository.getAllDescending()
            }
            val csv = buildCsv(rows)
            val file = withContext(Dispatchers.IO) {
                File(context.cacheDir, "bp_export_${System.currentTimeMillis()}.csv").also {
                    it.writeText(csv, Charsets.UTF_8)
                }
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(send, context.getString(R.string.export_chooser_title))
            context.startActivity(chooser)
        }
    }

    private fun buildCsv(measurements: List<Measurement>): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val header = "id,iso_local_datetime,sys_mmhg,dia_mmhg,pulse_bpm,tags_keys"
        val body = measurements.joinToString("\n") { m ->
            val tags = m.tags.joinToString("|")
            listOf(
                m.id.toString(),
                sdf.format(Date(m.timestamp)),
                m.sys.toString(),
                m.dia.toString(),
                m.pulse.toString(),
                "\"$tags\"",
            ).joinToString(",")
        }
        return if (body.isEmpty()) header else "$header\n$body"
    }
}
