import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../core/enums.dart';
import 'app.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final model = context.watch<AppModel>();
    final sensitivity = model.controller.getStrokeDetectionSensitivity();
    final gpsN = model.controller.getGpsDisplayAverageN();
    final language = model.platform.settings.getAppLanguage();

    return SafeArea(
      child: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Text(
            'Settings',
            style: Theme.of(context).textTheme.headlineMedium,
          ),
          const SizedBox(height: 24),
          Text(
            'Stroke detection sensitivity',
            style: Theme.of(context).textTheme.titleMedium,
          ),
          const SizedBox(height: 8),
          Text(
            _sensitivityLabel(sensitivity),
            style: const TextStyle(color: Colors.white70),
          ),
          Slider(
            value: sensitivity.index.toDouble(),
            min: 0,
            max: (StrokeDetectionSensitivity.values.length - 1).toDouble(),
            divisions: StrokeDetectionSensitivity.values.length - 1,
            label: _sensitivityLabel(sensitivity),
            onChanged: (v) {
              model.setSensitivity(
                StrokeDetectionSensitivity.values[v.round()],
              );
            },
          ),
          const SizedBox(height: 16),
          Text(
            'GPS speed average samples',
            style: Theme.of(context).textTheme.titleMedium,
          ),
          const SizedBox(height: 8),
          Text('$gpsN', style: const TextStyle(color: Colors.white70)),
          Slider(
            value: gpsN.toDouble(),
            min: 1,
            max: 8,
            divisions: 7,
            label: '$gpsN',
            onChanged: (v) => model.setGpsAvg(v.round()),
          ),
          const SizedBox(height: 16),
          Text(
            'Language',
            style: Theme.of(context).textTheme.titleMedium,
          ),
          const SizedBox(height: 8),
          DropdownButton<AppLanguage>(
            value: language,
            isExpanded: true,
            items: AppLanguage.values
                .map(
                  (l) => DropdownMenuItem(
                    value: l,
                    child: Text(_languageLabel(l)),
                  ),
                )
                .toList(),
            onChanged: (l) {
              if (l != null) model.setLanguage(l);
            },
          ),
        ],
      ),
    );
  }

  String _sensitivityLabel(StrokeDetectionSensitivity s) {
    switch (s) {
      case StrokeDetectionSensitivity.veryHigh:
        return 'Very high';
      case StrokeDetectionSensitivity.high:
        return 'High';
      case StrokeDetectionSensitivity.medium:
        return 'Medium';
      case StrokeDetectionSensitivity.low:
        return 'Low';
      case StrokeDetectionSensitivity.veryLow:
        return 'Very low';
    }
  }

  String _languageLabel(AppLanguage l) {
    switch (l) {
      case AppLanguage.english:
        return 'English';
      case AppLanguage.catalan:
        return 'Català';
      case AppLanguage.spanish:
        return 'Español';
      case AppLanguage.french:
        return 'Français';
    }
  }
}
