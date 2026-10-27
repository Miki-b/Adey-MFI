import 'dart:math' as math;

import 'package:adey_field/core/theme/app_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

/// WCAG 2.x contrast ratio: (lighter + 0.05) / (darker + 0.05) of relative luminances.
double contrast(Color a, Color b) {
  final la = a.computeLuminance();
  final lb = b.computeLuminance();
  return (math.max(la, lb) + 0.05) / (math.min(la, lb) + 0.05);
}

void main() {
  for (final (name, theme) in [('light', AppTheme.light()), ('dark', AppTheme.dark())]) {
    group('$name theme', () {
      final s = theme.colorScheme;
      final status = theme.extension<StatusColors>()!;

      // Text (4.5:1) on the backgrounds it is used on.
      final textPairs = <String, (Color, Color)>{
        'onPrimary on primary (filled buttons)': (s.onPrimary, s.primary),
        'onPrimaryContainer on primaryContainer (active tab)': (s.onPrimaryContainer, s.primaryContainer),
        'onSecondary on secondary (yellow fills)': (s.onSecondary, s.secondary),
        'onSecondaryContainer on secondaryContainer (chips, tonal buttons)': (s.onSecondaryContainer, s.secondaryContainer),
        'onSurface on surface': (s.onSurface, s.surface),
        'onSurfaceVariant on surface': (s.onSurfaceVariant, s.surface),
        'onSurface on cards': (s.onSurface, s.surfaceContainerLow),
        'onSurfaceVariant on cards': (s.onSurfaceVariant, s.surfaceContainerLow),
        'primary text (headers, links) on surface': (s.primary, s.surface),
        'primary text on cards': (s.primary, s.surfaceContainerLow),
        'primary label on the tab bar': (s.primary, s.surfaceContainer),
        'onError on error': (s.onError, s.error),
        'PAR healthy on cards': (status.good, s.surfaceContainerLow),
        'PAR watch on cards': (status.warning, s.surfaceContainerLow),
        'PAR high on cards': (status.danger, s.surfaceContainerLow),
        'PAR high on surface': (status.danger, s.surface),
      };
      for (final MapEntry(key: label, value: (fg, bg)) in textPairs.entries) {
        test('$label >= 4.5:1', () {
          expect(contrast(fg, bg), greaterThanOrEqualTo(4.5), reason: '$label: ${contrast(fg, bg).toStringAsFixed(2)}');
        });
      }

      test('PAR colours are distinct from the brand green and yellow', () {
        double hue(Color c) => HSVColor.fromColor(c).hue;
        double hueGap(Color a, Color b) {
          final d = (hue(a) - hue(b)).abs();
          return math.min(d, 360 - d);
        }

        expect(hueGap(status.good, AppTheme.brandGreen), greaterThan(40)); // teal vs grass green
        // Orange vs yellow: a clearly different hue AND lightness (a light dark-mode orange #FDBA74 failed
        // this: same lightness as the yellow, so the two looked alike).
        expect(hueGap(status.warning, AppTheme.brandYellow), greaterThan(20));
        expect(contrast(status.warning, AppTheme.brandYellow), greaterThanOrEqualTo(1.4));
        expect(hueGap(status.danger, AppTheme.brandYellow), greaterThan(30));
      });
    });
  }

  test('yellow is never readable as text on light backgrounds (so the theme only uses it as a fill)', () {
    final s = AppTheme.light().colorScheme;
    expect(contrast(s.secondary, s.surface), lessThan(3)); // documents WHY: 1.7:1
    expect(s.onSecondary.computeLuminance(), lessThan(0.05)); // dark text on yellow fills
  });
}
