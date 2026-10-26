// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'page_response_client_summary.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PageResponseClientSummaryCWProxy {
  PageResponseClientSummary content(List<ClientSummary>? content);

  PageResponseClientSummary page(int? page);

  PageResponseClientSummary size(int? size);

  PageResponseClientSummary totalElements(int? totalElements);

  PageResponseClientSummary totalPages(int? totalPages);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `PageResponseClientSummary(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// PageResponseClientSummary(...).copyWith(id: 12, name: "My name")
  /// ````
  PageResponseClientSummary call({
    List<ClientSummary>? content,
    int? page,
    int? size,
    int? totalElements,
    int? totalPages,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfPageResponseClientSummary.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfPageResponseClientSummary.copyWith.fieldName(...)`
class _$PageResponseClientSummaryCWProxyImpl
    implements _$PageResponseClientSummaryCWProxy {
  const _$PageResponseClientSummaryCWProxyImpl(this._value);

  final PageResponseClientSummary _value;

  @override
  PageResponseClientSummary content(List<ClientSummary>? content) =>
      this(content: content);

  @override
  PageResponseClientSummary page(int? page) => this(page: page);

  @override
  PageResponseClientSummary size(int? size) => this(size: size);

  @override
  PageResponseClientSummary totalElements(int? totalElements) =>
      this(totalElements: totalElements);

  @override
  PageResponseClientSummary totalPages(int? totalPages) =>
      this(totalPages: totalPages);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `PageResponseClientSummary(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// PageResponseClientSummary(...).copyWith(id: 12, name: "My name")
  /// ````
  PageResponseClientSummary call({
    Object? content = const $CopyWithPlaceholder(),
    Object? page = const $CopyWithPlaceholder(),
    Object? size = const $CopyWithPlaceholder(),
    Object? totalElements = const $CopyWithPlaceholder(),
    Object? totalPages = const $CopyWithPlaceholder(),
  }) {
    return PageResponseClientSummary(
      content: content == const $CopyWithPlaceholder()
          ? _value.content
          // ignore: cast_nullable_to_non_nullable
          : content as List<ClientSummary>?,
      page: page == const $CopyWithPlaceholder()
          ? _value.page
          // ignore: cast_nullable_to_non_nullable
          : page as int?,
      size: size == const $CopyWithPlaceholder()
          ? _value.size
          // ignore: cast_nullable_to_non_nullable
          : size as int?,
      totalElements: totalElements == const $CopyWithPlaceholder()
          ? _value.totalElements
          // ignore: cast_nullable_to_non_nullable
          : totalElements as int?,
      totalPages: totalPages == const $CopyWithPlaceholder()
          ? _value.totalPages
          // ignore: cast_nullable_to_non_nullable
          : totalPages as int?,
    );
  }
}

extension $PageResponseClientSummaryCopyWith on PageResponseClientSummary {
  /// Returns a callable class that can be used as follows: `instanceOfPageResponseClientSummary.copyWith(...)` or like so:`instanceOfPageResponseClientSummary.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PageResponseClientSummaryCWProxy get copyWith =>
      _$PageResponseClientSummaryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PageResponseClientSummary _$PageResponseClientSummaryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PageResponseClientSummary', json, ($checkedConvert) {
  final val = PageResponseClientSummary(
    content: $checkedConvert(
      'content',
      (v) => (v as List<dynamic>?)
          ?.map((e) => ClientSummary.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    page: $checkedConvert('page', (v) => (v as num?)?.toInt()),
    size: $checkedConvert('size', (v) => (v as num?)?.toInt()),
    totalElements: $checkedConvert(
      'totalElements',
      (v) => (v as num?)?.toInt(),
    ),
    totalPages: $checkedConvert('totalPages', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$PageResponseClientSummaryToJson(
  PageResponseClientSummary instance,
) => <String, dynamic>{
  'content': ?instance.content?.map((e) => e.toJson()).toList(),
  'page': ?instance.page,
  'size': ?instance.size,
  'totalElements': ?instance.totalElements,
  'totalPages': ?instance.totalPages,
};
