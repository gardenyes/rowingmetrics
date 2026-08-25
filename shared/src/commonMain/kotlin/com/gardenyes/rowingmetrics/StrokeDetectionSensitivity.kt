package com.gardenyes.rowingmetrics

/**
 * Five-step stroke detection profile (top = most permissive, bottom = strictest).
 *
 * Slider positions (top → bottom):
 * 1. [VERY_HIGH] — above former High (most permissive)
 * 2. [HIGH] — former High preset
 * 3. [MEDIUM] — default (former Medium blend)
 * 4. [LOW] — constructor defaults
 * 5. [VERY_LOW] — below former Low (strictest)
 *
 * Only [VERY_HIGH] (“High”), [MEDIUM], and [VERY_LOW] (“Low”) show text labels on the configuration slider.
 */
enum class StrokeDetectionSensitivity {
    VERY_HIGH,
    HIGH,
    MEDIUM,
    LOW,
    VERY_LOW,
    ;

    val showsSensitivityLabel: Boolean
        get() = this == VERY_HIGH || this == MEDIUM || this == VERY_LOW

    companion object {
        val DEFAULT = MEDIUM

        fun fromStored(value: String?): StrokeDetectionSensitivity =
            entries.find { it.name == value } ?: DEFAULT
    }
}
