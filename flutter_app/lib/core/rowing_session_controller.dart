import 'dart:async';

import '../core/accelerometer_stroke_detector.dart';
import '../core/completed_activity.dart';
import '../core/enums.dart';
import '../core/gps_fix.dart';
import '../core/gps_motion_tools.dart';
import '../core/gps_speed_smoother.dart';
import '../core/rowing_ui_state.dart';
import '../core/stroke_spm_math.dart';
import '../platform/platform_services.dart';

/// Cross-platform session engine (ported from Kotlin RowingSessionController).
class RowingSessionController {
  RowingSessionController(this._platform);

  static const _sessionUiTickMs = 1000;
  static const _liveSpmEngineCycleMs = 1995;
  static const _gpsFixMaxAccuracyM = 50.0;
  static const _minGpsMovementDisplayM = 4.0;
  static const _gpsDisplayMaxAccuracyM = 80.0;
  static const _gpsDisplayMaxJumpM = 200.0;
  static const _orientationStabilizeMs = 1200;

  final PlatformServices _platform;

  RowingUiState _uiState = const RowingUiState();
  void Function(RowingUiState)? _listener;

  bool _measuring = false;
  int _sessionStartRtMs = 0;
  int _sessionEndRtMs = 0;
  double _liveSpmDisplay = 0;
  double _lastStableLiveSpm = 0;
  double _sessionSpmSampleSum = 0;
  int _sessionSpmSampleCount = 0;
  double _sessionDistanceKm = 0;
  double? _lastLat;
  double? _lastLon;
  double? _displayAnchorLat;
  double? _displayAnchorLon;
  double _lastSmoothedSpeedKmh = 0;
  final _speedSmoother = GpsSpeedSmoother();
  late AccelerometerStrokeDetector _accelerometerStrokeDetector =
      StrokeDetectorPresets.buildDetector(
    StrokeDetectionSensitivity.defaultValue,
  );
  bool _gpsReady = false;
  final List<double> _lastGravity = [0, 0, 0];
  bool _gravitySampleReady = false;
  Timer? _sessionUiTickTimer;
  Timer? _metricsCycleTimer;
  Timer? _orientationStabilizeTimer;
  bool _strokeDetectionActive = true;

  void setListener(void Function(RowingUiState) l) {
    _listener = l;
    l(_uiState);
  }

  void clearListener() => _listener = null;

  StrokeDetectionSensitivity getStrokeDetectionSensitivity() =>
      _platform.settings.getStrokeDetectionSensitivity();

  int getGpsDisplayAverageN() => _platform.settings.getGpsDisplayAverageN();

  Future<void> setGpsDisplayAverageN(int n) async {
    await _platform.settings.setGpsDisplayAverageN(n.clamp(1, 8));
    _speedSmoother
        .setDisplayAverageCount(_platform.settings.getGpsDisplayAverageCount());
  }

  Future<void> setStrokeDetectionSensitivity(
    StrokeDetectionSensitivity s,
  ) async {
    await _platform.settings.setStrokeDetectionSensitivity(s);
    if (!_measuring) {
      _accelerometerStrokeDetector = StrokeDetectorPresets.buildDetector(s);
    }
  }

  Future<void> startSession() async {
    if (!_platform.motionSensors.isAvailable) return;
    await _stopInternals();
    _resetSessionState();
    _uiState = const RowingUiState(running: true);
    _listener?.call(_uiState);
    _measuring = true;

    await _platform.motionSensors.start(_onMotion);
    try {
      await _platform.locationTracker.start(_onLocation);
    } catch (_) {
      _measuring = false;
      await _platform.motionSensors.stop();
      _sessionStartRtMs = 0;
      _sessionEndRtMs = 0;
      _uiState = const RowingUiState(running: false);
      _listener?.call(_uiState);
      return;
    }

    await _platform.orientationMonitor.start(_onOrientationChanged);
    _pushUi();
    _startPeriodicJobs();
  }

  Future<void> stopSession() async {
    if (!_uiState.running) return;
    final endedAtWallMs = _platform.currentTimeMillis();
    _sessionEndRtMs = _platform.elapsedRealtimeMs();
    await _stopInternals();
    _uiState = _uiState.copyWith(running: false);
    _pushUi();
    await _persistCompletedSession(endedAtWallMs);
  }

