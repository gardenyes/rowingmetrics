package com.gardenyes.rowingmetrics.platform

import com.gardenyes.rowingmetrics.AppLanguage
import com.gardenyes.rowingmetrics.StrokeDetectionSensitivity
import com.gardenyes.rowingmetrics.data.CompletedActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface SettingsStore {
    fun getStrokeDetectionSensitivity(): StrokeDetectionSensitivity
    fun setStrokeDetectionSensitivity(s: StrokeDetectionSensitivity)
    fun getGpsDisplayAverageN(): Int
    fun setGpsDisplayAverageN(n: Int)
    fun getGpsDisplayAverageCount(): Int
    fun getAppLanguage(): AppLanguage
    fun setAppLanguage(language: AppLanguage)
}

interface ActivityRepository {
    fun observeAll(): Flow<List<CompletedActivity>>
    suspend fun insert(activity: CompletedActivity)
    suspend fun deleteById(id: Long)
    suspend fun deleteAll()
}

interface LocationTracker {
    fun start(onFix: (GpsFix) -> Unit)
    fun stop()
}

interface MotionSensorSource {
    val isAvailable: Boolean
    fun start(onSample: (ax: Float, ay: Float, az: Float, gravity: FloatArray?) -> Unit)
    fun stop()
}

/** Portrait vs landscape bucket changes during an active session. */
interface OrientationMonitor {
    fun start(onOrientationChanged: () -> Unit)
    fun stop()
}

interface PlatformServices {
    val settings: SettingsStore
    val activities: ActivityRepository
    val locationTracker: LocationTracker
    val motionSensors: MotionSensorSource
    val orientationMonitor: OrientationMonitor
    val scope: CoroutineScope
    fun postToMain(block: () -> Unit)
    fun elapsedRealtimeMs(): Long
    fun currentTimeMillis(): Long
}
