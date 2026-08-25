package com.gardenyes.rowingmetrics.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gardenyes.rowingmetrics.AppLanguage
import com.gardenyes.rowingmetrics.RowingSessionController
import com.gardenyes.rowingmetrics.RowingUiState
import com.gardenyes.rowingmetrics.generated.resources.Res
import com.gardenyes.rowingmetrics.generated.resources.app_title
import com.gardenyes.rowingmetrics.generated.resources.average_speed
import com.gardenyes.rowingmetrics.generated.resources.clock
import com.gardenyes.rowingmetrics.generated.resources.config_title
import com.gardenyes.rowingmetrics.generated.resources.detection_sensitivity
import com.gardenyes.rowingmetrics.generated.resources.distance
import com.gardenyes.rowingmetrics.generated.resources.language
import com.gardenyes.rowingmetrics.generated.resources.speed
import com.gardenyes.rowingmetrics.generated.resources.speed_smoothing
import com.gardenyes.rowingmetrics.generated.resources.speed_smoothing_desc
import com.gardenyes.rowingmetrics.generated.resources.start
import com.gardenyes.rowingmetrics.generated.resources.stop
import com.gardenyes.rowingmetrics.generated.resources.stroke_profile
import com.gardenyes.rowingmetrics.generated.resources.stroke_rate
import com.gardenyes.rowingmetrics.generated.resources.time
import org.jetbrains.compose.resources.stringResource
import com.gardenyes.rowingmetrics.platform.PlatformHandlers
import com.gardenyes.rowingmetrics.platform.PlatformServices
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.round
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

val LocalPlatformHandlers = staticCompositionLocalOf<PlatformHandlers> {
    error("PlatformHandlers not provided")
}

@Composable
fun RowingApp(
    platform: PlatformServices,
    handlers: PlatformHandlers,
) {
    var appLanguage by remember { mutableStateOf(platform.settings.getAppLanguage()) }
    CompositionLocalProvider(LocalPlatformHandlers provides handlers) {
        AppEnvironment(appLanguage.localeTag) {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black,
                ) {
                    RowingScreen(
                        platform = platform,
                        appLanguage = appLanguage,
                        onAppLanguageChange = { language ->
                            appLanguage = language
                            platform.settings.setAppLanguage(language)
                        },
                    )
                }
            }
        }
    }
}

/** Start button (session not running). */
private val ButtonStartGreen = Color(0xFF2E7D32)
/** Stop button (session running). */
private val ButtonStopRed = Color(0xFFC62828)
/** Stroke rate band: no recent stroke detection ( while session is running). */
private val StrokeRateBandNoStrokeBackground = Color(0xFFFFCDD2)

/**
 * When the stroke field shows [StrokeRateBandNoStrokeBackground], one half of the light-red ↔ black blink
 * (forward leg); full back-and-forth = 2× this. Higher = slower pulse.
 */
private const val STROKE_WARN_BLINK_MS = 1_000

/** Captions on colored bands; values use solid white. */
private val MetricFieldLabelOnColor = Color.White.copy(alpha = 0.88f)
private val MetricFieldValueOnColor = Color.White

/** Configuration screen card panels (slightly above black background). */
private val ConfigPanelBackground = Color(0xFF161616)
private val ConfigPanelBorder = Color.White.copy(alpha = 0.08f)

/** Stroke rate (avg SPM) value — largest numeric on screen; black band like other metrics. */
private const val StrokeValueFontFraction = 0.44f
private const val StrokeValueFontMinSp = 28f
private const val StrokeValueFontMaxSp = 96f

/**
 * [BlackMetricBand] (Speed, Average speed, Distance, Time): one shared, slightly larger than before.
 */
private const val QuartetMetricValueFontFraction = 0.38f
private const val QuartetMetricValueMinSp = 25f
private const val QuartetMetricValueMaxSp = 85f
/** Stroke row "Stroke rate" label only — smaller than quartet label so the four can scale up. */
private const val StrokeRowLabelFontFraction = 0.15f
private const val StrokeRowLabelFontMinSp = 11f
private const val StrokeRowLabelFontMaxSp = 18f

