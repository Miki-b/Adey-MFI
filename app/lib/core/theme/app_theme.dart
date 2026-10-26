import 'package:flutter/material.dart';

/// Material 3, calm green (trust, money) with a gold accent ("Adey" is the Meskel daisy: yellow).
/// Built for low-cost phones in daylight: strong contrast, 16 sp body text, big tap targets.
class AppTheme {
  const AppTheme._();

  static const _green = Color(0xFF1F6B45);
  static const _gold = Color(0xFFC9A227);

  static ThemeData light() => _build(Brightness.light);

  static ThemeData dark() => _build(Brightness.dark);

  static ThemeData _build(Brightness brightness) {
    final base = ColorScheme.fromSeed(seedColor: _green, brightness: brightness);
    final scheme = base.copyWith(
      secondary: brightness == Brightness.light ? const Color(0xFF8A6D00) : _gold,
      onSecondary: brightness == Brightness.light ? Colors.white : const Color(0xFF3A2E00),
      secondaryContainer: brightness == Brightness.light ? const Color(0xFFFFE08A) : const Color(0xFF5A4700),
      onSecondaryContainer: brightness == Brightness.light ? const Color(0xFF261A00) : const Color(0xFFFFE08A),
    );
    final theme = ThemeData(
      useMaterial3: true,
      brightness: brightness,
      colorScheme: scheme,
      visualDensity: VisualDensity.standard,
      materialTapTargetSize: MaterialTapTargetSize.padded,
      appBarTheme: AppBarTheme(
        centerTitle: false,
        backgroundColor: scheme.surface,
        foregroundColor: scheme.onSurface,
        scrolledUnderElevation: 1,
      ),
      cardTheme: CardThemeData(
        elevation: 0,
        color: scheme.surfaceContainerLow,
        margin: EdgeInsets.zero,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
          side: BorderSide(color: scheme.outlineVariant),
        ),
      ),
      inputDecorationTheme: const InputDecorationTheme(border: OutlineInputBorder()),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(minimumSize: const Size.fromHeight(52), textStyle: const TextStyle(fontSize: 16)),
      ),
      navigationBarTheme: NavigationBarThemeData(indicatorColor: scheme.secondaryContainer),
      extensions: [brightness == Brightness.light ? StatusColors.light : StatusColors.dark],
    );
    // Slightly larger body text than Material's default (14 sp): easier to read outdoors.
    return theme.copyWith(
      textTheme: theme.textTheme.copyWith(
        bodyMedium: theme.textTheme.bodyMedium?.copyWith(fontSize: 15),
        bodyLarge: theme.textTheme.bodyLarge?.copyWith(fontSize: 16),
      ),
    );
  }
}

/// Traffic-light colours for PAR and arrears, readable in both modes.
@immutable
class StatusColors extends ThemeExtension<StatusColors> {
  const StatusColors({required this.good, required this.warning, required this.danger});

  final Color good;
  final Color warning;
  final Color danger;

  static const light = StatusColors(good: Color(0xFF2E7D32), warning: Color(0xFFB26A00), danger: Color(0xFFC62828));
  static const dark = StatusColors(good: Color(0xFF81C784), warning: Color(0xFFFFB74D), danger: Color(0xFFEF9A9A));

  static StatusColors of(BuildContext context) => Theme.of(context).extension<StatusColors>()!;

  @override
  StatusColors copyWith({Color? good, Color? warning, Color? danger}) =>
      StatusColors(good: good ?? this.good, warning: warning ?? this.warning, danger: danger ?? this.danger);

  @override
  StatusColors lerp(ThemeExtension<StatusColors>? other, double t) {
    if (other is! StatusColors) return this;
    return StatusColors(
      good: Color.lerp(good, other.good, t)!,
      warning: Color.lerp(warning, other.warning, t)!,
      danger: Color.lerp(danger, other.danger, t)!,
    );
  }
}
