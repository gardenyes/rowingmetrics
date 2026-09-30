import 'dart:async';

import 'package:flutter/widgets.dart';
import 'package:geolocator/geolocator.dart';
import 'package:sensors_plus/sensors_plus.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../core/gps_fix.dart';
import 'platform_services.dart';
import 'prefs_settings_store.dart';
import 'sqflite_activity_repository.dart';

class GeolocatorLocationTracker implements LocationTracker {
  StreamSubscription<Position>? _sub;

  @override
  Future<void> start(void Function(GpsFix fix) onFix) async {
    var status = await Geolocator.checkPermission();
    if (status == LocationPermission.denied) {
      status = await Geolocator.requestPermission();
    }
    if (status == LocationPermission.denied ||
        status == LocationPermission.deniedForever) {
      throw StateError('Location permission denied');
    }
    const settings = LocationSettings(
      accuracy: LocationAccuracy.bestForNavigation,
      distanceFilter: 0,
    );
    _sub = Geolocator.getPositionStream(locationSettings: settings).listen(
      (pos) {
        onFix(
          GpsFix(
            latitude: pos.latitude,
            longitude: pos.longitude,
            hasAccuracy: pos.accuracy >= 0,
            accuracyM: pos.accuracy,
            hasSpeed: pos.speed >= 0,
            speedMps: pos.speed < 0 ? 0 : pos.speed,
          ),
        );
      },
    );
  }

  @override
  Future<void> stop() async {
    await _sub?.cancel();
    _sub = null;
  }
}

class SensorsMotionSource implements MotionSensorSource {
  StreamSubscription<UserAccelerometerEvent>? _userSub;
  StreamSubscription<AccelerometerEvent>? _accelSub;
  List<double>? _lastGravity;

  @override
  bool get isAvailable => true;

  @override
  Future<void> start(
    void Function(double ax, double ay, double az, List<double>? gravity)
        onSample,
  ) async {
    _accelSub = accelerometerEventStream().listen((e) {
      _lastGravity = [e.x, e.y, e.z];
    });
    _userSub = userAccelerometerEventStream().listen((e) {
      onSample(e.x, e.y, e.z, _lastGravity);
    });
  }

  @override
  Future<void> stop() async {
    await _userSub?.cancel();
    await _accelSub?.cancel();
    _userSub = null;
    _accelSub = null;
  }
}

class WidgetsOrientationMonitor
    with WidgetsBindingObserver
    implements OrientationMonitor {
  void Function()? _onChanged;
  Orientation? _last;

  @override
  Future<void> start(void Function() onOrientationChanged) async {
    _onChanged = onOrientationChanged;
    WidgetsBinding.instance.addObserver(this);
    final view = WidgetsBinding.instance.platformDispatcher.views.first;
    final size = view.physicalSize / view.devicePixelRatio;
    _last =
        size.width > size.height ? Orientation.landscape : Orientation.portrait;
  }

  @override
  Future<void> stop() async {
    WidgetsBinding.instance.removeObserver(this);
    _onChanged = null;
  }

  @override
  void didChangeMetrics() {
    final view = WidgetsBinding.instance.platformDispatcher.views.first;
    final size = view.physicalSize / view.devicePixelRatio;
    final next =
        size.width > size.height ? Orientation.landscape : Orientation.portrait;
    if (_last != null && next != _last) {
      _last = next;
      _onChanged?.call();
    } else {
      _last = next;
    }
  }
}

class FlutterPlatformServices implements PlatformServices {
  FlutterPlatformServices({
    required this.settings,
    required this.activities,
    required this.locationTracker,
    required this.motionSensors,
    required this.orientationMonitor,
  });

  @override
  final SettingsStore settings;
  @override
  final ActivityRepository activities;
  @override
  final LocationTracker locationTracker;
  @override
  final MotionSensorSource motionSensors;
  @override
  final OrientationMonitor orientationMonitor;

  final Stopwatch _elapsed = Stopwatch()..start();

  static Future<FlutterPlatformServices> create() async {
    final prefs = await SharedPreferences.getInstance();
    final activities = await SqfliteActivityRepository.open();
    return FlutterPlatformServices(
      settings: PrefsSettingsStore(prefs),
      activities: activities,
      locationTracker: GeolocatorLocationTracker(),
      motionSensors: SensorsMotionSource(),
      orientationMonitor: WidgetsOrientationMonitor(),
    );
  }

  @override
  void postToMain(void Function() block) {
    scheduleMicrotask(block);
  }

  @override
  int elapsedRealtimeMs() => _elapsed.elapsedMilliseconds;

  @override
  int currentTimeMillis() => DateTime.now().millisecondsSinceEpoch;
}
