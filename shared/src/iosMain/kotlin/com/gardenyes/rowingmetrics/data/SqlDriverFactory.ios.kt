package com.gardenyes.rowingmetrics.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.gardenyes.rowingmetrics.data.sqldelight.RowingMetricsDatabase

actual fun createSqlDriver(): SqlDriver =
    NativeSqliteDriver(
        RowingMetricsDatabase.Schema,
        "rowing_metrics.db",
    )
