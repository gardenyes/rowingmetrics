import PDFDocument from 'pdfkit';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const outputPath = path.join(__dirname, 'Rowing-Metrics-User-Guide.pdf');

const doc = new PDFDocument({
  size: 'A4',
  margins: { top: 56, bottom: 56, left: 56, right: 56 },
  info: {
    Title: 'Rowing Metrics — User Guide',
    Author: 'Rowing Metrics',
    Subject: 'Screens and options reference',
  },
});

doc.pipe(fs.createWriteStream(outputPath));

const colors = {
  title: '#1a1a1a',
  heading: '#2e7d32',
  subheading: '#333333',
  body: '#444444',
  muted: '#666666',
  accent: '#c62828',
};

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

function body(text, opts = {}) {
  doc.font('Helvetica').fontSize(10).fillColor(colors.body).text(text, {
    align: opts.align || 'left',
    continued: opts.continued || false,
    indent: opts.indent || 0,
    lineGap: 2,
  });
}

function bullet(text, indent = 16) {
  doc.font('Helvetica').fontSize(10).fillColor(colors.body);
  doc.text('•  ' + text, { indent, lineGap: 2 });
}

function tableRow(cols, widths, bold = false) {
  const y = doc.y;
  let x = doc.page.margins.left;
  doc.font(bold ? 'Helvetica-Bold' : 'Helvetica').fontSize(9).fillColor(colors.body);
  cols.forEach((col, i) => {
    doc.text(col, x, y, { width: widths[i], lineGap: 1 });
    x += widths[i];
  });
  doc.moveDown(0.3);
}

function ensureSpace(h = 80) {
  if (doc.y + h > doc.page.height - doc.page.margins.bottom) {
    doc.addPage();
  }
}

function hr() {
  doc.moveDown(0.3);
  doc.strokeColor('#cccccc').lineWidth(0.5)
    .moveTo(doc.page.margins.left, doc.y)
    .lineTo(doc.page.width - doc.page.margins.right, doc.y)
    .stroke();
  doc.moveDown(0.4);
}

// --- Cover ---
doc.font('Helvetica-Bold').fontSize(28).fillColor(colors.title)
  .text('Rowing Metrics', { align: 'center' });
doc.moveDown(0.3);
doc.font('Helvetica').fontSize(14).fillColor(colors.muted)
  .text('User Guide — Screens & Options', { align: 'center' });
doc.moveDown(1);
doc.font('Helvetica').fontSize(10).fillColor(colors.body)
  .text('Cross-platform rowing session tracker for Android and iOS.\nThis document describes every screen, metric, and configuration option in the app.', {
    align: 'center',
    lineGap: 4,
  });
doc.moveDown(1.5);
hr();

// --- Overview ---
title('1. Overview');
body('Rowing Metrics is a rowing session tracker that uses your phone\'s accelerometer to detect stroke rate (SPM) and GPS to measure speed and distance. The app is built with Kotlin Multiplatform and shares the same interface on Android and iOS.');
doc.moveDown(0.4);
body('Key capabilities:');
bullet('Live stroke rate, speed, average speed, distance, and elapsed time during a session');
bullet('Configurable stroke detection sensitivity and GPS speed smoothing');
bullet('Automatic saving of completed sessions to a local history');
bullet('Export activity history to CSV (Excel-compatible) on Android');
bullet('Multi-language UI: English, Catalan, Spanish, and French');

heading('Navigation');
body('The app has four screens arranged in a horizontal pager. Swipe left or right to move between them:');
doc.moveDown(0.2);
tableRow(['#', 'Screen', 'Purpose'], [30, 140, 300], true);
tableRow(['1', 'Main metrics', 'Live session display — Start/Stop rowing'], [30, 140, 300]);
tableRow(['2', 'Configuration', 'Stroke sensitivity, speed smoothing, language'], [30, 140, 300]);
tableRow(['3', 'Activities', 'History of completed sessions'], [30, 140, 300]);
tableRow(['4', 'About', 'App name, version, and contact information'], [30, 140, 300]);

// --- Screen 1 ---
doc.addPage();
title('2. Main Metrics Screen (Screen 1)');
body('This is the primary on-water display. It shows live metrics while a session is running, and the final values after you press Stop. The layout adapts automatically to portrait and landscape orientation.');

subheading('Metrics displayed');
tableRow(['Field', 'Unit / format', 'Description'], [90, 90, 300], true);
tableRow([
  'Stroke rate',
  'SPM (integer)',
  'Strokes per minute from accelerometer detection. Largest value on screen.',
], [90, 90, 300]);
tableRow([
  'Speed',
  'km/h (1 decimal)',
  'Current speed from filtered GPS. Smoothed using your Speed smoothing setting.',
], [90, 90, 300]);
tableRow([
  'Average speed',
  'km/h (1 decimal)',
  'Total distance ÷ elapsed time for the current session.',
], [90, 90, 300]);
tableRow([
  'Distance',
  'meters (integer)',
  'Cumulative GPS distance since Start. Only counts movement ≥ 4 m per segment.',
], [90, 90, 300]);
tableRow([
  'Time',
  'MM:SS or HH:MM:SS',
  'Elapsed session time from Start to Stop. Shows hours once ≥ 1 hour.',
], [90, 90, 300]);
tableRow([
  'Clock',
  'HH:MM',
  'Device local time (updates every second). Shown beside Time.',
], [90, 90, 300]);