/** Start/Stop: not tied to [quartetMetricValueSp]. */
private const val ButtonCaptionFontFraction = 0.24f
private const val ButtonCaptionFontMinSp = 14f
private const val ButtonCaptionFontMaxSp = 40f

/**
 * Column weights: [LAYOUT_STROKE_RATE_BASE] is the stroke row before a 10% height cut; the removed 10% is
 * re-added to the rows below in proportion to their [LAYOUT_SUM_BELOW_STROKE] shares. The stroke row is then
 * reduced by 5% of its *post-trim* height; that 5% is re-added the same way (proportional to the rows below).
 * **Speed, Average speed, Distance, and Time** share one equal height ([LAYOUT_WEIGHT_METRIC_QUARTET]).
 */
private const val LAYOUT_STROKE_RATE_BASE = 2.135f * 0.85f
/** After the first 10% trim from base: stroke keeps 90% of that slice. */
private const val LAYOUT_STROKE_RATE_PRIMARY_SHARE = 0.9f
/**
 * Shrink the stroke row by 5% of its (post–first-trim) size; the freed flex is in
 * [LAYOUT_REDIST_STROKE_TO_OTHERS] along with the original 10% from base.
 */
private const val LAYOUT_STROKE_RATE_HEIGHT_SHRINK = 0.05f
private const val LAYOUT_WEIGHT_STROKE_RATE =
    LAYOUT_STROKE_RATE_BASE * LAYOUT_STROKE_RATE_PRIMARY_SHARE * (1f - LAYOUT_STROKE_RATE_HEIGHT_SHRINK)
private const val LAYOUT_REDIST_STROKE_TO_OTHERS =
    LAYOUT_STROKE_RATE_BASE * (1f - LAYOUT_STROKE_RATE_PRIMARY_SHARE) +
        LAYOUT_STROKE_RATE_BASE * LAYOUT_STROKE_RATE_PRIMARY_SHARE * LAYOUT_STROKE_RATE_HEIGHT_SHRINK
/**
 * Below stroke: four equal flex bands (Speed, Avg speed, Distance, Time) and Start/Stop.
 * Sum of bases = 0.675×4 + 0.965 = pre-change 0.6+0.6+0.75+0.75+0.965.
 */
private const val LAYOUT_PER_QUARTET_METRIC = 0.675f
private const val LAYOUT_SUM_BELOW_STROKE = LAYOUT_PER_QUARTET_METRIC * 4f + 0.965f
private const val LAYOUT_WEIGHT_METRIC_QUARTET =
    LAYOUT_PER_QUARTET_METRIC + LAYOUT_REDIST_STROKE_TO_OTHERS * LAYOUT_PER_QUARTET_METRIC / LAYOUT_SUM_BELOW_STROKE
private const val LAYOUT_WEIGHT_START_STOP = 0.965f + LAYOUT_REDIST_STROKE_TO_OTHERS * 0.965f / LAYOUT_SUM_BELOW_STROKE

/**
 * Vertical share of the block below stroke in portrait: four [LAYOUT_WEIGHT_METRIC_QUARTET] rows + start/stop.
 */
private const val LAYOUT_SUM_NON_STROKE =
    4f * LAYOUT_WEIGHT_METRIC_QUARTET + LAYOUT_WEIGHT_START_STOP

/**
 * Landscape: left column (stroke + button) vs right column (spd+avg, distance, time) — sum 1f for [Row] weights.
 */
private const val LAYOUT_LANDSCAPE_STROKE_WIDTH = 0.38f
private const val LAYOUT_LANDSCAPE_METRICS_WIDTH = 0.62f
/**
 * Text scale for quartet-style fields in landscape (not [StrokesBand] / stroke rate).
 */
