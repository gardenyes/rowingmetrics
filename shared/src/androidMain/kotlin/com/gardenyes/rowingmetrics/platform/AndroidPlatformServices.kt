package com.gardenyes.rowingmetrics.platform

import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.gardenyes.rowingmetrics.AppLanguage
import com.gardenyes.rowingmetrics.GpsSpeedSmoother
import com.gardenyes.rowingmetrics.StrokeDetectionSensitivity
import com.gardenyes.rowingmetrics.data.createActivityRepository
import kotlinx.coroutines.CoroutineScope

private const val LOCATION_UPDATE_INTERVAL_MS = 1_000L
private const val STROKE_SENSOR_SAMPLING_PERIOD_US = 25_000

class AndroidPlatformServices(
    private val context: Context,
    override val scope: CoroutineScope,
) : PlatformServices {
    override val settings: SettingsStore = AndroidSettingsStore(context)
    override val activities = createActivityRepository()
    override val locationTracker: LocationTracker = AndroidLocationTracker(context)
    override val motionSensors: MotionSensorSource = AndroidMotionSensorSource(context)
    override val orientationMonitor: OrientationMonitor = AndroidOrientationMonitor(context)

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun postToMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }

    override fun elapsedRealtimeMs(): Long = SystemClock.elapsedRealtime()

    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}

private class AndroidSettingsStore(
    context: Context,
) : SettingsStore {
    private val sp = context.getSharedPreferences("rowing_metrics", Context.MODE_PRIVATE)

    override fun getStrokeDetectionSensitivity(): StrokeDetectionSensitivity {
        val v = sp.getString("stroke_detection_sensitivity", StrokeDetectionSensitivity.DEFAULT.name)
            ?: StrokeDetectionSensitivity.DEFAULT.name
        return runCatching { StrokeDetectionSensitivity.valueOf(v) }
            .getOrDefault(StrokeDetectionSensitivity.DEFAULT)
    }

    override fun setStrokeDetectionSensitivity(s: StrokeDetectionSensitivity) {
        sp.edit().putString("stroke_detection_sensitivity", s.name).apply()
    }

    override fun getGpsDisplayAverageN(): Int {
        val v = sp.getInt("gps_display_average_n", 4).coerceIn(0, 8)
        return (if (v == 0) 1 else v).coerceIn(1, 8)
    }

    override fun setGpsDisplayAverageN(n: Int) {
        sp.edit().putInt("gps_display_average_n", n.coerceIn(1, 8)).apply()
    }

    override fun getGpsDisplayAverageCount(): Int =
        getGpsDisplayAverageN().coerceIn(1, GpsSpeedSmoother.FIFO_MAX_DEFAULT)

    override fun getAppLanguage(): AppLanguage =
        AppLanguage.fromStored(sp.getString("app_language", AppLanguage.DEFAULT.name))

    override fun setAppLanguage(language: AppLanguage) {
        sp.edit().putString("app_language", language.name).apply()
    }
}

private class AndroidLocationTracker(
    context: Context,
) : LocationTracker {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private var callback: LocationCallback? = null

    override fun start(onFix: (GpsFix) -> Unit) {
        val locationCallback =
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val loc = result.lastLocation ?: return
                    onFix(loc.toGpsFix())
                }
            }
        callback = locationCallback
        val request =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL_MS)
                .setMinUpdateIntervalMillis(LOCATION_UPDATE_INTERVAL_MS)
                .setWaitForAccurateLocation(true)
                .setMaxUpdateDelayMillis(LOCATION_UPDATE_INTERVAL_MS)
                .build()
        fusedClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper(),
        )
    }

    override fun stop() {
        callback?.let { fusedClient.removeLocationUpdates(it) }
        callback = null
    }

    private fun Location.toGpsFix(): GpsFix =
        GpsFix(
            latitude = latitude,
            longitude = longitude,
            accuracyM = if (hasAccuracy()) accuracy else Float.MAX_VALUE,
            speedMps = if (hasSpeed()) speed.toDouble() else null,
            hasAccuracy = hasAccuracy(),
            hasSpeed = hasSpeed(),
        )
}

private class AndroidMotionSensorSource(
    context: Context,
) : MotionSensorSource {
    private val sensorManager = context.getSystemService(SensorManager::class.java)
    private val linearAccelerometer: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val accelerometerFallback: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val strokeMotionSensor: Sensor? = linearAccelerometer ?: accelerometerFallback
    private val gravitySensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)

    override val isAvailable: Boolean = strokeMotionSensor != null

    private var sampleCallback: ((Float, Float, Float, FloatArray?) -> Unit)? = null
    private val lastGravity = FloatArray(3)
    private var gravityReady = false

    private val listener =
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when {
                    gravitySensor != null && event.sensor == gravitySensor -> {
                        lastGravity[0] = event.values[0]
                        lastGravity[1] = event.values[1]
                        lastGravity[2] = event.values[2]
                        gravityReady = true
                    }
                    strokeMotionSensor != null && event.sensor == strokeMotionSensor -> {
                        val g = if (gravityReady) lastGravity else null
                        sampleCallback?.invoke(
                            event.values[0],
                            event.values[1],
                            event.values[2],
                            g,
                        )
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

    override fun start(onSample: (ax: Float, ay: Float, az: Float, gravity: FloatArray?) -> Unit) {
        sampleCallback = onSample
        gravityReady = false
        strokeMotionSensor?.let { sensor ->
            sensorManager?.registerListener(
                listener,
                sensor,
                STROKE_SENSOR_SAMPLING_PERIOD_US,
                STROKE_SENSOR_SAMPLING_PERIOD_US,
            )
        }
        gravitySensor?.let { sensor ->
            sensorManager?.registerListener(
                listener,
                sensor,
                STROKE_SENSOR_SAMPLING_PERIOD_US,
                STROKE_SENSOR_SAMPLING_PERIOD_US,
            )
        }
    }

    override fun stop() {
        sampleCallback = null
        sensorManager?.unregisterListener(listener)
    }
}

private class AndroidOrientationMonitor(
    context: Context,
) : OrientationMonitor {
    private val appContext = context.applicationContext
    private var onChanged: (() -> Unit)? = null
    private var lastBucket: Int? = null
    private var registered = false

    private val callbacks =
        object : ComponentCallbacks {
            override fun onConfigurationChanged(newConfig: Configuration) {
                val bucket =
                    when (newConfig.orientation) {
                        Configuration.ORIENTATION_LANDSCAPE -> 0
                        Configuration.ORIENTATION_PORTRAIT -> 1
                        else -> return
                    }
                if (lastBucket == null) {
                    lastBucket = bucket
                    return
                }
                if (bucket != lastBucket) {
                    lastBucket = bucket
                    onChanged?.invoke()
                }
            }

            override fun onLowMemory() = Unit
        }

    override fun start(onOrientationChanged: () -> Unit) {
        onChanged = onOrientationChanged
        if (!registered) {
            appContext.registerComponentCallbacks(callbacks)
            registered = true
        }
        lastBucket =
            when (appContext.resources.configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> 0
                Configuration.ORIENTATION_PORTRAIT -> 1
                else -> null
            }
    }

    override fun stop() {
        if (registered) {
            appContext.unregisterComponentCallbacks(callbacks)
            registered = false
        }
        onChanged = null
        lastBucket = null
    }
}
