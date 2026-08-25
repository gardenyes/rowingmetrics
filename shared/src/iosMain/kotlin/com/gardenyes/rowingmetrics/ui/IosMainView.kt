package com.gardenyes.rowingmetrics.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.gardenyes.rowingmetrics.platform.IosPlatformServices
import com.gardenyes.rowingmetrics.platform.PlatformHandlers
import com.gardenyes.rowingmetrics.platform.iosAppVersion

@Composable
fun IosRowingApp() {
    val scope = rememberCoroutineScope()
    val platform = remember(scope) { IosPlatformServices(scope) }
    val handlers =
        remember {
            PlatformHandlers(
                hasLocationPermission = { false },
                requestLocationPermissionAndStart = { },
                exportActivitiesCsv = { _, _ -> },
                appVersion = { iosAppVersion() },
            )
        }
    RowingApp(platform = platform, handlers = handlers)
}
