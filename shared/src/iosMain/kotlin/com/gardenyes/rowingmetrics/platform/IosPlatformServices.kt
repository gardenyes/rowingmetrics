package com.gardenyes.rowingmetrics.platform

import com.gardenyes.rowingmetrics.AppLanguage
import com.gardenyes.rowingmetrics.GpsSpeedSmoother
import com.gardenyes.rowingmetrics.StrokeDetectionSensitivity
import com.gardenyes.rowingmetrics.data.createActivityRepository
import kotlinx.coroutines.CoroutineScope
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIDevice

/**
 * iOS platform bindings. GPS and motion use stub implementations until Core Location / Core Motion
 * wrappers are added; settings and SQLDelight persistence work on device/simulator.
 */
class IosPlatformServices(
    override val scope: CoroutineScope,
) : PlatformServices {
    override val settings: SettingsStore = IosSettingsStore()
    override val activities = createActivityRepository()
    override val locationTracker: LocationTracker = IosLocationTrackerStub()
    override val motionSensors: MotionSensorSource = IosMotionSensorStub()
    override val orientationMonitor: OrientationMonitor = IosOrientationMonitorStub()

    override fun postToMain(block: () -> Unit) {
        block()
    }

    override fun elapsedRealtimeMs(): Long =
        (platform.Foundation.NSProcessInfo.processInfo.systemUptime * 1000.0).toLong()

    override fun currentTimeMillis(): Long =
        (platform.Foundation.NSDate().timeIntervalSince1970 * 1000.0).toLong()
}

private class IosSettingsStore : SettingsStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getStrokeDetectionSensitivity(): StrokeDetectionSensitivity {
        val v = defaults.stringForKey("stroke_detection_sensitivity")
            ?: StrokeDetectionSensitivity.DEFAULT.name
        return runCatching { StrokeDetectionSensitivity.valueOf(v) }
            .getOrDefault(StrokeDetectionSensitivity.DEFAULT)
    }

    override fun setStrokeDetectionSensitivity(s: StrokeDetectionSensitivity) {
        defaults.setObject(s.name, forKey = "stroke_detection_sensitivity")
    }

    override fun getGpsDisplayAverageN(): Int {
        val v = defaults.integerForKey("gps_display_average_n").toInt().coerceIn(0, 8)
        return (if (v == 0) 1 else v).coerceIn(1, 8)
    }

    override fun setGpsDisplayAverageN(n: Int) {
        defaults.setInteger(n.coerceIn(1, 8).toLong(), forKey = "gps_display_average_n")
    }

    override fun getGpsDisplayAverageCount(): Int =
        getGpsDisplayAverageN().coerceIn(1, GpsSpeedSmoother.FIFO_MAX_DEFAULT)

    override fun getAppLanguage(): AppLanguage =
        AppLanguage.fromStored(
            defaults.stringForKey("app_language") ?: AppLanguage.DEFAULT.name,
        )

    override fun setAppLanguage(language: AppLanguage) {
        defaults.setObject(language.name, forKey = "app_language")
    }
}

private class IosLocationTrackerStub : LocationTracker {
    override fun start(onFix: (GpsFix) -> Unit) = Unit
    override fun stop() = Unit
}

private class IosMotionSensorStub : MotionSensorSource {
    override val isAvailable: Boolean = false
    override fun start(onSample: (Float, Float, Float, FloatArray?) -> Unit) = Unit
    override fun stop() = Unit
}

private class IosOrientationMonitorStub : OrientationMonitor {
    override fun start(onOrientationChanged: () -> Unit) = Unit
    override fun stop() = Unit
}

fun iosAppVersion(): String = UIDevice.currentDevice.systemVersion
