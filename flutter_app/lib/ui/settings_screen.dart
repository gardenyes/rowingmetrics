import 'package:flutter/material.dart';
import 'package:rowing_metrics/l10n/app_localizations.dart';

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
    final l10n = AppLocalizations.of(context);

    return SafeArea(
      child: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Text(
            l10n.settingsTitle,
            style: Theme.of(context).textTheme.headlineMedium,
          ),
          const SizedBox(height: 24),
          Text(
            l10n.detectionSensitivity,
            style: Theme.of(context).textTheme.titleMedium,
          ),
          const SizedBox(height: 8),
          Text(
            _sensitivityLabel(l10n, sensitivity),
            style: const TextStyle(color: Colors.white70),
          ),
          Slider(
            value: sensitivity.index.toDouble(),
            min: 0,
            max: (StrokeDetectionSensitivity.values.length - 1).toDouble(),
            divisions: StrokeDetectionSensitivity.values.length - 1,
            label: _sensitivityLabel(l10n, sensitivity),
            onChanged: (v) {
              model.setSensitivity(
                StrokeDetectionSensitivity.values[v.round()],
              );
            },
          ),
          const SizedBox(height: 16),
          Text(
            l10n.speedSmoothing,
            style: Theme.of(context).textTheme.titleMedium,
          ),
          const SizedBox(height: 8),
          Text(
            l10n.speedSmoothingDesc,
            style: const TextStyle(color: Colors.white70),
          ),
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
            l10n.language,
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
                    child: Text(_languageLabel(l10n, l)),
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

  String _sensitivityLabel(
    AppLocalizations l10n,
    StrokeDetectionSensitivity s,
  ) {
    switch (s) {
      case StrokeDetectionSensitivity.veryHigh:
        return l10n.sensitivityVeryHigh;
      case StrokeDetectionSensitivity.high:
        return l10n.sensitivityHigh;
      case StrokeDetectionSensitivity.medium:
        return l10n.sensitivityMedium;
      case StrokeDetectionSensitivity.low:
        return l10n.sensitivityLow;
      case StrokeDetectionSensitivity.veryLow:
        return l10n.sensitivityVeryLow;
    }
  }

  String _languageLabel(AppLocalizations l10n, AppLanguage l) {
    switch (l) {
      case AppLanguage.english:
        return l10n.languageEnglish;
      case AppLanguage.catalan:
        return l10n.languageCatalan;
      case AppLanguage.spanish:
        return l10n.languageSpanish;
      case AppLanguage.french:
        return l10n.languageFrench;
    }
  }
}