  void _onMotion(double ax, double ay, double az, List<double>? gravity) {
    if (!_measuring || !_strokeDetectionActive) return;
    if (gravity != null && gravity.length >= 3) {
      _lastGravity[0] = gravity[0];
      _lastGravity[1] = gravity[1];
      _lastGravity[2] = gravity[2];
      _gravitySampleReady = true;
    }
    final now = _platform.elapsedRealtimeMs();
    final g = _gravitySampleReady ? _lastGravity : null;
    final result =
        _accelerometerStrokeDetector.onAccelerationSample(ax, ay, az, now,
            gravity: g);
    if (result is StrokePeaks) {
      _updateLiveSpmDisplayOnly(now);
      _platform.postToMain(_pushUi);
    }
  }

  void _onLocation(GpsFix loc) {
    if (!_measuring) return;
    if (!_isAcceptableGpsFix(loc)) return;

    final speedMps = loc.hasSpeed ? loc.speedMps : null;
    final smoothedKmh = _speedSmoother.calculate(speedMps);
    _lastSmoothedSpeedKmh = smoothedKmh;

    if (!_gpsReady) {
      _gpsReady = true;
      _lastLat = loc.latitude;
      _lastLon = loc.longitude;
      return;
    }

    final lat = loc.latitude;
    final lon = loc.longitude;
    final pLat = _lastLat;
    final pLon = _lastLon;

    if (pLat != null && pLon != null) {
      _displayAnchorLat ??= pLat;
      _displayAnchorLon ??= pLon;
      final anchorLat = _displayAnchorLat!;
      final anchorLon = _displayAnchorLon!;
      final dKm = calcDistanceKm(anchorLat, anchorLon, lat, lon);
      final dM = dKm * 1000.0;
      final qualityOk =
          loc.accuracyM <= _gpsDisplayMaxAccuracyM || dM < _gpsDisplayMaxJumpM;
      if (dM >= _minGpsMovementDisplayM && qualityOk) {
        _sessionDistanceKm += dKm;
        _displayAnchorLat = lat;
        _displayAnchorLon = lon;
      }
    } else if (_displayAnchorLat == null) {
      _displayAnchorLat = lat;
      _displayAnchorLon = lon;
    }

    _lastLat = lat;
    _lastLon = lon;
  }

  void _resetSessionState() {
    _liveSpmDisplay = 0;
    _lastStableLiveSpm = 0;
    _sessionSpmSampleSum = 0;
    _sessionSpmSampleCount = 0;
    _sessionDistanceKm = 0;
    _lastLat = null;
    _lastLon = null;
    _displayAnchorLat = null;
    _displayAnchorLon = null;
    _lastSmoothedSpeedKmh = 0;
    _speedSmoother.reset();
    _speedSmoother
        .setDisplayAverageCount(_platform.settings.getGpsDisplayAverageCount());
    _gpsReady = false;
    _accelerometerStrokeDetector = StrokeDetectorPresets.buildDetector(
      _platform.settings.getStrokeDetectionSensitivity(),
    );
    _gravitySampleReady = false;
    _strokeDetectionActive = true;
    _sessionStartRtMs = _platform.elapsedRealtimeMs();
    _sessionEndRtMs = _sessionStartRtMs;
  }

  void _onOrientationChanged() {
    if (!_measuring) return;
    _strokeDetectionActive = false;
    _accelerometerStrokeDetector.clearAdaptiveStatePreservingStrokeCounts();
    _orientationStabilizeTimer?.cancel();
    _orientationStabilizeTimer = Timer(
      const Duration(milliseconds: _orientationStabilizeMs),
      () {
        if (_measuring) _strokeDetectionActive = true;
        _platform.postToMain(_pushUi);
      },
    );
  }

  void _startPeriodicJobs() {
    _sessionUiTickTimer?.cancel();
    _metricsCycleTimer?.cancel();
    _sessionUiTickTimer = Timer.periodic(
      const Duration(milliseconds: _sessionUiTickMs),
      (_) {
        if (_measuring) _pushUi();
      },
    );
    _metricsCycleTimer = Timer.periodic(
      const Duration(milliseconds: _liveSpmEngineCycleMs),
      (_) {
        if (!_measuring) return;
        final nowRt = _platform.elapsedRealtimeMs();
        _applyStableLiveSpmForSessionSample(nowRt);
        _pushUi();
      },
    );
  }

