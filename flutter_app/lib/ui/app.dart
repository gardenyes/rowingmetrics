import 'dart:async';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../core/activity_record_format.dart';
import '../core/completed_activity.dart';
import '../core/enums.dart';
import '../core/rowing_session_controller.dart';
import '../core/rowing_ui_state.dart';
import '../platform/flutter_platform_services.dart';
import '../platform/platform_services.dart';
import 'activities_screen.dart';
import 'settings_screen.dart';

class AppModel extends ChangeNotifier {
  AppModel(this.platform) {
    controller = RowingSessionController(platform);
    controller.setListener((s) {
      ui = s;
      notifyListeners();
    });
    _sub = platform.activities.observeAll().listen((list) {
      activities = list;
      notifyListeners();
    });
  }

  final PlatformServices platform;
  late final RowingSessionController controller;
  RowingUiState ui = const RowingUiState();
  List<CompletedActivity> activities = const [];
  late final StreamSubscription<List<CompletedActivity>> _sub;

  Future<void> start() => controller.startSession();
  Future<void> stop() => controller.stopSession();

  Future<void> setSensitivity(StrokeDetectionSensitivity s) async {
    await controller.setStrokeDetectionSensitivity(s);
    notifyListeners();
  }

  Future<void> setGpsAvg(int n) async {
    await controller.setGpsDisplayAverageN(n);
    notifyListeners();
  }

  Future<void> setLanguage(AppLanguage language) async {
    await platform.settings.setAppLanguage(language);
    notifyListeners();
  }

  Future<void> deleteActivity(int id) => platform.activities.deleteById(id);

  Future<void> deleteAllActivities() => platform.activities.deleteAll();

  @override
  void dispose() {
    _sub.cancel();
    controller.dispose();
    super.dispose();
  }
}

class RowingMetricsApp extends StatelessWidget {
  const RowingMetricsApp({super.key, required this.platform});

  final FlutterPlatformServices platform;

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => AppModel(platform),
      child: Consumer<AppModel>(
        builder: (context, model, _) {
          final lang = model.platform.settings.getAppLanguage();
          return MaterialApp(
            title: 'Rowing Metrics',
            debugShowCheckedModeBanner: false,
            locale: Locale(lang.localeTag),
            theme: ThemeData(
              brightness: Brightness.dark,
              colorScheme: ColorScheme.fromSeed(
                seedColor: const Color(0xFF1B6CA8),
                brightness: Brightness.dark,
              ),
              useMaterial3: true,
            ),
            home: const HomeShell(),
          );
        },
      ),
    );
  }
}

class HomeShell extends StatefulWidget {
  const HomeShell({super.key});

  @override
  State<HomeShell> createState() => _HomeShellState();
}

class _HomeShellState extends State<HomeShell> {
  int _index = 0;

  @override
  Widget build(BuildContext context) {
    final pages = const [
      SessionScreen(),
      ActivitiesScreen(),
      SettingsScreen(),
    ];
    return Scaffold(
      body: pages[_index],
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (i) => setState(() => _index = i),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.rowing), label: 'Session'),
          NavigationDestination(icon: Icon(Icons.history), label: 'Activities'),
          NavigationDestination(icon: Icon(Icons.settings), label: 'Settings'),
        ],
      ),
    );
  }
}

class SessionScreen extends StatelessWidget {
  const SessionScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final model = context.watch<AppModel>();
    final ui = model.ui;
    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(
              'Rowing Metrics',
              style: Theme.of(context).textTheme.headlineMedium,
            ),
            const SizedBox(height: 24),
            Expanded(
              child: GridView.count(
                crossAxisCount: 2,
                mainAxisSpacing: 12,
                crossAxisSpacing: 12,
                childAspectRatio: 1.4,
                children: [
                  _MetricCard(
                    label: 'Stroke rate',
                    value: ui.avgStrokesPerMin.toStringAsFixed(1),
                    unit: 'spm',
                    live: ui.strokeRateDetectionLive,
                  ),
                  _MetricCard(
                    label: 'Speed',
                    value: ui.currentSpeedKmh.toStringAsFixed(1),
                    unit: 'km/h',
                  ),
                  _MetricCard(
                    label: 'Avg speed',
                    value: ui.averageSpeedKmh.toStringAsFixed(1),
                    unit: 'km/h',
                  ),
                  _MetricCard(
                    label: 'Distance',
                    value: formatDistanceTable(ui.distanceMeters),
                    unit: '',
                  ),
                  _MetricCard(
                    label: 'Time',
                    value: formatSessionElapsed(ui.activityElapsedMs),
                    unit: '',
                  ),
                ],
              ),
            ),
            FilledButton.tonal(
              onPressed: () async {
                if (ui.running) {
                  await model.stop();
                } else {
                  await model.start();
                }
              },
              style: FilledButton.styleFrom(
                minimumSize: const Size.fromHeight(56),
                backgroundColor:
                    ui.running ? Colors.red.shade700 : Colors.teal.shade700,
              ),
              child: Text(ui.running ? 'Stop' : 'Start'),
            ),
          ],
        ),
      ),
    );
  }
}

class _MetricCard extends StatelessWidget {
  const _MetricCard({
    required this.label,
    required this.value,
    required this.unit,
    this.live = true,
  });

  final String label;
  final String value;
  final String unit;
  final bool live;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: const Color(0xFF161616),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.white12),
      ),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(label, style: const TextStyle(color: Colors.white70)),
            const Spacer(),
            Row(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Text(
                  value,
                  style: TextStyle(
                    fontSize: 28,
                    fontWeight: FontWeight.w600,
                    color: live ? Colors.white : Colors.white38,
                  ),
                ),
                if (unit.isNotEmpty) ...[
                  const SizedBox(width: 6),
                  Padding(
                    padding: const EdgeInsets.only(bottom: 4),
                    child: Text(
                      unit,
                      style: const TextStyle(color: Colors.white54),
                    ),
                  ),
                ],
              ],
            ),
          ],
        ),
      ),
    );
  }
}
