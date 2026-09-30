import 'package:flutter_test/flutter_test.dart';

import 'package:rowing_metrics/core/stroke_spm_math.dart';

void main() {
  test('calculateSPM returns 0 until stable window', () {
    final result = calculateSPM([1000, 2000, 3000], 3500);
    expect(result.spm, 0);
  });

  test('calculateIntervals builds gaps', () {
    expect(calculateIntervals([100, 300, 500]), [200, 200]);
  });
}
