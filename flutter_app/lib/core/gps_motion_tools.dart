import 'dart:math' as math;

/// Great-circle distance between two WGS84 points (haversine), in kilometers.
double calcDistanceKm(
  double prevLat,
  double prevLon,
  double curLat,
  double curLon,
) {
  const earthRadiusM = 6371000.0;
  final lat1 = _toRadians(prevLat);
  final lat2 = _toRadians(curLat);
  final dLat = _toRadians(curLat - prevLat);
  final dLon = _toRadians(curLon - prevLon);
  final a = math.sin(dLat / 2) * math.sin(dLat / 2) +
      math.cos(lat1) * math.cos(lat2) * math.sin(dLon / 2) * math.sin(dLon / 2);
  final c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a));
  return earthRadiusM * c / 1000.0;
}

double _toRadians(double deg) => deg * math.pi / 180.0;
