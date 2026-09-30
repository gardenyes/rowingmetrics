String formatActivityTableDate(int epochMillis) {
  final dt = DateTime.fromMillisecondsSinceEpoch(epochMillis).toLocal();
  final d = dt.day.toString().padLeft(2, '0');
  final m = dt.month.toString().padLeft(2, '0');
  return '$d-$m-${dt.year}';
}

int activityStartedAtEpochMs(int endedAtEpochMs, int durationMs) {
  final started = endedAtEpochMs - (durationMs < 0 ? 0 : durationMs);
  return started < 0 ? 0 : started;
}

String formatActivityTableHour(int epochMillis) {
  final t = DateTime.fromMillisecondsSinceEpoch(epochMillis).toLocal();
  final h = t.hour.toString().padLeft(2, '0');
  final m = t.minute.toString().padLeft(2, '0');
  return '$h:$m';
}

String formatActivityTableStartHour(int endedAtEpochMs, int durationMs) =>
    formatActivityTableHour(activityStartedAtEpochMs(endedAtEpochMs, durationMs));

String formatActivityElapsedTable(int ms) {
  final totalSec = (ms ~/ 1000).clamp(0, 1 << 30);
  final h = totalSec ~/ 3600;
  final m = (totalSec % 3600) ~/ 60;
  final s = totalSec % 60;
  if (h < 1) {
    return '${m.toString().padLeft(2, '0')}:${s.toString().padLeft(2, '0')}';
  }
  return '${h.toString().padLeft(2, '0')}:${m.toString().padLeft(2, '0')}:${s.toString().padLeft(2, '0')}';
}

String formatOneDecimalTable(double x) => x.toStringAsFixed(1);

String formatDistanceTable(double meters) {
  if (meters >= 1000.0) {
    return '${(meters / 1000.0).toStringAsFixed(2)} km';
  }
  return '${meters.toStringAsFixed(0)} m';
}

String formatSessionElapsed(int ms) => formatActivityElapsedTable(ms);
