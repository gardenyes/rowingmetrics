import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:intl/intl.dart' as intl;

import 'app_localizations_ca.dart';
import 'app_localizations_en.dart';
import 'app_localizations_es.dart';
import 'app_localizations_fr.dart';

// ignore_for_file: type=lint

/// Callers can lookup localized strings with an instance of AppLocalizations
/// returned by `AppLocalizations.of(context)`.
///
/// Applications need to include `AppLocalizations.delegate()` in their app's
/// `localizationDelegates` list, and the locales they support in the app's
/// `supportedLocales` list. For example:
///
/// ```dart
/// import 'l10n/app_localizations.dart';
///
/// return MaterialApp(
///   localizationsDelegates: AppLocalizations.localizationsDelegates,
///   supportedLocales: AppLocalizations.supportedLocales,
///   home: MyApplicationHome(),
/// );
/// ```
///
/// ## Update pubspec.yaml
///
/// Please make sure to update your pubspec.yaml to include the following
/// packages:
///
/// ```yaml
/// dependencies:
///   # Internationalization support.
///   flutter_localizations:
///     sdk: flutter
///   intl: any # Use the pinned version from flutter_localizations
///
///   # Rest of dependencies
/// ```
///
/// ## iOS Applications
///
/// iOS applications define key application metadata, including supported
/// locales, in an Info.plist file that is built into the application bundle.
/// To configure the locales supported by your app, you’ll need to edit this
/// file.
///
/// First, open your project’s ios/Runner.xcworkspace Xcode workspace file.
/// Then, in the Project Navigator, open the Info.plist file under the Runner
/// project’s Runner folder.
///
/// Next, select the Information Property List item, select Add Item from the
/// Editor menu, then select Localizations from the pop-up menu.
///
/// Select and expand the newly-created Localizations item then, for each
/// locale your application supports, add a new item and select the locale
/// you wish to add from the pop-up menu in the Value field. This list should
/// be consistent with the languages listed in the AppLocalizations.supportedLocales
/// property.
abstract class AppLocalizations {
  AppLocalizations(String locale)
    : localeName = intl.Intl.canonicalizedLocale(locale.toString());

  final String localeName;

