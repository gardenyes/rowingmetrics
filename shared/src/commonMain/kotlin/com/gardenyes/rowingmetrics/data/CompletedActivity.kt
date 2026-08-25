package com.gardenyes.rowingmetrics.data

data class CompletedActivity(
    val id: Long = 0,
    val endedAtEpochMs: Long,
    val durationMs: Long,
    val avgStrokeRate: Double,
    val avgSpeedKmh: Double,
    val distanceMeters: Double,
)
