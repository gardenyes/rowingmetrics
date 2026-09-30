enum StrokeDetectionSensitivity {
  veryHigh,
  high,
  medium,
  low,
  veryLow;

  static const StrokeDetectionSensitivity defaultValue = medium;

  bool get showsSensitivityLabel =>
      this == veryHigh || this == medium || this == veryLow;

  static StrokeDetectionSensitivity fromStored(String? value) {
    if (value == null) return defaultValue;
    return StrokeDetectionSensitivity.values.firstWhere(
      (e) => e.name == value,
      orElse: () => defaultValue,
    );
  }
}

enum AppLanguage {
  english('en'),
  catalan('ca'),
  spanish('es'),
  french('fr');

  const AppLanguage(this.localeTag);
  final String localeTag;

  static const AppLanguage defaultValue = english;

  static AppLanguage fromStored(String? value) {
    if (value == null) return defaultValue;
    return AppLanguage.values.firstWhere(
      (e) => e.name == value,
      orElse: () => defaultValue,
    );
  }
}
