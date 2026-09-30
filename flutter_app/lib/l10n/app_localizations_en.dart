// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for English (`en`).
class AppLocalizationsEn extends AppLocalizations {
  AppLocalizationsEn([String locale = 'en']) : super(locale);

  @override
  String get appTitle => 'Rowing Metrics';

  @override
  String get navSession => 'Session';

  @override
  String get navActivities => 'Activities';

  @override
  String get navSettings => 'Settings';

  @override
  String get strokeRate => 'Stroke rate';

  @override
  String get speed => 'Speed';

  @override
  String get averageSpeed => 'Average speed';

  @override
  String get distance => 'Distance';

  @override
  String get time => 'Time';

  @override
  String get start => 'Start';

  @override
  String get stop => 'Stop';

  @override
  String get activitiesTitle => 'Activities';

  @override
  String get exportActivities => 'Export';

  @override
  String get exportActivitiesTitle => 'Export to Excel';

  @override
  String get exportActivitiesMessage =>
      'Export all activities to a CSV file? You can open it in Excel or another spreadsheet app.';

  @override
  String get exportActivitiesConfirm => 'Export';

  @override
  String get cancel => 'Cancel';

  @override
  String get deleteAll => 'Delete all';

  @override
  String get delete => 'Delete';

  @override
  String get deleteAllTitle => 'Delete all activities';

  @override
  String get deleteAllMessage =>
      'This removes every row in the list. This cannot be undone.';

  @override
  String get noActivitiesMessage =>
      'No activities yet. Press Start on the first screen, then Stop to record one.';

  @override
  String get colDate => 'Date';

  @override
  String get colHour => 'Hour';

  @override
  String get colTime => 'Time';

  @override
  String get colStrokeRate => 'Stroke rate';

  @override
  String get colAvgSpeed => 'Avg. speed';

  @override
  String get colDistance => 'Distance';

  @override
  String get speedKmhSuffix => 'km/h';

  @override
  String get csvColDate => 'Date';

  @override
  String get csvColHour => 'Hour';

  @override
  String get csvColTime => 'Time';

  @override
  String get csvColStrokeRate => 'Stroke rate';

  @override
  String get csvColAvgSpeed => 'Avg. speed (km/h)';

  @override
  String get csvColDistance => 'Distance';

  @override
  String get settingsTitle => 'Settings';

  @override
  String get detectionSensitivity => 'Detection sensitivity';

  @override
  String get sensitivityVeryHigh => 'Very high';

  @override
  String get sensitivityHigh => 'High';

  @override
  String get sensitivityMedium => 'Medium';

  @override
  String get sensitivityLow => 'Low';

  @override
  String get sensitivityVeryLow => 'Very low';

  @override
  String get speedSmoothing => 'Speed smoothing';

  @override
  String get speedSmoothingDesc =>
      'Average last N filtered GPS speed samples (live readout)';

  @override
  String get language => 'Language';

  @override
  String get languageEnglish => 'English';

  @override
  String get languageCatalan => 'Catalan';

  @override
  String get languageSpanish => 'Spanish';

  @override
  String get languageFrench => 'French';
}
