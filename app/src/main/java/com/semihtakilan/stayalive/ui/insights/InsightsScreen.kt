package com.semihtakilan.stayalive.ui.insights

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisTickComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.semihtakilan.stayalive.R
import com.semihtakilan.stayalive.data.local.Measurement
import com.semihtakilan.stayalive.domain.MeasurementTagKeys
import com.semihtakilan.stayalive.domain.WhoCategory
import com.semihtakilan.stayalive.domain.WhoHypertensionHelper
import com.semihtakilan.stayalive.ui.components.WrapRow
import com.semihtakilan.stayalive.ui.components.measurementTagLabel
import com.semihtakilan.stayalive.ui.theme.StayAliveTheme
import com.semihtakilan.stayalive.ui.theme.currentAppLocale
import com.semihtakilan.stayalive.ui.viewmodel.InsightsUiState
import java.text.DateFormat
import java.util.Date

private const val BandAlpha = 0.05f

private val ChartYRangeProvider = object : CartesianLayerRangeProvider {
    override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double =
        WhoHypertensionHelper.CHART_Y_MIN.toDouble()

    override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double =
        WhoHypertensionHelper.CHART_Y_MAX.toDouble()
}

@Composable
fun InsightsScreen(
    state: InsightsUiState,
    onExportCsv: (Context) -> Unit,
) {
    val scroll = rememberScrollState()
    val ctx = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scroll)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.insights_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.insights_chart_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.insights_chart_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (state.measurementsAscending.isEmpty()) {
            Text(
                text = stringResource(R.string.insights_chart_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            WhoChartCard(measurements = state.measurementsAscending)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Button(
                onClick = { onExportCsv(ctx) },
                modifier = Modifier
                    .wrapContentWidth()
                    .height(50.dp)
                    .padding(horizontal = 8.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp),
            ) {
                Text(
                    text = stringResource(R.string.insights_export_csv),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun WhoChartCard(measurements: List<Measurement>) {
    val highlightIndexState = remember { mutableIntStateOf(-1) }
    LaunchedEffect(measurements) {
        highlightIndexState.intValue = -1
    }

    val locale = currentAppLocale()
    val dateFmt = remember(locale) { DateFormat.getDateInstance(DateFormat.SHORT, locale) }
    val timeFmt = remember(locale) { DateFormat.getTimeInstance(DateFormat.SHORT, locale) }

    val twoLineFormat = stringResource(R.string.chart_axis_two_lines)
    val mmHgSuffix = stringResource(R.string.chart_axis_suffix_mmhg)

    val bottomFormatter = remember(measurements, twoLineFormat, locale, dateFmt, timeFmt) {
        CartesianValueFormatter { _, value, _ ->
            val i = value.toInt().coerceIn(measurements.indices)
            val d = Date(measurements[i].timestamp)
            String.format(
                locale,
                twoLineFormat,
                dateFmt.format(d),
                timeFmt.format(d),
            )
        }
    }

    val markerFormatter = remember(mmHgSuffix) {
        DefaultCartesianMarker.ValueFormatter.default(suffix = mmHgSuffix)
    }

    val markerLabel = rememberTextComponent(style = MaterialTheme.typography.labelMedium)

    val visibilityListener = remember(measurements) {
        object : CartesianMarkerVisibilityListener {
            override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                updateIndex(targets, measurements, highlightIndexState)
            }

            override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                updateIndex(targets, measurements, highlightIndexState)
            }

            override fun onHidden(marker: CartesianMarker) {
                highlightIndexState.intValue = -1
            }
        }
    }

    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(measurements) {
        if (measurements.isEmpty()) return@LaunchedEffect
        modelProducer.runTransaction {
            lineSeries {
                series(measurements.map { it.sys.toFloat() })
                series(measurements.map { it.dia.toFloat() })
            }
        }
    }

    val sysColor = MaterialTheme.colorScheme.primary
    val diaColor = MaterialTheme.colorScheme.tertiary
    val outlineMuted = MaterialTheme.colorScheme.outline.copy(alpha = 0.07f)
    val axisLineFill = remember(outlineMuted) { Fill(outlineMuted) }
    val splitYMin: (ExtraStore) -> Number = { WhoHypertensionHelper.CHART_Y_MIN }

    val lineLayer = rememberLineCartesianLayer(
        lineProvider = LineCartesianLayer.LineProvider.series(
            listOf(
                LineCartesianLayer.rememberLine(
                    fill = LineCartesianLayer.LineFill.single(Fill(sysColor)),
                    stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 3.dp),
                    areaFill = LineCartesianLayer.AreaFill.single(
                        fill = Fill(
                            Brush.verticalGradient(
                                colors = listOf(sysColor.copy(alpha = 0.30f), Color.Transparent),
                            ),
                        ),
                        splitY = splitYMin,
                    ),
                ),
                LineCartesianLayer.rememberLine(
                    fill = LineCartesianLayer.LineFill.single(Fill(diaColor)),
                    stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 3.dp),
                    areaFill = LineCartesianLayer.AreaFill.single(
                        fill = Fill(
                            Brush.verticalGradient(
                                colors = listOf(diaColor.copy(alpha = 0.28f), Color.Transparent),
                            ),
                        ),
                        splitY = splitYMin,
                    ),
                ),
            ),
        ),
        rangeProvider = ChartYRangeProvider,
    )

    val chart = rememberCartesianChart(
        lineLayer,
        startAxis = VerticalAxis.rememberStart(
            valueFormatter = CartesianValueFormatter.decimal(suffix = mmHgSuffix),
            line = rememberAxisLineComponent(fill = axisLineFill, thickness = 1.dp),
            tick = rememberAxisTickComponent(fill = axisLineFill, thickness = 1.dp),
            guideline = rememberAxisGuidelineComponent(fill = axisLineFill, thickness = 1.dp),
        ),
        bottomAxis = HorizontalAxis.rememberBottom(
            valueFormatter = bottomFormatter,
            labelRotationDegrees = 35f,
            line = rememberAxisLineComponent(fill = axisLineFill, thickness = 1.dp),
            tick = rememberAxisTickComponent(fill = axisLineFill, thickness = 1.dp),
            guideline = rememberAxisGuidelineComponent(fill = axisLineFill, thickness = 1.dp),
        ),
        marker = rememberDefaultCartesianMarker(
            label = markerLabel,
            valueFormatter = markerFormatter,
        ),
        markerVisibilityListener = visibilityListener,
    )

    val lightCards = MaterialTheme.colorScheme.surface == Color.White
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (lightCards) 3.dp else 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                LegendDot(color = MaterialTheme.colorScheme.primary, label = stringResource(R.string.insights_legend_sys))
                LegendDot(color = MaterialTheme.colorScheme.tertiary, label = stringResource(R.string.insights_legend_dia))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
            ) {
                WhoBackgroundBands()
                CartesianChartHost(
                    chart = chart,
                    modelProducer = modelProducer,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            val idx = highlightIndexState.intValue
            if (idx in measurements.indices) {
                Spacer(modifier = Modifier.height(12.dp))
                SelectedPointCard(measurement = measurements[idx])
            }
        }
    }
}

