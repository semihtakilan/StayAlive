package com.semihtakilan.stayalive.domain

import androidx.compose.ui.graphics.Color

/**
 * WHO-style hypertension categories for adults (simplified thresholds per PRD).
 *
 * Priority: Stage 2 / crisis if either sys or dia crosses the higher threshold,
 * then Stage 1, Elevated, Normal.
 */
enum class WhoCategory {
    Normal,
    Elevated,
    Stage1,
    Stage2Crisis,
}

object WhoHypertensionHelper {

    fun category(sys: Int, dia: Int): WhoCategory {
        if (sys >= 140 || dia >= 90) return WhoCategory.Stage2Crisis
        if (sys in 130..139 || dia in 80..89) return WhoCategory.Stage1
        if (sys in 120..129 && dia < 80) return WhoCategory.Elevated
        return WhoCategory.Normal
    }

    /** Strong contrast colors for status cards and list indicators (Material-friendly). */
    fun categoryColor(category: WhoCategory): Color = when (category) {
        WhoCategory.Normal -> Color(0xFF1B5E20)
        WhoCategory.Elevated -> Color(0xFFF9A825)
        WhoCategory.Stage1 -> Color(0xFFEF6C00)
        WhoCategory.Stage2Crisis -> Color(0xFFC62828)
    }

    /** Softer surface tints for chart WHO bands (still distinguishable). */
    fun categoryBandColor(category: WhoCategory): Color = when (category) {
        WhoCategory.Normal -> Color(0x662E7D32)
        WhoCategory.Elevated -> Color(0x66FDD835)
        WhoCategory.Stage1 -> Color(0x66FB8C00)
        WhoCategory.Stage2Crisis -> Color(0x66E53935)
    }

    /**
     * Chart Y range used for systolic/diastolic plotting and background bands (mmHg).
     */
    const val CHART_Y_MIN = 60f
    const val CHART_Y_MAX = 200f
}
