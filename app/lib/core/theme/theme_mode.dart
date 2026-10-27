import 'package:flutter/material.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../config/server_config.dart';

part 'theme_mode.g.dart';

/// Light / dark / follow the phone. Stored on the phone; default: follow the phone (system).
@Riverpod(keepAlive: true)
class ThemeModeSetting extends _$ThemeModeSetting {
  static const _prefsKey = 'theme_mode';

  @override
  ThemeMode build() {
    final stored = ref.watch(sharedPreferencesProvider).getString(_prefsKey);
    return ThemeMode.values.firstWhere((m) => m.name == stored, orElse: () => ThemeMode.system);
  }

  Future<void> set(ThemeMode mode) async {
    state = mode;
    await ref.read(sharedPreferencesProvider).setString(_prefsKey, mode.name);
  }
}
