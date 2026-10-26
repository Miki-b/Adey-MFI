//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'reject_loan_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RejectLoanRequest {
  /// Returns a new [RejectLoanRequest] instance.
  RejectLoanRequest({

     this.note,

     this.rejectedOnDate,
  });

      /// Why the application was rejected.
  @JsonKey(
    
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;



      /// ISO date. Defaults to today.
  @JsonKey(
    
    name: r'rejectedOnDate',
    required: false,
    includeIfNull: false,
  )


  final DateTime? rejectedOnDate;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RejectLoanRequest &&
      other.note == note &&
      other.rejectedOnDate == rejectedOnDate;

    @override
    int get hashCode =>
        note.hashCode +
        rejectedOnDate.hashCode;

  factory RejectLoanRequest.fromJson(Map<String, dynamic> json) => _$RejectLoanRequestFromJson(json);

  Map<String, dynamic> toJson() => _$RejectLoanRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

