package com.gardenyes.rowingmetrics.platform

/**
 * Platform-specific hooks for permissions, CSV export, and app metadata.
 * Provided by each host (Android / iOS) via [LocalPlatformHandlers].
 */
data class PlatformHandlers(
    val hasLocationPermission: () -> Boolean,
    val requestLocationPermissionAndStart: (onGranted: () -> Unit) -> Unit,
    val exportActivitiesCsv: (csvBytes: ByteArray, suggestedFileName: String) -> Unit,
    val appVersion: () -> String,
)
