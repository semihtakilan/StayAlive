package com.semihtakilan.stayalive.domain

/**
 * Universal storage keys for measurement tags. Persist these in Room; never localized strings.
 */
object MeasurementTagKeys {
    const val POST_MEDICATION = "TAG_POST_MEDICATION"
    const val HEADACHE = "TAG_HEADACHE"
    const val EXERCISE = "TAG_EXERCISE"
    const val RESTING = "TAG_RESTING"
    const val STRESS = "TAG_STRESS"
    const val AFTER_MEAL = "TAG_AFTER_MEAL"
    const val MORNING = "TAG_MORNING"
    const val EVENING = "TAG_EVENING"

    /** All selectable tags for UI grids (order is display order). */
    val ALL: List<String> = listOf(
        POST_MEDICATION,
        HEADACHE,
        EXERCISE,
        RESTING,
        STRESS,
        AFTER_MEAL,
        MORNING,
        EVENING,
    )
}
