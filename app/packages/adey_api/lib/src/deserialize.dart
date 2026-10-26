import 'package:adey_api/src/model/amounts.dart';
import 'package:adey_api/src/model/approve_loan_request.dart';
import 'package:adey_api/src/model/client_details.dart';
import 'package:adey_api/src/model/client_summary.dart';
import 'package:adey_api/src/model/collection_item.dart';
import 'package:adey_api/src/model/counts.dart';
import 'package:adey_api/src/model/create_client_request.dart';
import 'package:adey_api/src/model/dates.dart';
import 'package:adey_api/src/model/disburse_loan_request.dart';
import 'package:adey_api/src/model/finding_response.dart';
import 'package:adey_api/src/model/installment.dart';
import 'package:adey_api/src/model/loan_application_request.dart';
import 'package:adey_api/src/model/loan_details.dart';
import 'package:adey_api/src/model/loan_list_item.dart';
import 'package:adey_api/src/model/loan_product_response.dart';
import 'package:adey_api/src/model/loan_status_response.dart';
import 'package:adey_api/src/model/loan_summary.dart';
import 'package:adey_api/src/model/loan_transaction_response.dart';
import 'package:adey_api/src/model/login_request.dart';
import 'package:adey_api/src/model/mobile_payment_request.dart';
import 'package:adey_api/src/model/mobile_payment_response.dart';
import 'package:adey_api/src/model/office_response.dart';
import 'package:adey_api/src/model/officer_response.dart';
import 'package:adey_api/src/model/officer_summary.dart';
import 'package:adey_api/src/model/page_response_client_summary.dart';
import 'package:adey_api/src/model/par.dart';
import 'package:adey_api/src/model/payment_type_response.dart';
import 'package:adey_api/src/model/portfolio.dart';
import 'package:adey_api/src/model/reconciliation_report.dart';
import 'package:adey_api/src/model/refresh_request.dart';
import 'package:adey_api/src/model/reject_loan_request.dart';
import 'package:adey_api/src/model/repayment_request.dart';
import 'package:adey_api/src/model/resolve_request.dart';
import 'package:adey_api/src/model/schedule_period.dart';
import 'package:adey_api/src/model/schedule_preview_request.dart';
import 'package:adey_api/src/model/schedule_preview_response.dart';
import 'package:adey_api/src/model/scope.dart';
import 'package:adey_api/src/model/summary.dart';
import 'package:adey_api/src/model/token_response.dart';
import 'package:adey_api/src/model/transition.dart';
import 'package:adey_api/src/model/undo_approval_request.dart';
import 'package:adey_api/src/model/update_client_request.dart';

final _regList = RegExp(r'^List<(.*)>$');
final _regSet = RegExp(r'^Set<(.*)>$');
final _regMap = RegExp(r'^Map<String,(.*)>$');

  ReturnType deserialize<ReturnType, BaseType>(dynamic value, String targetType, {bool growable= true}) {
      switch (targetType) {
        case 'String':
          return '$value' as ReturnType;
        case 'int':
          return (value is int ? value : int.parse('$value')) as ReturnType;
        case 'bool':
          if (value is bool) {
            return value as ReturnType;
          }
          final valueString = '$value'.toLowerCase();
          return (valueString == 'true' || valueString == '1') as ReturnType;
        case 'double':
          return (value is double ? value : double.parse('$value')) as ReturnType;
        case 'Amounts':
          return Amounts.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ApproveLoanRequest':
          return ApproveLoanRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ClientDetails':
          return ClientDetails.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ClientSummary':
          return ClientSummary.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CollectionItem':
          return CollectionItem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Counts':
          return Counts.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CreateClientRequest':
          return CreateClientRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Dates':
          return Dates.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DisburseLoanRequest':
          return DisburseLoanRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FindingResponse':
          return FindingResponse.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Installment':
          return Installment.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LoanApplicationRequest':
          return LoanApplicationRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LoanDetails':
          return LoanDetails.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LoanListItem':
          return LoanListItem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LoanProductResponse':
          return LoanProductResponse.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LoanStatusResponse':
          return LoanStatusResponse.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LoanSummary':
          return LoanSummary.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LoanTransactionResponse':
          return LoanTransactionResponse.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LoginRequest':
          return LoginRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MobilePaymentRequest':
          return MobilePaymentRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MobilePaymentResponse':
          return MobilePaymentResponse.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OfficeResponse':
          return OfficeResponse.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OfficerResponse':
          return OfficerResponse.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OfficerSummary':
          return OfficerSummary.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PageResponseClientSummary':
          return PageResponseClientSummary.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Par':
          return Par.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PaymentTypeResponse':
          return PaymentTypeResponse.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Portfolio':
          return Portfolio.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReconciliationReport':
          return ReconciliationReport.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RefreshRequest':
          return RefreshRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RejectLoanRequest':
          return RejectLoanRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RepaymentRequest':
          return RepaymentRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ResolveRequest':
          return ResolveRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SchedulePeriod':
          return SchedulePeriod.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SchedulePreviewRequest':
          return SchedulePreviewRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SchedulePreviewResponse':
          return SchedulePreviewResponse.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Scope':
          return Scope.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Summary':
          return Summary.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TokenResponse':
          return TokenResponse.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Transition':
          return Transition.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'UndoApprovalRequest':
          return UndoApprovalRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'UpdateClientRequest':
          return UpdateClientRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        default:
          RegExpMatch? match;

          if (value is List && (match = _regList.firstMatch(targetType)) != null) {
            targetType = match![1]!; // ignore: parameter_assignments
            return value
              .map<BaseType>((dynamic v) => deserialize<BaseType, BaseType>(v, targetType, growable: growable))
              .toList(growable: growable) as ReturnType;
          }
          if (value is Set && (match = _regSet.firstMatch(targetType)) != null) {
            targetType = match![1]!; // ignore: parameter_assignments
            return value
              .map<BaseType>((dynamic v) => deserialize<BaseType, BaseType>(v, targetType, growable: growable))
              .toSet() as ReturnType;
          }
          if (value is Map && (match = _regMap.firstMatch(targetType)) != null) {
            targetType = match![1]!.trim(); // ignore: parameter_assignments
            return Map<String, BaseType>.fromIterables(
              value.keys as Iterable<String>,
              value.values.map((dynamic v) => deserialize<BaseType, BaseType>(v, targetType, growable: growable)),
            ) as ReturnType;
          }
          break;
    }
    throw Exception('Cannot deserialize');
  }