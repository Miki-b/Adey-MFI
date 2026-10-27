import 'package:flutter/material.dart';

/// Adey brand: GRASS GREEN (primary) and YELLOW (secondary), used alternately:
/// - green: primary actions (filled buttons), the active tab, key headers, links;
/// - yellow: highlights, chips, badges, secondary (tonal) actions, accents.
///
/// Tones were picked for WCAG AA (4.5:1 for text, 3:1 for large text and UI parts), checked by
/// test/core/theme/theme_contrast_test.dart:
/// - The brand seed green #3F8F29 has only 4.06:1 with white text, so LIGHT mode uses a deeper
///   grass green #2E6B1F (6.5:1); DARK mode uses a light green #8FD975 on dark surfaces (11:1).
/// - Yellow #F2C200 has 1.7:1 on white, so it is NEVER a text colour on light backgrounds: it is a
///   fill with dark text on it (#2B2200 on yellow = 9.4:1). On dark surfaces yellow text is fine (11:1).
class AppTheme {
  const AppTheme._();

  static const brandGreen = Color(0xFF3F8F29);
  static const brandYellow = Color(0xFFF2C200);

  static ThemeData light() => _build(_lightScheme(), StatusColors.light);

  static ThemeData dark() => _build(_darkScheme(), StatusColors.dark);

  static ColorScheme _lightScheme() =>
      ColorScheme.fromSeed(seedColor: brandGreen, brightness: Brightness.light).copyWith(
        primary: const Color(0xFF2E6B1F),
        onPrimary: Colors.white,
        primaryContainer: const Color(0xFFC2EBAE),
        onPrimaryContainer: const Color(0xFF0B2E05),
        secondary: brandYellow, // a FILL in light mode, never text
        onSecondary: const Color(0xFF2B2200),
        secondaryContainer: const Color(0xFFFFE58A),
        onSecondaryContainer: const Color(0xFF3A2E00),
        surface: const Color(0xFFF8FAF2),
        onSurface: const Color(0xFF1A1C18),
        onSurfaceVariant: const Color(0xFF43483E),
        surfaceContainerLowest: Colors.white,
        surfaceContainerLow: const Color(0xFFF0F3EA),
        surfaceContainer: const Color(0xFFEAEEE3),
        surfaceContainerHigh: const Color(0xFFE4E8DD),
        surfaceContainerHighest: const Color(0xFFDEE3D7),
        outline: const Color(0xFF73796D),
        outlineVariant: const Color(0xFFC3C8BB),
        error: const Color(0xFFBA1A1A),
        onError: Colors.white,
      );

  static ColorScheme _darkScheme() =>
      ColorScheme.fromSeed(seedColor: brandGreen, brightness: Brightness.dark).copyWith(
        primary: const Color(0xFF8FD975),
        onPrimary: const Color(0xFF0F3A05),
        primaryContainer: const Color(0xFF2A5F1B),
        onPrimaryContainer: const Color(0xFFC8F2B5),
        secondary: brandYellow,
        onSecondary: const Color(0xFF2B2200),
        secondaryContainer: const Color(0xFF574500),
        onSecondaryContainer: const Color(0xFFFFE17A),
        surface: const Color(0xFF11140F),
        onSurface: const Color(0xFFE2E3DC),
        onSurfaceVariant: const Color(0xFFC3C8BB),
        surfaceContainerLowest: const Color(0xFF0C0F0A),
        surfaceContainerLow: const Color(0xFF1D211A),
        surfaceContainer: const Color(0xFF21251E),
        surfaceContainerHigh: const Color(0xFF2B2F28),
        surfaceContainerHighest: const Color(0xFF363A32),
        outline: const Color(0xFF8D9387),
        outlineVariant: const Color(0xFF43483E),
        error: const Color(0xFFFFB4AB),
        onError: const Color(0xFF690005),
      );

  static ThemeData _build(ColorScheme scheme, StatusColors status) {
    final theme = ThemeData(
      useMaterial3: true,
      brightness: scheme.brightness,
      colorScheme: scheme,
      scaffoldBackgroundColor: scheme.surface,
      visualDensity: VisualDensity.standard,
      materialTapTargetSize: MaterialTapTargetSize.padded,
      appBarTheme: AppBarTheme(
        centerTitle: false,
        backgroundColor: scheme.surface,
        foregroundColor: scheme.onSurface,
        scrolledUnderElevation: 1,
        titleTextStyle: TextStyle(fontSize: 22, fontWeight: FontWeight.w600, color: scheme.primary),
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
      // Green filled = the main action; FilledButton.tonal (yellow container) = secondary actions.
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(minimumSize: const Size(64, 52), textStyle: const TextStyle(fontSize: 16)),
      ),
      // Chips and badges are yellow-accented (filled, dark text).
      chipTheme: ChipThemeData(
        backgroundColor: scheme.secondaryContainer,
        labelStyle: TextStyle(color: scheme.onSecondaryContainer, fontWeight: FontWeight.w600),
        side: BorderSide.none,
        shape: const StadiumBorder(),
      ),
      // The active tab is green.
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: scheme.surfaceContainer,
        indicatorColor: scheme.primaryContainer,
        iconTheme: WidgetStateProperty.resolveWith((states) => IconThemeData(
              color: states.contains(WidgetState.selected) ? scheme.onPrimaryContainer : scheme.onSurfaceVariant,
            )),
        labelTextStyle: WidgetStateProperty.resolveWith((states) => TextStyle(
              fontSize: 12,
              fontWeight: states.contains(WidgetState.selected) ? FontWeight.w700 : FontWeight.w500,
              color: states.contains(WidgetState.selected) ? scheme.primary : scheme.onSurfaceVariant,
            )),
      ),
      segmentedButtonTheme: SegmentedButtonThemeData(
        style: ButtonStyle(
          backgroundColor: WidgetStateProperty.resolveWith(
              (states) => states.contains(WidgetState.selected) ? scheme.secondaryContainer : null),
          foregroundColor: WidgetStateProperty.resolveWith((states) =>
              states.contains(WidgetState.selected) ? scheme.onSecondaryContainer : scheme.onSurface),
        ),
      ),
      progressIndicatorTheme: ProgressIndicatorThemeData(
        color: scheme.primary,
        linearTrackColor: scheme.secondaryContainer,
      ),
      extensions: [status],
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

/// Traffic-light colours for PAR and arrears. Chosen to stay distinct from the BRAND colours:
/// "healthy" is teal (not the brand green), "watch" is orange (not the brand yellow), "high" is red.
/// They are used as text on cards, so each meets 4.5:1, and every use also has a word label
/// (Healthy / Watch / High risk), so colour is never the only signal.
@immutable
class StatusColors extends ThemeExtension<StatusColors> {
  const StatusColors({required this.good, required this.warning, required this.danger});

  final Color good;
  final Color warning;
  final Color danger;

  static const light = StatusColors(good: Color(0xFF00695C), warning: Color(0xFF9A4A06), danger: Color(0xFFB3261E));
  static const dark = StatusColors(good: Color(0xFF5FD4C4), warning: Color(0xFFFF7F3F), danger: Color(0xFFF2B8B5));

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
