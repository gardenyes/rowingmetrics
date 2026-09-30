import 'dart:math' as math;

import 'enums.dart';

class AccelSampleEvent {
  AccelSampleEvent(
    this.timeMs,
    this.acceleration,
    this.positiveThreshold,
    this.negativeThreshold,
  );

  final int timeMs;
  final double acceleration;
  final double positiveThreshold;
  final double negativeThreshold;
}

sealed class StrokeDetectorResult {
  const StrokeDetectorResult();
}

class StrokeDetectorNone extends StrokeDetectorResult {
  const StrokeDetectorNone();
}

class StrokePeaks extends StrokeDetectorResult {
  const StrokePeaks(this.peakTimesMs);
  final List<int> peakTimesMs;
}

/// Stroke timing from acceleration (ported from Kotlin AccelerometerStrokeDetector).
class AccelerometerStrokeDetector {
  AccelerometerStrokeDetector({
    this.thresholdWindowMs = 1800,
    this.maxEvents = 240,
    this.minStrokeGapMs = 300,
    this.minCalibrationSamples = 64,
    this.minDynamicRange = 0.14,
    this.positiveBandFraction = 0.56,
    this.magnitudeSmoothAlpha = 0.50,
    this.recoveryBandFraction = 0.16,
    this.peakProminenceFraction = 0.04,
    this.minPeakProminenceAbs = 0.05,
  });

  final int thresholdWindowMs;
  final int maxEvents;
  final int minStrokeGapMs;
  final int minCalibrationSamples;
  final double minDynamicRange;
  final double positiveBandFraction;
  final double magnitudeSmoothAlpha;
  final double recoveryBandFraction;
  final double peakProminenceFraction;
  final double minPeakProminenceAbs;

  final List<AccelSampleEvent> _events = [];
  final List<(int, double)> _recentValues = [];

  int _sampleCount = 0;
  double _smoothMagnitude = -1;
  bool _armedForNextStroke = true;
  double _rollMin = 0;
  double _rollMax = 0;
  double _positiveThreshold = 0;
  double _negativeThreshold = 0;
  bool _thresholdsValid = false;
  int _acceptedStrokeCount = 0;
  final List<int> _strokePeakTimesMs = [];
  int _lastStrokePeakMs = 0;

  void reset() {
    _events.clear();
    _recentValues.clear();
    _sampleCount = 0;
    _rollMin = 0;
    _rollMax = 0;
    _positiveThreshold = 0;
    _negativeThreshold = 0;
    _thresholdsValid = false;
    _acceptedStrokeCount = 0;
    _strokePeakTimesMs.clear();
    _lastStrokePeakMs = 0;
    _smoothMagnitude = -1;
    _armedForNextStroke = true;
  }

  void clearAdaptiveStatePreservingStrokeCounts() {
    _events.clear();
    _recentValues.clear();
    _sampleCount = 0;
    _rollMin = 0;
    _rollMax = 0;
    _positiveThreshold = 0;
    _negativeThreshold = 0;
    _thresholdsValid = false;
    _smoothMagnitude = -1;
    _armedForNextStroke = true;
  }

  int acceptedStrokeCount() => _acceptedStrokeCount;

  List<int> strokeTimestampsForSpm() => List.unmodifiable(_strokePeakTimesMs);

