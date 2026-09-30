// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Catalan Valencian (`ca`).
class AppLocalizationsCa extends AppLocalizations {
  AppLocalizationsCa([String locale = 'ca']) : super(locale);

  @override
  String get appTitle => 'Rowing Metrics';

  @override
  String get navSession => 'Sessió';

  @override
  String get navActivities => 'Activitats';

  @override
  String get navSettings => 'Configuració';

  @override
  String get strokeRate => 'Ritme';

  @override
  String get speed => 'Velocitat';

  @override
  String get averageSpeed => 'Velocitat mitjana';

  @override
  String get distance => 'Distància';

  @override
  String get time => 'Temps';

  @override
  String get start => 'Inici';

  @override
  String get stop => 'Aturar';

  @override
  String get activitiesTitle => 'Activitats';

  @override
  String get exportActivities => 'Exportar';

  @override
  String get exportActivitiesTitle => 'Exportar a Excel';

  @override
  String get exportActivitiesMessage =>
      'Vols exportar totes les activitats a un fitxer CSV? Pots obrir-lo a Excel o una altra fulla de càlcul.';

  @override
  String get exportActivitiesConfirm => 'Exportar';

  @override
  String get cancel => 'Cancel·lar';

  @override
  String get deleteAll => 'Esborrar tot';

  @override
  String get delete => 'Esborrar';

  @override
  String get deleteAllTitle => 'Esborrar totes les activitats';

  @override
  String get deleteAllMessage =>
      'Això elimina totes les files de la llista. Aquesta acció no es pot desfer.';

  @override
  String get noActivitiesMessage =>
      'Encara no hi ha activitats. Prem Inici a la primera pantalla i després Aturar per enregistrar-ne una.';

  @override
  String get colDate => 'Data';

  @override
  String get colHour => 'Hora';

  @override
  String get colTime => 'Temps';

  @override
  String get colStrokeRate => 'Ritme';

  @override
  String get colAvgSpeed => 'Vel. mitjana';

  @override
  String get colDistance => 'Distància';

  @override
  String get speedKmhSuffix => 'km/h';

  @override
  String get csvColDate => 'Data';

  @override
  String get csvColHour => 'Hora';

  @override
  String get csvColTime => 'Temps';

  @override
  String get csvColStrokeRate => 'Ritme';

  @override
  String get csvColAvgSpeed => 'Vel. mitjana (km/h)';

  @override
  String get csvColDistance => 'Distància';

  @override
  String get settingsTitle => 'Configuració';

  @override
  String get detectionSensitivity => 'Sensibilitat de detecció';

  @override
  String get sensitivityVeryHigh => 'Molt alta';

  @override
  String get sensitivityHigh => 'Alta';

  @override
  String get sensitivityMedium => 'Mitjana';

  @override
  String get sensitivityLow => 'Baixa';

  @override
  String get sensitivityVeryLow => 'Molt baixa';

  @override
  String get speedSmoothing => 'Suavització de velocitat';

  @override
  String get speedSmoothingDesc =>
      'Mitjana de les últimes N mostres de velocitat GPS filtrades (lectura en viu)';

  @override
  String get language => 'Idioma';

  @override
  String get languageEnglish => 'Anglès';

  @override
  String get languageCatalan => 'Català';

  @override
  String get languageSpanish => 'Castellà';

  @override
  String get languageFrench => 'Francès';
}