private const val LANDSCAPE_QUARTET_TYPO_SCALE = 1.22f
/** Landscape: Start/Stop caption in the left column under the stroke. */
private const val LANDSCAPE_BUTTON_TYPO_SCALE = 1.2f

@Composable
private fun RowingScreen(
    platform: PlatformServices,
    appLanguage: AppLanguage,
    onAppLanguageChange: (AppLanguage) -> Unit,
) {
    val handlers = LocalPlatformHandlers.current
    val scope = rememberCoroutineScope()
    val controller = remember(platform) { RowingSessionController(platform) }
    var state by remember { mutableStateOf(RowingUiState()) }

    DisposableEffect(controller) {
        val cb: (RowingUiState) -> Unit = { state = it }
        controller.setListener(cb)
        onDispose {
            controller.clearListener()
            controller.dispose()
        }
    }

    // Narrow each band by 20px total vs full width; equal horizontal padding keeps rectangles centered.
    val density = LocalDensity.current.density
    val fieldHorizontalPadding = 16.dp + (10f / density).dp
    val contentHorizontal = Modifier.padding(horizontal = fieldHorizontalPadding)
    val onSessionAction: () -> Unit = {
        when {
            state.running -> controller.stopSession()
            handlers.hasLocationPermission() -> controller.startSession()
            else -> handlers.requestLocationPermissionAndStart { controller.startSession() }
        }
    }

    val pagerState = rememberPagerState(pageCount = { 4 })
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) { page ->
        when (page) {
            0 ->
                RowingMetricsContent(
                    state = state,
                    contentHorizontal = contentHorizontal,
                    onSessionAction = onSessionAction,
                )
            1 -> {
                val activities by platform.activities.observeAll().collectAsState(initial = emptyList())
                ActivitiesHistoryScreen(
                    activities = activities,
                    onDelete = { id ->
                        scope.launch { platform.activities.deleteById(id) }
                    },
                    onDeleteAll = {
                        scope.launch { platform.activities.deleteAll() }
                    },
                )
            }
            2 ->
                RowingConfigurationScreen(
                    controller = controller,
                    appLanguage = appLanguage,
                    onAppLanguageChange = onAppLanguageChange,
                )
            3 -> RegisterInformationScreen()
        }
    }
}

