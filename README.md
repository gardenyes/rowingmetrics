# Rowing Metrics (Kotlin Multiplatform + Flutter migration)

Cross-platform rowing session tracker.

## Active migration

A Flutter rewrite lives in **`flutter_app/`**. Prefer this for new iOS/Android work.

| Path | Role |
|------|------|
| `flutter_app` | Flutter app (session engine, UI, GPS/motion, sqflite) |
| `shared` | Legacy KMP Compose library (still present) |
| `app` | Legacy Android host for KMP |
| `iosApp` | Legacy incomplete SwiftUI + Compose host |

### Run Flutter

```bash
cd flutter_app
flutter pub get
flutter run
```

iOS builds require macOS + Xcode. Android can be built from this Windows environment.

### Flutter dependencies (iOS + Android)

- `geolocator` — GPS speed/distance
- `sensors_plus` — accelerometer / stroke detection
- `sqflite` — activity history (`rowing_metrics.db`)
- `shared_preferences` — settings
- `provider` — UI state
- `share_plus` — reserved for CSV export

## Legacy KMP modules

| Module | Role |
|--------|------|
| `shared` | KMP library: stroke/GPS math, SQLDelight, Compose UI |
| `app` | Android application |
| `iosApp` | Incomplete iOS host (missing `.xcodeproj`) |

```bash
./gradlew :app:assembleDebug
```
