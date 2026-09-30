class SpmCalcResult {
  const SpmCalcResult(this.spm, this.intervalMs);
  final int spm;
  final int intervalMs;
}

const double _outlierRelativeFraction = 0.4;

const int liveSpmStrokeWindow = 8;
const int liveSpmMinStrokesForStable = 8;
const int liveSpmIdleAfterLastStrokeMs = 3000;
const int liveSpmDisplayHoldAfterLastStrokeMs = 60000;

double? avgMsOrNull(List<int> intervals) {
  if (intervals.isEmpty) return null;
  return intervals.map((e) => e.toDouble()).reduce((a, b) => a + b) /
      intervals.length;
}

List<int> calculateIntervals(List<int> positionsMs) {
  if (positionsMs.length < 2) return const [];
  final out = <int>[];
  for (var i = 1; i < positionsMs.length; i++) {
    final gap = positionsMs[i] - positionsMs[i - 1];
    out.add(gap < 0 ? 0 : gap);
  }
  return out;
}

List<int> removeIntervalOutliers(
  List<int> intervals, {
  double relativeFraction = _outlierRelativeFraction,
}) {
  if (intervals.length < 2) return intervals;
  var current = List<int>.from(intervals);
  for (var i = 0; i < 5; i++) {
    final mean = avgMsOrNull(current);
    if (mean == null || mean <= 0) return current;
    final threshold = relativeFraction * mean;
    final filtered =
        current.where((v) => (v - mean).abs() < threshold).toList();
    if (filtered.length == current.length) return current;
    if (filtered.length >= 2) {
      current = filtered;
    } else {
      return _pickBestMatchWhenTooFew(current, mean);
    }
  }
  return current;
}

List<int> _pickBestMatchWhenTooFew(List<int> intervals, double mean) {
  if (intervals.length < 2) return intervals;
  final scored = intervals.map((v) => (v, (v - mean).abs())).toList()
    ..sort((a, b) => a.$2.compareTo(b.$2));
  return [scored[0].$1, scored[1].$1];
}

SpmCalcResult calculateSPM(
  List<int> strokeTimestampsMs,
  int nowElapsedMs, {
  int maxStrokesInWindow = liveSpmStrokeWindow,
  int idleAfterLastStrokeMs = liveSpmIdleAfterLastStrokeMs,
  int minStrokesForStableSpm = liveSpmMinStrokesForStable,
}) {
  if (strokeTimestampsMs.isEmpty) return const SpmCalcResult(0, 0);
  final last = strokeTimestampsMs.last;
  if (nowElapsedMs - last > idleAfterLastStrokeMs) {
    return const SpmCalcResult(0, 0);
  }
  if (strokeTimestampsMs.length < minStrokesForStableSpm) {
    return const SpmCalcResult(0, 0);
  }
  final cap = maxStrokesInWindow < 2 ? 2 : maxStrokesInWindow;
  final window = strokeTimestampsMs.length <= cap
      ? strokeTimestampsMs
      : strokeTimestampsMs.sublist(strokeTimestampsMs.length - cap);
  var intervals = calculateIntervals(window);
  if (intervals.isEmpty) return const SpmCalcResult(0, 0);
  intervals = removeIntervalOutliers(intervals);
  if (intervals.isEmpty) return const SpmCalcResult(0, 0);
  final avgMs = avgMsOrNull(intervals);
  if (avgMs == null || avgMs <= 0) return const SpmCalcResult(0, 0);
  final spm = (60000.0 / avgMs).round().clamp(0, 999999);
  final intervalRounded = avgMs.round().clamp(1, 999999999);
  return SpmCalcResult(spm, intervalRounded);
}
