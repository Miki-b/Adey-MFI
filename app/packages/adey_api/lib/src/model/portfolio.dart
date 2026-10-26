//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:adey_api/src/model/amounts.dart';
import 'package:adey_api/src/model/counts.dart';
import 'package:adey_api/src/model/collection_item.dart';
import 'package:adey_api/src/model/officer_summary.dart';
import 'package:adey_api/src/model/par.dart';
import 'package:adey_api/src/model/scope.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'portfolio.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Portfolio {
  /// Returns a new [Portfolio] instance.
  Portfolio({

     this.amounts,

     this.asOf,

     this.counts,

     this.dueToday,

     this.dueTodayCount,

     this.officers,

     this.overdue,

     this.overdueCount,

     this.par,

     this.scope,
  });

  @JsonKey(
    
    name: r'amounts',
    required: false,
    includeIfNull: false,
  )


  final Amounts? amounts;



  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;



  @JsonKey(
    
    name: r'counts',
    required: false,
    includeIfNull: false,
  )


  final Counts? counts;



  @JsonKey(
    
    name: r'dueToday',
    required: false,
    includeIfNull: false,
  )


  final List<CollectionItem>? dueToday;



  @JsonKey(
    
    name: r'dueTodayCount',
    required: false,
    includeIfNull: false,
  )


  final int? dueTodayCount;



  @JsonKey(
    
    name: r'officers',
    required: false,
    includeIfNull: false,
  )


  final List<OfficerSummary>? officers;



  @JsonKey(
    
    name: r'overdue',
    required: false,
    includeIfNull: false,
  )


  final List<CollectionItem>? overdue;



  @JsonKey(
    
    name: r'overdueCount',
    required: false,
    includeIfNull: false,
  )


  final int? overdueCount;



  @JsonKey(
    
    name: r'par',
    required: false,
    includeIfNull: false,
  )


  final Par? par;



  @JsonKey(
    
    name: r'scope',
    required: false,
    includeIfNull: false,
  )


  final Scope? scope;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Portfolio &&
      other.amounts == amounts &&
      other.asOf == asOf &&
      other.counts == counts &&
      other.dueToday == dueToday &&
      other.dueTodayCount == dueTodayCount &&
      other.officers == officers &&
      other.overdue == overdue &&
      other.overdueCount == overdueCount &&
      other.par == par &&
      other.scope == scope;

    @override
    int get hashCode =>
        amounts.hashCode +
        asOf.hashCode +
        counts.hashCode +
        dueToday.hashCode +
        dueTodayCount.hashCode +
        officers.hashCode +
        overdue.hashCode +
        overdueCount.hashCode +
        par.hashCode +
        scope.hashCode;

  factory Portfolio.fromJson(Map<String, dynamic> json) => _$PortfolioFromJson(json);

  Map<String, dynamic> toJson() => _$PortfolioToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

