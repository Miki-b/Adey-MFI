// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for English (`en`).
class AppLocalizationsEn extends AppLocalizations {
  AppLocalizationsEn([String locale = 'en']) : super(locale);

  @override
  String get appTitle => 'Adey Field';

  @override
  String get retry => 'Try again';

  @override
  String get cancel => 'Cancel';

  @override
  String get save => 'Save';

  @override
  String get close => 'Close';

  @override
  String get comingSoonTitle => 'Coming in the next version';

  @override
  String comingSoonBody(String feature) {
    return '$feature is planned for a later release of Adey Field.';
  }

  @override
  String get notAvailable => '–';

  @override
  String get tabDashboard => 'Dashboard';

  @override
  String get tabClients => 'Clients';

  @override
  String get tabCollections => 'Collections';

  @override
  String get tabApprovals => 'Approvals';

  @override
  String get tabMore => 'More';

  @override
  String get loginTitle => 'Sign in';

  @override
  String get loginSubtitle => 'Use your Adey MFI username and password.';

  @override
  String get usernameLabel => 'Username';

  @override
  String get passwordLabel => 'Password';

  @override
  String get showPassword => 'Show password';

  @override
  String get hidePassword => 'Hide password';

  @override
  String get signIn => 'Sign in';

  @override
  String get usernameRequired => 'Enter your username';

  @override
  String get passwordRequired => 'Enter your password';

  @override
  String loginServer(String url) {
    return 'Server: $url';
  }

  @override
  String get changeServer => 'Change';

  @override
  String get restoringSession => 'Signing you in…';

  @override
  String get errorInvalidCredentials => 'Wrong username or password.';

  @override
  String errorTooManyAttempts(int minutes) {
    String _temp0 = intl.Intl.pluralLogic(
      minutes,
      locale: localeName,
      other: '$minutes minutes',
      one: '1 minute',
    );
    return 'Too many failed sign-ins. Try again in $_temp0.';
  }

  @override
  String get errorTooManyAttemptsNoTime =>
      'Too many failed sign-ins. Wait a few minutes and try again.';

  @override
  String get errorNotOfficer =>
      'This account is not a loan officer or branch manager account. Ask your branch manager to set it up.';

  @override
  String get errorSessionExpired =>
      'Your session has ended. Please sign in again.';

  @override
  String get errorForbidden => 'You don\'t have access to this.';

  @override
  String get errorNotFound => 'Not found. It may have been removed.';

  @override
  String get errorValidation => 'Some fields are not valid. Please check them.';

  @override
  String get errorNetwork =>
      'Can\'t reach the server. Check your internet connection and try again.';

  @override
  String get errorServerPrivate =>
      'The server is not reachable from this phone (the Codespace port is private). Ask for it to be made public, or change the server in Settings.';

  @override
  String get errorUnexpectedResponse =>
      'The server gave an answer this app doesn\'t understand. Check the server address in Settings.';

  @override
  String get errorFineractUnavailable =>
      'The core banking system is not responding. Try again in a minute.';

  @override
  String get errorFineractError =>
      'The core banking system reported a problem. Try again later.';

  @override
  String get errorInternal =>
      'Something went wrong on the server. Try again later.';

  @override
  String get errorProviderUnavailable =>
      'The mobile money provider can\'t be reached right now. Try again later.';

  @override
  String get errorIdempotencyInProgress =>
      'Your previous attempt is still being processed. Wait a moment, then try again.';

  @override
  String get errorIdempotencyOutcomeUnknown =>
      'We couldn\'t confirm whether your previous attempt went through. Check the loan before trying again.';

  @override
  String get errorIdempotencyReused =>
      'This request was already sent with different details. Start again.';

  @override
  String get errorOpenLoanExists =>
      'This client already has a pending, approved or active loan.';

  @override
  String get errorDuplicateClient =>
      'Another client already uses this mobile number or national ID.';

  @override
  String get errorLoanWrongState =>
      'This can\'t be done in the loan\'s current state.';

  @override
  String get errorLoanNotActive =>
      'Mobile money payments are only possible on active loans.';

  @override
  String get errorAmountExceedsOutstanding =>
      'The amount is more than what is still owed on this loan.';

  @override
  String get errorPhoneMethodMismatch =>
      'This phone number doesn\'t belong to the chosen wallet.';

  @override
  String get errorGeneric => 'Something went wrong. Please try again.';

  @override
  String dashboardHello(String name) {
    return 'Hello, $name';
  }

  @override
  String dashboardAsOf(String date) {
    return 'As of $date';
  }

  @override
  String get roleOfficer => 'Loan officer';

  @override
  String get roleManager => 'Branch manager';

  @override
  String get countActiveLoans => 'Active loans';

  @override
  String get countPending => 'Pending approval';

  @override
  String get countApprovedNotDisbursed => 'Approved, not disbursed';

  @override
  String get countInArrears => 'In arrears';

  @override
  String get countClients => 'Clients';

  @override
  String get principalOutstanding => 'Principal outstanding';

  @override
  String get totalOverdue => 'Total overdue';

  @override
  String get par30 => 'PAR30';

  @override
  String par30AtRisk(String amount) {
    return '$amount at risk';
  }

  @override
  String par1Line(String percent) {
    return 'PAR1: $percent';
  }

  @override
  String get parHealthy => 'Healthy';

