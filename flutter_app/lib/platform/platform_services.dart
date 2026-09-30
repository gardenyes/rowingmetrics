import '../core/completed_activity.dart';
import '../core/enums.dart';
import '../core/gps_fix.dart';

abstract class SettingsStore {
  StrokeDetectionSensitivity getStrokeDetectionSensitivity();
  Future<void> setStrokeDetectionSensitivity(StrokeDetectionSensitivity s);
  int getGpsDisplayAverageN();
  Future<void> setGpsDisplayAverageN(int n);
  int getGpsDisplayAverageCount();
  AppLanguage getAppLanguage();
  Future<void> setAppLanguage(AppLanguage language);
}

abstract class ActivityRepository {
  Stream<List<CompletedActivity>> observeAll();
  Future<void> insert(CompletedActivity activity);
  Future<void> deleteById(int id);
  Future<void> deleteAll();
}

abstract class LocationTracker {
  Future<void> start(void Function(GpsFix fix) onFix);
  Future<void> stop();
}

abstract class MotionSensorSource {
  bool get isAvailable;
  Future<void> start(
    void Function(double ax, double ay, double az, List<double>? gravity)
        onSample,
  );
  Future<void> stop();
}

abstract class OrientationMonitor {
  Future<void> start(void Function() onOrientationChanged);
  Future<void> stop();
}

abstract class PlatformServices {
  SettingsStore get settings;
  ActivityRepository get activities;
  LocationTracker get locationTracker;
  MotionSensorSource get motionSensors;
  OrientationMonitor get orientationMonitor;

  void postToMain(void Function() block);
  int elapsedRealtimeMs();
  int currentTimeMillis();
}
