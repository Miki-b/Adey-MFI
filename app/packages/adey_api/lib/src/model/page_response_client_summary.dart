//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:adey_api/src/model/client_summary.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'page_response_client_summary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PageResponseClientSummary {
  /// Returns a new [PageResponseClientSummary] instance.
  PageResponseClientSummary({

    required  this.content,

    required  this.page,

    required  this.size,

    required  this.totalElements,

    required  this.totalPages,
  });

  @JsonKey(
    
    name: r'content',
    required: true,
    includeIfNull: false,
  )


  final List<ClientSummary> content;



  @JsonKey(
    
    name: r'page',
    required: true,
    includeIfNull: false,
  )


  final int page;



  @JsonKey(
    
    name: r'size',
    required: true,
    includeIfNull: false,
  )


  final int size;



  @JsonKey(
    
    name: r'totalElements',
    required: true,
    includeIfNull: false,
  )


  final int totalElements;



  @JsonKey(
    
    name: r'totalPages',
    required: true,
    includeIfNull: false,
  )


  final int totalPages;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PageResponseClientSummary &&
      other.content == content &&
      other.page == page &&
      other.size == size &&
      other.totalElements == totalElements &&
      other.totalPages == totalPages;

    @override
    int get hashCode =>
        content.hashCode +
        page.hashCode +
        size.hashCode +
        totalElements.hashCode +
        totalPages.hashCode;

  factory PageResponseClientSummary.fromJson(Map<String, dynamic> json) => _$PageResponseClientSummaryFromJson(json);

  Map<String, dynamic> toJson() => _$PageResponseClientSummaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

