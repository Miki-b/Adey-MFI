import 'package:flutter/material.dart';

/// Key headers ("Due today", "Loans", ...) in the brand green.
class SectionHeader extends StatelessWidget {
  const SectionHeader(this.text, {super.key, this.trailing});

  final String text;
  final Widget? trailing;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Row(
      children: [
        Expanded(
          child: Text(
            text,
            style: theme.textTheme.titleMedium?.copyWith(color: theme.colorScheme.primary, fontWeight: FontWeight.w700),
          ),
        ),
        ?trailing,
      ],
    );
  }
}

/// A status label. outlined: coloured text and border on a clear background (PAR, loan states);
/// filled: the yellow chip style (dark text on yellow), for highlights.
class StatusChip extends StatelessWidget {
  const StatusChip({super.key, required this.label, this.color, this.icon});

  /// null = the filled yellow highlight style.
  final Color? color;
  final String label;
  final IconData? icon;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    final fg = color ?? scheme.onSecondaryContainer;
    return Chip(
      avatar: icon == null ? null : Icon(icon, size: 16, color: fg),
      label: Text(label),
      labelStyle: TextStyle(color: fg, fontWeight: FontWeight.w600),
      backgroundColor: color == null ? scheme.secondaryContainer : Colors.transparent,
      side: color == null ? BorderSide.none : BorderSide(color: color!),
      visualDensity: VisualDensity.compact,
    );
  }
}

/// The Adey flower (the Meskel daisy, "adey abeba"): a dark flower on a yellow disc, readable in
/// both themes (yellow is a fill, never a foreground colour on light backgrounds).
class BrandMark extends StatelessWidget {
  const BrandMark({super.key, this.size = 72});

  final double size;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(color: scheme.secondary, shape: BoxShape.circle),
      child: Icon(Icons.local_florist, size: size * 0.6, color: scheme.onSecondary),
    );
  }
}
