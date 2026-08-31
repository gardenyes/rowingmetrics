import PDFDocument from 'pdfkit';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

const colors = {
  title: '#1a1a1a',
  heading: '#2e7d32',
  subheading: '#333333',
  body: '#444444',
  muted: '#666666',
};

/** Content for each locale — matches current app UI (screens & options). */
const guides = {
  en: {
    file: 'Rowing-Metrics-User-Guide-en.pdf',
    alsoAsDefault: true,
    metaTitle: 'Rowing Metrics — User Guide',
    coverTitle: 'Rowing Metrics',
    coverSubtitle: 'User Guide — Screens & Options',
    coverIntro:
      'Cross-platform rowing session tracker for Android and iOS.\nThis document describes every screen, metric, and configuration option in the app.',
    sections: {
      overview: '1. Overview',
      overviewBody:
        'Rowing Metrics tracks rowing sessions using the phone accelerometer for stroke rate (SPM) and GPS for speed and distance. The same interface runs on Android and iOS (shared Kotlin Multiplatform UI).',
      keyCaps: 'Key capabilities:',
      caps: [
        'Live stroke rate, speed, average speed, distance, and elapsed time',
        'Configurable stroke detection sensitivity and GPS speed smoothing',
        'Automatic saving of completed sessions to local history',
        'Export activity history to CSV (Excel-compatible) on Android',
        'UI languages: English, Catalan, Spanish, and French',
      ],
      navigation: 'Navigation',
      navigationBody:
        'The app has four screens in a horizontal pager. Swipe left or right to move between them:',
      tableHeaders: ['#', 'Screen', 'Purpose'],
      screens: [
        ['1', 'Main metrics', 'Live session display — Start / Stop'],
        ['2', 'Activities', 'History of completed sessions; export / delete'],
        ['3', 'Configuration', 'Stroke sensitivity, speed smoothing, language'],
        ['4', 'About', 'App name, version, and contact'],
      ],
      main: '2. Main Metrics Screen (Screen 1)',
      mainBody:
        'Primary on-water display. Shows live metrics while a session is running, and final values after Stop. Layout adapts to portrait and landscape.',
      metricsTitle: 'Metrics displayed',
      metricHeaders: ['Field', 'Unit / format', 'Description'],
      metrics: [
        ['Stroke rate', 'SPM (integer)', 'Strokes per minute from accelerometer. Largest value on screen.'],
        ['Speed', 'km/h (1 decimal)', 'Current GPS speed, smoothed by the Speed smoothing setting.'],
        ['Average speed', 'km/h (1 decimal)', 'Total distance ÷ elapsed time for the session.'],
        ['Distance', 'meters (integer)', 'Cumulative GPS distance since Start (segments ≥ 4 m).'],
        ['Time', 'MM:SS or HH:MM:SS', 'Elapsed session time. Shows hours from 1 hour onward.'],
        ['Clock', 'HH:MM', 'Device local time (updates every second), beside Time.'],
      ],
      startStop: 'Start / Stop button',
      startStopBody:
        'Green Start begins a session. While running it turns red and shows Stop.',
      startStopBullets: [
        'Start: accelerometer stroke detection, GPS tracking, and session timer',
        'Stop: ends the session and saves a summary to Activities',
        'On first Start, Android requests location permission if not already granted',
      ],
      warning: 'Stroke detection warning',
      warningBody:
        'While running, if no stroke has been detected recently, the stroke rate band blinks between light red and black. Adjust phone placement or increase detection sensitivity in Configuration.',
      portrait: 'Portrait layout',
      portraitBody:
        'Top to bottom: Stroke rate, Speed, Average speed, Distance, Time + Clock, Start/Stop.',
      landscape: 'Landscape layout',
      landscapeBody:
        'Left: Stroke rate and Start/Stop. Right: Speed + Average speed, Distance, Time + Clock.',
      androidNotes: 'Android-specific behavior',
      androidBullets: [
        'Screen stays on while the app is in the foreground',
        'Can appear over the lock screen and turn the screen on while rowing',
        'Requires accelerometer/gyroscope for stroke detection',
      ],
      activities: '3. Activities Screen (Screen 2)',
      activitiesBody:
        'Title: “Activities”. Scrollable table of completed sessions saved when you press Stop.',
      columnsTitle: 'Table columns',
      colHeaders: ['Column', 'Content'],
      columns: [
        ['Date', 'Date the session ended'],
        ['Hour', 'Start time (end time − duration)'],
        ['Time', 'Session duration'],
        ['Stroke rate', 'Average SPM for the session'],
        ['Avg. speed', 'Average speed in km/h'],
        ['Distance', 'Total distance in meters'],
        ['Delete', 'Remove that single session'],
      ],
      empty: 'Empty state',
      emptyBody:
        '“No activities yet. Press Start on the first screen, then Stop to record one.”',
      export: 'Export activities (Android)',
      exportBullets: [
        'Download icon appears when at least one activity exists',
        'Confirm “Export to Excel”, then choose where to save the CSV',
        'Columns: Date, Hour, Time, Stroke rate, Avg. speed (km/h), Distance',
      ],
      deleteAll: 'Delete all',
      deleteAllBody:
        '“Delete all” asks for confirmation and permanently removes every session.',
      deleteOne: 'Delete one session',
      deleteOneBody: 'Tap Delete on a row to remove that session immediately.',
      config: '4. Configuration Screen (Screen 3)',
      configBody:
        'Title: “Rowing Metrics Configuration”. Changes are saved immediately. Stroke sensitivity applied to the next session (or after Stop if changed mid-session).',
      sensitivity: 'Stroke profile — Detection sensitivity',
      sensitivityBody:
        '5-step horizontal slider. Left = strictest (Low); right = most permissive (High). Only Low, Medium, and High show text labels; intermediate steps are tick marks only.',
      sensHeaders: ['Position', 'Label', 'Behavior'],
      sensRows: [
        ['1 (left)', 'Low', 'Strictest — fewer false strokes; soft strokes may be missed'],
        ['2', '(tick)', 'Between Low and Medium'],
        ['3 (center)', 'Medium', 'Default — balanced'],
        ['4', '(tick)', 'Between Medium and High'],
        ['5 (right)', 'High', 'Most permissive — easier detection; more false positives'],
      ],
      sensTip:
        'Move toward High if strokes are missed; toward Low in rough water or when handling the phone causes false counts.',
      smoothing: 'Speed smoothing',
      smoothingBody:
        'Slider 1–8 (default 4). How many recent filtered GPS speed samples are averaged for live Speed. Does not change Average speed (always distance ÷ time).',
      language: 'Language',
      languageBody: 'Dropdown: English (default), Catalan, Spanish, French. Applies to all UI labels and CSV headers.',
      about: '5. About Screen (Screen 4)',
      aboutBody: 'Shows app title “Rowing Metrics”, version number, and contact gardenyes@gmail.com.',
      how: '6. How Metrics Are Calculated',
      howIntro: 'Meaning of the numbers behind the UI.',
      spm: 'Stroke rate (live SPM)',
      spmBullets: [
        'Accelerometer samples detect stroke peaks with adaptive thresholds',
        'SPM updates about every 2 seconds from recent stroke intervals',
        'Needs several strokes before a stable SPM is shown',
        'Returns toward 0 if no stroke is detected for a few seconds',
        'Session average SPM = mean of SPM samples while running (not strokes ÷ time)',
      ],
      speed: 'Speed (current)',
      speedBullets: [
        'GPS speed is filtered, then averaged over the last N samples (N = Speed smoothing)',
        'Shown in km/h; poor GPS fixes are rejected',
      ],
      avgSpeed: 'Average speed',
      avgSpeedBody: 'Total session distance ÷ elapsed time (not an average of instantaneous speeds).',
      distance: 'Distance',
      distanceBullets: [
        'From GPS position changes',
        'Only segments of at least about 4 meters are counted',
      ],
      orientation: 'Orientation changes',
      orientationBody:
        'If phone orientation changes during a session, stroke detection pauses briefly (~1.2 s) then resumes.',
      requirements: '7. Requirements & Platform Notes',
      perms: 'Permissions (Android)',
      permsBullets: [
        'Location (Fine or Coarse): for speed and distance',
        'Motion sensors: for stroke detection',
      ],
      platform: 'Platform differences',
      platHeaders: ['Feature', 'Android', 'iOS'],
      platRows: [
        ['Stroke detection', 'Full support', 'Pending Core Motion bindings'],
        ['GPS speed / distance', 'Full support', 'Pending Core Location bindings'],
        ['Activities history', 'Full support', 'Full support'],
        ['Configuration / language', 'Full support', 'Full support'],
        ['CSV export', 'Full support', 'Not yet implemented'],
        ['Lock screen display', 'Yes', 'Platform default'],
      ],
      hardware: 'Hardware',
      hardwareBullets: [
        'GPS for speed and distance',
        'Accelerometer / gyroscope for stroke rate',
      ],
      quick: '8. Quick Reference',
      swipe: 'Screen swipe order',
      swipeBody: 'Main metrics → Activities → Configuration → About',
      defaults: 'Default settings',
      defHeaders: ['Setting', 'Default'],
      defRows: [
        ['Detection sensitivity', 'Medium (center)'],
        ['Speed smoothing', '4'],
        ['Language', 'English'],
      ],
      workflow: 'Typical workflow',
      workflowSteps: [
        '1. Open the app (grant location when prompted).',
        '2. Optionally open Configuration to adjust sensitivity or language.',
        '3. On the main screen, press Start when you begin rowing.',
        '4. Monitor stroke rate, speed, and distance live.',
        '5. Press Stop when finished — the session is saved to Activities.',
        '6. Review, export, or delete past sessions on Activities.',
      ],
      footer: 'Rowing Metrics User Guide — matches current app behavior.',
      contact: 'Contact: gardenyes@gmail.com',
    },
  },

  ca: {
    file: 'Rowing-Metrics-User-Guide-ca.pdf',
    metaTitle: 'Rowing Metrics — Guia d’usuari',
    coverTitle: 'Rowing Metrics',
    coverSubtitle: 'Guia d’usuari — Pantalles i opcions',
    coverIntro:
      'Seguiment de sessions de rem multiplataforma per a Android i iOS.\nAquest document descriu cada pantalla, mètrica i opció de configuració de l’aplicació.',
    sections: {
      overview: '1. Visió general',
      overviewBody:
        'Rowing Metrics registra sessions de rem amb l’acceleròmetre del telèfon per al ritme de braçada (SPM) i el GPS per a la velocitat i la distància. La mateixa interfície funciona a Android i iOS (UI compartida Kotlin Multiplatform).',
      keyCaps: 'Funcions principals:',
      caps: [
        'Ritme, velocitat, velocitat mitjana, distància i temps en viu',
        'Sensibilitat de detecció i suavització de velocitat GPS configurables',
        'Desament automàtic de les sessions a l’historial local',
        'Exportació de l’historial a CSV (compatible amb Excel) a Android',
        'Idiomes: anglès, català, castellà i francès',
      ],
      navigation: 'Navegació',
      navigationBody:
        'L’app té quatre pantalles en un paginador horitzontal. Llisqueu a esquerra o dreta:',
      tableHeaders: ['#', 'Pantalla', 'Finalitat'],
      screens: [
        ['1', 'Mètriques', 'Sessió en viu — Inici / Aturar'],
        ['2', 'Activitats', 'Historial de sessions; exportar / esborrar'],
        ['3', 'Configuració', 'Sensibilitat, suavització de velocitat, idioma'],
        ['4', 'Informació', 'Nom de l’app, versió i contacte'],
      ],
      main: '2. Pantalla de mètriques (pantalla 1)',
      mainBody:
        'Pantalla principal a l’aigua. Mostra mètriques en viu mentre corre la sessió, i els valors finals després d’Aturar. El disseny s’adapta a vertical i horitzontal.',
      metricsTitle: 'Mètriques mostrades',
      metricHeaders: ['Camp', 'Unitat / format', 'Descripció'],
      metrics: [
        ['Ritme', 'SPM (enter)', 'Braçades per minut (acceleròmetre). El valor més gran de la pantalla.'],
        ['Velocitat', 'km/h (1 decimal)', 'Velocitat GPS actual, suavitzada segons la configuració.'],
        ['Velocitat mitjana', 'km/h (1 decimal)', 'Distància total ÷ temps transcorregut de la sessió.'],
        ['Distància', 'metres (enter)', 'Distància GPS acumulada des d’Inici (segments ≥ 4 m).'],
        ['Temps', 'MM:SS o HH:MM:SS', 'Temps de sessió. Mostra hores a partir d’1 hora.'],
        ['Rellotge', 'HH:MM', 'Hora local del dispositiu (cada segon), al costat del Temps.'],
      ],
      startStop: 'Botó Inici / Aturar',
      startStopBody:
        'Inici verd comença la sessió. Mentre corre, es torna vermell i mostra Aturar.',
      startStopBullets: [
        'Inici: detecció de braçades, GPS i cronòmetre',
        'Aturar: acaba la sessió i desa un resum a Activitats',
        'Al primer Inici, Android demana el permís d’ubicació si cal',
      ],
      warning: 'Avís de detecció de braçades',
      warningBody:
        'Si no es detecta cap braçada recentment mentre corre la sessió, la banda del ritme parpelleja entre vermell clar i negre. Ajusteu la col·locació del telèfon o pugeu la sensibilitat a Configuració.',
      portrait: 'Disposició vertical',
      portraitBody:
        'De dalt a baix: Ritme, Velocitat, Velocitat mitjana, Distància, Temps + Rellotge, Inici/Aturar.',
      landscape: 'Disposició horitzontal',
      landscapeBody:
        'Esquerra: Ritme i Inici/Aturar. Dreta: Velocitat + mitjana, Distància, Temps + Rellotge.',
      androidNotes: 'Comportament específic d’Android',
      androidBullets: [
        'La pantalla roman encesa en primer pla',
        'Pot mostrar-se sobre la pantalla de bloqueig mentre remeu',
        'Cal acceleròmetre/giroscopi per al ritme',
      ],
      activities: '3. Pantalla d’Activitats (pantalla 2)',
      activitiesBody:
        'Títol: «Activitats». Taula desplaçable de sessions desades en prémer Aturar.',
      columnsTitle: 'Columnes de la taula',
      colHeaders: ['Columna', 'Contingut'],
      columns: [
        ['Data', 'Data de finalització de la sessió'],
        ['Hora', 'Hora d’inici (fi − durada)'],
        ['Temps', 'Durada de la sessió'],
        ['Ritme', 'SPM mitjà de la sessió'],
        ['Vel. mitjana', 'Velocitat mitjana en km/h'],
        ['Distància', 'Distància total en metres'],
        ['Esborrar', 'Elimina aquella sessió'],
      ],
      empty: 'Estat buit',
      emptyBody:
        '«Encara no hi ha activitats. Prem Inici a la primera pantalla i després Aturar per enregistrar-ne una.»',
      export: 'Exportar activitats (Android)',
      exportBullets: [
        'La icona de baixada apareix si hi ha almenys una activitat',
        'Confirmeu «Exportar a Excel» i trieu on desar el CSV',
        'Columnes: Data, Hora, Temps, Ritme, Vel. mitjana (km/h), Distància',
      ],
      deleteAll: 'Esborrar tot',
      deleteAllBody:
        '«Esborrar tot» demana confirmació i elimina permanentment totes les sessions.',
      deleteOne: 'Esborrar una sessió',
      deleteOneBody: 'Toqueu Esborrar a una fila per eliminar-la de seguida.',
      config: '4. Pantalla de Configuració (pantalla 3)',
      configBody:
        'Títol: «Configuració de Rowing Metrics». Els canvis es desen de seguida. La sensibilitat s’aplica a la sessió següent (o després d’Aturar si es canvia a mitja sessió).',
      sensitivity: 'Perfil de braçada — Sensibilitat de detecció',
      sensitivityBody:
        'Control lliscant de 5 passos. Esquerra = més estricte (Baixa); dreta = més permissiu (Alta). Només Baixa, Mitjana i Alta tenen text; els passos intermedis són marques.',
      sensHeaders: ['Posició', 'Etiqueta', 'Comportament'],
      sensRows: [
        ['1 (esquerra)', 'Baixa', 'Més estricta — menys falsos positius; poden perdre’s braçades suaus'],
        ['2', '(marca)', 'Entre Baixa i Mitjana'],
        ['3 (centre)', 'Mitjana', 'Per defecte — equilibrada'],
        ['4', '(marca)', 'Entre Mitjana i Alta'],
        ['5 (dreta)', 'Alta', 'Més permissiva — detecció més fàcil; més falsos positius'],
      ],
      sensTip:
        'Aneu cap a Alta si falten braçades; cap a Baixa en aigua moguda o si el maneig del telèfon genera comptes falsos.',
      smoothing: 'Suavització de velocitat',
      smoothingBody:
        'Control 1–8 (per defecte 4). Quantes mostres GPS filtrades recents s’utilitzen per a la Velocitat en viu. No afecta la Velocitat mitjana (sempre distància ÷ temps).',
      language: 'Idioma',
      languageBody: 'Desplegable: anglès (per defecte), català, castellà, francès. Aplica a etiquetes i capçaleres CSV.',
      about: '5. Pantalla d’Informació (pantalla 4)',
      aboutBody: 'Mostra el títol «Rowing Metrics», el número de versió i el contacte gardenyes@gmail.com.',
      how: '6. Com es calculen les mètriques',
      howIntro: 'Significat dels números darrere de la interfície.',
      spm: 'Ritme (SPM en viu)',
      spmBullets: [
        'L’acceleròmetre detecta pics de braçada amb llindars adaptatius',
        'L’SPM s’actualitza aproximadament cada 2 segons',
        'Calen diverses braçades abans d’un SPM estable',
        'Torna cap a 0 si no hi ha braçada durant uns segons',
        'La mitjana de sessió = mitjana de mostres d’SPM mentre corre (no braçades ÷ temps)',
      ],
      speed: 'Velocitat (actual)',
      speedBullets: [
        'La velocitat GPS es filtra i es mitjana les últimes N mostres (N = suavització)',
        'Es mostra en km/h; es rebutgen fixes GPS de mala qualitat',
      ],
      avgSpeed: 'Velocitat mitjana',
      avgSpeedBody: 'Distància total de la sessió ÷ temps transcorregut.',
      distance: 'Distància',
      distanceBullets: [
        'A partir dels canvis de posició GPS',
        'Només es compten segments d’uns 4 metres o més',
      ],
      orientation: 'Canvis d’orientació',
      orientationBody:
        'Si canvia l’orientació del telèfon durant la sessió, la detecció de braçades es pausa uns ~1,2 s i després es reprend.',
      requirements: '7. Requisits i notes de plataforma',
      perms: 'Permisos (Android)',
      permsBullets: [
        'Ubicació (precisa o aproximada): per a velocitat i distància',
        'Sensors de moviment: per al ritme de braçada',
      ],
      platform: 'Diferències de plataforma',
      platHeaders: ['Funció', 'Android', 'iOS'],
      platRows: [
        ['Detecció de braçades', 'Completa', 'Pendent Core Motion'],
        ['Velocitat / distància GPS', 'Completa', 'Pendent Core Location'],
        ['Historial d’activitats', 'Completa', 'Completa'],
        ['Configuració / idioma', 'Completa', 'Completa'],
        ['Exportació CSV', 'Completa', 'Encara no'],
        ['Pantalla de bloqueig', 'Sí', 'Per defecte del sistema'],
      ],
      hardware: 'Maquinari',
      hardwareBullets: [
        'GPS per a velocitat i distància',
        'Acceleròmetre / giroscopi per al ritme',
      ],
      quick: '8. Referència ràpida',
      swipe: 'Ordre de les pantalles',
      swipeBody: 'Mètriques → Activitats → Configuració → Informació',
      defaults: 'Valors per defecte',
      defHeaders: ['Paràmetre', 'Per defecte'],
      defRows: [
        ['Sensibilitat de detecció', 'Mitjana (centre)'],
        ['Suavització de velocitat', '4'],
        ['Idioma', 'Anglès'],
      ],
      workflow: 'Flux habitual',
      workflowSteps: [
        '1. Obriu l’app (concedeix ubicació si us ho demana).',
        '2. Opcionalment aneu a Configuració per ajustar sensibilitat o idioma.',
        '3. A la pantalla principal, premeu Inici quan comenceu a remar.',
        '4. Superviseu ritme, velocitat i distància en viu.',
        '5. Premeu Aturar en acabar — la sessió es desa a Activitats.',
        '6. Reviseu, exporteu o esborreu sessions a Activitats.',
      ],
      footer: 'Guia d’usuari de Rowing Metrics — comportament actual de l’app.',
      contact: 'Contacte: gardenyes@gmail.com',
    },
  },

  fr: {
    file: 'Rowing-Metrics-User-Guide-fr.pdf',
    metaTitle: 'Rowing Metrics — Guide utilisateur',
    coverTitle: 'Rowing Metrics',
    coverSubtitle: 'Guide utilisateur — Écrans et options',
    coverIntro:
      'Suivi de sessions d’aviron multiplateforme pour Android et iOS.\nCe document décrit chaque écran, métrique et option de configuration de l’application.',
    sections: {
      overview: '1. Présentation',
      overviewBody:
        'Rowing Metrics suit les sessions d’aviron avec l’accéléromètre du téléphone pour la cadence (SPM) et le GPS pour la vitesse et la distance. La même interface tourne sur Android et iOS (UI Kotlin Multiplatform partagée).',
      keyCaps: 'Fonctionnalités principales :',
      caps: [
        'Cadence, vitesse, vitesse moyenne, distance et temps en direct',
        'Sensibilité de détection et lissage GPS configurables',
        'Enregistrement automatique des sessions dans l’historique local',
        'Export CSV de l’historique (compatible Excel) sur Android',
        'Langues : anglais, catalan, espagnol et français',
      ],
      navigation: 'Navigation',
      navigationBody:
        'L’app comporte quatre écrans dans un pager horizontal. Faites glisser vers la gauche ou la droite :',
      tableHeaders: ['#', 'Écran', 'Rôle'],
      screens: [
        ['1', 'Métriques', 'Session en direct — Démarrer / Arrêter'],
        ['2', 'Activités', 'Historique des sessions ; export / suppression'],
        ['3', 'Configuration', 'Sensibilité, lissage de vitesse, langue'],
        ['4', 'À propos', 'Nom de l’app, version et contact'],
      ],
      main: '2. Écran des métriques (écran 1)',
      mainBody:
        'Affichage principal sur l’eau. Métriques en direct pendant la session, valeurs finales après Arrêter. Mise en page portrait et paysage.',
      metricsTitle: 'Métriques affichées',
      metricHeaders: ['Champ', 'Unité / format', 'Description'],
      metrics: [
        ['Cadence', 'SPM (entier)', 'Coups par minute (accéléromètre). Plus grande valeur à l’écran.'],
        ['Vitesse', 'km/h (1 décimale)', 'Vitesse GPS actuelle, lissée selon le réglage.'],
        ['Vitesse moyenne', 'km/h (1 décimale)', 'Distance totale ÷ temps écoulé de la session.'],
        ['Distance', 'mètres (entier)', 'Distance GPS cumulée depuis Démarrer (segments ≥ 4 m).'],
        ['Temps', 'MM:SS ou HH:MM:SS', 'Durée de session. Heures à partir d’1 heure.'],
        ['Horloge', 'HH:MM', 'Heure locale de l’appareil (chaque seconde), à côté du Temps.'],
      ],
      startStop: 'Bouton Démarrer / Arrêter',
      startStopBody:
        'Démarrer vert lance la session. En cours, il devient rouge et affiche Arrêter.',
      startStopBullets: [
        'Démarrer : détection des coups, GPS et chronomètre',
        'Arrêter : termine la session et enregistre un résumé dans Activités',
        'Au premier Démarrer, Android demande la localisation si besoin',
      ],
      warning: 'Alerte de détection',
      warningBody:
        'Si aucun coup n’est détecté récemment pendant la session, la bande de cadence clignote entre rouge clair et noir. Ajustez la position du téléphone ou augmentez la sensibilité dans Configuration.',
      portrait: 'Disposition portrait',
      portraitBody:
        'De haut en bas : Cadence, Vitesse, Vitesse moyenne, Distance, Temps + Horloge, Démarrer/Arrêter.',
      landscape: 'Disposition paysage',
      landscapeBody:
        'Gauche : Cadence et Démarrer/Arrêter. Droite : Vitesse + moyenne, Distance, Temps + Horloge.',
      androidNotes: 'Comportement Android',
      androidBullets: [
        'L’écran reste allumé au premier plan',
        'Peut s’afficher au-dessus de l’écran de verrouillage pendant l’aviron',
        'Accéléromètre / gyroscope requis pour la cadence',
      ],
      activities: '3. Écran Activités (écran 2)',
      activitiesBody:
        'Titre : « Activités ». Tableau défilant des sessions enregistrées après Arrêter.',
      columnsTitle: 'Colonnes du tableau',
      colHeaders: ['Colonne', 'Contenu'],
      columns: [
        ['Date', 'Date de fin de session'],
        ['Heure', 'Heure de début (fin − durée)'],
        ['Temps', 'Durée de la session'],
        ['Cadence', 'SPM moyen de la session'],
        ['Vit. moyenne', 'Vitesse moyenne en km/h'],
        ['Distance', 'Distance totale en mètres'],
        ['Supprimer', 'Supprime cette session'],
      ],
      empty: 'État vide',
      emptyBody:
        '« Aucune activité pour l’instant. Appuyez sur Démarrer sur le premier écran, puis Arrêter pour en enregistrer une. »',
      export: 'Exporter les activités (Android)',
      exportBullets: [
        'L’icône de téléchargement apparaît s’il y a au moins une activité',
        'Confirmez « Exporter vers Excel », puis choisissez où enregistrer le CSV',
        'Colonnes : Date, Heure, Temps, Cadence, Vit. moyenne (km/h), Distance',
      ],
      deleteAll: 'Tout supprimer',
      deleteAllBody:
        '« Tout supprimer » demande confirmation et efface définitivement toutes les sessions.',
      deleteOne: 'Supprimer une session',
      deleteOneBody: 'Appuyez sur Supprimer sur une ligne pour l’enlever immédiatement.',
      config: '4. Écran Configuration (écran 3)',
      configBody:
        'Titre : « Configuration de Rowing Metrics ». Les changements sont enregistrés aussitôt. La sensibilité s’applique à la session suivante (ou après Arrêter si modifiée en cours).',
      sensitivity: 'Profil de coup — Sensibilité de détection',
      sensitivityBody:
        'Curseur à 5 positions. Gauche = plus strict (Faible) ; droite = plus permissif (Élevée). Seuls Faible, Moyenne et Élevée ont un libellé ; les étapes intermédiaires sont des marques.',
      sensHeaders: ['Position', 'Libellé', 'Comportement'],
      sensRows: [
        ['1 (gauche)', 'Faible', 'Plus strict — moins de faux coups ; coups souples parfois manqués'],
        ['2', '(marque)', 'Entre Faible et Moyenne'],
        ['3 (centre)', 'Moyenne', 'Par défaut — équilibré'],
        ['4', '(marque)', 'Entre Moyenne et Élevée'],
        ['5 (droite)', 'Élevée', 'Plus permissif — détection plus facile ; plus de faux positifs'],
      ],
      sensTip:
        'Vers Élevée si des coups manquent ; vers Faible par mer agitée ou si la manipulation du téléphone crée de faux comptages.',
      smoothing: 'Lissage de la vitesse',
      smoothingBody:
        'Curseur 1–8 (défaut 4). Nombre d’échantillons GPS filtrés récents moyennés pour la Vitesse en direct. N’affecte pas la Vitesse moyenne (toujours distance ÷ temps).',
      language: 'Langue',
      languageBody: 'Liste : anglais (défaut), catalan, espagnol, français. S’applique aux libellés et en-têtes CSV.',
      about: '5. Écran À propos (écran 4)',
      aboutBody: 'Affiche le titre « Rowing Metrics », le numéro de version et le contact gardenyes@gmail.com.',
      how: '6. Calcul des métriques',
      howIntro: 'Signification des valeurs derrière l’interface.',
      spm: 'Cadence (SPM en direct)',
      spmBullets: [
        'L’accéléromètre détecte les pics de coups avec des seuils adaptatifs',
        'Le SPM se met à jour environ toutes les 2 secondes',
        'Plusieurs coups sont nécessaires avant un SPM stable',
        'Revient vers 0 sans coup pendant quelques secondes',
        'Moyenne de session = moyenne des échantillons SPM en cours (pas coups ÷ temps)',
      ],
      speed: 'Vitesse (actuelle)',
      speedBullets: [
        'La vitesse GPS est filtrée puis moyennée sur les N derniers échantillons (N = lissage)',
        'Affichée en km/h ; les fixes GPS de mauvaise qualité sont rejetés',
      ],
      avgSpeed: 'Vitesse moyenne',
      avgSpeedBody: 'Distance totale de la session ÷ temps écoulé.',
      distance: 'Distance',
      distanceBullets: [
        'À partir des changements de position GPS',
        'Seuls les segments d’environ 4 mètres ou plus sont comptés',
      ],
      orientation: 'Changements d’orientation',
      orientationBody:
        'Si l’orientation du téléphone change pendant la session, la détection se met en pause ~1,2 s puis reprend.',
      requirements: '7. Exigences et notes plateforme',
      perms: 'Autorisations (Android)',
      permsBullets: [
        'Localisation (précise ou approximative) : vitesse et distance',
        'Capteurs de mouvement : cadence',
      ],
      platform: 'Différences de plateforme',
      platHeaders: ['Fonction', 'Android', 'iOS'],
      platRows: [
        ['Détection des coups', 'Complète', 'En attente Core Motion'],
        ['Vitesse / distance GPS', 'Complète', 'En attente Core Location'],
        ['Historique d’activités', 'Complète', 'Complète'],
        ['Configuration / langue', 'Complète', 'Complète'],
        ['Export CSV', 'Complète', 'Pas encore'],
        ['Écran de verrouillage', 'Oui', 'Par défaut système'],
      ],
      hardware: 'Matériel',
      hardwareBullets: [
        'GPS pour vitesse et distance',
        'Accéléromètre / gyroscope pour la cadence',
      ],
      quick: '8. Référence rapide',
      swipe: 'Ordre des écrans',
      swipeBody: 'Métriques → Activités → Configuration → À propos',
      defaults: 'Réglages par défaut',
      defHeaders: ['Réglage', 'Défaut'],
      defRows: [
        ['Sensibilité de détection', 'Moyenne (centre)'],
        ['Lissage de vitesse', '4'],
        ['Langue', 'Anglais'],
      ],
      workflow: 'Parcours typique',
      workflowSteps: [
        '1. Ouvrez l’app (accordez la localisation si demandé).',
        '2. Optionnellement ouvrez Configuration pour sensibilité ou langue.',
        '3. Sur l’écran principal, appuyez sur Démarrer pour ramer.',
        '4. Surveillez cadence, vitesse et distance en direct.',
        '5. Appuyez sur Arrêter — la session est enregistrée dans Activités.',
        '6. Consultez, exportez ou supprimez les sessions dans Activités.',
      ],
      footer: 'Guide utilisateur Rowing Metrics — comportement actuel de l’app.',
      contact: 'Contact : gardenyes@gmail.com',
    },
  },
};

