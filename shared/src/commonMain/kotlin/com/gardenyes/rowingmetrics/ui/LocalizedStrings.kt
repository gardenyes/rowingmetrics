package com.gardenyes.rowingmetrics.ui

import androidx.compose.runtime.Composable
import com.gardenyes.rowingmetrics.AppLanguage
import com.gardenyes.rowingmetrics.StrokeDetectionSensitivity
import com.gardenyes.rowingmetrics.generated.resources.Res
import com.gardenyes.rowingmetrics.generated.resources.language_catalan
import com.gardenyes.rowingmetrics.generated.resources.language_english
import com.gardenyes.rowingmetrics.generated.resources.language_french
import com.gardenyes.rowingmetrics.generated.resources.language_spanish
import com.gardenyes.rowingmetrics.generated.resources.sensitivity_high
import com.gardenyes.rowingmetrics.generated.resources.sensitivity_low
import com.gardenyes.rowingmetrics.generated.resources.sensitivity_medium
import org.jetbrains.compose.resources.stringResource

@Composable
fun languageDisplayName(language: AppLanguage): String =
    when (language) {
        AppLanguage.ENGLISH -> stringResource(Res.string.language_english)
        AppLanguage.CATALAN -> stringResource(Res.string.language_catalan)
        AppLanguage.SPANISH -> stringResource(Res.string.language_spanish)
        AppLanguage.FRENCH -> stringResource(Res.string.language_french)
    }

@Composable
fun sensitivityLabel(sensitivity: StrokeDetectionSensitivity): String =
    when (sensitivity) {
        StrokeDetectionSensitivity.VERY_HIGH -> stringResource(Res.string.sensitivity_high)
        StrokeDetectionSensitivity.MEDIUM -> stringResource(Res.string.sensitivity_medium)
        StrokeDetectionSensitivity.VERY_LOW -> stringResource(Res.string.sensitivity_low)
        StrokeDetectionSensitivity.HIGH,
        StrokeDetectionSensitivity.LOW,
        -> ""
    }
