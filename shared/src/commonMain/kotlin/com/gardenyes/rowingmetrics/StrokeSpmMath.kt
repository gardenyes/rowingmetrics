package com.gardenyes.rowingmetrics

import kotlin.math.roundToInt

/** Result of [calculateSPM]: rounded strokes/min and mean interval (ms) used for that SPM. */
data class SpmCalcResult(
    val spm: Int,
    val intervalMs: Long,
)

private const val OUTLIER_RELATIVE_FRACTION = 0.4

/** Last this many stroke peak timestamps feed live SPM (seven gaps when full). */
const val LIVE_SPM_STROKE_WINDOW = 8

/**
 * Live SPM is treated as **stable** only once at least this many strokes exist in history (matches web:
 * need last-eight stroke window full before SPM is meaningful).
 */
const val LIVE_SPM_MIN_STROKES_FOR_STABLE = 8

/** In [calculateSPM]: if longer than this since the last stroke time, computed SPM is 0 (silence rule). */
const val LIVE_SPM_IDLE_AFTER_LAST_STROKE_MS = 3_000L

/**
 * UI hold only: after the last stroke peak, keep showing the last non-zero stroke rate (SPM) for this
 * long when the calculator still returns 0 (no new strokes, unstable window, 3s silence in math, etc.).
 * Tune this one constant to change the hold (e.g. 90_000L for 90s); independent of
 * [LIVE_SPM_IDLE_AFTER_LAST_STROKE_MS] used inside [calculateSPM].
 */
const val LIVE_SPM_DISPLAY_HOLD_AFTER_LAST_STROKE_MS = 60_000L

/** Arithmetic mean of Long intervals (as Double ms). */
fun List<Long>.avgMsOrNull(): Double? =
    if (isEmpty()) null else map { it.toDouble() }.average()

/** Consecutive differences: position[i] - position[i-1]. */
fun calculateIntervals(positionsMs: List<Long>): List<Long> {
    if (positionsMs.size < 2) return emptyList()
    return positionsMs.zipWithNext { a, b -> (b - a).coerceAtLeast(0L) }
}

/**
 * Remove intervals whose value differs from the mean by at least [relativeFraction] of the mean
 * (|x - mean| >= relativeFraction * mean). Recompute mean and repeat until stable or too few remain.
 */
fun removeIntervalOutliers(
    intervals: List<Long>,
    relativeFraction: Double = OUTLIER_RELATIVE_FRACTION,
): List<Long> {
    if (intervals.size < 2) return intervals
    var current = intervals
    repeat(5) {
        val mean = current.avgMsOrNull() ?: return current
        if (mean <= 0.0) return current
        val threshold = relativeFraction * mean
        val filtered = current.filter { kotlin.math.abs(it - mean) < threshold }
        when {
            filtered.size == current.size -> return current
            filtered.size >= 2 -> current = filtered
            else -> return pickBestMatchWhenTooFew(current, mean)
        }
    }
    return current
}

/**
 * When filtering would leave &lt; 2 intervals, prefer keeping intervals closest to the mean (JS pickBestMatch).
 */
private fun pickBestMatchWhenTooFew(intervals: List<Long>, mean: Double): List<Long> {
    if (intervals.size < 2) return intervals
    val scored = intervals.map { it to kotlin.math.abs(it - mean) }.sortedBy { it.second }
    return listOf(scored[0].first, scored[1].first)
}

/**
 * Live average strokes per minute from recent stroke **detected** times (same monotonic clock as [nowElapsedMs]).
 *
 * 1. Take the last up to [maxStrokesInWindow] timestamps from [strokeTimestampsMs] (e.g. last eight strokes).
 * 2. [calculateIntervals] → gaps in ms between consecutive times; arithmetic mean drives outlier removal.
 * 3. [removeIntervalOutliers]: drop any gap with \|gap − mean\| ≥ 40% of the mean; iterate until stable.
 *    If that would leave fewer than two gaps, keep the two gaps closest to the mean (pickBestMatch).
 * 4. If no gaps remain → 0 SPM and 0 ms interval. Else SPM = `round(60 / (avgGapMs / 1000))` = `round(60000 / avgGapMs)`.
 * 5. Silence: if more than [idleAfterLastStrokeMs] since the **last** stroke time → 0 SPM, 0 ms.
 *
 * Stable SPM requires at least [LIVE_SPM_MIN_STROKES_FOR_STABLE] stroke timestamps (full cadence window).
 * Below that, returns 0 SPM (interval 0) — same idea as web until eight strokes are available.
 */
fun calculateSPM(
    strokeTimestampsMs: List<Long>,
    nowElapsedMs: Long,
    maxStrokesInWindow: Int = LIVE_SPM_STROKE_WINDOW,
    idleAfterLastStrokeMs: Long = LIVE_SPM_IDLE_AFTER_LAST_STROKE_MS,
    minStrokesForStableSpm: Int = LIVE_SPM_MIN_STROKES_FOR_STABLE,
): SpmCalcResult {
    if (strokeTimestampsMs.isEmpty()) return SpmCalcResult(0, 0L)
    val last = strokeTimestampsMs.last()
    if (nowElapsedMs - last > idleAfterLastStrokeMs) {
        return SpmCalcResult(0, 0L)
    }
    if (strokeTimestampsMs.size < minStrokesForStableSpm) {
        return SpmCalcResult(0, 0L)
    }
    val cap = maxStrokesInWindow.coerceAtLeast(2)
    val window = strokeTimestampsMs.takeLast(cap)
    var intervals = calculateIntervals(window)
    if (intervals.isEmpty()) return SpmCalcResult(0, 0L)
    intervals = removeIntervalOutliers(intervals)
    if (intervals.isEmpty()) return SpmCalcResult(0, 0L)
    val avgMs = intervals.avgMsOrNull() ?: return SpmCalcResult(0, 0L)
    if (avgMs <= 0.0) return SpmCalcResult(0, 0L)
    val spm = (60_000.0 / avgMs).roundToInt().coerceAtLeast(0)
    val intervalRounded = avgMs.roundToInt().toLong().coerceAtLeast(1L)
    return SpmCalcResult(spm, intervalRounded)
}
