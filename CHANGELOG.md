# Changelog

All notable changes to Rowing Metrics are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1] - 2026-05-19

### Activity history

- **Export all activities to CSV** (Excel-compatible): tap the download icon on the Activities screen, confirm, then choose where to save the file. Exports date, time, duration, stroke rate, average speed, and distance for every stored session.


## [1.0] - 2026-05-19

Initial release.

### Live session metrics

- Start / Stop rowing sessions from the main screen.
- Live display of stroke rate (accelerometer-based), current speed, average speed, distance, session elapsed time, and local clock.
- Portrait and landscape layouts optimized for on-water use.
- Visual alert when the session is running but no stroke is detected for a while.
- Works over the lock screen with screen wake (for use while rowing).

### Configuration

- Stroke detection sensitivity: High, Medium, or Low.
- GPS speed smoothing: average the last 1–8 filtered GPS samples for the live speed readout.

### Activity history

- Swipe to the **Activities** screen to view completed sessions (date, time, duration, stroke rate, average speed, distance).
- Delete individual sessions or delete all (with confirmation).

### Requirements

- GPS location permission for speed and distance.
- Device gyroscope required.

### About

- App info screen with version number and contact: [gardenyes@gmail.com](mailto:gardenyes@gmail.com).
