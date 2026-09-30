import 'package:flutter/material.dart';
import 'package:rowing_metrics/l10n/app_localizations.dart';

import 'package:provider/provider.dart';

import '../core/activity_record_format.dart';
import 'app.dart';

class ActivitiesScreen extends StatelessWidget {
  const ActivitiesScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final model = context.watch<AppModel>();
    final rows = model.activities;
    final l10n = AppLocalizations.of(context);
    return SafeArea(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(20, 20, 20, 8),
            child: Row(
              children: [
                Expanded(
                  child: Text(
                    l10n.activitiesTitle,
                    style: Theme.of(context).textTheme.headlineMedium,
                  ),
                ),
                if (rows.isNotEmpty) ...[
                  TextButton(
                    onPressed: () async {
                      final ok = await showDialog<bool>(
                        context: context,
                        builder: (ctx) => AlertDialog(
                          title: Text(l10n.exportActivitiesTitle),
                          content: Text(l10n.exportActivitiesMessage),
                          actions: [
                            TextButton(
                              onPressed: () => Navigator.pop(ctx, false),
                              child: Text(l10n.cancel),
                            ),
                            TextButton(
                              onPressed: () => Navigator.pop(ctx, true),
                              child: Text(l10n.exportActivitiesConfirm),
                            ),
                          ],
                        ),
                      );
                      if (ok == true && context.mounted) {
                        await model.exportActivitiesCsv(l10n);
                      }
                    },
                    child: Text(l10n.exportActivities),
                  ),
                  TextButton(
                    onPressed: () async {
                      final ok = await showDialog<bool>(
                        context: context,
                        builder: (ctx) => AlertDialog(
                          title: Text(l10n.deleteAllTitle),
                          content: Text(l10n.deleteAllMessage),
                          actions: [
                            TextButton(
                              onPressed: () => Navigator.pop(ctx, false),
                              child: Text(l10n.cancel),
                            ),
                            TextButton(
                              onPressed: () => Navigator.pop(ctx, true),
                              child: Text(l10n.delete),
                            ),
                          ],
                        ),
                      );
                      if (ok == true) await model.deleteAllActivities();
                    },
                    child: Text(l10n.deleteAll),
                  ),
                ],
              ],
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 12),
            child: _HeaderRow(l10n: l10n),
          ),
          const Divider(height: 1),
          Expanded(
            child: rows.isEmpty
                ? Center(child: Text(l10n.noActivitiesMessage))
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
                              '${formatOneDecimalTable(e.avgSpeedKmh)} ${l10n.speedKmhSuffix}',
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
                              child: Text(
                                l10n.delete,
                                style: const TextStyle(
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
  const _HeaderRow({required this.l10n});

  final AppLocalizations l10n;

  @override
  Widget build(BuildContext context) {
    const style = TextStyle(
      fontSize: 12,
      fontWeight: FontWeight.w600,
      color: Colors.white70,
    );
    Widget h(String t, double w) =>
        SizedBox(width: w, child: Text(t, style: style));
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 10),
      child: Row(
        children: [
          h(l10n.colDate, 88),
          h(l10n.colHour, 52),
          h(l10n.colTime, 64),
          h(l10n.colStrokeRate, 48),
          h(l10n.colAvgSpeed, 72),
          Expanded(child: Text(l10n.colDistance, style: style)),
          const SizedBox(width: 48),
        ],
      ),
    );
  }
}
