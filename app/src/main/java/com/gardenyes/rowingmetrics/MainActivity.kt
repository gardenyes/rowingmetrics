package com.gardenyes.rowingmetrics

import android.Manifest
import android.app.Application
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.gardenyes.rowingmetrics.data.initSqlDriverContext
import com.gardenyes.rowingmetrics.platform.AndroidPlatformServices
import com.gardenyes.rowingmetrics.platform.PlatformHandlers
import com.gardenyes.rowingmetrics.ui.RowingApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class RowingApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initSqlDriverContext(this)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        applyShowOverLockScreen()
        setContent {
            val scope = rememberCoroutineScope()
            val appScope = remember { CoroutineScope(SupervisorJob() + scope.coroutineContext) }
            val platform = remember(appScope) { AndroidPlatformServices(this, appScope) }
            val handlers = rememberAndroidHandlers()
            RowingApp(platform = platform, handlers = handlers)
        }
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestKeyguardDismissIfPossible()
    }

    override fun onPause() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onPause()
    }

    private fun applyShowOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            )
        }
    }

    private fun requestKeyguardDismissIfPossible() {
        val keyguard = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        keyguard.requestDismissKeyguard(
            this,
            object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissError() = Unit
                override fun onDismissSucceeded() = Unit
                override fun onDismissCancelled() = Unit
            },
        )
    }
}

@Composable
private fun MainActivity.rememberAndroidHandlers(): PlatformHandlers {
    val activity = this
    val pendingStart = remember { mutableStateOf<(() -> Unit)?>(null) }
    val pendingExport = remember { mutableStateOf<ByteArray?>(null) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { grants ->
            val fine = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarse = grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (fine || coarse) {
                pendingStart.value?.invoke()
            }
            pendingStart.value = null
        }

    val exportLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("text/csv"),
        ) { uri: Uri? ->
            val bytes = pendingExport.value
            pendingExport.value = null
            if (uri != null && bytes != null) {
                activity.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            }
        }

    return remember(activity) {
        PlatformHandlers(
            hasLocationPermission = {
                val fine =
                    ContextCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED
                val coarse =
                    ContextCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED
                fine || coarse
            },
            requestLocationPermissionAndStart = { onGranted ->
                pendingStart.value = onGranted
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                )
            },
            exportActivitiesCsv = { bytes, name ->
                pendingExport.value = bytes
                exportLauncher.launch(name)
            },
            appVersion = {
                runCatching {
                    activity.packageManager.getPackageInfo(activity.packageName, 0).versionName
                }.getOrNull() ?: "Unknown"
            },
        )
    }
}
