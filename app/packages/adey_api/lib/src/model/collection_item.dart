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

    required  this.accountNo,

    required  this.amount,

    required  this.clientId,

     this.clientMobileNo,

    required  this.clientName,

    required  this.daysOverdue,

    required  this.dueDate,

    required  this.installment,

    required  this.loanId,
  });

  @JsonKey(
    
    name: r'accountNo',
    required: true,
    includeIfNull: false,
  )


  final String accountNo;



  @JsonKey(
    
    name: r'amount',
    required: true,
    includeIfNull: false,
  )


  final num amount;



  @JsonKey(
    
    name: r'clientId',
    required: true,
    includeIfNull: false,
  )


  final int clientId;



  @JsonKey(
    
    name: r'clientMobileNo',
    required: false,
    includeIfNull: false,
  )


  final String? clientMobileNo;



  @JsonKey(
    
    name: r'clientName',
    required: true,
    includeIfNull: false,
  )


  final String clientName;



  @JsonKey(
    
    name: r'daysOverdue',
    required: true,
    includeIfNull: false,
  )


  final int daysOverdue;



  @JsonKey(
    
    name: r'dueDate',
    required: true,
    includeIfNull: false,
  )


  final DateTime dueDate;



  @JsonKey(
    
    name: r'installment',
    required: true,
    includeIfNull: false,
  )


  final int installment;



  @JsonKey(
    
    name: r'loanId',
    required: true,
    includeIfNull: false,
  )


  final int loanId;





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

