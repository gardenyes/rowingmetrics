package com.gardenyes.rowingmetrics.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import com.gardenyes.rowingmetrics.data.sqldelight.RowingMetricsDatabase
import com.gardenyes.rowingmetrics.platform.ActivityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

expect fun createSqlDriver(): SqlDriver

fun createActivityRepository(): ActivityRepository {
    val driver = createSqlDriver()
    val db = RowingMetricsDatabase(driver)
    return SqlDelightActivityRepository(db)
}

private class SqlDelightActivityRepository(
    private val db: RowingMetricsDatabase,
) : ActivityRepository {
    override fun observeAll(): Flow<List<CompletedActivity>> =
        db.completedActivityQueries
            .observeAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows ->
                rows.map { row ->
                    CompletedActivity(
                        id = row.id,
                        endedAtEpochMs = row.endedAtEpochMs,
                        durationMs = row.durationMs,
                        avgStrokeRate = row.avgStrokeRate,
                        avgSpeedKmh = row.avgSpeedKmh,
                        distanceMeters = row.distanceMeters,
                    )
                }
            }

    override suspend fun insert(activity: CompletedActivity) {
        withContext(Dispatchers.Default) {
            db.completedActivityQueries.insertActivity(
                endedAtEpochMs = activity.endedAtEpochMs,
                durationMs = activity.durationMs,
                avgStrokeRate = activity.avgStrokeRate,
                avgSpeedKmh = activity.avgSpeedKmh,
                distanceMeters = activity.distanceMeters,
            )
        }
    }

    override suspend fun deleteById(id: Long) {
        withContext(Dispatchers.Default) {
            db.completedActivityQueries.deleteById(id)
        }
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.Default) {
            db.completedActivityQueries.deleteAll()
        }
    }
}
