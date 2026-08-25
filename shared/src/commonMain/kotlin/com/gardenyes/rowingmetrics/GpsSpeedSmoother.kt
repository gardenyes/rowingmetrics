package com.gardenyes.rowingmetrics

/**
 * GPS speed pipeline: Kalman filter (Q=0.8, R=2) on m/s, FIFO (≤8), mean of last N → km/h.
 */
class GpsSpeedSmoother(
    private val fifoMax: Int = FIFO_MAX_DEFAULT,
    private val processNoiseQ: Double = 0.8,
    private val measurementNoiseR: Double = 2.0,
) {
    companion object {
        const val FIFO_MAX_DEFAULT: Int = 8
    }

    private var kalmanX = 0.0
    private var kalmanP = 1.0
    private var kalmanInitialized = false
    private val filteredSpeedsMps = ArrayDeque<Double>(fifoMax + 2)
    private var lastOutputKmh = 0.0
    private var displayAverageCount: Int = 4

    fun setDisplayAverageCount(count: Int) {
        displayAverageCount = count.coerceIn(1, fifoMax)
    }

    fun reset() {
        kalmanX = 0.0
        kalmanP = 1.0
        kalmanInitialized = false
        filteredSpeedsMps.clear()
        lastOutputKmh = 0.0
    }

    /**
     * @param speedMps GPS speed in m/s, or null if unavailable (returns previous km/h).
     */
    fun calculate(speedMps: Double?): Double {
        if (speedMps == null) {
            return lastOutputKmh
        }
        val z = speedMps.coerceIn(0.0, 50.0)
        if (!kalmanInitialized) {
            kalmanX = z
            kalmanP = measurementNoiseR
            kalmanInitialized = true
        } else {
            kalmanP += processNoiseQ
            val k = kalmanP / (kalmanP + measurementNoiseR)
            kalmanX += k * (z - kalmanX)
            kalmanP = (1.0 - k) * kalmanP
        }
        filteredSpeedsMps.addLast(kalmanX)
        while (filteredSpeedsMps.size > fifoMax) {
            filteredSpeedsMps.removeFirst()
        }
        val nTake =
            displayAverageCount.coerceIn(1, fifoMax).let { want ->
                kotlin.math.min(want, filteredSpeedsMps.size)
            }
        val slice = filteredSpeedsMps.takeLast(nTake)
        val avgMps =
            if (slice.isEmpty()) {
                0.0
            } else {
                slice.average()
            }
        lastOutputKmh = avgMps * 3.6
        return lastOutputKmh
    }
}
