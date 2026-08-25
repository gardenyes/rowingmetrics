package com.gardenyes.rowingmetrics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gardenyes.rowingmetrics.StrokeDetectionSensitivity
import kotlin.math.roundToInt

private val LabelAreaHeight = 40.dp
private val TickColor = Color.White.copy(alpha = 0.45f)
private val LabelColor = Color.White

private fun Modifier.alignHorizontallyAt(fraction: Float): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val x =
            (constraints.maxWidth * fraction - placeable.width / 2f)
                .roundToInt()
                .coerceIn(0, (constraints.maxWidth - placeable.width).coerceAtLeast(0))
        layout(constraints.maxWidth, placeable.height) {
            placeable.placeRelative(x, 0)
        }
    }

/**
 * Horizontal 5-step stroke sensitivity control. Left = strictest ([StrokeDetectionSensitivity.VERY_LOW]),
 * right = most permissive ([StrokeDetectionSensitivity.VERY_HIGH]).
 * Tick marks and text labels (Low / Medium / High) are shown below the slider.
 */
@Composable
fun StrokeSensitivitySlider(
    value: StrokeDetectionSensitivity,
    onValueChange: (StrokeDetectionSensitivity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val levels = StrokeDetectionSensitivity.entries
    val lastIndex = levels.lastIndex
    val sliderColors =
        SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = Color.White.copy(alpha = 0.65f),
            inactiveTrackColor = Color.White.copy(alpha = 0.22f),
        )

    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = (lastIndex - value.ordinal).toFloat(),
            onValueChange = { v ->
                val idx = (lastIndex - v.roundToInt()).coerceIn(0, lastIndex)
                onValueChange(levels[idx])
            },
            valueRange = 0f..lastIndex.toFloat(),
            steps = lastIndex - 1,
            modifier = Modifier.fillMaxWidth(),
            colors = sliderColors,
        )
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(LabelAreaHeight)
                .padding(top = 4.dp),
        ) {
            levels.forEachIndexed { index, level ->
                val frac = (lastIndex - index) / lastIndex.toFloat()
                Box(
                    modifier = Modifier.alignHorizontallyAt(frac),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(if (level.showsSensitivityLabel) 6.dp else 8.dp)
                                .background(TickColor),
                        )
                        if (level.showsSensitivityLabel) {
                            Text(
                                text = sensitivityLabel(level),
                                color = LabelColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                            )
                        }
                    }
                }
            }
        }
    }
}
