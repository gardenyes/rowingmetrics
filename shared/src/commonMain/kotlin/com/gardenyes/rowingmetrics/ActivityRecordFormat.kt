package com.gardenyes.rowingmetrics

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private fun localDateTime(epochMillis: Long) =
    Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())

fun formatActivityTableDate(epochMillis: Long): String {
    val dt = localDateTime(epochMillis)
    return "${dt.dayOfMonth.toString().padStart(2, '0')}-${dt.monthNumber.toString().padStart(2, '0')}-${dt.year}"
}

fun activityStartedAtEpochMs(endedAtEpochMs: Long, durationMs: Long): Long =
    (endedAtEpochMs - durationMs.coerceAtLeast(0L)).coerceAtLeast(0L)

fun formatActivityTableHour(epochMillis: Long): String {
    val t = localDateTime(epochMillis)
    return "%02d:%02d".format(t.hour, t.minute)
}

fun formatActivityTableStartHour(endedAtEpochMs: Long, durationMs: Long): String =
    formatActivityTableHour(activityStartedAtEpochMs(endedAtEpochMs, durationMs))

fun formatActivityElapsedTable(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val h = totalSec / 3600L
    val m = (totalSec % 3600L) / 60L
    val s = totalSec % 60L
    return if (h < 1L) {
        "%02d:%02d".format(m, s)
    } else {
        "%02d:%02d:%02d".format(h, m, s)
    }
}

fun formatOneDecimalTable(x: Double): String =
    "%.1f".format(x)

fun formatDistanceTable(meters: Double): String =
    if (meters >= 1000.0) {
        "%.2f km".format(meters / 1000.0)
    } else {
        "%.0f m".format(meters)
    }