@Composable
private fun RegisterInformationScreen() {
    val handlers = LocalPlatformHandlers.current
    val appVersion = remember { handlers.appVersion() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ConfigPanelBackground),
            border = BorderStroke(1.dp, ConfigPanelBorder),
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(Res.string.app_title),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "v$appVersion",
                    color = MetricFieldValueOnColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "gardenyes@gmail.com",
                    color = MetricFieldValueOnColor,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun RowingMetricsContent(
    state: RowingUiState,
    contentHorizontal: Modifier,
    onSessionAction: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight
        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top,
            ) {
                LandscapeLeftStrokeAndButton(
                    state = state,
                    contentHorizontal = contentHorizontal,
                    isRunning = state.running,
                    onSessionAction = onSessionAction,
                    modifier = Modifier
                        .weight(LAYOUT_LANDSCAPE_STROKE_WIDTH)
                        .fillMaxHeight()
                        .fillMaxWidth(),
                )
                LandscapeRightContent(
                    state = state,
                    contentHorizontal = contentHorizontal,
                    quartetTypographyScale = LANDSCAPE_QUARTET_TYPO_SCALE,
                    modifier = Modifier
                        .weight(LAYOUT_LANDSCAPE_METRICS_WIDTH)
                        .fillMaxHeight()
                        .fillMaxWidth(),
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                StrokesFieldBox(
                    state = state,
                    contentHorizontal = contentHorizontal,
                    modifier = Modifier
                        .weight(LAYOUT_WEIGHT_STROKE_RATE)
                        .fillMaxWidth(),
                )
                NonStrokeContent(
                    state = state,
                    contentHorizontal = contentHorizontal,
                    onSessionAction = onSessionAction,
                    isRunning = state.running,
                    modifier = Modifier
                        .weight(LAYOUT_SUM_NON_STROKE)
                        .fillMaxWidth()
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun RowingConfigurationScreen(
    controller: RowingSessionController,
    appLanguage: AppLanguage,
    onAppLanguageChange: (AppLanguage) -> Unit,
) {
    var level by remember { mutableStateOf(controller.getStrokeDetectionSensitivity()) }
    var speedAverageN by remember { mutableIntStateOf(controller.getGpsDisplayAverageN()) }
    var languageMenuExpanded by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()
    val dropdownButtonColors =
        ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White,
        )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(scroll)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Text(
            text = stringResource(Res.string.config_title),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ConfigPanelBackground),
            border = BorderStroke(1.dp, ConfigPanelBorder),
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(Res.string.stroke_profile),
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.detection_sensitivity),
                    color = MetricFieldLabelOnColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                )
                Spacer(modifier = Modifier.height(12.dp))
                StrokeSensitivitySlider(
                    value = level,
                    onValueChange = { sensitivity ->
                        controller.setStrokeDetectionSensitivity(sensitivity)
                        level = sensitivity
                    },
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ConfigPanelBackground),
            border = BorderStroke(1.dp, ConfigPanelBorder),
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(Res.string.speed_smoothing),
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.speed_smoothing_desc),
                    color = MetricFieldLabelOnColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "$speedAverageN",
                    color = MetricFieldValueOnColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                )
                Slider(
                    value = speedAverageN.toFloat(),
                    onValueChange = { v ->
                        val i = v.roundToInt().coerceIn(1, 8)
                        if (i != speedAverageN) {
                            speedAverageN = i
                            controller.setGpsDisplayAverageN(i)
                        }
                    },
                    valueRange = 1f..8f,
                    steps = 6,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White.copy(alpha = 0.65f),
                        inactiveTrackColor = Color.White.copy(alpha = 0.22f),
                    ),
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ConfigPanelBackground),
            border = BorderStroke(1.dp, ConfigPanelBorder),
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(Res.string.language),
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { languageMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = dropdownButtonColors,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = languageDisplayName(appLanguage),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start,
                        )
                    }
                    DropdownMenu(
                        expanded = languageMenuExpanded,
                        onDismissRequest = { languageMenuExpanded = false },
                        containerColor = ConfigPanelBackground,
                    ) {
                        AppLanguage.entries.forEach { language ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = languageDisplayName(language),
                                        color = Color.White,
                                    )
                                },
                                onClick = {
                                    onAppLanguageChange(language)
                                    languageMenuExpanded = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StrokesFieldBox(
    state: RowingUiState,
    contentHorizontal: Modifier,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.then(contentHorizontal)) {
        StrokesBand(
            avgStrokesPerMin = state.avgStrokesPerMin,
            shouldBlinkNoStrokeWarning = state.running && !state.strokeRateDetectionLive,
        )
    }
}

/**
 * Landscape only: [StrokesBand] (unchanged typography) + [SessionStartStopButton] below, same column width
 * (weights [LAYOUT_WEIGHT_STROKE_RATE] : [LAYOUT_WEIGHT_START_STOP] to mirror portrait stroke vs button).
 */
@Composable
private fun LandscapeLeftStrokeAndButton(
    state: RowingUiState,
    contentHorizontal: Modifier,
    isRunning: Boolean,
    onSessionAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .then(contentHorizontal),
    ) {
        Box(
            modifier = Modifier
                .weight(LAYOUT_WEIGHT_STROKE_RATE)
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            StrokesBand(
                avgStrokesPerMin = state.avgStrokesPerMin,
                shouldBlinkNoStrokeWarning = state.running && !state.strokeRateDetectionLive,
            )
        }
        Box(
            modifier = Modifier
                .weight(LAYOUT_WEIGHT_START_STOP)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            SessionStartStopButton(
                isRunning = isRunning,
                onClick = onSessionAction,
                modifier = Modifier.fillMaxWidth(),
                captionScale = LANDSCAPE_BUTTON_TYPO_SCALE,
            )
        }
    }
}

