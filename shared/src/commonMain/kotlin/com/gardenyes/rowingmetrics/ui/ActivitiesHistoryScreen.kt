package com.gardenyes.rowingmetrics.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gardenyes.rowingmetrics.ActivitiesCsvExport
import com.gardenyes.rowingmetrics.CsvColumnHeaders
import com.gardenyes.rowingmetrics.data.CompletedActivity
import com.gardenyes.rowingmetrics.formatActivityElapsedTable
import com.gardenyes.rowingmetrics.formatActivityTableDate
import com.gardenyes.rowingmetrics.formatActivityTableStartHour
import com.gardenyes.rowingmetrics.formatDistanceTable
import com.gardenyes.rowingmetrics.formatOneDecimalTable
import com.gardenyes.rowingmetrics.generated.resources.Res
import com.gardenyes.rowingmetrics.generated.resources.activities_title
import com.gardenyes.rowingmetrics.generated.resources.cancel
import com.gardenyes.rowingmetrics.generated.resources.col_avg_speed
import com.gardenyes.rowingmetrics.generated.resources.col_date
import com.gardenyes.rowingmetrics.generated.resources.col_distance
import com.gardenyes.rowingmetrics.generated.resources.col_hour
import com.gardenyes.rowingmetrics.generated.resources.col_stroke_rate
import com.gardenyes.rowingmetrics.generated.resources.col_time
import com.gardenyes.rowingmetrics.generated.resources.csv_col_avg_speed
import com.gardenyes.rowingmetrics.generated.resources.csv_col_date
import com.gardenyes.rowingmetrics.generated.resources.csv_col_distance
import com.gardenyes.rowingmetrics.generated.resources.csv_col_hour
import com.gardenyes.rowingmetrics.generated.resources.csv_col_stroke_rate
import com.gardenyes.rowingmetrics.generated.resources.csv_col_time
import com.gardenyes.rowingmetrics.generated.resources.delete
import com.gardenyes.rowingmetrics.generated.resources.delete_all
import com.gardenyes.rowingmetrics.generated.resources.delete_all_message
import com.gardenyes.rowingmetrics.generated.resources.delete_all_title
import com.gardenyes.rowingmetrics.generated.resources.export_activities
import com.gardenyes.rowingmetrics.generated.resources.export_activities_confirm
import com.gardenyes.rowingmetrics.generated.resources.export_activities_message
import com.gardenyes.rowingmetrics.generated.resources.export_activities_title
import com.gardenyes.rowingmetrics.generated.resources.no_activities_message
import com.gardenyes.rowingmetrics.generated.resources.speed_kmh_suffix
import org.jetbrains.compose.resources.stringResource

private val ActivitiesPanelBackground = Color(0xFF161616)
private val ActivitiesPanelBorder = Color.White.copy(alpha = 0.08f)
private val ActivitiesLabelColor = Color.White.copy(alpha = 0.88f)
private val ActivitiesValueColor = Color.White

