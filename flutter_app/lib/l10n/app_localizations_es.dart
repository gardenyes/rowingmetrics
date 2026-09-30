// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Spanish Castilian (`es`).
class AppLocalizationsEs extends AppLocalizations {
  AppLocalizationsEs([String locale = 'es']) : super(locale);

  @override
  String get appTitle => 'Rowing Metrics';

  @override
  String get navSession => 'Sesión';

  @override
  String get navActivities => 'Actividades';

  @override
  String get navSettings => 'Ajustes';

  @override
  String get strokeRate => 'Ritmo';

  @override
  String get speed => 'Velocidad';

  @override
  String get averageSpeed => 'Velocidad media';

  @override
  String get distance => 'Distancia';

  @override
  String get time => 'Tiempo';

  @override
  String get start => 'Iniciar';

  @override
  String get stop => 'Detener';

  @override
  String get activitiesTitle => 'Actividades';

  @override
  String get exportActivities => 'Exportar';

  @override
  String get exportActivitiesTitle => 'Exportar a Excel';

  @override
  String get exportActivitiesMessage =>
      '¿Exportar todas las actividades a un archivo CSV? Puedes abrirlo en Excel u otra hoja de cálculo.';

  @override
  String get exportActivitiesConfirm => 'Exportar';

  @override
  String get cancel => 'Cancelar';

  @override
  String get deleteAll => 'Eliminar todo';

  @override
  String get delete => 'Eliminar';

  @override
  String get deleteAllTitle => 'Eliminar todas las actividades';

  @override
  String get deleteAllMessage =>
      'Esto elimina todas las filas de la lista. Esta acción no se puede deshacer.';

  @override
  String get noActivitiesMessage =>
      'Aún no hay actividades. Pulsa Iniciar en la primera pantalla y luego Detener para registrar una.';

  @override
  String get colDate => 'Fecha';

  @override
  String get colHour => 'Hora';

  @override
  String get colTime => 'Tiempo';

  @override
  String get colStrokeRate => 'Ritmo';

  @override
  String get colAvgSpeed => 'Vel. media';

  @override
  String get colDistance => 'Distancia';

  @override
  String get speedKmhSuffix => 'km/h';

  @override
  String get csvColDate => 'Fecha';

  @override
  String get csvColHour => 'Hora';

  @override
  String get csvColTime => 'Tiempo';

  @override
  String get csvColStrokeRate => 'Ritmo';

  @override
  String get csvColAvgSpeed => 'Vel. media (km/h)';

  @override
  String get csvColDistance => 'Distancia';

  @override
  String get settingsTitle => 'Ajustes';

  @override
  String get detectionSensitivity => 'Sensibilidad de detección';

  @override
  String get sensitivityVeryHigh => 'Muy alta';

  @override
  String get sensitivityHigh => 'Alta';

  @override
  String get sensitivityMedium => 'Media';

  @override
  String get sensitivityLow => 'Baja';

  @override
  String get sensitivityVeryLow => 'Muy baja';

  @override
  String get speedSmoothing => 'Suavizado de velocidad';

  @override
  String get speedSmoothingDesc =>
      'Promedio de las últimas N muestras de velocidad GPS filtradas (lectura en vivo)';

  @override
  String get language => 'Idioma';

  @override
  String get languageEnglish => 'Inglés';

  @override
  String get languageCatalan => 'Catalán';

  @override
  String get languageSpanish => 'Español';

  @override
  String get languageFrench => 'Francés';
}
