//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'collection_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CollectionItem {
  /// Returns a new [CollectionItem] instance.
  CollectionItem({

     this.accountNo,

     this.amount,

     this.clientId,

     this.clientMobileNo,

     this.clientName,

     this.daysOverdue,

     this.dueDate,

     this.installment,

     this.loanId,
  });

  @JsonKey(
    
    name: r'accountNo',
    required: false,
    includeIfNull: false,
  )


  final String? accountNo;



  @JsonKey(
    
    name: r'amount',
    required: false,
    includeIfNull: false,
  )


  final num? amount;



  @JsonKey(
    
    name: r'clientId',
    required: false,
    includeIfNull: false,
  )


  final int? clientId;



  @JsonKey(
    
    name: r'clientMobileNo',
    required: false,
    includeIfNull: false,
  )


  final String? clientMobileNo;



  @JsonKey(
    
    name: r'clientName',
    required: false,
    includeIfNull: false,
  )


  final String? clientName;



  @JsonKey(
    
    name: r'daysOverdue',
    required: false,
    includeIfNull: false,
  )


  final int? daysOverdue;



  @JsonKey(
    
    name: r'dueDate',
    required: false,
    includeIfNull: false,
  )


  final DateTime? dueDate;



  @JsonKey(
    
    name: r'installment',
    required: false,
    includeIfNull: false,
  )


  final int? installment;



  @JsonKey(
    
    name: r'loanId',
    required: false,
    includeIfNull: false,
  )


  final int? loanId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CollectionItem &&
      other.accountNo == accountNo &&
      other.amount == amount &&
      other.clientId == clientId &&
      other.clientMobileNo == clientMobileNo &&
      other.clientName == clientName &&
      other.daysOverdue == daysOverdue &&
      other.dueDate == dueDate &&
      other.installment == installment &&
      other.loanId == loanId;

    @override
    int get hashCode =>
        accountNo.hashCode +
        amount.hashCode +
        clientId.hashCode +
        clientMobileNo.hashCode +
        clientName.hashCode +
        daysOverdue.hashCode +
        dueDate.hashCode +
        installment.hashCode +
        loanId.hashCode;

  factory CollectionItem.fromJson(Map<String, dynamic> json) => _$CollectionItemFromJson(json);

  Map<String, dynamic> toJson() => _$CollectionItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

