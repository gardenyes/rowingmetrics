import 'package:flutter/material.dart';

import 'platform/flutter_platform_services.dart';
import 'ui/app.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final platform = await FlutterPlatformServices.create();
  runApp(RowingMetricsApp(platform: platform));
}
