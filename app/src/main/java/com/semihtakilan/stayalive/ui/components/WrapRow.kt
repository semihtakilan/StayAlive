package com.semihtakilan.stayalive.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * Wraps children to the next line when they exceed [maxWidth]. Avoids [androidx.compose.foundation.layout.FlowRow],
 * which can trigger NoSuchMethodError when compile-time and runtime Compose Foundation versions diverge.
 */
@Composable
fun WrapRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    content: @Composable () -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val maxWidth = constraints.maxWidth
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val measurables = subcompose("wrap", content)
        val placeables = measurables.map { it.measure(loose) }
        if (placeables.isEmpty()) {
            return@SubcomposeLayout layout(constraints.minWidth.coerceAtMost(maxWidth), 0) {}
        }
        val hGapPx = horizontalSpacing.roundToPx()
        val vGapPx = verticalSpacing.roundToPx()
        var x = 0
        var y = 0
        var rowHeight = 0
        val xs = IntArray(placeables.size)
        val ys = IntArray(placeables.size)
        placeables.forEachIndexed { index, placeable ->
            if (x > 0 && x + placeable.width > maxWidth) {
                x = 0
                y += rowHeight + vGapPx
                rowHeight = 0
            }
            xs[index] = x
            ys[index] = y
            rowHeight = max(rowHeight, placeable.height)
            x += placeable.width + hGapPx
        }
        val totalHeight = y + rowHeight
        val outWidth = maxWidth.coerceIn(constraints.minWidth, constraints.maxWidth)
        val outHeight = totalHeight.coerceIn(constraints.minHeight, constraints.maxHeight)
        layout(outWidth, outHeight) {
            placeables.forEachIndexed { index, placeable ->
                placeable.place(xs[index], ys[index])
            }
        }
    }
}