/**
 * Landscape: Speed and Average speed on one row, then distance and time+clock, larger [quartetTypographyScale]
 * text (no [StrokesBand] here). All five fields use the same row height; a bottom spacer leaves free space
 * aligned with the Start/Stop area in the left column.
 */
@Composable
private fun LandscapeRightContent(
    state: RowingUiState,
    contentHorizontal: Modifier,
    quartetTypographyScale: Float,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .then(contentHorizontal),
    ) {
        val wq = LAYOUT_WEIGHT_METRIC_QUARTET
        val bottomGapWeight = LAYOUT_WEIGHT_START_STOP
        val weightSum = wq + wq + wq + bottomGapWeight
        val textBandHeight = maxHeight * wq / weightSum
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .weight(wq)
                    .fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .fillMaxWidth(),
                ) {
                    CurrentSpeedBand(
                        kmh = state.currentSpeedKmh,
                        quartetTypographyScale = quartetTypographyScale,
                        textBandHeight = textBandHeight,
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .fillMaxWidth(),
                ) {
                    AverageSpeedBand(
                        kmh = state.averageSpeedKmh,
                        quartetTypographyScale = quartetTypographyScale,
                        textBandHeight = textBandHeight,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(wq)
                    .fillMaxWidth()
                    .fillMaxHeight(),
            ) {
                DistanceBand(
                    meters = state.distanceMeters,
                    quartetTypographyScale = quartetTypographyScale,
                    textBandHeight = textBandHeight,
                )
            }
            Box(
                modifier = Modifier
                    .weight(wq)
                    .fillMaxWidth()
                    .fillMaxHeight(),
            ) {
                SessionTimeAndLocalClockRow(
                    elapsedMs = state.activityElapsedMs,
                    quartetTypographyScale = quartetTypographyScale,
                    textBandHeight = textBandHeight,
                )
            }
            Spacer(
                modifier = Modifier
                    .weight(bottomGapWeight)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun NonStrokeContent(
    state: RowingUiState,
    contentHorizontal: Modifier,
    isRunning: Boolean,
    onSessionAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .weight(LAYOUT_WEIGHT_METRIC_QUARTET)
                .fillMaxWidth()
                .then(contentHorizontal),
        ) {
            CurrentSpeedBand(kmh = state.currentSpeedKmh)
        }
        Box(
            modifier = Modifier
                .weight(LAYOUT_WEIGHT_METRIC_QUARTET)
                .fillMaxWidth()
                .then(contentHorizontal),
        ) {
            AverageSpeedBand(kmh = state.averageSpeedKmh)
        }
        Box(
            modifier = Modifier
                .weight(LAYOUT_WEIGHT_METRIC_QUARTET)
                .fillMaxWidth()
                .then(contentHorizontal),
        ) {
            DistanceBand(meters = state.distanceMeters)
        }
        Box(
            modifier = Modifier
                .weight(LAYOUT_WEIGHT_METRIC_QUARTET)
                .fillMaxWidth()
                .then(contentHorizontal),
        ) {
            SessionTimeAndLocalClockRow(elapsedMs = state.activityElapsedMs)
        }
        Box(
            modifier = Modifier
                .weight(LAYOUT_WEIGHT_START_STOP)
                .fillMaxWidth()
                .then(contentHorizontal),
            contentAlignment = Alignment.Center,
        ) {
            SessionStartStopButton(
                isRunning = isRunning,
                onClick = onSessionAction,
            )
        }
    }
}

@Composable
private fun SessionStartStopButton(
    isRunning: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    captionScale: Float = 1f,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
    ) {
        val captionSp = scaledSp(
            maxHeight,
            fraction = ButtonCaptionFontFraction * captionScale,
            min = ButtonCaptionFontMinSp * captionScale,
            max = ButtonCaptionFontMaxSp * captionScale,
        )
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .defaultMinSize(minHeight = 0.dp),
            shape = RoundedCornerShape(0.dp),
            contentPadding = PaddingValues(vertical = 10.dp, horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRunning) ButtonStopRed else ButtonStartGreen,
            ),
        ) {
            Text(
                text = if (isRunning) stringResource(Res.string.stop) else stringResource(Res.string.start),
                fontSize = captionSp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/**
 * Stroke rate: [strokeRowLabelSp] for the title; SPM value stays larger.
 * When [shouldBlinkNoStrokeWarning] is true, the background blinks between [StrokeRateBandNoStrokeBackground] and
 * [Color.Black]; otherwise it stays black.
 */
@Composable
private fun StrokesBand(avgStrokesPerMin: Double, shouldBlinkNoStrokeWarning: Boolean) {
    // Transition always remembered (Compose rules); only affects color when the warning is active.
    val pulse = rememberInfiniteTransition(label = "strokeRateNoDetection")
    val t by pulse.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                STROKE_WARN_BLINK_MS,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    val bandBackground = if (shouldBlinkNoStrokeWarning) {
        lerp(StrokeRateBandNoStrokeBackground, Color.Black, t)
    } else {
        Color.Black
    }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(bandBackground),
        contentAlignment = Alignment.Center,
    ) {
        val h = maxHeight
        val labelSp = strokeRowLabelSp(h)
        val primaryValueSp = scaledSp(
            h,
            fraction = StrokeValueFontFraction * 0.94f,
            min = StrokeValueFontMinSp,
            max = StrokeValueFontMaxSp,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(Res.string.stroke_rate),
                fontSize = labelSp,
                fontWeight = FontWeight.Medium,
                color = MetricFieldLabelOnColor,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(h * 0.022f))
            Text(
                text = round(avgStrokesPerMin).toInt().toString(),
                fontSize = primaryValueSp,
                fontWeight = FontWeight.Bold,
                color = MetricFieldValueOnColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CurrentSpeedBand(
    kmh: Double,
    quartetTypographyScale: Float = 1f,
    /** In landscape, pass one quartet-row height so type matches Time (Speed row is 2× taller). */
    textBandHeight: Dp? = null,
) {
    BlackMetricBand(
        label = stringResource(Res.string.speed),
        valueText = formatOneDecimal(kmh),
        quartetTypographyScale = quartetTypographyScale,
        textBandHeight = textBandHeight,
    )
}

@Composable
private fun AverageSpeedBand(
    kmh: Double,
    quartetTypographyScale: Float = 1f,
    textBandHeight: Dp? = null,
) {
    BlackMetricBand(
        label = stringResource(Res.string.average_speed),
        valueText = formatOneDecimal(kmh),
        quartetTypographyScale = quartetTypographyScale,
        textBandHeight = textBandHeight,
    )
}

@Composable
private fun DistanceBand(
    meters: Double,
    quartetTypographyScale: Float = 1f,
    textBandHeight: Dp? = null,
) {
    BlackMetricBand(
        label = stringResource(Res.string.distance),
        valueText = round(meters).toInt().toString(),
        quartetTypographyScale = quartetTypographyScale,
        textBandHeight = textBandHeight,
    )
}

/**
 * One shared label + value size for Speed, Average speed, Distance; [SessionTimeAndLocalClockRow] uses
 * the same metrics for the split **Time** / **Clock** line ([LAYOUT_WEIGHT_METRIC_QUARTET] for the full row).
 */
@Composable
private fun BlackMetricBand(
    label: String,
    valueText: String,
    quartetTypographyScale: Float = 1f,
    textBandHeight: Dp? = null,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        val h = textBandHeight ?: maxHeight
        BlackMetricBandInner(
            label = label,
            valueText = valueText,
            bandHeight = h,
            quartetTypographyScale = quartetTypographyScale,
        )
    }
}

@Composable
private fun BlackMetricBandInner(
    label: String,
    valueText: String,
    bandHeight: Dp,
    quartetTypographyScale: Float = 1f,
) {
    val labelSp = quartetMetricLabelSp(bandHeight, quartetTypographyScale)
    val valueSp = quartetMetricValueSp(bandHeight, quartetTypographyScale)
    val h = bandHeight
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = labelSp,
            fontWeight = FontWeight.Medium,
            color = MetricFieldLabelOnColor,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(h * 0.04f))
        Text(
            text = valueText,
            fontSize = valueSp,
            fontWeight = FontWeight.SemiBold,
            color = MetricFieldValueOnColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Session **Time** (left): [formatActivityElapsed] (minutes:seconds, or with hours from 1h up).
 * **Clock** (right): system local time [formatLocalClockHhMm]. Optional [quartetTypographyScale] for landscape.
 */
@Composable
private fun SessionTimeAndLocalClockRow(
    elapsedMs: Long,
    quartetTypographyScale: Float = 1f,
    /** In landscape, same as other quartet fields for identical caption/value sizes. */
    textBandHeight: Dp? = null,
) {
    var clockEpochMs by remember { mutableStateOf(Clock.System.now().toEpochMilliseconds()) }
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(1_000L)
            clockEpochMs = Clock.System.now().toEpochMilliseconds()
        }
    }
    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color.Black),
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                val h = textBandHeight ?: maxHeight
                BlackMetricBandInner(
                    label = stringResource(Res.string.time),
                    valueText = formatActivityElapsed(elapsedMs),
                    bandHeight = h,
                    quartetTypographyScale = quartetTypographyScale,
                )
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color.Black),
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                val h = textBandHeight ?: maxHeight
                BlackMetricBandInner(
                    label = stringResource(Res.string.clock),
                    valueText = formatLocalClockHhMm(clockEpochMs),
                    bandHeight = h,
                    quartetTypographyScale = quartetTypographyScale,
                )
            }
        }
    }
}

