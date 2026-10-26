import 'package:flutter/foundation.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:shared_preferences/shared_preferences.dart';

part 'server_config.g.dart';

/// Where the backend runs. Chosen at runtime in Settings, stored on the phone (not secret, so
/// shared_preferences, not secure storage). Changing it signs the user out: tokens belong to one
/// backend.
class ServerConfig {
  const ServerConfig._();

  static const codespaceUrl = 'https://solid-train-6x66vw9qg4jhp56-8080.app.github.dev';

  /// The Android emulator reaches the PC's localhost at 10.0.2.2.
  static const emulatorUrl = 'http://10.0.2.2:8080';

  /// Phone on a USB cable: `adb reverse tcp:8080 tcp:<port>` makes the phone's localhost reach the PC
  /// (and from there e.g. `gh codespace ports forward`), so the Codespace port can stay private.
  static const usbUrl = 'http://127.0.0.1:8080';

  /// A backend on the PC, reached over Wi-Fi (see backend/README.md "LAN").
  static const lanUrl = 'http://192.168.1.3:8080';

  static const defaultUrl = codespaceUrl;
  static const _prefsKey = 'server_url';

  /// Plain http only in debug builds (release builds also refuse it at the Android level).
  static bool get allowHttp => kDebugMode;
}

enum ServerUrlProblem { invalid, httpNotAllowed }

/// null = fine. Accepts "https://host[:port][/path]"; trailing slashes are removed by [normalizeServerUrl].
ServerUrlProblem? checkServerUrl(String input, {required bool allowHttp}) {
  final uri = Uri.tryParse(input.trim());
  if (uri == null || !uri.hasScheme || uri.host.isEmpty) return ServerUrlProblem.invalid;
  if (uri.scheme == 'https') return null;
  if (uri.scheme == 'http') return allowHttp ? null : ServerUrlProblem.httpNotAllowed;
  return ServerUrlProblem.invalid;
}

String normalizeServerUrl(String input) => input.trim().replaceAll(RegExp(r'/+$'), '');

/// Loaded once in main() and injected (overrideWithValue), so reading the URL is synchronous.
@Riverpod(keepAlive: true)
SharedPreferences sharedPreferences(Ref ref) =>
    throw UnimplementedError('sharedPreferencesProvider must be overridden in main()');

@Riverpod(keepAlive: true)
class ServerUrl extends _$ServerUrl {
  @override
  String build() {
    final stored = ref.watch(sharedPreferencesProvider).getString(ServerConfig._prefsKey);
    if (stored == null || checkServerUrl(stored, allowHttp: ServerConfig.allowHttp) != null) {
      return ServerConfig.defaultUrl;
    }
    return stored;
  }

  Future<void> set(String url) async {
    final normalized = normalizeServerUrl(url);
    await ref.read(sharedPreferencesProvider).setString(ServerConfig._prefsKey, normalized);
    state = normalized;
  }
}
