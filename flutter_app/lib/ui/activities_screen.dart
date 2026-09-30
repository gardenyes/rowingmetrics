import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../core/activity_record_format.dart';
import 'app.dart';

class ActivitiesScreen extends StatelessWidget {
  const ActivitiesScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final model = context.watch<AppModel>();
    final rows = model.activities;
    return SafeArea(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(20, 20, 20, 8),
            child: Row(
              children: [
                Text(
                  'Activities',
                  style: Theme.of(context).textTheme.headlineMedium,
                ),
                const Spacer(),
                if (rows.isNotEmpty)
                  TextButton(
                    onPressed: () async {
                      final ok = await showDialog<bool>(
                        context: context,
                        builder: (ctx) => AlertDialog(
                          title: const Text('Delete all?'),
                          content: const Text(
                            'This removes every saved activity.',
                          ),
                          actions: [
                            TextButton(
                              onPressed: () => Navigator.pop(ctx, false),
                              child: const Text('Cancel'),
                            ),
                            TextButton(
                              onPressed: () => Navigator.pop(ctx, true),
                              child: const Text('Delete'),
                            ),
                          ],
                        ),
                      );
                      if (ok == true) await model.deleteAllActivities();
                    },
                    child: const Text('Delete all'),
                  ),
              ],
            ),
          ),
          const Padding(
            padding: EdgeInsets.symmetric(horizontal: 12),
            child: _HeaderRow(),
          ),
          const Divider(height: 1),
          Expanded(
            child: rows.isEmpty
                ? const Center(child: Text('No activities yet'))
                : ListView.separated(
                    itemCount: rows.length,
                    separatorBuilder: (_, _) =>
                        const Divider(height: 1, indent: 12),
                    itemBuilder: (context, i) {
                      final e = rows[i];
                      return Padding(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 12,
                          vertical: 10,
                        ),
                        child: Row(
                          children: [
                            _cell(formatActivityTableDate(e.endedAtEpochMs), 88),
                            _cell(
                              formatActivityTableStartHour(
                                e.endedAtEpochMs,
                                e.durationMs,
                              ),
                              52,
                            ),
                            _cell(formatActivityElapsedTable(e.durationMs), 64),
                            _cell(formatOneDecimalTable(e.avgStrokeRate), 48),
                            _cell(
                              '${formatOneDecimalTable(e.avgSpeedKmh)} km/h',
                              72,
                            ),
                            Expanded(
                              child: Text(
                                formatDistanceTable(e.distanceMeters),
                                style: const TextStyle(fontSize: 12),
                              ),
                            ),
                            GestureDetector(
                              onTap: () => model.deleteActivity(e.id),
                              child: const Text(
                                'Delete',
                                style: TextStyle(
                                  color: Color(0xFFFF8A80),
                                  fontSize: 12,
                                ),
                              ),
                            ),
                          ],
                        ),
                      );
                    },
                  ),
          ),
        ],
      ),
    );
  }

  Widget _cell(String text, double width) => SizedBox(
        width: width,
        child: Text(text, style: const TextStyle(fontSize: 12)),
      );
}

class _HeaderRow extends StatelessWidget {
  const _HeaderRow();

  @override
  Widget build(BuildContext context) {
    TextStyle style = const TextStyle(
      fontSize: 12,
      fontWeight: FontWeight.w600,
      color: Colors.white70,
    );
    Widget h(String t, double w) => SizedBox(width: w, child: Text(t, style: style));
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 10),
      child: Row(
        children: [
          h('Date', 88),
          h('Hour', 52),
          h('Time', 64),
          h('SPM', 48),
          h('Speed', 72),
          Expanded(child: Text('Dist', style: style)),
          const SizedBox(width: 48),
        ],
      ),
    );
  }
}
