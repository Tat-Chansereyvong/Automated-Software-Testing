import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_counter/counter.dart';

void main() {
  testWidgets('Counter increments and decrements', (WidgetTester tester) async {
    await tester.pumpWidget(
      const MaterialApp(home: Scaffold(body: CounterWidget())),
    );

    // initial value
    expect(find.byKey(const Key('counterText')), findsOneWidget);
    expect(find.text('0'), findsOneWidget);

    // increment twice
    await tester.tap(find.byKey(const Key('increment')));
    await tester.pump();
    expect(find.text('1'), findsOneWidget);

    await tester.tap(find.byKey(const Key('increment')));
    await tester.pump();
    expect(find.text('2'), findsOneWidget);

    // decrement once
    await tester.tap(find.byKey(const Key('decrement')));
    await tester.pump();
    expect(find.text('1'), findsOneWidget);
  });
}
