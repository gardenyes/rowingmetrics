import 'dart:async';

import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';
import 'package:sqflite/sqflite.dart';

import '../core/completed_activity.dart';
import 'platform_services.dart';

class SqfliteActivityRepository implements ActivityRepository {
  SqfliteActivityRepository(this._db);

  final Database _db;
  final _controller = StreamController<List<CompletedActivity>>.broadcast();

  static Future<SqfliteActivityRepository> open() async {
    final dir = await getApplicationDocumentsDirectory();
    final dbPath = p.join(dir.path, 'rowing_metrics.db');
    final db = await openDatabase(
      dbPath,
      version: 1,
      onCreate: (db, version) async {
        await db.execute('''
CREATE TABLE completed_activities (
  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  endedAtEpochMs INTEGER NOT NULL,
  durationMs INTEGER NOT NULL,
  avgStrokeRate REAL NOT NULL,
  avgSpeedKmh REAL NOT NULL,
  distanceMeters REAL NOT NULL
)
''');
      },
    );
    final repo = SqfliteActivityRepository(db);
    await repo._emit();
    return repo;
  }

  Future<List<CompletedActivity>> _queryAll() async {
    final rows = await _db.query(
      'completed_activities',
      orderBy: 'endedAtEpochMs DESC',
    );
    return rows.map(CompletedActivity.fromMap).toList();
  }

  Future<void> _emit() async {
    if (!_controller.isClosed) {
      _controller.add(await _queryAll());
    }
  }

  @override
  Stream<List<CompletedActivity>> observeAll() async* {
    yield await _queryAll();
    yield* _controller.stream;
  }

  @override
  Future<void> insert(CompletedActivity activity) async {
    await _db.insert('completed_activities', activity.toMap()
      ..remove('id'));
    await _emit();
  }

  @override
  Future<void> deleteById(int id) async {
    await _db.delete('completed_activities', where: 'id = ?', whereArgs: [id]);
    await _emit();
  }

  @override
  Future<void> deleteAll() async {
    await _db.delete('completed_activities');
    await _emit();
  }

  Future<void> dispose() async {
    await _controller.close();
    await _db.close();
  }
}
