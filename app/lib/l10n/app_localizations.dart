import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:intl/intl.dart' as intl;

import 'app_localizations_en.dart';

// ignore_for_file: type=lint

/// Callers can lookup localized strings with an instance of AppLocalizations
/// returned by `AppLocalizations.of(context)`.
///
/// Applications need to include `AppLocalizations.delegate()` in their app's
/// `localizationDelegates` list, and the locales they support in the app's
/// `supportedLocales` list. For example:
///
/// ```dart
/// import 'l10n/app_localizations.dart';
///
/// return MaterialApp(
///   localizationsDelegates: AppLocalizations.localizationsDelegates,
///   supportedLocales: AppLocalizations.supportedLocales,
///   home: MyApplicationHome(),
/// );
/// ```
///
/// ## Update pubspec.yaml
///
/// Please make sure to update your pubspec.yaml to include the following
/// packages:
///
/// ```yaml
/// dependencies:
///   # Internationalization support.
///   flutter_localizations:
///     sdk: flutter
///   intl: any # Use the pinned version from flutter_localizations
///
///   # Rest of dependencies
/// ```
///
/// ## iOS Applications
///
/// iOS applications define key application metadata, including supported
/// locales, in an Info.plist file that is built into the application bundle.
/// To configure the locales supported by your app, you’ll need to edit this
/// file.
///
/// First, open your project’s ios/Runner.xcworkspace Xcode workspace file.
/// Then, in the Project Navigator, open the Info.plist file under the Runner
/// project’s Runner folder.
///
/// Next, select the Information Property List item, select Add Item from the
/// Editor menu, then select Localizations from the pop-up menu.
///
/// Select and expand the newly-created Localizations item then, for each
/// locale your application supports, add a new item and select the locale
/// you wish to add from the pop-up menu in the Value field. This list should
/// be consistent with the languages listed in the AppLocalizations.supportedLocales
/// property.
abstract class AppLocalizations {
  AppLocalizations(String locale)
    : localeName = intl.Intl.canonicalizedLocale(locale.toString());

  final String localeName;

