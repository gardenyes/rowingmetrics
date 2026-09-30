class RowingUiState {
  const RowingUiState({
    this.running = false,
    this.avgStrokesPerMin = 0.0,
    this.strokeRateDetectionLive = true,
    this.currentSpeedKmh = 0.0,
    this.averageSpeedKmh = 0.0,
    this.activityElapsedMs = 0,
    this.distanceMeters = 0.0,
  });

  final bool running;
  final double avgStrokesPerMin;
  final bool strokeRateDetectionLive;
  final double currentSpeedKmh;
  final double averageSpeedKmh;
  final int activityElapsedMs;
  final double distanceMeters;

  RowingUiState copyWith({
    bool? running,
    double? avgStrokesPerMin,
    bool? strokeRateDetectionLive,
    double? currentSpeedKmh,
    double? averageSpeedKmh,
    int? activityElapsedMs,
    double? distanceMeters,
  }) {
    return RowingUiState(
      running: running ?? this.running,
      avgStrokesPerMin: avgStrokesPerMin ?? this.avgStrokesPerMin,
      strokeRateDetectionLive:
          strokeRateDetectionLive ?? this.strokeRateDetectionLive,
      currentSpeedKmh: currentSpeedKmh ?? this.currentSpeedKmh,
      averageSpeedKmh: averageSpeedKmh ?? this.averageSpeedKmh,
      activityElapsedMs: activityElapsedMs ?? this.activityElapsedMs,
      distanceMeters: distanceMeters ?? this.distanceMeters,
    );
  }
}