private fun updateIndex(
    targets: List<CartesianMarker.Target>,
    measurements: List<Measurement>,
    indexState: androidx.compose.runtime.MutableIntState,
) {
    val lineTarget = targets.firstOrNull { it is LineCartesianLayerMarkerTarget } as? LineCartesianLayerMarkerTarget
    val x = lineTarget?.points?.firstOrNull()?.entry?.x ?: return
    val i = x.toInt().coerceIn(0, (measurements.size - 1).coerceAtLeast(0))
    indexState.intValue = i
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun WhoBackgroundBands() {
    val min = WhoHypertensionHelper.CHART_Y_MIN
    val max = WhoHypertensionHelper.CHART_Y_MAX
    val span = max - min
    val wRed = (max - 140f) / span
    val wOrange = (140f - 130f) / span
    val wYellow = (130f - 120f) / span
    val wGreen = (120f - min) / span

    fun bandColor(category: WhoCategory): Color =
        WhoHypertensionHelper.categoryColor(category).copy(alpha = BandAlpha)

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(wRed)
                .background(bandColor(WhoCategory.Stage2Crisis)),
        )
        Box(
            Modifier
                .fillMaxWidth()
                .weight(wOrange)
                .background(bandColor(WhoCategory.Stage1)),
        )
        Box(
            Modifier
                .fillMaxWidth()
                .weight(wYellow)
                .background(bandColor(WhoCategory.Elevated)),
        )
        Box(
            Modifier
                .fillMaxWidth()
                .weight(wGreen)
                .background(bandColor(WhoCategory.Normal)),
        )
    }
}

@Composable
private fun SelectedPointCard(measurement: Measurement) {
    val locale = currentAppLocale()
    val dateFmt = remember(locale) { DateFormat.getDateInstance(DateFormat.MEDIUM, locale) }
    val timeFmt = remember(locale) { DateFormat.getTimeInstance(DateFormat.SHORT, locale) }
    val d = Date(measurement.timestamp)
    val category = WhoHypertensionHelper.category(measurement.sys, measurement.dia)
    val accent = WhoHypertensionHelper.categoryColor(category)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.drawBehind {
                    val w = 6.dp.toPx()
                    val r = 3.dp.toPx()
                    drawRoundRect(
                        color = accent,
                        topLeft = Offset.Zero,
                        size = Size(w, size.height),
                        cornerRadius = CornerRadius(r, r),
                    )
                }
                    .padding(start = 14.dp),
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.insights_selected_header),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(
                            R.string.insights_datetime_join,
                            dateFmt.format(d),
                            timeFmt.format(d),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = stringResource(
                    R.string.bp_sys_dia_only,
                    measurement.sys,
                    measurement.dia,
                ),
                style = MaterialTheme.typography.headlineMedium,
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
                Text(
                    text = stringResource(R.string.insights_tags_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                WrapRow(
                    horizontalSpacing = 8.dp,
                    verticalSpacing = 8.dp,
                ) {
                    measurement.tags.forEach { key ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        ) {
                            Text(
                                text = measurementTagLabel(key),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InsightsPreview() {
    StayAliveTheme {
        InsightsScreen(
            state = InsightsUiState(
                measurementsAscending = listOf(
                    Measurement(
                        id = 1,
                        sys = 118,
                        dia = 76,
                        pulse = 70,
                        timestamp = System.currentTimeMillis() - 86_400_000L,
                        tags = listOf(MeasurementTagKeys.MORNING),
                    ),
                    Measurement(
                        id = 2,
                        sys = 132,
                        dia = 84,
                        pulse = 78,
                        timestamp = System.currentTimeMillis(),
                        tags = listOf(MeasurementTagKeys.STRESS),
                    ),
                ),
            ),
            onExportCsv = {},
        )
    }
}