function writeGuide(localeKey, guide) {
  const outputPath = path.join(__dirname, guide.file);
  const doc = new PDFDocument({
    size: 'A4',
    margins: { top: 56, bottom: 56, left: 56, right: 56 },
    info: {
      Title: guide.metaTitle,
      Author: 'Rowing Metrics',
      Subject: 'Screens and options reference',
      Language: localeKey,
    },
  });

  const streams = [fs.createWriteStream(outputPath)];
  if (guide.alsoAsDefault) {
    streams.push(fs.createWriteStream(path.join(__dirname, 'Rowing-Metrics-User-Guide.pdf')));
  }
  streams.forEach((s) => doc.pipe(s));

  const s = guide.sections;

  function title(text) {
    doc.moveDown(0.5);
    doc.font('Helvetica-Bold').fontSize(22).fillColor(colors.title).text(text);
    doc.moveDown(0.3);
  }
  function heading(text) {
    doc.moveDown(0.8);
    doc.font('Helvetica-Bold').fontSize(14).fillColor(colors.heading).text(text);
    doc.moveDown(0.25);
  }
  function subheading(text) {
    doc.moveDown(0.4);
    doc.font('Helvetica-Bold').fontSize(11).fillColor(colors.subheading).text(text);
    doc.moveDown(0.15);
  }
  function body(text) {
    doc.font('Helvetica').fontSize(10).fillColor(colors.body).text(text, { lineGap: 2 });
  }
  function bullet(text) {
    doc.font('Helvetica').fontSize(10).fillColor(colors.body).text('•  ' + text, {
      indent: 16,
      lineGap: 2,
    });
  }
  function tableRow(cols, widths, bold = false) {
    const y = doc.y;
    let x = doc.page.margins.left;
    const startY = y;
    let maxH = 0;
    doc.font(bold ? 'Helvetica-Bold' : 'Helvetica').fontSize(9).fillColor(colors.body);
    cols.forEach((col, i) => {
      const h = doc.heightOfString(col, { width: widths[i], lineGap: 1 });
      maxH = Math.max(maxH, h);
    });
    if (doc.y + maxH > doc.page.height - doc.page.margins.bottom) {
      doc.addPage();
    }
    const rowY = doc.y;
    cols.forEach((col, i) => {
      doc.text(col, x, rowY, { width: widths[i], lineGap: 1 });
      x += widths[i];
    });
    doc.y = rowY + maxH + 4;
  }
  function hr() {
    doc.moveDown(0.3);
    doc
      .strokeColor('#cccccc')
      .lineWidth(0.5)
      .moveTo(doc.page.margins.left, doc.y)
      .lineTo(doc.page.width - doc.page.margins.right, doc.y)
      .stroke();
    doc.moveDown(0.4);
  }

  // Cover
  doc.font('Helvetica-Bold').fontSize(28).fillColor(colors.title).text(guide.coverTitle, {
    align: 'center',
  });
  doc.moveDown(0.3);
  doc.font('Helvetica').fontSize(14).fillColor(colors.muted).text(guide.coverSubtitle, {
    align: 'center',
  });
  doc.moveDown(1);
  doc.font('Helvetica').fontSize(10).fillColor(colors.body).text(guide.coverIntro, {
    align: 'center',
    lineGap: 4,
  });
  doc.moveDown(1.5);
  hr();

  title(s.overview);
  body(s.overviewBody);
  doc.moveDown(0.4);
  body(s.keyCaps);
  s.caps.forEach(bullet);

  heading(s.navigation);
  body(s.navigationBody);
  doc.moveDown(0.2);
  tableRow(s.tableHeaders, [30, 120, 310], true);
  s.screens.forEach((row) => tableRow(row, [30, 120, 310]));

  doc.addPage();
  title(s.main);
  body(s.mainBody);

  subheading(s.metricsTitle);
  tableRow(s.metricHeaders, [90, 90, 300], true);
  s.metrics.forEach((row) => tableRow(row, [90, 90, 300]));

  subheading(s.startStop);
  body(s.startStopBody);
  s.startStopBullets.forEach(bullet);

  subheading(s.warning);
  body(s.warningBody);

  subheading(s.portrait);
  body(s.portraitBody);
  subheading(s.landscape);
  body(s.landscapeBody);

  subheading(s.androidNotes);
  s.androidBullets.forEach(bullet);

  doc.addPage();
  title(s.activities);
  body(s.activitiesBody);

  subheading(s.columnsTitle);
  tableRow(s.colHeaders, [100, 380], true);
  s.columns.forEach((row) => tableRow(row, [100, 380]));

  subheading(s.empty);
  body(s.emptyBody);
  subheading(s.export);
  s.exportBullets.forEach(bullet);
  subheading(s.deleteAll);
  body(s.deleteAllBody);
  subheading(s.deleteOne);
  body(s.deleteOneBody);

  heading(s.config);
  body(s.configBody);

  subheading(s.sensitivity);
  body(s.sensitivityBody);
  doc.moveDown(0.2);
  tableRow(s.sensHeaders, [70, 70, 320], true);
  s.sensRows.forEach((row) => tableRow(row, [70, 70, 320]));
  doc.moveDown(0.3);
  body(s.sensTip);

  subheading(s.smoothing);
  body(s.smoothingBody);
  subheading(s.language);
  body(s.languageBody);

  heading(s.about);
  body(s.aboutBody);

  doc.addPage();
  title(s.how);
  body(s.howIntro);

  subheading(s.spm);
  s.spmBullets.forEach(bullet);
  subheading(s.speed);
  s.speedBullets.forEach(bullet);
  subheading(s.avgSpeed);
  body(s.avgSpeedBody);
  subheading(s.distance);
  s.distanceBullets.forEach(bullet);
  subheading(s.orientation);
  body(s.orientationBody);

  heading(s.requirements);
  subheading(s.perms);
  s.permsBullets.forEach(bullet);
  subheading(s.platform);
  tableRow(s.platHeaders, [130, 160, 180], true);
  s.platRows.forEach((row) => tableRow(row, [130, 160, 180]));
  subheading(s.hardware);
  s.hardwareBullets.forEach(bullet);

  doc.addPage();
  title(s.quick);
  subheading(s.swipe);
  body(s.swipeBody);
  subheading(s.defaults);
  tableRow(s.defHeaders, [200, 280], true);
  s.defRows.forEach((row) => tableRow(row, [200, 280]));
  subheading(s.workflow);
  s.workflowSteps.forEach((step) => body(step));

  doc.moveDown(2);
  hr();
  doc.font('Helvetica').fontSize(9).fillColor(colors.muted).text(s.footer, { align: 'center' });
  doc.text(s.contact, { align: 'center' });

  doc.end();
  const extras = guide.alsoAsDefault ? ' (+ Rowing-Metrics-User-Guide.pdf)' : '';
  console.log('Wrote', guide.file + extras);
}

for (const [locale, guide] of Object.entries(guides)) {
  writeGuide(locale, guide);
}
