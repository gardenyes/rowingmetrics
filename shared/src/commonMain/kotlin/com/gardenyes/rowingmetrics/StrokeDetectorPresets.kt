package com.gardenyes.rowingmetrics

/**
 * Maps [StrokeDetectionSensitivity] to [AccelerometerStrokeDetector] parameters.
 *
 * Five levels (most → least permissive): [VERY_HIGH], [HIGH], [MEDIUM], [LOW], [VERY_LOW].
 * Default preference: [StrokeDetectionSensitivity.MEDIUM].
 */
object StrokeDetectorPresets {

    private const val MEDIUM_BLEND_FROM_PERMISSIVE_TO_BASELINE = 0.5f

    fun buildDetector(sensitivity: StrokeDetectionSensitivity): AccelerometerStrokeDetector =
        when (sensitivity) {
            StrokeDetectionSensitivity.VERY_HIGH -> veryHighSensitivity()
            StrokeDetectionSensitivity.HIGH -> highSensitivity()
            StrokeDetectionSensitivity.MEDIUM -> mediumSensitivity()
            StrokeDetectionSensitivity.LOW -> lowSensitivity()
            StrokeDetectionSensitivity.VERY_LOW -> veryLowSensitivity()
        }

    /** Above former High — ultra-permissive (former app “High” tier). */
    private fun veryHighSensitivity(): AccelerometerStrokeDetector =
        AccelerometerStrokeDetector(
            minStrokeGapMs = 220L,
            minDynamicRange = 0.08f,
            positiveBandFraction = 0.48f,
            magnitudeSmoothAlpha = 0.58f,
            recoveryBandFraction = 0.20f,
            peakProminenceFraction = 0.025f,
            minPeakProminenceAbs = 0.035f,
        )

    /** Former **Medium** preset — **High** profile. */
    private fun highSensitivity(): AccelerometerStrokeDetector =
        AccelerometerStrokeDetector(
            minStrokeGapMs = 260L,
            minDynamicRange = 0.10f,
            positiveBandFraction = 0.50f,
            magnitudeSmoothAlpha = 0.55f,
            recoveryBandFraction = 0.18f,
            peakProminenceFraction = 0.03f,
            minPeakProminenceAbs = 0.04f,
        )

    /** Blend between High tuning and defaults — default **Medium** preference. */
    private fun mediumSensitivity(): AccelerometerStrokeDetector {
        val t = MEDIUM_BLEND_FROM_PERMISSIVE_TO_BASELINE
        return AccelerometerStrokeDetector(
            minStrokeGapMs = lerpLong(260L, 300L, t),
            minDynamicRange = lerp(0.10f, 0.14f, t),
            positiveBandFraction = lerp(0.50f, 0.56f, t),
            magnitudeSmoothAlpha = lerp(0.55f, 0.50f, t),
            recoveryBandFraction = lerp(0.18f, 0.16f, t),
            peakProminenceFraction = lerp(0.03f, 0.04f, t),
            minPeakProminenceAbs = lerp(0.04f, 0.05f, t),
        )
    }

    /** Full constructor defaults — **Low** profile. */
    private fun lowSensitivity(): AccelerometerStrokeDetector = AccelerometerStrokeDetector()

    /** Below Low — extrapolated stricter tuning. */
    private fun veryLowSensitivity(): AccelerometerStrokeDetector =
        AccelerometerStrokeDetector(
            minStrokeGapMs = 320L,
            minDynamicRange = 0.16f,
            positiveBandFraction = 0.59f,
            magnitudeSmoothAlpha = 0.475f,
            recoveryBandFraction = 0.15f,
            peakProminenceFraction = 0.045f,
            minPeakProminenceAbs = 0.055f,
        )

    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

    private fun lerpLong(a: Long, b: Long, t: Float): Long =
        (a + (b - a) * t.toDouble()).toLong()
}