  static AppLocalizations of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations)!;
  }

  static const LocalizationsDelegate<AppLocalizations> delegate =
      _AppLocalizationsDelegate();

  /// A list of this localizations delegate along with the default localizations
  /// delegates.
  ///
  /// Returns a list of localizations delegates containing this delegate along with
  /// GlobalMaterialLocalizations.delegate, GlobalCupertinoLocalizations.delegate,
  /// and GlobalWidgetsLocalizations.delegate.
  ///
  /// Additional delegates can be added by appending to this list in
  /// MaterialApp. This list does not have to be used at all if a custom list
  /// of delegates is preferred or required.
  static const List<LocalizationsDelegate<dynamic>> localizationsDelegates =
      <LocalizationsDelegate<dynamic>>[
        delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
      ];

  /// A list of this localizations delegate's supported locales.
  static const List<Locale> supportedLocales = <Locale>[
    Locale('ca'),
    Locale('en'),
    Locale('es'),
    Locale('fr'),
  ];

  /// No description provided for @appTitle.
  ///
  /// In en, this message translates to:
  /// **'Rowing Metrics'**
  String get appTitle;

  /// No description provided for @navSession.
  ///
  /// In en, this message translates to:
  /// **'Session'**
  String get navSession;

  /// No description provided for @navActivities.
  ///
  /// In en, this message translates to:
  /// **'Activities'**
  String get navActivities;

  /// No description provided for @navSettings.
  ///
  /// In en, this message translates to:
  /// **'Settings'**
  String get navSettings;

  /// No description provided for @strokeRate.
  ///
  /// In en, this message translates to:
  /// **'Stroke rate'**
  String get strokeRate;

  /// No description provided for @speed.
  ///
  /// In en, this message translates to:
  /// **'Speed'**
  String get speed;

  /// No description provided for @averageSpeed.
  ///
  /// In en, this message translates to:
  /// **'Average speed'**
  String get averageSpeed;

  /// No description provided for @distance.
  ///
  /// In en, this message translates to:
  /// **'Distance'**
  String get distance;

  /// No description provided for @time.
  ///
  /// In en, this message translates to:
  /// **'Time'**
  String get time;

  /// No description provided for @start.
  ///
  /// In en, this message translates to:
  /// **'Start'**
  String get start;

  /// No description provided for @stop.
  ///
  /// In en, this message translates to:
  /// **'Stop'**
  String get stop;

  /// No description provided for @activitiesTitle.
  ///
  /// In en, this message translates to:
  /// **'Activities'**
  String get activitiesTitle;

  /// No description provided for @exportActivities.
  ///
  /// In en, this message translates to:
  /// **'Export'**
  String get exportActivities;

  /// No description provided for @exportActivitiesTitle.
  ///
  /// In en, this message translates to:
  /// **'Export to Excel'**
  String get exportActivitiesTitle;

  /// No description provided for @exportActivitiesMessage.
  ///
  /// In en, this message translates to:
  /// **'Export all activities to a CSV file? You can open it in Excel or another spreadsheet app.'**
  String get exportActivitiesMessage;

  /// No description provided for @exportActivitiesConfirm.
  ///
  /// In en, this message translates to:
  /// **'Export'**
  String get exportActivitiesConfirm;

  /// No description provided for @cancel.
  ///
  /// In en, this message translates to:
  /// **'Cancel'**
  String get cancel;

  /// No description provided for @deleteAll.
  ///
  /// In en, this message translates to:
  /// **'Delete all'**
  String get deleteAll;

  /// No description provided for @delete.
  ///
  /// In en, this message translates to:
  /// **'Delete'**
  String get delete;

  /// No description provided for @deleteAllTitle.
  ///
  /// In en, this message translates to:
  /// **'Delete all activities'**
  String get deleteAllTitle;

  /// No description provided for @deleteAllMessage.
  ///
  /// In en, this message translates to:
  /// **'This removes every row in the list. This cannot be undone.'**
  String get deleteAllMessage;

  /// No description provided for @noActivitiesMessage.
  ///
  /// In en, this message translates to:
  /// **'No activities yet. Press Start on the first screen, then Stop to record one.'**
  String get noActivitiesMessage;

  /// No description provided for @colDate.
  ///
  /// In en, this message translates to:
  /// **'Date'**
  String get colDate;

  /// No description provided for @colHour.
  ///
  /// In en, this message translates to:
  /// **'Hour'**
  String get colHour;

  /// No description provided for @colTime.
  ///
  /// In en, this message translates to:
  /// **'Time'**
  String get colTime;

  /// No description provided for @colStrokeRate.
  ///
  /// In en, this message translates to:
  /// **'Stroke rate'**
  String get colStrokeRate;

  /// No description provided for @colAvgSpeed.
  ///
  /// In en, this message translates to:
  /// **'Avg. speed'**
  String get colAvgSpeed;

  /// No description provided for @colDistance.
  ///
  /// In en, this message translates to:
  /// **'Distance'**
  String get colDistance;

  /// No description provided for @speedKmhSuffix.
  ///
  /// In en, this message translates to:
  /// **'km/h'**
  String get speedKmhSuffix;

  /// No description provided for @csvColDate.
  ///
  /// In en, this message translates to:
  /// **'Date'**
  String get csvColDate;

  /// No description provided for @csvColHour.
  ///
  /// In en, this message translates to:
  /// **'Hour'**
  String get csvColHour;

  /// No description provided for @csvColTime.
  ///
  /// In en, this message translates to:
  /// **'Time'**
  String get csvColTime;

  /// No description provided for @csvColStrokeRate.
  ///
  /// In en, this message translates to:
  /// **'Stroke rate'**
  String get csvColStrokeRate;

  /// No description provided for @csvColAvgSpeed.
  ///
  /// In en, this message translates to:
  /// **'Avg. speed (km/h)'**
  String get csvColAvgSpeed;

  /// No description provided for @csvColDistance.
  ///
  /// In en, this message translates to:
  /// **'Distance'**
  String get csvColDistance;

  /// No description provided for @settingsTitle.
  ///
  /// In en, this message translates to:
  /// **'Settings'**
  String get settingsTitle;

  /// No description provided for @detectionSensitivity.
  ///
  /// In en, this message translates to:
  /// **'Detection sensitivity'**
  String get detectionSensitivity;

  /// No description provided for @sensitivityVeryHigh.
  ///
  /// In en, this message translates to:
  /// **'Very high'**
  String get sensitivityVeryHigh;

  /// No description provided for @sensitivityHigh.
  ///
  /// In en, this message translates to:
  /// **'High'**
  String get sensitivityHigh;

  /// No description provided for @sensitivityMedium.
  ///
  /// In en, this message translates to:
  /// **'Medium'**
  String get sensitivityMedium;

  /// No description provided for @sensitivityLow.
  ///
  /// In en, this message translates to:
  /// **'Low'**
  String get sensitivityLow;

  /// No description provided for @sensitivityVeryLow.
  ///
  /// In en, this message translates to:
  /// **'Very low'**
  String get sensitivityVeryLow;

  /// No description provided for @speedSmoothing.
  ///
  /// In en, this message translates to:
  /// **'Speed smoothing'**
  String get speedSmoothing;

  /// No description provided for @speedSmoothingDesc.
  ///
  /// In en, this message translates to:
  /// **'Average last N filtered GPS speed samples (live readout)'**
  String get speedSmoothingDesc;

  /// No description provided for @language.
  ///
  /// In en, this message translates to:
  /// **'Language'**
  String get language;

  /// No description provided for @languageEnglish.
  ///
  /// In en, this message translates to:
  /// **'English'**
  String get languageEnglish;

  /// No description provided for @languageCatalan.
  ///
  /// In en, this message translates to:
  /// **'Catalan'**
  String get languageCatalan;

  /// No description provided for @languageSpanish.
  ///
  /// In en, this message translates to:
  /// **'Spanish'**
  String get languageSpanish;

  /// No description provided for @languageFrench.
  ///
  /// In en, this message translates to:
  /// **'French'**
  String get languageFrench;
}

class _AppLocalizationsDelegate
    extends LocalizationsDelegate<AppLocalizations> {
  const _AppLocalizationsDelegate();

  @override
  Future<AppLocalizations> load(Locale locale) {
    return SynchronousFuture<AppLocalizations>(lookupAppLocalizations(locale));
  }

  @override
  bool isSupported(Locale locale) =>
      <String>['ca', 'en', 'es', 'fr'].contains(locale.languageCode);

  @override
  bool shouldReload(_AppLocalizationsDelegate old) => false;
}

AppLocalizations lookupAppLocalizations(Locale locale) {
  // Lookup logic when only language code is specified.
  switch (locale.languageCode) {
    case 'ca':
      return AppLocalizationsCa();
    case 'en':
      return AppLocalizationsEn();
    case 'es':
      return AppLocalizationsEs();
    case 'fr':
      return AppLocalizationsFr();
  }

  throw FlutterError(
    'AppLocalizations.delegate failed to load unsupported locale "$locale". This is likely '
    'an issue with the localizations generation tool. Please file an issue '
    'on GitHub with a reproducible sample app and the gen-l10n configuration '
    'that was used.',
  );
}