  StrokeDetectorResult onAccelerationSample(
    double ax,
    double ay,
    double az,
    int elapsedRtMs, {
    List<double>? gravity,
  }) {
    final mag = _linearMagnitudeForStrokes(ax, ay, az, gravity);
    final raw = _round2(mag);
    late final double a;
    if (_smoothMagnitude < 0) {
      _smoothMagnitude = raw;
      a = raw;
    } else {
      final s = magnitudeSmoothAlpha;
      _smoothMagnitude = _smoothMagnitude * (1 - s) + raw * s;
      a = _round2(_smoothMagnitude);
    }

    _pruneRecent(elapsedRtMs);
    _recentValues.add((elapsedRtMs, a));
    _updateThresholdsFromRollingWindow();
    _sampleCount++;

    if (!_thresholdsValid || _sampleCount < minCalibrationSamples) {
      return const StrokeDetectorNone();
    }

    final span = math.max(_rollMax - _rollMin, minDynamicRange);
    final releaseLine = _rollMin + span * recoveryBandFraction;
    if (!_armedForNextStroke && a <= releaseLine) {
      _armedForNextStroke = true;
    }

    final posTh = _positiveThreshold;
    final negTh = _negativeThreshold;
    _events.add(AccelSampleEvent(elapsedRtMs, a, posTh, negTh));
    while (_events.length > maxEvents) {
      _events.removeAt(0);
    }

    if (_events.length < 2) return const StrokeDetectorNone();

    final prev = _events[_events.length - 2];
    final cur = _events.last;
    if (!_armedForNextStroke ||
        !_isAccelerationCrossingPositiveThreshold(prev, cur)) {
      _consolidateOldTail(elapsedRtMs);
      return const StrokeDetectorNone();
    }

    final peakMargin =
        math.max(peakProminenceFraction * span, minPeakProminenceAbs);
    final crossingIndex = _events.length - 2;
    final peak = _findMaxAbovePositiveThreshold(crossingIndex, peakMargin);
    if (peak == null) {
      _consolidateOldTail(elapsedRtMs);
      return const StrokeDetectorNone();
    }

    final peakTime = peak.timeMs;
    if (_lastStrokePeakMs > 0 && peakTime - _lastStrokePeakMs < minStrokeGapMs) {
      _events.removeLast();
      return const StrokeDetectorNone();
    }

    final out = _didWeLostOne(peakTime);
    _armedForNextStroke = false;
    _consolidateAfterStroke(elapsedRtMs);
    return StrokePeaks(out);
  }

  double _round2(double x) {
    const q = 100.0;
    return (x * q).round() / q;
  }

  double _linearMagnitudeForStrokes(
    double ax,
    double ay,
    double az,
    List<double>? gravity,
  ) {
    if (gravity == null || gravity.length < 3) {
      return math.sqrt(ax * ax + ay * ay + az * az);
    }
    final gx = gravity[0];
    final gy = gravity[1];
    final gz = gravity[2];
    final g2 = gx * gx + gy * gy + gz * gz;
    if (g2 < 0.25) {
      return math.sqrt(ax * ax + ay * ay + az * az);
    }
    final invG2 = 1.0 / g2;
    final dot = ax * gx + ay * gy + az * gz;
    final hx = ax - invG2 * dot * gx;
    final hy = ay - invG2 * dot * gy;
    final hz = az - invG2 * dot * gz;
    return math.sqrt(hx * hx + hy * hy + hz * hz);
  }

  void _pruneRecent(int nowMs) {
    while (_recentValues.isNotEmpty &&
        nowMs - _recentValues.first.$1 > thresholdWindowMs) {
      _recentValues.removeAt(0);
    }
  }

  void _updateThresholdsFromRollingWindow() {
    if (_recentValues.isEmpty) return;
    var mn = double.infinity;
    var mx = double.negativeInfinity;
    for (final (_, v) in _recentValues) {
      mn = math.min(mn, v);
      mx = math.max(mx, v);
    }
    _rollMin = mn;
    _rollMax = mx;
    final span = _rollMax - _rollMin;
    _thresholdsValid = span >= minDynamicRange;
    if (!_thresholdsValid) return;
    _positiveThreshold = _rollMin + span * positiveBandFraction;
    _negativeThreshold = _rollMin + span * (1 - positiveBandFraction);
  }

  bool _isAccelerationCrossingPositiveThreshold(
    AccelSampleEvent prev,
    AccelSampleEvent cur,
  ) =>
      prev.acceleration < prev.positiveThreshold &&
      cur.acceleration >= cur.positiveThreshold;

  AccelSampleEvent? _findMaxAbovePositiveThreshold(
    int fromIndex,
    double peakMargin,
  ) {
    if (fromIndex >= _events.length) return null;
    AccelSampleEvent? best;
    for (var i = fromIndex; i < _events.length; i++) {
      final e = _events[i];
      final need = e.positiveThreshold + peakMargin;
      if (e.acceleration >= need) {
        if (best == null || e.acceleration > best.acceleration) best = e;
      }
    }
    return best;
  }