  @override
  String get parWatch => 'Watch';

  @override
  String get parHigh => 'High risk';

  @override
  String get whatIsPar30 => 'What is PAR30?';

  @override
  String get par30ExplainTitle => 'Portfolio at risk (PAR30)';

  @override
  String get par30ExplainBody =>
      'PAR30 is the share of the principal still owed that sits in loans with a payment more than 30 days late.\n\nIt counts the WHOLE outstanding principal of each late loan, not only the late installment, because a loan that is a month behind is likely to fall further behind.\n\nMicrofinance institutions usually aim to stay below 5%. Between 5% and 10% needs watching; above 10% is high risk.';

  @override
  String get par30ExplainThresholds =>
      'Green below 5% · Amber 5–10% · Red above 10%';

  @override
  String get dueToday => 'Due today';

  @override
  String get overdue => 'Overdue';

  @override
  String sectionCount(int shown, int total) {
    return '$shown of $total';
  }

  @override
  String get nothingDueToday => 'Nothing due today.';

  @override
  String get nothingOverdue => 'No overdue installments. Well done!';

  @override
  String installmentNumber(int number) {
    return 'Installment $number';
  }

  @override
  String daysLate(int days) {
    String _temp0 = intl.Intl.pluralLogic(
      days,
      locale: localeName,
      other: '$days days late',
      one: '1 day late',
      zero: 'due today',
    );
    return '$_temp0';
  }

  @override
  String get officersTitle => 'By officer';

  @override
  String officerLine(int clients, int loans) {
    return '$clients clients · $loans active loans';
  }

  @override
  String officerOutstanding(String amount) {
    return 'Outstanding $amount';
  }

  @override
  String officerOverdue(String amount) {
    return 'Overdue $amount';
  }

  @override
  String get clientsTitle => 'Clients';

  @override
  String get searchClients => 'Search by name';

  @override
  String get searchCaseSensitive =>
      'Search is case-sensitive: \"Tek\" finds Tekle, \"tek\" does not.';

  @override
  String get noClients => 'No clients yet.';

  @override
  String noClientsFound(String query) {
    return 'No client name contains \"$query\".';
  }

  @override
  String clientsTotal(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count clients',
      one: '1 client',
    );
    return '$_temp0';
  }

  @override
  String get clientInactive => 'Inactive';

  @override
  String accountNumber(String number) {
    return 'Account $number';
  }

  @override
  String get clientDetailsTitle => 'Client';

  @override
  String get mobileNumber => 'Mobile';

  @override
  String get gender => 'Gender';

  @override
  String get dateOfBirth => 'Date of birth';

  @override
  String get nationalId => 'National ID';

  @override
  String get loanOfficer => 'Loan officer';

  @override
  String get office => 'Office';

  @override
  String get clientSince => 'Client since';

  @override
  String get loansTitle => 'Loans';

  @override
  String get noLoans => 'No loans yet.';

  @override
  String get loanPrincipal => 'Principal';

  @override
  String get loanOutstanding => 'Outstanding';

  @override
  String loanProgress(int paid, int total) {
    return '$paid of $total installments paid';
  }

  @override
  String loanNextDue(String date, String amount) {
    return 'Next due $date: $amount';
  }

  @override
  String get loanInArrears => 'In arrears';

  @override
  String get loanStatusPending => 'Pending approval';

  @override
  String get loanStatusApproved => 'Approved';

  @override
  String get loanStatusActive => 'Active';

  @override
  String get loanStatusClosed => 'Closed';

  @override
  String get loanStatusRejected => 'Rejected';

  @override
  String get loanStatusWithdrawn => 'Withdrawn';

  @override
  String get loanStatusWrittenOff => 'Written off';

  @override
  String get loanStatusRescheduled => 'Rescheduled';

  @override
  String get loanStatusOverpaid => 'Overpaid';

  @override
  String get loanStatusUnknown => 'Unknown';

  @override
  String get moreTitle => 'More';

  @override
  String get reconciliation => 'Reconciliation';

  @override
  String get settingsTitle => 'Settings';

  @override
  String signedInAs(String name) {
    return 'Signed in as $name';
  }

  @override
  String get logout => 'Sign out';

  @override
  String get logoutConfirmTitle => 'Sign out?';

  @override
  String get logoutConfirmBody =>
      'You\'ll need your password to sign in again.';

  @override
  String get serverSection => 'Server';

  @override
  String get serverUrlLabel => 'Backend URL';

  @override
  String get serverUrlHelp =>
      'Where the Adey backend runs. HTTPS, except plain http in debug builds.';

  @override
  String get serverPresetCodespace => 'Codespace';

  @override
  String get serverPresetEmulator => 'Android emulator (10.0.2.2:8080)';

  @override
  String get serverPresetLan => 'PC on this Wi-Fi';

  @override
  String get serverSaved => 'Server saved. Sign in again.';

  @override
  String get serverInvalidUrl =>
      'Enter a full URL, e.g. https://example.app.github.dev';

  @override
  String get serverHttpNotAllowed =>
      'Plain http:// is only allowed in debug builds. Use https://.';

  @override
  String get serverTest => 'Test connection';

  @override
  String get serverReachable => 'Server is reachable.';

  @override
  String get aboutSection => 'About';

  @override
  String appVersion(String version) {
    return 'Version $version';
  }
}
