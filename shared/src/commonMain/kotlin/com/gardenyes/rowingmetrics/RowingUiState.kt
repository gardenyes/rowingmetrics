package com.gardenyes.rowingmetrics

data class RowingUiState(
    val running: Boolean = false,
    val avgStrokesPerMin: Double = 0.0,
    val strokeRateDetectionLive: Boolean = true,
    val currentSpeedKmh: Double = 0.0,
    val averageSpeedKmh: Double = 0.0,
    val activityElapsedMs: Long = 0L,
    val distanceMeters: Double = 0.0,
)