  List<int> _didWeLostOne(int peakTimeMs) {
    final medianGap = _medianLastStrokeIntervalsMs();
    final last = _lastStrokePeakMs;
    if (medianGap == null || last <= 0 || peakTimeMs <= last) {
      _commitStroke(peakTimeMs);
      return [peakTimeMs];
    }
    final gap = peakTimeMs - last;
    final lost = medianGap >= 260 &&
        medianGap <= 3800 &&
        gap > (medianGap * 1.75).toInt() &&
        gap < 5500;
    if (!lost) {
      _commitStroke(peakTimeMs);
      return [peakTimeMs];
    }
    final inserted = last + medianGap;
    if (inserted < peakTimeMs - minStrokeGapMs ~/ 2) {
      _commitStroke(inserted);
      _commitStroke(peakTimeMs);
      return [inserted, peakTimeMs];
    }
    _commitStroke(peakTimeMs);
    return [peakTimeMs];
  }

  int? _medianLastStrokeIntervalsMs() {
    if (_strokePeakTimesMs.length < 4) return null;
    final list = List<int>.from(_strokePeakTimesMs);
    final iv = <int>[];
    for (var i = 1; i < list.length; i++) {
      final gap = list[i] - list[i - 1];
      iv.add(gap < 0 ? 0 : gap);
    }
    if (iv.isEmpty) return null;
    iv.sort();
    return iv[iv.length ~/ 2];
  }

  void _commitStroke(int peakTimeMs) {
    _acceptedStrokeCount++;
    _strokePeakTimesMs.add(peakTimeMs);
    while (_strokePeakTimesMs.length > 8) {
      _strokePeakTimesMs.removeAt(0);
    }
    _lastStrokePeakMs = peakTimeMs;
  }

  void _consolidateAfterStroke(int nowMs) {
    _events.clear();
    _pruneRecent(nowMs);
  }

  void _consolidateOldTail(int nowMs) {
    final keepFrom = nowMs - thresholdWindowMs;
    while (_events.isNotEmpty && _events.first.timeMs < keepFrom) {
      _events.removeAt(0);
    }
  }
}

class StrokeDetectorPresets {
  static const double _mediumBlend = 0.5;

  static AccelerometerStrokeDetector buildDetector(
    StrokeDetectionSensitivity sensitivity,
  ) {
    switch (sensitivity) {
      case StrokeDetectionSensitivity.veryHigh:
        return AccelerometerStrokeDetector(
          minStrokeGapMs: 220,
          minDynamicRange: 0.08,
          positiveBandFraction: 0.48,
          magnitudeSmoothAlpha: 0.58,
          recoveryBandFraction: 0.20,
          peakProminenceFraction: 0.025,
          minPeakProminenceAbs: 0.035,
        );
      case StrokeDetectionSensitivity.high:
        return AccelerometerStrokeDetector(
          minStrokeGapMs: 260,
          minDynamicRange: 0.10,
          positiveBandFraction: 0.50,
          magnitudeSmoothAlpha: 0.55,
          recoveryBandFraction: 0.18,
          peakProminenceFraction: 0.03,
          minPeakProminenceAbs: 0.04,
        );
      case StrokeDetectionSensitivity.medium:
        return AccelerometerStrokeDetector(
          minStrokeGapMs: _lerpLong(260, 300, _mediumBlend),
          minDynamicRange: _lerp(0.10, 0.14, _mediumBlend),
          positiveBandFraction: _lerp(0.50, 0.56, _mediumBlend),
          magnitudeSmoothAlpha: _lerp(0.55, 0.50, _mediumBlend),
          recoveryBandFraction: _lerp(0.18, 0.16, _mediumBlend),
          peakProminenceFraction: _lerp(0.03, 0.04, _mediumBlend),
          minPeakProminenceAbs: _lerp(0.04, 0.05, _mediumBlend),
        );
      case StrokeDetectionSensitivity.low:
        return AccelerometerStrokeDetector();
      case StrokeDetectionSensitivity.veryLow:
        return AccelerometerStrokeDetector(
          minStrokeGapMs: 320,
          minDynamicRange: 0.16,
          positiveBandFraction: 0.59,
          magnitudeSmoothAlpha: 0.475,
          recoveryBandFraction: 0.15,
          peakProminenceFraction: 0.045,
          minPeakProminenceAbs: 0.055,
        );
    }
  }

  static double _lerp(double a, double b, double t) => a + (b - a) * t;

  static int _lerpLong(int a, int b, double t) =>
      (a + (b - a) * t).toInt();
}
