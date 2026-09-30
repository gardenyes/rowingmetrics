class GpsFix {
  const GpsFix({
    required this.latitude,
    required this.longitude,
    required this.hasAccuracy,
    required this.accuracyM,
    required this.hasSpeed,
    required this.speedMps,
  });

  final double latitude;
  final double longitude;
  final bool hasAccuracy;
  final double accuracyM;
  final bool hasSpeed;
  final double speedMps;
}
