package com.gardenyes.rowingmetrics

import com.gardenyes.rowingmetrics.data.CompletedActivity
import com.gardenyes.rowingmetrics.platform.GpsFix
import com.gardenyes.rowingmetrics.platform.PlatformServices
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Cross-platform session engine: stroke detection, GPS distance/speed, SPM, and persistence.
 * UI binds via [setListener]; platform I/O via [PlatformServices].
 */
class RowingSessionController(
    private val platform: PlatformServices,
) {
    companion object {
        private const val SESSION_UI_TICK_MS = 1_000L
        private const val LIVE_SPM_ENGINE_CYCLE_MS = 1_995L
        private const val GPS_FIX_MAX_ACCURACY_M = 50f
        private const val MIN_GPS_MOVEMENT_DISPLAY_M = 4.0
        private const val GPS_DISPLAY_MAX_ACCURACY_M = 80f
        private const val GPS_DISPLAY_MAX_JUMP_M = 200.0
        private const val ORIENTATION_STABILIZE_MS = 1_200L
    }

    private var uiState = RowingUiState()
    private var listener: ((RowingUiState) -> Unit)? = null

    @Volatile
    private var measuring = false

    private var sessionStartRtMs: Long = 0L
    private var sessionEndRtMs: Long = 0L
    private var liveSpmDisplay: Double = 0.0
    private var lastStableLiveSpm: Double = 0.0
    private var sessionSpmSampleSum: Double = 0.0
    private var sessionSpmSampleCount: Int = 0
    private var sessionDistanceKm = 0.0
    private var lastLat: Double? = null
    private var lastLon: Double? = null
    private var displayAnchorLat: Double? = null
    private var displayAnchorLon: Double? = null
    private var lastSmoothedSpeedKmh = 0.0
    private val speedSmoother = GpsSpeedSmoother()

    private var accelerometerStrokeDetector: AccelerometerStrokeDetector =
        StrokeDetectorPresets.buildDetector(platform.settings.getStrokeDetectionSensitivity())

    @Volatile
    private var gpsReady = false

    private val lastGravity = FloatArray(3)

    @Volatile
    private var gravitySampleReady: Boolean = false

    private var sessionUiTickJob: Job? = null
    private var metricsCycleJob: Job? = null

    @Volatile
    private var strokeDetectionActive: Boolean = true

    private var orientationStabilizeJob: Job? = null

    private val motionCallback: (Float, Float, Float, FloatArray?) -> Unit = { ax, ay, az, gravity ->
        synchronized(this) {
            if (!measuring) return@synchronized
            if (!strokeDetectionActive) return@synchronized
            if (gravity != null) {
                lastGravity[0] = gravity[0]
                lastGravity[1] = gravity[1]
                lastGravity[2] = gravity[2]
                gravitySampleReady = true
            }
            val now = platform.elapsedRealtimeMs()
            val g = if (gravitySampleReady) lastGravity else null
            when (accelerometerStrokeDetector.onAccelerationSample(ax, ay, az, now, g)) {
                is AccelerometerStrokeDetector.Result.StrokePeaks -> {
                    updateLiveSpmDisplayOnly(now)
                    platform.postToMain { pushUi() }
                }
                AccelerometerStrokeDetector.Result.None -> Unit
            }
        }
    }

    private val locationCallback: (GpsFix) -> Unit = { loc ->
        synchronized(this) {
            if (!measuring) return@synchronized
            if (!isAcceptableGpsFix(loc)) return@synchronized

            val speedMps = if (loc.hasSpeed) loc.speedMps else null
            val smoothedKmh = speedSmoother.calculate(speedMps)
            lastSmoothedSpeedKmh = smoothedKmh

            if (!gpsReady) {
                gpsReady = true
                lastLat = loc.latitude
                lastLon = loc.longitude
                return@synchronized
            }

            val lat = loc.latitude
            val lon = loc.longitude
            val pLat = lastLat
            val pLon = lastLon

            if (pLat != null && pLon != null) {
                if (displayAnchorLat == null || displayAnchorLon == null) {
                    displayAnchorLat = pLat
                    displayAnchorLon = pLon
                }
                val anchorLat = displayAnchorLat!!
                val anchorLon = displayAnchorLon!!
                val dKm = calcDistanceKm(anchorLat, anchorLon, lat, lon)
                val dM = dKm * 1000.0
                val qualityOk =
                    loc.accuracyM <= GPS_DISPLAY_MAX_ACCURACY_M || dM < GPS_DISPLAY_MAX_JUMP_M
                if (dM >= MIN_GPS_MOVEMENT_DISPLAY_M && qualityOk) {
                    sessionDistanceKm += dKm
                    displayAnchorLat = lat
                    displayAnchorLon = lon
                }
            } else if (displayAnchorLat == null) {
                displayAnchorLat = lat
                displayAnchorLon = lon
            }

            lastLat = lat
            lastLon = lon
        }
    }

    fun setListener(l: (RowingUiState) -> Unit) {
        listener = l
        l(uiState)
    }

    fun clearListener() {
        listener = null
    }

    fun getStrokeDetectionSensitivity(): StrokeDetectionSensitivity =
        platform.settings.getStrokeDetectionSensitivity()

    fun getGpsDisplayAverageN(): Int = platform.settings.getGpsDisplayAverageN()

    fun setGpsDisplayAverageN(n: Int) {
        platform.settings.setGpsDisplayAverageN(n.coerceIn(1, 8))
        synchronized(this) {
            speedSmoother.setDisplayAverageCount(platform.settings.getGpsDisplayAverageCount())
        }
    }

    fun setStrokeDetectionSensitivity(s: StrokeDetectionSensitivity) {
        platform.settings.setStrokeDetectionSensitivity(s)
        synchronized(this) {
            if (!measuring) {
                accelerometerStrokeDetector = StrokeDetectorPresets.buildDetector(s)
            }
        }
    }

    fun startSession() {
        if (!platform.motionSensors.isAvailable) return
        stopInternals()
        resetSessionState()
        uiState = RowingUiState(running = true)
        listener?.invoke(uiState)
        measuring = true

        platform.motionSensors.start(motionCallback)
        try {
            platform.locationTracker.start(locationCallback)
        } catch (_: SecurityException) {
            measuring = false
            platform.motionSensors.stop()
            sessionStartRtMs = 0L
            sessionEndRtMs = 0L
            uiState = RowingUiState(running = false)
            listener?.invoke(uiState)
            return
        }

        platform.orientationMonitor.start {
            onOrientationChanged()
        }

        pushUi()
        startPeriodicJobs()
    }

    fun stopSession() {
        if (!uiState.running) return
        val endedAtWallMs = platform.currentTimeMillis()
        sessionEndRtMs = platform.elapsedRealtimeMs()
        stopInternals()
        uiState = uiState.copy(running = false)
        pushUi()
        persistCompletedSession(endedAtWallMs)
    }

    private fun resetSessionState() {
        liveSpmDisplay = 0.0
        lastStableLiveSpm = 0.0
        sessionSpmSampleSum = 0.0
        sessionSpmSampleCount = 0
        sessionDistanceKm = 0.0
        lastLat = null
        lastLon = null
        displayAnchorLat = null
        displayAnchorLon = null
        lastSmoothedSpeedKmh = 0.0
        speedSmoother.reset()
        speedSmoother.setDisplayAverageCount(platform.settings.getGpsDisplayAverageCount())
        gpsReady = false
        accelerometerStrokeDetector =
            StrokeDetectorPresets.buildDetector(platform.settings.getStrokeDetectionSensitivity())
        gravitySampleReady = false
        strokeDetectionActive = true
        sessionStartRtMs = platform.elapsedRealtimeMs()
        sessionEndRtMs = sessionStartRtMs
    }

    private fun onOrientationChanged() {
        if (!measuring) return
        synchronized(this) {
            strokeDetectionActive = false
            accelerometerStrokeDetector.clearAdaptiveStatePreservingStrokeCounts()
        }
        orientationStabilizeJob?.cancel()
        orientationStabilizeJob =
            platform.scope.launch {
                delay(ORIENTATION_STABILIZE_MS)
                synchronized(this@RowingSessionController) {
                    if (measuring) strokeDetectionActive = true
                }
                platform.postToMain { pushUi() }
            }
    }

    private fun startPeriodicJobs() {
        sessionUiTickJob?.cancel()
        metricsCycleJob?.cancel()
        sessionUiTickJob =
            platform.scope.launch {
                while (isActive && measuring) {
                    delay(SESSION_UI_TICK_MS)
                    pushUi()
                }
            }
        metricsCycleJob =
            platform.scope.launch {
                while (isActive && measuring) {
                    delay(LIVE_SPM_ENGINE_CYCLE_MS)
                    val nowRt = platform.elapsedRealtimeMs()
                    synchronized(this@RowingSessionController) {
                        applyStableLiveSpmForSessionSample(nowRt)
                    }
                    pushUi()
                }
            }
    }

    private fun isAcceptableGpsFix(loc: GpsFix): Boolean {
        if (!loc.hasAccuracy) return false
        if (loc.accuracyM > GPS_FIX_MAX_ACCURACY_M) return false
        if (loc.latitude == 0.0 && loc.longitude == 0.0) return false
        return true
    }

    private fun sessionElapsedMs(): Long {
        if (sessionStartRtMs == 0L) return 0L
        val end = if (uiState.running) platform.elapsedRealtimeMs() else sessionEndRtMs
        return (end - sessionStartRtMs).coerceAtLeast(0L)
    }

    private fun calculateAverageSpeedKmh(): Double {
        val elapsedMs = sessionElapsedMs()
        return if (elapsedMs > 0L && sessionDistanceKm > 0.0) {
            sessionDistanceKm / (elapsedMs / 1e3) * 3600.0
        } else {
            0.0
        }
    }

    private fun refreshSPM(nowRt: Long): SpmCalcResult =
        calculateSPM(accelerometerStrokeDetector.strokeTimestampsForSpm(), nowRt)

    private fun updateLiveSpmDisplayOnly(nowRt: Long) {
        val spmResult = refreshSPM(nowRt)
        val lastStroke = accelerometerStrokeDetector.strokeTimestampsForSpm().lastOrNull()
        val strokeIdle =
            lastStroke == null || (nowRt - lastStroke) > LIVE_SPM_DISPLAY_HOLD_AFTER_LAST_STROKE_MS
        when {
            spmResult.spm > 0 -> {
                lastStableLiveSpm = spmResult.spm.toDouble()
                liveSpmDisplay = lastStableLiveSpm
            }
            !strokeIdle && lastStableLiveSpm > 0.0 -> Unit
            else -> {
                lastStableLiveSpm = 0.0
                liveSpmDisplay = 0.0
            }
        }
    }

    private fun applyStableLiveSpmForSessionSample(nowRt: Long) {
        updateLiveSpmDisplayOnly(nowRt)
        sessionSpmSampleSum += liveSpmDisplay
        sessionSpmSampleCount++
    }

    private fun pushUi() {
        val snapshot =
            synchronized(this) {
                val nowRt = platform.elapsedRealtimeMs()
                val avgSpm =
                    if (uiState.running) {
                        liveSpmDisplay
                    } else if (sessionSpmSampleCount > 0) {
                        sessionSpmSampleSum / sessionSpmSampleCount
                    } else {
                        liveSpmDisplay
                    }
                val live = gpsReady
                val curKmh = if (live) lastSmoothedSpeedKmh else 0.0
                val activityElapsed = sessionElapsedMs()
                val avgSpd = calculateAverageSpeedKmh()
                val lastStrokeForUi = accelerometerStrokeDetector.strokeTimestampsForSpm().lastOrNull()
                val strokeRateLive =
                    !uiState.running ||
                        (
                            lastStrokeForUi != null &&
                                (nowRt - lastStrokeForUi) <= LIVE_SPM_IDLE_AFTER_LAST_STROKE_MS
                        )
                RowingUiState(
                    running = uiState.running,
                    avgStrokesPerMin = avgSpm,
                    strokeRateDetectionLive = strokeRateLive,
                    currentSpeedKmh = curKmh,
                    averageSpeedKmh = avgSpd,
                    activityElapsedMs = activityElapsed,
                    distanceMeters = if (live) sessionDistanceKm * 1000.0 else 0.0,
                )
            }
        uiState = snapshot
        listener?.invoke(uiState)
    }

    private fun persistCompletedSession(endedAtWallMs: Long) {
        platform.scope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val entity =
                synchronized(this@RowingSessionController) {
                    CompletedActivity(
                        endedAtEpochMs = endedAtWallMs,
                        durationMs = sessionElapsedMs(),
                        avgStrokeRate =
                            if (sessionSpmSampleCount > 0) {
                                sessionSpmSampleSum / sessionSpmSampleCount
                            } else {
                                0.0
                            },
                        avgSpeedKmh = calculateAverageSpeedKmh(),
                        distanceMeters = sessionDistanceKm * 1000.0,
                    )
                }
            platform.activities.insert(entity)
        }
    }

    private fun stopInternals() {
        measuring = false
        orientationStabilizeJob?.cancel()
        orientationStabilizeJob = null
        sessionUiTickJob?.cancel()
        sessionUiTickJob = null
        metricsCycleJob?.cancel()
        metricsCycleJob = null
        platform.motionSensors.stop()
        platform.locationTracker.stop()
        platform.orientationMonitor.stop()
    }

    fun dispose() {
        stopInternals()
    }
}
