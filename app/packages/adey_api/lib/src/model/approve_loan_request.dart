//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'approve_loan_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ApproveLoanRequest {
  /// Returns a new [ApproveLoanRequest] instance.
  ApproveLoanRequest({

     this.approvedAmount,

     this.approvedOnDate,

     this.note,
  });

      /// Approve less than requested. Defaults to the requested principal.
          // minimum: 0
  @JsonKey(
    
    name: r'approvedAmount',
    required: false,
    includeIfNull: false,
  )


  final num? approvedAmount;



      /// ISO date. Defaults to today.
  @JsonKey(
    
    name: r'approvedOnDate',
    required: false,
    includeIfNull: false,
  )


  final DateTime? approvedOnDate;



  @JsonKey(
    
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ApproveLoanRequest &&
      other.approvedAmount == approvedAmount &&
      other.approvedOnDate == approvedOnDate &&
      other.note == note;

    @override
    int get hashCode =>
        approvedAmount.hashCode +
        approvedOnDate.hashCode +
        note.hashCode;

  factory ApproveLoanRequest.fromJson(Map<String, dynamic> json) => _$ApproveLoanRequestFromJson(json);

  Map<String, dynamic> toJson() => _$ApproveLoanRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