subheading('Start / Stop button');
body('A large green Start button begins a new session. While running, it turns red and shows Stop.');
bullet('Start: begins accelerometer stroke detection, GPS tracking, and the session timer');
bullet('Stop: ends the session, saves a summary to Activities history, and resets live metrics');
bullet('On first Start, Android requests location permission (Fine and Coarse) if not already granted');

subheading('Stroke detection warning');
body('While a session is running, if no stroke has been detected recently, the stroke rate band background blinks between light red and black. This alerts you that the app is not registering strokes — you may need to adjust phone placement or increase detection sensitivity in Configuration.');

subheading('Portrait layout');
body('Top to bottom: Stroke rate (largest band), then Speed, Average speed, Distance, Time + Clock side by side, and Start/Stop at the bottom.');

subheading('Landscape layout');
body('Left column: Stroke rate and Start/Stop button. Right column: Speed and Average speed on one row, then Distance, then Time + Clock. Text is scaled up for readability on wider screens.');

subheading('Android-specific behavior');
bullet('Screen stays on while the app is in the foreground (FLAG_KEEP_SCREEN_ON)');
bullet('App can appear over the lock screen and turn the screen on — useful while rowing');
bullet('Requires device gyroscope/accelerometer for stroke detection');

// --- Screen 2 ---
doc.addPage();
title('3. Configuration Screen (Screen 2)');
body('Title: "Rowing Metrics Configuration". Settings are stored immediately when changed and apply to the next session (stroke sensitivity changes mid-session take effect after Stop).');

subheading('Stroke profile — Detection sensitivity');
body('A 5-step horizontal slider controls how aggressively the accelerometer accepts stroke events. Left = strictest; right = most permissive. Only three positions show text labels; two intermediate steps are unlabeled tick marks.');
doc.moveDown(0.2);
tableRow(['Position', 'Label', 'Behavior'], [70, 70, 320], true);
tableRow(['1 (left)', 'Low', 'Strictest — fewest false strokes; soft strokes may be missed'], [70, 70, 320]);
tableRow(['2', '(tick only)', 'Between Low and Medium — constructor defaults'], [70, 70, 320]);
tableRow(['3 (center)', 'Medium', 'Default — balanced blend between permissive and strict'], [70, 70, 320]);
tableRow(['4', '(tick only)', 'Between Medium and High — former "High" preset'], [70, 70, 320]);
tableRow(['5 (right)', 'High', 'Most permissive — easiest stroke acceptance; higher false-positive risk'], [70, 70, 320]);
doc.moveDown(0.3);
body('Move the slider toward High (right) if strokes are not being detected. Move toward Low (left) in rough water or when handling the phone causes spurious counts. Medium works well for most rowers.');

subheading('Speed smoothing');
body('Slider range: 1 to 8 (default 4). Controls how many recent filtered GPS speed samples are averaged for the live Speed readout.');
bullet('1 = most responsive — uses only the latest filtered sample');
bullet('8 = smoothest — averages the last 8 filtered samples');
body('Higher values reduce GPS jitter but add lag to speed changes. The setting does not affect Average speed (which is always distance ÷ time).');

subheading('Language');
body('Dropdown to select the UI language. Available options:');
bullet('English (default)');
bullet('Catalan');
bullet('Spanish');
bullet('French');
body('All labels, buttons, dialog text, and CSV export column headers use the selected language.');

// --- Screen 3 ---
doc.addPage();
title('4. Activities Screen (Screen 3)');
body('Title: "Activities". Shows a scrollable table of all completed rowing sessions saved on the device. Sessions are created automatically when you press Stop on the main screen.');

subheading('Table columns');
tableRow(['Column', 'Content'], [100, 380], true);
tableRow(['Date', 'Date the session ended'], [100, 380]);
tableRow(['Hour', 'Start time of the session (derived from end time − duration)'], [100, 380]);
tableRow(['Time', 'Session duration (MM:SS or HH:MM:SS)'], [100, 380]);
tableRow(['Stroke rate', 'Average SPM for the session'], [100, 380]);
tableRow(['Avg. speed', 'Average speed in km/h'], [100, 380]);
tableRow(['Distance', 'Total distance in meters'], [100, 380]);
tableRow(['Delete', 'Tap to remove that single session'], [100, 380]);

subheading('Empty state');
body('If no sessions exist yet, the table shows: "No activities yet. Press Start on the first screen, then Stop to record one."');