/** Map band height to a text size that fits the allocated slice of the screen. */
private fun scaledSp(height: Dp, fraction: Float, min: Float, max: Float): TextUnit {
    val raw = height.value * fraction
    return raw.coerceIn(min, max).sp
}

/** "Stroke rate" line only. */
private fun strokeRowLabelSp(bandHeight: Dp): TextUnit =
    scaledSp(
        bandHeight,
        fraction = StrokeRowLabelFontFraction,
        min = StrokeRowLabelFontMinSp,
        max = StrokeRowLabelFontMaxSp,
    )

/** Label for the four equal-height metric bands; [typographyScale] e.g. [LANDSCAPE_QUARTET_TYPO_SCALE] in landscape. */
private fun quartetMetricLabelSp(bandHeight: Dp, typographyScale: Float = 1f): TextUnit =
    scaledSp(
        bandHeight,
        fraction = 0.175f * typographyScale,
        min = 12f * typographyScale,
        max = 20f * typographyScale,
    )

/** Value for metric bands; [typographyScale] e.g. [LANDSCAPE_QUARTET_TYPO_SCALE] in landscape. */
private fun quartetMetricValueSp(bandHeight: Dp, typographyScale: Float = 1f): TextUnit =
    scaledSp(
        bandHeight,
        fraction = QuartetMetricValueFontFraction * 0.96f * typographyScale,
        min = QuartetMetricValueMinSp * typographyScale,
        max = QuartetMetricValueMaxSp * typographyScale,
    )

private fun formatOneDecimal(x: Double): String = "%.1f".format(x)

/**
 * Elapsed session time (Start→Stop): `MM:SS` with minutes and seconds while under one hour; from **1h**
 * upward, show `##:##:##` (hours, minutes, seconds, zero-padded).
 */
private fun formatActivityElapsed(ms: Long): String {
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

/** Device system local time for [SessionTimeAndLocalClockRow] — hour and minutes only (`##:##`). */
private fun formatLocalClockHhMm(epochMillis: Long): String {
    val t = kotlinx.datetime.Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return "%02d:%02d".format(t.hour, t.minute)
}
