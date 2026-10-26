import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// The two tokens from /api/auth/login (access: 15 min, refresh: 7 days, rotated on every use).
class TokenPair {
  const TokenPair({required this.accessToken, required this.refreshToken});

  final String accessToken;
  final String refreshToken;
}

/// Where the tokens live. An interface so tests can use [MemoryTokenStore].
abstract class TokenStore {
  Future<String?> accessToken();

  Future<String?> refreshToken();

  Future<void> save(TokenPair tokens);

  Future<void> clear();
}

/// Tokens in the platform keystore (Android Keystore-backed encryption / iOS Keychain), with an
/// in-memory copy so every API call doesn't hit the keystore.
class SecureTokenStore implements TokenStore {
  SecureTokenStore([FlutterSecureStorage? storage]) : _storage = storage ?? const FlutterSecureStorage();

  static const _accessKey = 'access_token';
  static const _refreshKey = 'refresh_token';

  final FlutterSecureStorage _storage;
  bool _loaded = false;
  String? _access;
  String? _refresh;

  Future<void> _load() async {
    if (_loaded) return;
    _access = await _storage.read(key: _accessKey);
    _refresh = await _storage.read(key: _refreshKey);
    _loaded = true;
  }

  @override
  Future<String?> accessToken() async {
    await _load();
    return _access;
  }

  @override
  Future<String?> refreshToken() async {
    await _load();
    return _refresh;
  }

  @override
  Future<void> save(TokenPair tokens) async {
    _access = tokens.accessToken;
    _refresh = tokens.refreshToken;
    _loaded = true;
    await _storage.write(key: _accessKey, value: tokens.accessToken);
    await _storage.write(key: _refreshKey, value: tokens.refreshToken);
  }

  @override
  Future<void> clear() async {
    _access = null;
    _refresh = null;
    _loaded = true;
    await _storage.delete(key: _accessKey);
    await _storage.delete(key: _refreshKey);
  }
}

class MemoryTokenStore implements TokenStore {
  MemoryTokenStore([TokenPair? initial])
      : _access = initial?.accessToken,
        _refresh = initial?.refreshToken;

  String? _access;
  String? _refresh;

  @override
  Future<String?> accessToken() async => _access;

  @override
  Future<String?> refreshToken() async => _refresh;

  @override
  Future<void> save(TokenPair tokens) async {
    _access = tokens.accessToken;
    _refresh = tokens.refreshToken;
  }

  @override
  Future<void> clear() async {
    _access = null;
    _refresh = null;
  }
}
