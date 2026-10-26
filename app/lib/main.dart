import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'app.dart';
import 'core/config/server_config.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  // Loaded before the first frame, so the server URL is known synchronously everywhere.
  final prefs = await SharedPreferences.getInstance();
  runApp(
    ProviderScope(
      overrides: [sharedPreferencesProvider.overrideWithValue(prefs)],
      // Riverpod 3 retries failed providers automatically; we show an error with "Try again"
      // instead, so a wrong password or a 403 isn't silently re-sent.
      retry: (retryCount, error) => null,
      child: const AdeyApp(),
    ),
  );
}
