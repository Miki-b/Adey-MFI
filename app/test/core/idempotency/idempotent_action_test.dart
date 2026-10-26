import 'package:adey_field/core/errors/api_error.dart';
import 'package:adey_field/core/idempotency/idempotent_action.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  late IdempotentAction action;
  late int counter;

  setUp(() {
    counter = 0;
    action = IdempotentAction(newKey: () => 'key-${++counter}');
  });

  ApiError backend(String code) => ApiError(kind: ApiErrorKind.backend, code: code);
  const network = ApiError(kind: ApiErrorKind.network);

  test('the same request reuses the key until it succeeds; the next action gets a new key', () async {
    final sent = <String>[];
    Future<String> failOnce(String key) async {
      sent.add(key);
      if (sent.length == 1) throw network; // answer lost on the way back
      return 'ok';
    }

    await expectLater(action.run('repay 1833.60', failOnce), throwsA(isA<ApiError>()));
    expect(await action.run('repay 1833.60', failOnce), 'ok');
    expect(sent, ['key-1', 'key-1']); // the retry carried the SAME key: no double payment
    expect(action.currentKey, isNull);

    await action.run('repay 500.00', (key) async => sent.add(key));
    expect(sent.last, 'key-2');
  });

  test('changing the request (user edited the amount) is a new action with a new key', () {
    expect(action.keyFor('repay 1833.60'), 'key-1');
    expect(action.keyFor('repay 1833.60'), 'key-1');
    expect(action.keyFor('repay 1800.00'), 'key-2');
  });

  test('keeps the key while the outcome is unknown or still running', () async {
    for (final error in [network, backend('IDEMPOTENCY_IN_PROGRESS'), backend('FINERACT_UNAVAILABLE')]) {
      await expectLater(action.run('x', (_) async => throw error), throwsA(isA<ApiError>()));
      expect(action.currentKey, 'key-1', reason: '$error');
    }
  });

  test('forgets the key when the backend will never accept it again', () async {
    for (final code in ['IDEMPOTENCY_OUTCOME_UNKNOWN', 'IDEMPOTENCY_KEY_REUSED']) {
      action.reset();
      await expectLater(action.run('x', (_) async => throw backend(code)), throwsA(isA<ApiError>()));
      expect(action.currentKey, isNull, reason: code);
      expect(action.keyFor('x'), isNot('key-1'));
    }
  });

  test('failures are rethrown as ApiError', () async {
    await expectLater(
      action.run('x', (_) async => throw StateError('boom')),
      throwsA(isA<ApiError>().having((e) => e.kind, 'kind', ApiErrorKind.unknown)),
    );
  });

  test('default keys are UUIDs (valid for the backend: 8-100 chars of [A-Za-z0-9_-])', () {
    final key = IdempotentAction().keyFor('x');
    expect(RegExp(r'^[A-Za-z0-9_-]{8,100}$').hasMatch(key), isTrue);
  });
}
