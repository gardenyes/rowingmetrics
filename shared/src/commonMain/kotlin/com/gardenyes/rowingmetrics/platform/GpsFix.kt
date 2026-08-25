package com.gardenyes.rowingmetrics.platform

/** Platform-neutral GPS fix passed into the shared session engine. */
data class GpsFix(
    val latitude: Double,
    val longitude: Double,
    val accuracyM: Float,
    val speedMps: Double?,
    val hasAccuracy: Boolean,
    val hasSpeed: Boolean,
)