@Composable
fun ActivitiesHistoryScreen(
    activities: List<CompletedActivity>,
    onDelete: (Long) -> Unit,
    onDeleteAll: () -> Unit,
) {
    val handlers = LocalPlatformHandlers.current
    var showDeleteAllConfirm by remember { mutableStateOf(false) }
    var showExportConfirm by remember { mutableStateOf(false) }
    val resolvedCsvHeaders =
        CsvColumnHeaders(
            date = stringResource(Res.string.csv_col_date),
            hour = stringResource(Res.string.csv_col_hour),
            time = stringResource(Res.string.csv_col_time),
            strokeRate = stringResource(Res.string.csv_col_stroke_rate),
            avgSpeed = stringResource(Res.string.csv_col_avg_speed),
            distance = stringResource(Res.string.csv_col_distance),
        )

    if (showExportConfirm) {
        AlertDialog(
            onDismissRequest = { showExportConfirm = false },
            containerColor = ActivitiesPanelBackground,
            title = {
                Text(
                    text = stringResource(Res.string.export_activities_title),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            text = {
                Text(
                    text = stringResource(Res.string.export_activities_message),
                    color = ActivitiesLabelColor,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExportConfirm = false
                        handlers.exportActivitiesCsv(
                            ActivitiesCsvExport.buildCsvBytes(activities, resolvedCsvHeaders),
                            ActivitiesCsvExport.defaultFileName(),
                        )
                    },
                ) {
                    Text(
                        stringResource(Res.string.export_activities_confirm),
                        color = Color.White,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportConfirm = false }) {
                    Text(
                        stringResource(Res.string.cancel),
                        color = Color.White.copy(alpha = 0.88f),
                    )
                }
            },
        )
    }

    if (showDeleteAllConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirm = false },
            containerColor = ActivitiesPanelBackground,
            title = {
                Text(
                    text = stringResource(Res.string.delete_all_title),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            text = {
                Text(
                    text = stringResource(Res.string.delete_all_message),
                    color = ActivitiesLabelColor,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAll()
                        showDeleteAllConfirm = false
                    },
                ) {
                    Text(stringResource(Res.string.delete_all), color = Color(0xFFFF8A80))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllConfirm = false }) {
                    Text(
                        stringResource(Res.string.cancel),
                        color = Color.White.copy(alpha = 0.88f),
                    )
                }
            },
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        val landscape = maxWidth > maxHeight
        val padH = if (landscape) 32.dp else 16.dp
        val padV = if (landscape) 20.dp else 16.dp
        val titleSp = if (landscape) 22.sp else 20.sp
        val headerSp = if (landscape) 14.sp else 12.sp
        val cellSp = if (landscape) 15.sp else 13.sp
        val colDate = if (landscape) 108.dp else 96.dp
        val colHour = if (landscape) 64.dp else 56.dp
        val colTime = if (landscape) 72.dp else 64.dp
        val colSpm = if (landscape) 96.dp else 88.dp
        val colSpd = if (landscape) 96.dp else 88.dp
        val colDist = if (landscape) 112.dp else 100.dp
        val colDel = if (landscape) 88.dp else 76.dp
        val tableW = colDate + colHour + colTime + colSpm + colSpd + colDist + colDel
        val scroll = rememberScrollState()
        val speedSuffix = stringResource(Res.string.speed_kmh_suffix)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = padH, vertical = padV),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.activities_title),
                    color = Color.White,
                    fontSize = titleSp,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (activities.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = stringResource(Res.string.export_activities),
                            tint = Color.White.copy(alpha = 0.88f),
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { showExportConfirm = true },
                        )
                        Text(
                            text = stringResource(Res.string.delete_all),
                            color = Color(0xFFFF8A80),
                            fontSize = if (landscape) 16.sp else 15.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { showDeleteAllConfirm = true },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ActivitiesPanelBackground),
                border = BorderStroke(1.dp, ActivitiesPanelBorder),
                shape = RoundedCornerShape(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(scroll),
                    ) {
                        Column(
                            modifier = Modifier
                                .width(tableW)
                                .fillMaxHeight(),
                        ) {
                            ActivitiesTableHeader(
                                colDate = colDate,
                                colHour = colHour,
                                colTime = colTime,
                                colSpm = colSpm,
                                colSpd = colSpd,
                                colDist = colDist,
                                colDel = colDel,
                                fontSize = headerSp,
                            )
                            HorizontalDivider(
                                color = ActivitiesPanelBorder,
                                modifier = Modifier.padding(horizontal = 12.dp),
                            )
                            if (activities.isEmpty()) {
                                Text(
                                    text = stringResource(Res.string.no_activities_message),
                                    color = ActivitiesLabelColor,
                                    fontSize = cellSp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                ) {
                                    items(
                                        items = activities,
                                        key = { it.id },
                                    ) { row ->
                                        ActivityTableRow(
                                            entity = row,
                                            onDelete = { onDelete(row.id) },
                                            colDate = colDate,
                                            colHour = colHour,
                                            colTime = colTime,
                                            colSpm = colSpm,
                                            colSpd = colSpd,
                                            colDist = colDist,
                                            colDel = colDel,
                                            cellFontSize = cellSp,
                                            speedSuffix = speedSuffix,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivitiesTableHeader(
    colDate: Dp,
    colHour: Dp,
    colTime: Dp,
    colSpm: Dp,
    colSpd: Dp,
    colDist: Dp,
    colDel: Dp,
    fontSize: TextUnit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderCell(stringResource(Res.string.col_date), colDate, fontSize)
        HeaderCell(stringResource(Res.string.col_hour), colHour, fontSize)
        HeaderCell(stringResource(Res.string.col_time), colTime, fontSize)
        HeaderCell(stringResource(Res.string.col_stroke_rate), colSpm, fontSize)
        HeaderCell(stringResource(Res.string.col_avg_speed), colSpd, fontSize)
        HeaderCell(stringResource(Res.string.col_distance), colDist, fontSize)
        HeaderCell(" ", colDel, fontSize)
    }
}

@Composable
private fun HeaderCell(text: String, width: Dp, fontSize: TextUnit) {
    Text(
        text = text,
        color = ActivitiesLabelColor,
        fontSize = fontSize,
        fontWeight = FontWeight.SemiBold,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.width(width),
    )
}

@Composable
private fun ActivityTableRow(
    entity: CompletedActivity,
    onDelete: () -> Unit,
    colDate: Dp,
    colHour: Dp,
    colTime: Dp,
    colSpm: Dp,
    colSpd: Dp,
    colDist: Dp,
    colDel: Dp,
    cellFontSize: TextUnit,
    speedSuffix: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DataCell(formatActivityTableDate(entity.endedAtEpochMs), colDate, cellFontSize)
        DataCell(formatActivityTableStartHour(entity.endedAtEpochMs, entity.durationMs), colHour, cellFontSize)
        DataCell(formatActivityElapsedTable(entity.durationMs), colTime, cellFontSize)
        DataCell(formatOneDecimalTable(entity.avgStrokeRate), colSpm, cellFontSize)
        DataCell("${formatOneDecimalTable(entity.avgSpeedKmh)} $speedSuffix", colSpd, cellFontSize)
        DataCell(formatDistanceTable(entity.distanceMeters), colDist, cellFontSize)
        Text(
            text = stringResource(Res.string.delete),
            color = Color(0xFFFF8A80),
            fontSize = cellFontSize,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .width(colDel)
                .clickable(onClick = onDelete),
        )
    }
    HorizontalDivider(
        color = ActivitiesPanelBorder.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 12.dp),
    )
}

@Composable
private fun DataCell(text: String, width: Dp, fontSize: TextUnit) {
    Text(
        text = text,
        color = ActivitiesValueColor,
        fontSize = fontSize,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.width(width),
    )
}
