import 'package:shared_preferences/shared_preferences.dart';

import '../core/enums.dart';
import 'platform_services.dart';

class PrefsSettingsStore implements SettingsStore {
  PrefsSettingsStore(this._prefs);

  final SharedPreferences _prefs;

  static const _kSensitivity = 'stroke_detection_sensitivity';
  static const _kGpsAvgN = 'gps_display_average_n';
  static const _kLanguage = 'app_language';

  @override
  StrokeDetectionSensitivity getStrokeDetectionSensitivity() =>
      StrokeDetectionSensitivity.fromStored(_prefs.getString(_kSensitivity));

  @override
  Future<void> setStrokeDetectionSensitivity(
    StrokeDetectionSensitivity s,
  ) async {
    await _prefs.setString(_kSensitivity, s.name);
  }

  @override
  int getGpsDisplayAverageN() => _prefs.getInt(_kGpsAvgN) ?? 4;

  @override
  Future<void> setGpsDisplayAverageN(int n) async {
    await _prefs.setInt(_kGpsAvgN, n.clamp(1, 8));
  }

  @override
  int getGpsDisplayAverageCount() => getGpsDisplayAverageN().clamp(1, 8);

  @override
  AppLanguage getAppLanguage() =>
      AppLanguage.fromStored(_prefs.getString(_kLanguage));

  @override
  Future<void> setAppLanguage(AppLanguage language) async {
    await _prefs.setString(_kLanguage, language.name);
  }
}
