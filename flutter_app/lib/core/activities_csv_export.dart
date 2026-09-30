import '../core/activity_record_format.dart';
import '../core/completed_activity.dart';

class CsvColumnHeaders {
  const CsvColumnHeaders({
    required this.date,
    required this.hour,
    required this.time,
    required this.strokeRate,
    required this.avgSpeed,
    required this.distance,
  });

  final String date;
  final String hour;
  final String time;
  final String strokeRate;
  final String avgSpeed;
  final String distance;
}

class ActivitiesCsvExport {
  static String buildCsv(
    List<CompletedActivity> activities,
    CsvColumnHeaders headers,
  ) {
    final sb = StringBuffer()
      ..write('\uFEFF')
      ..writeln(
        [
          headers.date,
          headers.hour,
          headers.time,
          headers.strokeRate,
          headers.avgSpeed,
          headers.distance,
        ].join(','),
      );
    for (final entity in activities) {
      sb
        ..write(_csvField(formatActivityTableDate(entity.endedAtEpochMs)))
        ..write(',')
        ..write(
          _csvField(
            formatActivityTableStartHour(
              entity.endedAtEpochMs,
              entity.durationMs,
            ),
          ),
        )
        ..write(',')
        ..write(_csvField(formatActivityElapsedTable(entity.durationMs)))
        ..write(',')
        ..write(_csvField(formatOneDecimalTable(entity.avgStrokeRate)))
        ..write(',')
        ..write(_csvField(formatOneDecimalTable(entity.avgSpeedKmh)))
        ..write(',')
        ..writeln(_csvField(formatDistanceTable(entity.distanceMeters)));
    }
    return sb.toString();
  }

  static String defaultFileName([DateTime? now]) {
    final n = now ?? DateTime.now();
    final stamp =
        '${n.year.toString().padLeft(4, '0')}'
        '${n.month.toString().padLeft(2, '0')}'
        '${n.day.toString().padLeft(2, '0')}_'
        '${n.hour.toString().padLeft(2, '0')}'
        '${n.minute.toString().padLeft(2, '0')}'
        '${n.second.toString().padLeft(2, '0')}';
    return 'rowing_activities_$stamp.csv';
  }

  static String _csvField(String value) {
    if (value.contains(',') ||
        value.contains('"') ||
        value.contains('\n') ||
        value.contains('\r')) {
      return '"${value.replaceAll('"', '""')}"';
    }
    return value;
  }
}
