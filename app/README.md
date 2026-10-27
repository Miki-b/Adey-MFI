# Adey Field (Flutter app)

The field app for Adey MFI's loan officers and branch managers. It talks **only** to the Adey backend
(`../backend`), never to Fineract. Plan and status across sessions: [PLAN.md](PLAN.md). Design notes:
[../notes/09-flutter-foundation.md](../notes/09-flutter-foundation.md).

Session A (done): login, dashboard (portfolio, PAR30, due today, overdue, per officer for managers),
client search and details. Collections, Approvals and Reconciliation tabs are placeholders.

## Setup (FVM)

The Flutter SDK is pinned with [FVM](https://fvm.app) in `.fvmrc` (Flutter **3.41.6**). Run every
Flutter/Dart command through FVM, from this `app/` folder:

```powershell
dart pub global activate fvm      # once, if `fvm --version` fails
cd app
fvm install                       # installs the pinned SDK (if not cached yet)
fvm flutter pub get
```

VS Code: `.vscode/settings.json` points `dart.flutterSdkPath` at `.fvm/flutter_sdk` (the per-machine
link FVM creates; git-ignored).

## Run on a phone

1. Phone: Developer options → USB debugging on; plug in; accept "Allow USB debugging". `adb devices` lists it.
2. Backend: `..\scripts\codespace.ps1 start` (waits until READY).
3. **Recommended: over the USB cable, so the Codespace port stays private.**

   ```powershell
   gh codespace ports forward 8080:18080 -c <codespace>   # PC 127.0.0.1:18080 -> Codespace 8080 (keep it running)
   adb reverse tcp:8080 tcp:18080                         # phone 127.0.0.1:8080 -> PC 18080
   ```

   In the app: Settings → preset **USB cable (adb reverse, 127.0.0.1:8080)** (debug builds). Afterwards:
   `adb reverse --remove-all` and stop the forward.
4. Alternative without a cable: `..\scripts\codespace.ps1 public` + the Codespace preset, then **`private` again**.
   On 2026-10-26 GitHub's public relay was unreliable after the Codespace woke from its idle stop (404 from
   `tunnels-prod-...` although the backend was up; toggling private → public helped only briefly), so prefer USB.
5. `fvm flutter build apk --debug`, then `adb install -r build\app\outputs\flutter-apk\app-debug.apk`
   (or `fvm flutter run`). The debug APK is ~150 MB; on a nearly full phone uninstall the old build first.

Demo accounts (backend/README.md): officer `samuel` / `Adey#Loan2026`, managers `tadesse` (Bishoftu) and
`selamawit` (Adama) / `Adey#Lead2026`.

## Theme

More → Settings → **Theme**: System (default), Light or Dark, stored on the phone. Brand colours and the
contrast rules are in `lib/core/theme/app_theme.dart` and [notes/10](../notes/10-api-contract-and-theme.md).

## Server URL

Set at runtime: **More → Settings → Server** (also reachable from the login screen via "Change"), stored
on the phone. Presets:

| Preset | URL | Builds |
| --- | --- | --- |
| Codespace (default) | `https://solid-train-6x66vw9qg4jhp56-8080.app.github.dev` | all |
| Android emulator | `http://10.0.2.2:8080` | debug only |
| PC on this Wi-Fi | `http://192.168.1.3:8080` | debug only |

Plain `http://` is accepted only in debug builds: the app refuses it in release, and Android release
builds block cleartext anyway (`android/app/src/debug/res/xml/network_security_config.xml` allows it for
debug only). Changing the server signs you out (tokens belong to one backend). "Test connection" checks
that the URL answers like our backend.

If the Codespace port is private, the app says so ("the Codespace port is private") instead of a generic error.

## Generated API client

`packages/adey_api` is generated from `../backend/openapi.json` by openapi-generator (dart-dio +
json_serializable). **Never edit it by hand.** After a backend API change:

```powershell
# backend: re-export the spec (OpenApiSpecTest writes backend/openapi.json), e.g. in the Codespace
cd app
.\tool\generate_api.ps1           # downloads the generator jar once (tool/.cache), regenerates, runs build_runner
fvm flutter analyze; fvm flutter test
```

The app never uses generated types in widgets: repositories (`lib/features/*/..._repository.dart`) map
them to app models (money as `Decimal`, nullable fields to defaults).

## Code generation in the app

Riverpod providers (`@riverpod`) and localizations are generated:

```powershell
fvm flutter gen-l10n                                      # lib/l10n/app_en.arb -> app_localizations*.dart
fvm dart run build_runner build --delete-conflicting-outputs   # *.g.dart for providers
```

Both outputs are committed, so a fresh clone builds without running them first.

## Layout

```
lib/
  main.dart, app.dart          ProviderScope, MaterialApp.router
  core/
    api/        Dio + generated client providers
    auth/       token store, AuthInterceptor (refresh once), AuthController, Officer
    config/     server URL (runtime setting)
    errors/     ApiError + the one error-code -> message mapper
    formatting/ money (Decimal, "ETB 1,833.60"), dates (one place, Ethiopian calendar later)
    idempotency/ IdempotentAction (Idempotency-Key per confirmed action)
    routing/    go_router with auth guard, bottom-tab shell
    theme/      Material 3 green/gold, light + dark, StatusColors
    widgets/    loading / empty / error-with-retry / coming soon
  features/     auth, dashboard, clients, settings, home (later: loans, payments, collections, ...)
  l10n/         app_en.arb (+ generated)
packages/adey_api   generated client (do not edit)
tool/generate_api.ps1
```

## Checks

```powershell
fvm flutter analyze   # no issues
fvm flutter test      # unit tests: interceptor, error mapper, money, idempotency, router, mapping
```