subheading('Export activities (Android)');
body('When at least one activity exists, a download icon appears in the top-right corner.');
bullet('Tap the icon → confirm "Export to Excel" dialog');
bullet('Choose a save location; a CSV file is written with all sessions');
bullet('Open the CSV in Excel, Google Sheets, or any spreadsheet app');
body('Exported columns: Date, Hour, Time, Stroke rate, Avg. speed (km/h), Distance.');

subheading('Delete all');
body('A "Delete all" link appears next to the export icon when activities exist. Tapping it shows a confirmation dialog. This permanently removes every row — it cannot be undone.');

subheading('Delete individual session');
body('Tap "Delete" on any row to remove that session immediately (no confirmation dialog).');

// --- Screen 4 ---
heading('5. About Screen (Screen 4)');
body('The fourth screen (swipe left from Activities) shows a card with:');
bullet('App title: "Rowing Metrics"');
bullet('Version number (e.g. v2 — matches the installed app version)');
bullet('Contact email: gardenyes@gmail.com');

// --- Technical reference ---
doc.addPage();
title('6. How Metrics Are Calculated');
body('This section explains what the numbers mean behind the UI.');

subheading('Stroke rate (live SPM)');
bullet('Accelerometer samples at ~40 Hz detect stroke peaks using adaptive thresholds');
bullet('SPM is recalculated approximately every 2 seconds from recent stroke intervals');
bullet('Requires at least 8 detected strokes before a stable SPM is shown');
bullet('SPM returns to 0 if no stroke is detected for ~3 seconds');
bullet('Session average SPM = mean of SPM readings sampled every ~2 s while running (not total strokes ÷ time)');

subheading('Speed (current)');
bullet('Raw GPS speed (m/s) passes through a Kalman filter, then a FIFO buffer (max 8 samples)');
bullet('The Speed smoothing setting (1–8) controls how many of the latest filtered samples are averaged');
bullet('Result is converted to km/h (× 3.6) for display');
bullet('GPS fixes with accuracy > 50 m are rejected; speed shows 0 until GPS is ready');

subheading('Average speed');
body('Computed as total session distance (km) ÷ elapsed time (hours). Not an average of instantaneous speed readings.');

subheading('Distance');
bullet('Accumulated from GPS position changes using haversine distance');
bullet('Only segments of ≥ 4 meters are counted (filters GPS noise when stationary)');
bullet('Large GPS jumps (> 200 m with poor accuracy) are ignored');

subheading('Orientation changes');
body('If the phone orientation changes during a session, stroke detection pauses for ~1.2 seconds to stabilize, then resumes automatically.');

// --- Requirements ---
heading('7. Requirements & Platform Notes');

subheading('Permissions (Android)');
bullet('Location (Fine or Coarse): required for speed and distance during a session');
bullet('Motion sensors: required for stroke detection (no separate permission on Android)');

subheading('Platform differences');
tableRow(['Feature', 'Android', 'iOS'], [120, 180, 180], true);
tableRow(['Stroke detection', 'Full support', 'Stubbed — pending Core Motion bindings'], [120, 180, 180]);
tableRow(['GPS speed/distance', 'Full support', 'Stubbed — pending Core Location bindings'], [120, 180, 180]);
tableRow(['Activities history', 'Full support', 'Full support (SQLDelight)'], [120, 180, 180]);
tableRow(['Configuration / language', 'Full support', 'Full support'], [120, 180, 180]);
tableRow(['CSV export', 'Full support', 'Not yet implemented'], [120, 180, 180]);
tableRow(['Lock screen display', 'Yes', 'Platform default'], [120, 180, 180]);

subheading('Hardware');
bullet('GPS-capable device for speed and distance');
bullet('Accelerometer/gyroscope for stroke rate detection');

// --- Quick reference ---
doc.addPage();
title('8. Quick Reference');

subheading('Screen swipe order');
body('Main metrics → Configuration → Activities → About → (wraps) Main metrics');

subheading('Default settings');
tableRow(['Setting', 'Default'], [200, 280], true);
tableRow(['Detection sensitivity', 'Medium (center position)'], [200, 280]);
tableRow(['Speed smoothing', '4'], [200, 280]);
tableRow(['Language', 'English'], [200, 280]);

subheading('Typical workflow');
body('1. Open the app (grant location permission when prompted).');
body('2. Optionally swipe to Configuration and adjust sensitivity or language.');
body('3. On the main screen, press Start when you begin rowing.');
body('4. Monitor stroke rate, speed, and distance live.');
body('5. Press Stop when finished — the session is saved to Activities.');
body('6. Swipe to Activities to review, export, or delete past sessions.');

doc.moveDown(2);
hr();
doc.font('Helvetica').fontSize(9).fillColor(colors.muted)
  .text('Rowing Metrics User Guide — generated from app source code.', { align: 'center' });
doc.text('Contact: gardenyes@gmail.com', { align: 'center' });

doc.end();

console.log('PDF written to:', outputPath);
