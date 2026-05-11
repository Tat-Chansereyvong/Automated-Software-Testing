import 'package:flutter/material.dart';

class CounterWidget extends StatefulWidget {
  const CounterWidget({Key? key}) : super(key: key);

  @override
  State<CounterWidget> createState() => _CounterWidgetState();
}

class _CounterWidgetState extends State<CounterWidget> {
  int _count = 0;

  void _increment() => setState(() => _count++);
  void _decrement() => setState(() => _count--);

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Text(
          '$_count',
          key: const Key('counterText'),
          style: const TextStyle(fontSize: 24),
        ),
        const SizedBox(height: 8),
        Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            ElevatedButton(
              key: const Key('increment'),
              onPressed: _increment,
              child: const Text('+'),
            ),
            const SizedBox(width: 8),
            ElevatedButton(
              key: const Key('decrement'),
              onPressed: _decrement,
              child: const Text('-'),
            ),
          ],
        ),
      ],
    );
  }
}
