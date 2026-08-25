package com.gardenyes.rowingmetrics.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import platform.Foundation.NSUserDefaults

actual object LocalAppLocale {
    private const val LANG_KEY = "AppleLanguages"
    private val LocalAppLocaleState = staticCompositionLocalOf { "en" }

    actual val current: String
        @Composable get() = LocalAppLocaleState.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val new = value ?: "en"
        NSUserDefaults.standardUserDefaults.setObject(listOf(new), LANG_KEY)
        return LocalAppLocaleState.provides(new)
    }
}
