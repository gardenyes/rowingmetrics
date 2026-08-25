# Rowing Metrics (Kotlin Multiplatform)

Cross-platform rowing session tracker with shared UI and business logic.

## Modules

| Module | Role |
|--------|------|
| `shared` | KMP library: stroke/GPS math, SQLDelight storage, Compose UI, session engine |
| `app` | Android application (permissions, lock screen, signing) |
| `iosApp` | iOS host (SwiftUI + Compose Multiplatform) |

## Build Android

```bash
./gradlew :app:assembleDebug
```

Open the project in Android Studio and run the `app` configuration.

## Build iOS (macOS + Xcode)

1. Generate the shared framework:

   ```bash
   ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
   ```

2. Open `iosApp/iosApp.xcodeproj` in Xcode (project file must reference the `Shared` framework from `shared/build/bin/...`).

3. Run on a simulator or device.

iOS GPS and motion are stubbed in `IosPlatformServices` until Core Location / Core Motion bindings are added. Settings and activity history work via SQLDelight.

## Architecture

- **Shared session engine** — `RowingSessionController` (stroke detection, SPM, distance, speed smoothing)
- **Platform services** — `PlatformServices` (GPS, sensors, preferences, DB) with Android and iOS implementations
- **UI** — `RowingApp` in `shared` (Compose Multiplatform)

## Migrating from the old Android-only layout

Room was replaced with **SQLDelight** in `shared`. The database file name remains `rowing_metrics.db` on Android.
