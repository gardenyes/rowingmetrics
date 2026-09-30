/// GPS speed pipeline: Kalman filter (Q=0.8, R=2) on m/s, FIFO (≤8), mean of last N → km/h.
class GpsSpeedSmoother {
  GpsSpeedSmoother({
    this.fifoMax = fifoMaxDefault,
    this.processNoiseQ = 0.8,
    this.measurementNoiseR = 2.0,
  });

  static const int fifoMaxDefault = 8;

  final int fifoMax;
  final double processNoiseQ;
  final double measurementNoiseR;

  double _kalmanX = 0.0;
  double _kalmanP = 1.0;
  bool _kalmanInitialized = false;
  final List<double> _filteredSpeedsMps = [];
  double _lastOutputKmh = 0.0;
  int _displayAverageCount = 4;

  void setDisplayAverageCount(int count) {
    _displayAverageCount = count.clamp(1, fifoMax);
  }

  void reset() {
    _kalmanX = 0.0;
    _kalmanP = 1.0;
    _kalmanInitialized = false;
    _filteredSpeedsMps.clear();
    _lastOutputKmh = 0.0;
  }

  double calculate(double? speedMps) {
    if (speedMps == null) return _lastOutputKmh;
    final z = speedMps.clamp(0.0, 50.0);
    if (!_kalmanInitialized) {
      _kalmanX = z;
      _kalmanP = measurementNoiseR;
      _kalmanInitialized = true;
    } else {
      _kalmanP += processNoiseQ;
      final k = _kalmanP / (_kalmanP + measurementNoiseR);
      _kalmanX += k * (z - _kalmanX);
      _kalmanP = (1.0 - k) * _kalmanP;
    }
    _filteredSpeedsMps.add(_kalmanX);
    while (_filteredSpeedsMps.length > fifoMax) {
      _filteredSpeedsMps.removeAt(0);
    }
    final want = _displayAverageCount.clamp(1, fifoMax);
    final nTake = mathMin(want, _filteredSpeedsMps.length);
    final slice = _filteredSpeedsMps.sublist(_filteredSpeedsMps.length - nTake);
    final avgMps =
        slice.isEmpty ? 0.0 : slice.reduce((a, b) => a + b) / slice.length;
    _lastOutputKmh = avgMps * 3.6;
    return _lastOutputKmh;
  }

  int mathMin(int a, int b) => a < b ? a : b;
}