  bool _isAcceptableGpsFix(GpsFix loc) {
    if (!loc.hasAccuracy) return false;
    if (loc.accuracyM > _gpsFixMaxAccuracyM) return false;
    if (loc.latitude == 0.0 && loc.longitude == 0.0) return false;
    return true;
  }

  int _sessionElapsedMs() {
    if (_sessionStartRtMs == 0) return 0;
    final end =
        _uiState.running ? _platform.elapsedRealtimeMs() : _sessionEndRtMs;
    final elapsed = end - _sessionStartRtMs;
    return elapsed < 0 ? 0 : elapsed;
  }

  double _calculateAverageSpeedKmh() {
    final elapsedMs = _sessionElapsedMs();
    if (elapsedMs > 0 && _sessionDistanceKm > 0) {
      return _sessionDistanceKm / (elapsedMs / 1e3) * 3600.0;
    }
    return 0;
  }

  SpmCalcResult _refreshSPM(int nowRt) =>
      calculateSPM(_accelerometerStrokeDetector.strokeTimestampsForSpm(), nowRt);

  void _updateLiveSpmDisplayOnly(int nowRt) {
    final spmResult = _refreshSPM(nowRt);
    final stamps = _accelerometerStrokeDetector.strokeTimestampsForSpm();
    final lastStroke = stamps.isEmpty ? null : stamps.last;
    final strokeIdle = lastStroke == null ||
        (nowRt - lastStroke) > liveSpmDisplayHoldAfterLastStrokeMs;
    if (spmResult.spm > 0) {
      _lastStableLiveSpm = spmResult.spm.toDouble();
      _liveSpmDisplay = _lastStableLiveSpm;
    } else if (!strokeIdle && _lastStableLiveSpm > 0) {
      // hold last stable
    } else {
      _lastStableLiveSpm = 0;
      _liveSpmDisplay = 0;
    }
  }

  void _applyStableLiveSpmForSessionSample(int nowRt) {
    _updateLiveSpmDisplayOnly(nowRt);
    _sessionSpmSampleSum += _liveSpmDisplay;
    _sessionSpmSampleCount++;
  }

  void _pushUi() {
    final nowRt = _platform.elapsedRealtimeMs();
    final avgSpm = _uiState.running
        ? _liveSpmDisplay
        : (_sessionSpmSampleCount > 0
            ? _sessionSpmSampleSum / _sessionSpmSampleCount
            : _liveSpmDisplay);
    final live = _gpsReady;
    final curKmh = live ? _lastSmoothedSpeedKmh : 0.0;
    final activityElapsed = _sessionElapsedMs();
    final avgSpd = _calculateAverageSpeedKmh();
    final stamps = _accelerometerStrokeDetector.strokeTimestampsForSpm();
    final lastStrokeForUi = stamps.isEmpty ? null : stamps.last;
    final strokeRateLive = !_uiState.running ||
        (lastStrokeForUi != null &&
            (nowRt - lastStrokeForUi) <= liveSpmIdleAfterLastStrokeMs);
    final snapshot = RowingUiState(
      running: _uiState.running,
      avgStrokesPerMin: avgSpm,
      strokeRateDetectionLive: strokeRateLive,
      currentSpeedKmh: curKmh,
      averageSpeedKmh: avgSpd,
      activityElapsedMs: activityElapsed,
      distanceMeters: live ? _sessionDistanceKm * 1000.0 : 0.0,
    );
    _uiState = snapshot;
    _listener?.call(_uiState);
  }

  Future<void> _persistCompletedSession(int endedAtWallMs) async {
    final entity = CompletedActivity(
      endedAtEpochMs: endedAtWallMs,
      durationMs: _sessionElapsedMs(),
      avgStrokeRate: _sessionSpmSampleCount > 0
          ? _sessionSpmSampleSum / _sessionSpmSampleCount
          : 0.0,
      avgSpeedKmh: _calculateAverageSpeedKmh(),
      distanceMeters: _sessionDistanceKm * 1000.0,
    );
    await _platform.activities.insert(entity);
  }

  Future<void> _stopInternals() async {
    _measuring = false;
    _orientationStabilizeTimer?.cancel();
    _orientationStabilizeTimer = null;
    _sessionUiTickTimer?.cancel();
    _sessionUiTickTimer = null;
    _metricsCycleTimer?.cancel();
    _metricsCycleTimer = null;
    await _platform.motionSensors.stop();
    await _platform.locationTracker.stop();
    await _platform.orientationMonitor.stop();
  }

  Future<void> dispose() => _stopInternals();
}
