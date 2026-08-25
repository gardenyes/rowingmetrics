package com.gardenyes.rowingmetrics.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.gardenyes.rowingmetrics.data.sqldelight.RowingMetricsDatabase

private var appContext: Context? = null

fun initSqlDriverContext(context: Context) {
    appContext = context.applicationContext
}

actual fun createSqlDriver(): SqlDriver {
    val ctx =
        requireNotNull(appContext) {
            "Call initSqlDriverContext(context) before using the database (e.g. in Application.onCreate)."
        }
    return AndroidSqliteDriver(
        RowingMetricsDatabase.Schema,
        ctx,
        "rowing_metrics.db",
    )
}
