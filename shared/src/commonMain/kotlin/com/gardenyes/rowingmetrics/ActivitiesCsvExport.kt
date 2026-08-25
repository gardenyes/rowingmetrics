package com.gardenyes.rowingmetrics

import com.gardenyes.rowingmetrics.data.CompletedActivity
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class CsvColumnHeaders(
    val date: String,
    val hour: String,
    val time: String,
    val strokeRate: String,
    val avgSpeed: String,
    val distance: String,
)

object ActivitiesCsvExport {
    fun buildCsvBytes(
        activities: List<CompletedActivity>,
        headers: CsvColumnHeaders,
    ): ByteArray {
        val sb = StringBuilder()
        sb.append('\uFEFF')
        sb.appendLine(
            listOf(
                headers.date,
                headers.hour,
                headers.time,
                headers.strokeRate,
                headers.avgSpeed,
                headers.distance,
            ).joinToString(","),
        )
        for (entity in activities) {
            sb.append(csvField(formatActivityTableDate(entity.endedAtEpochMs)))
            sb.append(',')
            sb.append(csvField(formatActivityTableStartHour(entity.endedAtEpochMs, entity.durationMs)))
            sb.append(',')
            sb.append(csvField(formatActivityElapsedTable(entity.durationMs)))
            sb.append(',')
            sb.append(csvField(formatOneDecimalTable(entity.avgStrokeRate)))
            sb.append(',')
            sb.append(csvField(formatOneDecimalTable(entity.avgSpeedKmh)))
            sb.append(',')
            sb.append(csvField(formatDistanceTable(entity.distanceMeters)))
            sb.appendLine()
        }
        return sb.toString().encodeToByteArray()
    }

    fun defaultFileName(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val stamp =
            "%04d%02d%02d_%02d%02d%02d".format(
                now.year,
                now.monthNumber,
                now.dayOfMonth,
                now.hour,
                now.minute,
                now.second,
            )
        return "rowing_activities_$stamp.csv"
    }

    private fun csvField(value: String): String {
        if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            return "\"" + value.replace("\"", "\"\"") + "\""
        }
        return value
    }
}
