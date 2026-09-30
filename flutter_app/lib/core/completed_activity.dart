class CompletedActivity {
  const CompletedActivity({
    this.id = 0,
    required this.endedAtEpochMs,
    required this.durationMs,
    required this.avgStrokeRate,
    required this.avgSpeedKmh,
    required this.distanceMeters,
  });

  final int id;
  final int endedAtEpochMs;
  final int durationMs;
  final double avgStrokeRate;
  final double avgSpeedKmh;
  final double distanceMeters;

  Map<String, Object?> toMap() => {
        'id': id == 0 ? null : id,
        'endedAtEpochMs': endedAtEpochMs,
        'durationMs': durationMs,
        'avgStrokeRate': avgStrokeRate,
        'avgSpeedKmh': avgSpeedKmh,
        'distanceMeters': distanceMeters,
      };

  factory CompletedActivity.fromMap(Map<String, Object?> map) =>
      CompletedActivity(
        id: map['id'] as int? ?? 0,
        endedAtEpochMs: map['endedAtEpochMs'] as int,
        durationMs: map['durationMs'] as int,
        avgStrokeRate: (map['avgStrokeRate'] as num).toDouble(),
        avgSpeedKmh: (map['avgSpeedKmh'] as num).toDouble(),
        distanceMeters: (map['distanceMeters'] as num).toDouble(),
      );
}
