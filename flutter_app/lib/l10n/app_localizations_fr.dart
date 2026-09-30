// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for French (`fr`).
class AppLocalizationsFr extends AppLocalizations {
  AppLocalizationsFr([String locale = 'fr']) : super(locale);

  @override
  String get appTitle => 'Rowing Metrics';

  @override
  String get navSession => 'Session';

  @override
  String get navActivities => 'Activités';

  @override
  String get navSettings => 'Réglages';

  @override
  String get strokeRate => 'Cadence';

  @override
  String get speed => 'Vitesse';

  @override
  String get averageSpeed => 'Vitesse moyenne';

  @override
  String get distance => 'Distance';

  @override
  String get time => 'Temps';

  @override
  String get start => 'Démarrer';

  @override
  String get stop => 'Arrêter';

  @override
  String get activitiesTitle => 'Activités';

  @override
  String get exportActivities => 'Exporter';

  @override
  String get exportActivitiesTitle => 'Exporter vers Excel';

  @override
  String get exportActivitiesMessage =>
      'Exporter toutes les activités vers un fichier CSV ? Vous pouvez l\'ouvrir dans Excel ou un autre tableur.';

  @override
  String get exportActivitiesConfirm => 'Exporter';

  @override
  String get cancel => 'Annuler';

  @override
  String get deleteAll => 'Tout supprimer';

  @override
  String get delete => 'Supprimer';

  @override
  String get deleteAllTitle => 'Supprimer toutes les activités';

  @override
  String get deleteAllMessage =>
      'Cela supprime toutes les lignes de la liste. Cette action est irréversible.';

  @override
  String get noActivitiesMessage =>
      'Aucune activité pour l\'instant. Appuyez sur Démarrer sur le premier écran, puis Arrêter pour en enregistrer une.';

  @override
  String get colDate => 'Date';

  @override
  String get colHour => 'Heure';

  @override
  String get colTime => 'Temps';

  @override
  String get colStrokeRate => 'Cadence';

  @override
  String get colAvgSpeed => 'Vit. moy.';

  @override
  String get colDistance => 'Distance';

  @override
  String get speedKmhSuffix => 'km/h';

  @override
  String get csvColDate => 'Date';

  @override
  String get csvColHour => 'Heure';

  @override
  String get csvColTime => 'Temps';

  @override
  String get csvColStrokeRate => 'Cadence';

  @override
  String get csvColAvgSpeed => 'Vit. moy. (km/h)';

  @override
  String get csvColDistance => 'Distance';

  @override
  String get settingsTitle => 'Réglages';

  @override
  String get detectionSensitivity => 'Sensibilité de détection';

  @override
  String get sensitivityVeryHigh => 'Très élevée';

  @override
  String get sensitivityHigh => 'Élevée';

  @override
  String get sensitivityMedium => 'Moyenne';

  @override
  String get sensitivityLow => 'Faible';

  @override
  String get sensitivityVeryLow => 'Très faible';

  @override
  String get speedSmoothing => 'Lissage de vitesse';

  @override
  String get speedSmoothingDesc =>
      'Moyenne des N dernières échantillons de vitesse GPS filtrés (lecture en direct)';

  @override
  String get language => 'Langue';

  @override
  String get languageEnglish => 'Anglais';

  @override
  String get languageCatalan => 'Catalan';

  @override
  String get languageSpanish => 'Espagnol';

  @override
  String get languageFrench => 'Français';
}
