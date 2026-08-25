package com.gardenyes.rowingmetrics

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Stroke timing from **acceleration**: smoothed magnitude, rolling min/max → adaptive thresholds,
 * **recovery gate** (signal must drop after a stroke before the next can arm), upward threshold
 * crossing, **minimum peak prominence** above the positive band, optional [didWeLostOne], then
 * counter + last-eight peak times for [calculateSPM]. Tuned for **lower sensitivity** (stronger peaks
 * required, stricter re-arm, heavier smoothing) to reduce false detections; very soft strokes may be missed.
 *
 * Uses **gravity-orthogonal** (horizontal-plane) linear acceleration magnitude when a gravity vector is
 * supplied, so the same body motion is scored similarly in portrait and landscape. Falls back to full
 * 3D |a| if gravity is missing or too weak. Not GPS-derived.
 */
class AccelerometerStrokeDetector(
    /** Samples in this elapsed window contribute to min/max (checkpoint-style rolling range). */
    private val thresholdWindowMs: Long = 1_800L,
    private val maxEvents: Int = 240,
    /** Minimum time between accepted peaks; higher = fewer double-counts / stricter. */
    private val minStrokeGapMs: Long = 300L,
    /** Ignore peak detection until this many samples (warm-up / calibration). */
    private val minCalibrationSamples: Int = 64,
    /**
     * Minimum peak-to-trough span (m/s²) before thresholds are usable (filters idle / small motion);
     * higher = only larger motions qualify.
     */
    private val minDynamicRange: Float = 0.14f,
    /**
     * Positive band: higher fraction = crossing threshold higher in the range = stricter (lower sensitivity).
     */
    private val positiveBandFraction: Float = 0.56f,
    /** EMA on magnitude; lower = smoother (fewer false threshold crossings). */
    private val magnitudeSmoothAlpha: Float = 0.50f,
    /**
     * After a stroke, magnitude must fall to this fraction of the rolling span (from [rollMin]) before
     * the next stroke can arm; lower = need a deeper “recovery” before the next count.
     */
    private val recoveryBandFraction: Float = 0.16f,
    /**
     * Peak must exceed positive threshold by at least max([peakProminenceFraction]×span, [minPeakProminenceAbs]);
     * both increased vs sensitive defaults to reject small bumps.
     */
    private val peakProminenceFraction: Float = 0.04f,
    private val minPeakProminenceAbs: Float = 0.05f,
) {

    data class AccelSampleEvent(
        val timeMs: Long,
        val acceleration: Float,
        val positiveThreshold: Float,
        val negativeThreshold: Float,
    )

    sealed class Result {
        data object None : Result()
        data class StrokePeaks(val peakTimesMs: LongArray) : Result()
    }

    private val events = ArrayDeque<AccelSampleEvent>(maxEvents + 8)
    private val recentValues = ArrayDeque<Pair<Long, Float>>(maxEvents + 8)

    private var sampleCount = 0
    /** -1 = unset; EMA of rounded magnitude for stable thresholds and crossings. */
    private var smoothMagnitude = -1f
    /** False after a stroke until smoothed signal drops to the recovery line (prevents double bumps). */
    private var armedForNextStroke = true

    private var rollMin = 0f
    private var rollMax = 0f
    private var positiveThreshold = 0f
    private var negativeThreshold = 0f
    private var thresholdsValid = false

    private var acceptedStrokeCount = 0
    private val strokePeakTimesMs = ArrayDeque<Long>(9)
    private var lastStrokePeakMs = 0L

    fun reset() {
        events.clear()
        recentValues.clear()
        sampleCount = 0
        rollMin = 0f
        rollMax = 0f
        positiveThreshold = 0f
        negativeThreshold = 0f
        thresholdsValid = false
        acceptedStrokeCount = 0
        strokePeakTimesMs.clear()
        lastStrokePeakMs = 0L
        smoothMagnitude = -1f
        armedForNextStroke = true
    }

    /**
     * Clears adaptive thresholds / buffers (e.g. after device rotation) but keeps accepted stroke count and
     * recent peak times used for SPM so a session total is not reset mid-row.
     */
    fun clearAdaptiveStatePreservingStrokeCounts() {
        events.clear()
        recentValues.clear()
        sampleCount = 0
        rollMin = 0f
        rollMax = 0f
        positiveThreshold = 0f
        negativeThreshold = 0f
        thresholdsValid = false
        smoothMagnitude = -1f
        armedForNextStroke = true
    }

    fun acceptedStrokeCount(): Int = acceptedStrokeCount

    fun strokeTimestampsForSpm(): List<Long> = strokePeakTimesMs.toList()

    /**
     * @param elapsedRtMs monotonic ms (e.g. [android.os.SystemClock.elapsedRealtime])
     * @param ax ay az linear acceleration (m/s²) preferred; gravity removed by sensor
     * @param gravity if non-null, `[gx,gy,gz]` from [android.hardware.Sensor.TYPE_GRAVITY] (m/s²);
     *   used to take linear acceleration in the **horizontal** plane w.r.t. gravity
     */
    fun onAccelerationSample(
        ax: Float,
        ay: Float,
        az: Float,
        elapsedRtMs: Long,
        gravity: FloatArray? = null,
    ): Result {
        val mag = linearMagnitudeForStrokes(ax, ay, az, gravity)
        val raw = round2(mag)
        val a =
            if (smoothMagnitude < 0f) {
                smoothMagnitude = raw
                raw
            } else {
                val s = magnitudeSmoothAlpha
                smoothMagnitude = smoothMagnitude * (1f - s) + raw * s
                round2(smoothMagnitude)
            }

        pruneRecent(elapsedRtMs)
        recentValues.addLast(elapsedRtMs to a)
        updateThresholdsFromRollingWindow()
        sampleCount++

        if (!thresholdsValid || sampleCount < minCalibrationSamples) {
            return Result.None
        }

        val span = (rollMax - rollMin).coerceAtLeast(minDynamicRange)
        val releaseLine = rollMin + span * recoveryBandFraction
        if (!armedForNextStroke && a <= releaseLine) {
            armedForNextStroke = true
        }

        val posTh = positiveThreshold
        val negTh = negativeThreshold
        events.addLast(AccelSampleEvent(elapsedRtMs, a, posTh, negTh))
        while (events.size > maxEvents) events.removeFirst()

        if (events.size < 2) return Result.None

        val prev = events[events.size - 2]
        val cur = events.last()
        if (!armedForNextStroke ||
            !isAccelerationCrossingPositiveThreshold(prev, cur)
        ) {
            consolidateOldTail(elapsedRtMs)
            return Result.None
        }

        val peakMargin = max(peakProminenceFraction * span, minPeakProminenceAbs)
        val crossingIndex = events.size - 2
        val peak = findMaxAbovePositiveThreshold(crossingIndex, peakMargin) ?: run {
            consolidateOldTail(elapsedRtMs)
            return Result.None
        }

        val peakTime = peak.timeMs
        if (lastStrokePeakMs > 0 && peakTime - lastStrokePeakMs < minStrokeGapMs) {
            events.removeLast()
            return Result.None
        }

        val out = didWeLostOne(peakTime)
        armedForNextStroke = false
        consolidateAfterStroke(elapsedRtMs)
        return Result.StrokePeaks(out)
    }

    private fun round2(x: Float): Float {
        val q = 100f
        return (x * q).roundToInt() / q
    }

    /**
     * Magnitude of the component of [ax,ay,az] **orthogonal to [gravity]** (local horizontal / tangential).
     * Closer to orientation-invariant stroke feel than 3D |a| when the fusion sensor is tilt-sensitive.
     */
    private fun linearMagnitudeForStrokes(
        ax: Float,
        ay: Float,
        az: Float,
        gravity: FloatArray?,
    ): Float {
        if (gravity == null || gravity.size < 3) {
            return sqrt((ax * ax + ay * ay + az * az).toDouble()).toFloat()
        }
        val gx = gravity[0]
        val gy = gravity[1]
        val gz = gravity[2]
        val g2 = gx * gx + gy * gy + gz * gz
        if (g2 < 0.25f) { // < ~0.5 m/s² in norm; unusable, fallback
            return sqrt((ax * ax + ay * ay + az * az).toDouble()).toFloat()
        }
        val invG2 = 1f / g2
        val dot = ax * gx + ay * gy + az * gz
        val hx = ax - invG2 * dot * gx
        val hy = ay - invG2 * dot * gy
        val hz = az - invG2 * dot * gz
        return sqrt((hx * hx + hy * hy + hz * hz).toDouble()).toFloat()
    }

    private fun pruneRecent(nowMs: Long) {
        while (recentValues.isNotEmpty() && nowMs - recentValues.first().first > thresholdWindowMs) {
            recentValues.removeFirst()
        }
    }

    private fun updateThresholdsFromRollingWindow() {
        if (recentValues.isEmpty()) return
        var mn = Float.MAX_VALUE
        var mx = -Float.MAX_VALUE
        for ((_, v) in recentValues) {
            mn = min(mn, v)
            mx = max(mx, v)
        }
        rollMin = mn
        rollMax = mx
        val span = rollMax - rollMin
        thresholdsValid = span >= minDynamicRange
        if (!thresholdsValid) return
        positiveThreshold = rollMin + span * positiveBandFraction
        negativeThreshold = rollMin + span * (1f - positiveBandFraction)
    }

    private fun isAccelerationCrossingPositiveThreshold(
        prev: AccelSampleEvent,
        cur: AccelSampleEvent,
    ): Boolean =
        prev.acceleration < prev.positiveThreshold && cur.acceleration >= cur.positiveThreshold

    /**
     * Local maximum at or after [fromIndex], at least [peakMargin] above that sample's positive threshold
     * (rejects tiny threshold grazes from non-stroke motion).
     */
    private fun findMaxAbovePositiveThreshold(fromIndex: Int, peakMargin: Float): AccelSampleEvent? {
        if (fromIndex >= events.size) return null
        var best: AccelSampleEvent? = null
        var i = fromIndex
        while (i < events.size) {
            val e = events[i]
            val need = e.positiveThreshold + peakMargin
            if (e.acceleration >= need) {
                if (best == null || e.acceleration > best.acceleration) best = e
            }
            i++
        }
        return best
    }

    /**
     * If the gap since the last stroke is much larger than recent cadence, insert one missed stroke
     * time before the current peak (two peaks in one decision).
     */
    private fun didWeLostOne(peakTimeMs: Long): LongArray {
        val medianGap = medianLastStrokeIntervalsMs()
        val last = lastStrokePeakMs
        if (medianGap == null || last <= 0L || peakTimeMs <= last) {
            commitStroke(peakTimeMs)
            return longArrayOf(peakTimeMs)
        }
        val gap = peakTimeMs - last
        val lost =
            medianGap in 260L..3_800L &&
                gap > (medianGap * 1.75).toLong() &&
                gap < 5_500L
        if (!lost) {
            commitStroke(peakTimeMs)
            return longArrayOf(peakTimeMs)
        }
        val inserted = last + medianGap
        return if (inserted < peakTimeMs - minStrokeGapMs / 2) {
            commitStroke(inserted)
            commitStroke(peakTimeMs)
            longArrayOf(inserted, peakTimeMs)
        } else {
            commitStroke(peakTimeMs)
            longArrayOf(peakTimeMs)
        }
    }

    private fun medianLastStrokeIntervalsMs(): Long? {
        if (strokePeakTimesMs.size < 4) return null
        val list = strokePeakTimesMs.toList()
        val iv = ArrayList<Long>(list.size - 1)
        for (i in 1 until list.size) {
            iv.add((list[i] - list[i - 1]).coerceAtLeast(0L))
        }
        if (iv.isEmpty()) return null
        iv.sort()
        return iv[iv.size / 2]
    }

    private fun commitStroke(peakTimeMs: Long) {
        acceptedStrokeCount++
        strokePeakTimesMs.addLast(peakTimeMs)
        while (strokePeakTimesMs.size > 8) strokePeakTimesMs.removeFirst()
        lastStrokePeakMs = peakTimeMs
    }

    /** After a confirmed stroke, clear the buffer (fresh search for the next peak). */
    private fun consolidateAfterStroke(nowMs: Long) {
        events.clear()
        pruneRecent(nowMs)
    }

    /** Drop very old tail entries to bound memory while hunting the next peak. */
    private fun consolidateOldTail(nowMs: Long) {
        val keepFrom = nowMs - thresholdWindowMs
        while (events.isNotEmpty() && events.first().timeMs < keepFrom) {
            events.removeFirst()
        }
    }
}