  static AppLocalizations of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations)!;
  }

  static const LocalizationsDelegate<AppLocalizations> delegate =
      _AppLocalizationsDelegate();

  /// A list of this localizations delegate along with the default localizations
  /// delegates.
  ///
  /// Returns a list of localizations delegates containing this delegate along with
  /// GlobalMaterialLocalizations.delegate, GlobalCupertinoLocalizations.delegate,
  /// and GlobalWidgetsLocalizations.delegate.
  ///
  /// Additional delegates can be added by appending to this list in
  /// MaterialApp. This list does not have to be used at all if a custom list
  /// of delegates is preferred or required.
  static const List<LocalizationsDelegate<dynamic>> localizationsDelegates =
      <LocalizationsDelegate<dynamic>>[
        delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
      ];

  /// A list of this localizations delegate's supported locales.
  static const List<Locale> supportedLocales = <Locale>[Locale('en')];

  /// No description provided for @appTitle.
  ///
  /// In en, this message translates to:
  /// **'Adey Field'**
  String get appTitle;

  /// No description provided for @retry.
  ///
  /// In en, this message translates to:
  /// **'Try again'**
  String get retry;

  /// No description provided for @cancel.
  ///
  /// In en, this message translates to:
  /// **'Cancel'**
  String get cancel;

  /// No description provided for @save.
  ///
  /// In en, this message translates to:
  /// **'Save'**
  String get save;

  /// No description provided for @close.
  ///
  /// In en, this message translates to:
  /// **'Close'**
  String get close;

  /// No description provided for @comingSoonTitle.
  ///
  /// In en, this message translates to:
  /// **'Coming in the next version'**
  String get comingSoonTitle;

  /// No description provided for @comingSoonBody.
  ///
  /// In en, this message translates to:
  /// **'{feature} is planned for a later release of Adey Field.'**
  String comingSoonBody(String feature);

  /// No description provided for @notAvailable.
  ///
  /// In en, this message translates to:
  /// **'–'**
  String get notAvailable;

  /// No description provided for @tabDashboard.
  ///
  /// In en, this message translates to:
  /// **'Dashboard'**
  String get tabDashboard;

  /// No description provided for @tabClients.
  ///
  /// In en, this message translates to:
  /// **'Clients'**
  String get tabClients;

  /// No description provided for @tabCollections.
  ///
  /// In en, this message translates to:
  /// **'Collections'**
  String get tabCollections;

  /// No description provided for @tabApprovals.
  ///
  /// In en, this message translates to:
  /// **'Approvals'**
  String get tabApprovals;

  /// No description provided for @tabMore.
  ///
  /// In en, this message translates to:
  /// **'More'**
  String get tabMore;

  /// No description provided for @loginTitle.
  ///
  /// In en, this message translates to:
  /// **'Sign in'**
  String get loginTitle;

  /// No description provided for @loginSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Use your Adey MFI username and password.'**
  String get loginSubtitle;

  /// No description provided for @usernameLabel.
  ///
  /// In en, this message translates to:
  /// **'Username'**
  String get usernameLabel;

  /// No description provided for @passwordLabel.
  ///
  /// In en, this message translates to:
  /// **'Password'**
  String get passwordLabel;

  /// No description provided for @showPassword.
  ///
  /// In en, this message translates to:
  /// **'Show password'**
  String get showPassword;

  /// No description provided for @hidePassword.
  ///
  /// In en, this message translates to:
  /// **'Hide password'**
  String get hidePassword;

  /// No description provided for @signIn.
  ///
  /// In en, this message translates to:
  /// **'Sign in'**
  String get signIn;

  /// No description provided for @usernameRequired.
  ///
  /// In en, this message translates to:
  /// **'Enter your username'**
  String get usernameRequired;

  /// No description provided for @passwordRequired.
  ///
  /// In en, this message translates to:
  /// **'Enter your password'**
  String get passwordRequired;

  /// No description provided for @loginServer.
  ///
  /// In en, this message translates to:
  /// **'Server: {url}'**
  String loginServer(String url);

  /// No description provided for @changeServer.
  ///
  /// In en, this message translates to:
  /// **'Change'**
  String get changeServer;

  /// No description provided for @restoringSession.
  ///
  /// In en, this message translates to:
  /// **'Signing you in…'**
  String get restoringSession;

  /// No description provided for @errorInvalidCredentials.
  ///
  /// In en, this message translates to:
  /// **'Wrong username or password.'**
  String get errorInvalidCredentials;

  /// No description provided for @errorTooManyAttempts.
  ///
  /// In en, this message translates to:
  /// **'Too many failed sign-ins. Try again in {minutes, plural, =1{1 minute} other{{minutes} minutes}}.'**
  String errorTooManyAttempts(int minutes);

  /// No description provided for @errorTooManyAttemptsNoTime.
  ///
  /// In en, this message translates to:
  /// **'Too many failed sign-ins. Wait a few minutes and try again.'**
  String get errorTooManyAttemptsNoTime;

  /// No description provided for @errorNotOfficer.
  ///
  /// In en, this message translates to:
  /// **'This account is not a loan officer or branch manager account. Ask your branch manager to set it up.'**
  String get errorNotOfficer;

  /// No description provided for @errorSessionExpired.
  ///
  /// In en, this message translates to:
  /// **'Your session has ended. Please sign in again.'**
  String get errorSessionExpired;

  /// No description provided for @errorForbidden.
  ///
  /// In en, this message translates to:
  /// **'You don\'t have access to this.'**
  String get errorForbidden;

  /// No description provided for @errorNotFound.
  ///
  /// In en, this message translates to:
  /// **'Not found. It may have been removed.'**
  String get errorNotFound;

  /// No description provided for @errorValidation.
  ///
  /// In en, this message translates to:
  /// **'Some fields are not valid. Please check them.'**
  String get errorValidation;

  /// No description provided for @errorNetwork.
  ///
  /// In en, this message translates to:
  /// **'Can\'t reach the server. Check your internet connection and try again.'**
  String get errorNetwork;

  /// No description provided for @errorServerPrivate.
  ///
  /// In en, this message translates to:
  /// **'The server is not reachable from this phone (the Codespace port is private). Ask for it to be made public, or change the server in Settings.'**
  String get errorServerPrivate;

  /// No description provided for @errorUnexpectedResponse.
  ///
  /// In en, this message translates to:
  /// **'The server gave an answer this app doesn\'t understand. Check the server address in Settings.'**
  String get errorUnexpectedResponse;

  /// No description provided for @errorFineractUnavailable.
  ///
  /// In en, this message translates to:
  /// **'The core banking system is not responding. Try again in a minute.'**
  String get errorFineractUnavailable;

  /// No description provided for @errorFineractError.
  ///
  /// In en, this message translates to:
  /// **'The core banking system reported a problem. Try again later.'**
  String get errorFineractError;

  /// No description provided for @errorInternal.
  ///
  /// In en, this message translates to:
  /// **'Something went wrong on the server. Try again later.'**
  String get errorInternal;

  /// No description provided for @errorProviderUnavailable.
  ///
  /// In en, this message translates to:
  /// **'The mobile money provider can\'t be reached right now. Try again later.'**
  String get errorProviderUnavailable;

  /// No description provided for @errorIdempotencyInProgress.
  ///
  /// In en, this message translates to:
  /// **'Your previous attempt is still being processed. Wait a moment, then try again.'**
  String get errorIdempotencyInProgress;

  /// No description provided for @errorIdempotencyOutcomeUnknown.
  ///
  /// In en, this message translates to:
  /// **'We couldn\'t confirm whether your previous attempt went through. Check the loan before trying again.'**
  String get errorIdempotencyOutcomeUnknown;

  /// No description provided for @errorIdempotencyReused.
  ///
  /// In en, this message translates to:
  /// **'This request was already sent with different details. Start again.'**
  String get errorIdempotencyReused;

  /// No description provided for @errorOpenLoanExists.
  ///
  /// In en, this message translates to:
  /// **'This client already has a pending, approved or active loan.'**
  String get errorOpenLoanExists;

  /// No description provided for @errorDuplicateClient.
  ///
  /// In en, this message translates to:
  /// **'Another client already uses this mobile number or national ID.'**
  String get errorDuplicateClient;

  /// No description provided for @errorLoanWrongState.
  ///
  /// In en, this message translates to:
  /// **'This can\'t be done in the loan\'s current state.'**
  String get errorLoanWrongState;

  /// No description provided for @errorLoanNotActive.
  ///
  /// In en, this message translates to:
  /// **'Mobile money payments are only possible on active loans.'**
  String get errorLoanNotActive;

  /// No description provided for @errorAmountExceedsOutstanding.
  ///
  /// In en, this message translates to:
  /// **'The amount is more than what is still owed on this loan.'**
  String get errorAmountExceedsOutstanding;

  /// No description provided for @errorPhoneMethodMismatch.
  ///
  /// In en, this message translates to:
  /// **'This phone number doesn\'t belong to the chosen wallet.'**
  String get errorPhoneMethodMismatch;

  /// No description provided for @errorGeneric.
  ///
  /// In en, this message translates to:
  /// **'Something went wrong. Please try again.'**
  String get errorGeneric;

  /// No description provided for @dashboardHello.
  ///
  /// In en, this message translates to:
  /// **'Hello, {name}'**
  String dashboardHello(String name);

  /// No description provided for @dashboardAsOf.
  ///
  /// In en, this message translates to:
  /// **'As of {date}'**
  String dashboardAsOf(String date);

  /// No description provided for @roleOfficer.
  ///
  /// In en, this message translates to:
  /// **'Loan officer'**
  String get roleOfficer;

  /// No description provided for @roleManager.
  ///
  /// In en, this message translates to:
  /// **'Branch manager'**
  String get roleManager;

  /// No description provided for @countActiveLoans.
  ///
  /// In en, this message translates to:
  /// **'Active loans'**
  String get countActiveLoans;

  /// No description provided for @countPending.
  ///
  /// In en, this message translates to:
  /// **'Pending approval'**
  String get countPending;

  /// No description provided for @countApprovedNotDisbursed.
  ///
  /// In en, this message translates to:
  /// **'Approved, not disbursed'**
  String get countApprovedNotDisbursed;

  /// No description provided for @countInArrears.
  ///
  /// In en, this message translates to:
  /// **'In arrears'**
  String get countInArrears;

  /// No description provided for @countClients.
  ///
  /// In en, this message translates to:
  /// **'Clients'**
  String get countClients;

  /// No description provided for @principalOutstanding.
  ///
  /// In en, this message translates to:
  /// **'Principal outstanding'**
  String get principalOutstanding;

  /// No description provided for @totalOverdue.
  ///
  /// In en, this message translates to:
  /// **'Total overdue'**
  String get totalOverdue;

  /// No description provided for @par30.
  ///
  /// In en, this message translates to:
  /// **'PAR30'**
  String get par30;

  /// No description provided for @par30AtRisk.
  ///
  /// In en, this message translates to:
  /// **'{amount} at risk'**
  String par30AtRisk(String amount);

  /// No description provided for @par1Line.
  ///
  /// In en, this message translates to:
  /// **'PAR1: {percent}'**
  String par1Line(String percent);

  /// No description provided for @parHealthy.
  ///
  /// In en, this message translates to:
  /// **'Healthy'**
  String get parHealthy;

  /// No description provided for @parWatch.
  ///
  /// In en, this message translates to:
  /// **'Watch'**
  String get parWatch;

  /// No description provided for @parHigh.
  ///
  /// In en, this message translates to:
  /// **'High risk'**
  String get parHigh;

  /// No description provided for @whatIsPar30.
  ///
  /// In en, this message translates to:
  /// **'What is PAR30?'**
  String get whatIsPar30;

  /// No description provided for @par30ExplainTitle.
  ///
  /// In en, this message translates to:
  /// **'Portfolio at risk (PAR30)'**
  String get par30ExplainTitle;

  /// No description provided for @par30ExplainBody.
  ///
  /// In en, this message translates to:
  /// **'PAR30 is the share of the principal still owed that sits in loans with a payment more than 30 days late.\n\nIt counts the WHOLE outstanding principal of each late loan, not only the late installment, because a loan that is a month behind is likely to fall further behind.\n\nMicrofinance institutions usually aim to stay below 5%. Between 5% and 10% needs watching; above 10% is high risk.'**
  String get par30ExplainBody;

  /// No description provided for @par30ExplainThresholds.
  ///
  /// In en, this message translates to:
  /// **'Green below 5% · Amber 5–10% · Red above 10%'**
  String get par30ExplainThresholds;

  /// No description provided for @dueToday.
  ///
  /// In en, this message translates to:
  /// **'Due today'**
  String get dueToday;

  /// No description provided for @overdue.
  ///
  /// In en, this message translates to:
  /// **'Overdue'**
  String get overdue;

  /// No description provided for @sectionCount.
  ///
  /// In en, this message translates to:
  /// **'{shown} of {total}'**
  String sectionCount(int shown, int total);

  /// No description provided for @nothingDueToday.
  ///
  /// In en, this message translates to:
  /// **'Nothing due today.'**
  String get nothingDueToday;

  /// No description provided for @nothingOverdue.
  ///
  /// In en, this message translates to:
  /// **'No overdue installments. Well done!'**
  String get nothingOverdue;

  /// No description provided for @installmentNumber.
  ///
  /// In en, this message translates to:
  /// **'Installment {number}'**
  String installmentNumber(int number);

  /// No description provided for @daysLate.
  ///
  /// In en, this message translates to:
  /// **'{days, plural, =0{due today} =1{1 day late} other{{days} days late}}'**
  String daysLate(int days);

  /// No description provided for @officersTitle.
  ///
  /// In en, this message translates to:
  /// **'By officer'**
  String get officersTitle;

  /// No description provided for @officerLine.
  ///
  /// In en, this message translates to:
  /// **'{clients} clients · {loans} active loans'**
  String officerLine(int clients, int loans);

  /// No description provided for @officerOutstanding.
  ///
  /// In en, this message translates to:
  /// **'Outstanding {amount}'**
  String officerOutstanding(String amount);

  /// No description provided for @officerOverdue.
  ///
  /// In en, this message translates to:
  /// **'Overdue {amount}'**
  String officerOverdue(String amount);

  /// No description provided for @clientsTitle.
  ///
  /// In en, this message translates to:
  /// **'Clients'**
  String get clientsTitle;

  /// No description provided for @searchClients.
  ///
  /// In en, this message translates to:
  /// **'Search by name'**
  String get searchClients;

  /// No description provided for @searchCaseSensitive.
  ///
  /// In en, this message translates to:
  /// **'Search is case-sensitive: \"Tek\" finds Tekle, \"tek\" does not.'**
  String get searchCaseSensitive;

  /// No description provided for @noClients.
  ///
  /// In en, this message translates to:
  /// **'No clients yet.'**
  String get noClients;

  /// No description provided for @noClientsFound.
  ///
  /// In en, this message translates to:
  /// **'No client name contains \"{query}\".'**
  String noClientsFound(String query);

  /// No description provided for @clientsTotal.
  ///
  /// In en, this message translates to:
  /// **'{count, plural, =1{1 client} other{{count} clients}}'**
  String clientsTotal(int count);

  /// No description provided for @clientInactive.
  ///
  /// In en, this message translates to:
  /// **'Inactive'**
  String get clientInactive;

  /// No description provided for @accountNumber.
  ///
  /// In en, this message translates to:
  /// **'Account {number}'**
  String accountNumber(String number);

  /// No description provided for @clientDetailsTitle.
  ///
  /// In en, this message translates to:
  /// **'Client'**
  String get clientDetailsTitle;

  /// No description provided for @mobileNumber.
  ///
  /// In en, this message translates to:
  /// **'Mobile'**
  String get mobileNumber;

  /// No description provided for @gender.
  ///
  /// In en, this message translates to:
  /// **'Gender'**
  String get gender;

  /// No description provided for @dateOfBirth.
  ///
  /// In en, this message translates to:
  /// **'Date of birth'**
  String get dateOfBirth;

  /// No description provided for @nationalId.
  ///
  /// In en, this message translates to:
  /// **'National ID'**
  String get nationalId;

  /// No description provided for @loanOfficer.
  ///
  /// In en, this message translates to:
  /// **'Loan officer'**
  String get loanOfficer;

  /// No description provided for @office.
  ///
  /// In en, this message translates to:
  /// **'Office'**
  String get office;

  /// No description provided for @clientSince.
  ///
  /// In en, this message translates to:
  /// **'Client since'**
  String get clientSince;

  /// No description provided for @loansTitle.
  ///
  /// In en, this message translates to:
  /// **'Loans'**
  String get loansTitle;

  /// No description provided for @noLoans.
  ///
  /// In en, this message translates to:
  /// **'No loans yet.'**
  String get noLoans;

  /// No description provided for @loanPrincipal.
  ///
  /// In en, this message translates to:
  /// **'Principal'**
  String get loanPrincipal;

  /// No description provided for @loanOutstanding.
  ///
  /// In en, this message translates to:
  /// **'Outstanding'**
  String get loanOutstanding;

  /// No description provided for @loanProgress.
  ///
  /// In en, this message translates to:
  /// **'{paid} of {total} installments paid'**
  String loanProgress(int paid, int total);

  /// No description provided for @loanNextDue.
  ///
  /// In en, this message translates to:
  /// **'Next due {date}: {amount}'**
  String loanNextDue(String date, String amount);

  /// No description provided for @loanInArrears.
  ///
  /// In en, this message translates to:
  /// **'In arrears'**
  String get loanInArrears;

  /// No description provided for @loanStatusPending.
  ///
  /// In en, this message translates to:
  /// **'Pending approval'**
  String get loanStatusPending;

  /// No description provided for @loanStatusApproved.
  ///
  /// In en, this message translates to:
  /// **'Approved'**
  String get loanStatusApproved;

  /// No description provided for @loanStatusActive.
  ///
  /// In en, this message translates to:
  /// **'Active'**
  String get loanStatusActive;

  /// No description provided for @loanStatusClosed.
  ///
  /// In en, this message translates to:
  /// **'Closed'**
  String get loanStatusClosed;

  /// No description provided for @loanStatusRejected.
  ///
  /// In en, this message translates to:
  /// **'Rejected'**
  String get loanStatusRejected;

  /// No description provided for @loanStatusWithdrawn.
  ///
  /// In en, this message translates to:
  /// **'Withdrawn'**
  String get loanStatusWithdrawn;

  /// No description provided for @loanStatusWrittenOff.
  ///
  /// In en, this message translates to:
  /// **'Written off'**
  String get loanStatusWrittenOff;

  /// No description provided for @loanStatusRescheduled.
  ///
  /// In en, this message translates to:
  /// **'Rescheduled'**
  String get loanStatusRescheduled;

  /// No description provided for @loanStatusOverpaid.
  ///
  /// In en, this message translates to:
  /// **'Overpaid'**
  String get loanStatusOverpaid;

  /// No description provided for @loanStatusUnknown.
  ///
  /// In en, this message translates to:
  /// **'Unknown'**
  String get loanStatusUnknown;

  /// No description provided for @moreTitle.
  ///
  /// In en, this message translates to:
  /// **'More'**
  String get moreTitle;

  /// No description provided for @reconciliation.
  ///
  /// In en, this message translates to:
  /// **'Reconciliation'**
  String get reconciliation;

  /// No description provided for @settingsTitle.
  ///
  /// In en, this message translates to:
  /// **'Settings'**
  String get settingsTitle;

  /// No description provided for @signedInAs.
  ///
  /// In en, this message translates to:
  /// **'Signed in as {name}'**
  String signedInAs(String name);

  /// No description provided for @logout.
  ///
  /// In en, this message translates to:
  /// **'Sign out'**
  String get logout;

  /// No description provided for @logoutConfirmTitle.
  ///
  /// In en, this message translates to:
  /// **'Sign out?'**
  String get logoutConfirmTitle;

  /// No description provided for @logoutConfirmBody.
  ///
  /// In en, this message translates to:
  /// **'You\'ll need your password to sign in again.'**
  String get logoutConfirmBody;

  /// No description provided for @serverSection.
  ///
  /// In en, this message translates to:
  /// **'Server'**
  String get serverSection;

  /// No description provided for @serverUrlLabel.
  ///
  /// In en, this message translates to:
  /// **'Backend URL'**
  String get serverUrlLabel;

  /// No description provided for @serverUrlHelp.
  ///
  /// In en, this message translates to:
  /// **'Where the Adey backend runs. HTTPS, except plain http in debug builds.'**
  String get serverUrlHelp;

  /// No description provided for @serverPresetCodespace.
  ///
  /// In en, this message translates to:
  /// **'Codespace'**
  String get serverPresetCodespace;

  /// No description provided for @serverPresetUsb.
  ///
  /// In en, this message translates to:
  /// **'USB cable (adb reverse, 127.0.0.1:8080)'**
  String get serverPresetUsb;

  /// No description provided for @serverPresetEmulator.
  ///
  /// In en, this message translates to:
  /// **'Android emulator (10.0.2.2:8080)'**
  String get serverPresetEmulator;

  /// No description provided for @serverPresetLan.
  ///
  /// In en, this message translates to:
  /// **'PC on this Wi-Fi'**
  String get serverPresetLan;

  /// No description provided for @serverSaved.
  ///
  /// In en, this message translates to:
  /// **'Server saved. Sign in again.'**
  String get serverSaved;

  /// No description provided for @serverInvalidUrl.
  ///
  /// In en, this message translates to:
  /// **'Enter a full URL, e.g. https://example.app.github.dev'**
  String get serverInvalidUrl;

  /// No description provided for @serverHttpNotAllowed.
  ///
  /// In en, this message translates to:
  /// **'Plain http:// is only allowed in debug builds. Use https://.'**
  String get serverHttpNotAllowed;

  /// No description provided for @serverTest.
  ///
  /// In en, this message translates to:
  /// **'Test connection'**
  String get serverTest;

  /// No description provided for @serverReachable.
  ///
  /// In en, this message translates to:
  /// **'Server is reachable.'**
  String get serverReachable;

  /// No description provided for @aboutSection.
  ///
  /// In en, this message translates to:
  /// **'About'**
  String get aboutSection;

  /// No description provided for @appVersion.
  ///
  /// In en, this message translates to:
  /// **'Version {version}'**
  String appVersion(String version);
}

class _AppLocalizationsDelegate
    extends LocalizationsDelegate<AppLocalizations> {
  const _AppLocalizationsDelegate();

  @override
  Future<AppLocalizations> load(Locale locale) {
    return SynchronousFuture<AppLocalizations>(lookupAppLocalizations(locale));
  }

  @override
  bool isSupported(Locale locale) =>
      <String>['en'].contains(locale.languageCode);

  @override
  bool shouldReload(_AppLocalizationsDelegate old) => false;
}

AppLocalizations lookupAppLocalizations(Locale locale) {
  // Lookup logic when only language code is specified.
  switch (locale.languageCode) {
    case 'en':
      return AppLocalizationsEn();
  }

  throw FlutterError(
    'AppLocalizations.delegate failed to load unsupported locale "$locale". This is likely '
    'an issue with the localizations generation tool. Please file an issue '
    'on GitHub with a reproducible sample app and the gen-l10n configuration '
    'that was used.',
  );
}
