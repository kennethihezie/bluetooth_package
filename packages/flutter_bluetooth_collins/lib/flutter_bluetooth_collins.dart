library flutter_bluetooth_collins;

import 'package:flutter/services.dart';


class FlutterBluetoothCollins {
  late final MethodChannel platform;

  FlutterBluetoothCollins() {
    platform = const MethodChannel('com.collins.bluetooth_app/bluetooth');
  }

  Future<void> print() async {
    try {
      await platform.invokeMethod('print');
    } on PlatformException catch (e) {}
  }
}
