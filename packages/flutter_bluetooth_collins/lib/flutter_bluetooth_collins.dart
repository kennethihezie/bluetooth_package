library flutter_bluetooth_collins;

import 'package:flutter/services.dart';
import 'package:flutter_bluetooth_collins/model/bluetooth.dart';
import 'package:rxdart/rxdart.dart';

import 'esc_pos_print.dart';

class FlutterBluetoothCollins {
  late final MethodChannel platform;
  late final EventChannel eventChannel;
  final streamController = BehaviorSubject<List<BTDevice>>.seeded(const []);

  FlutterBluetoothCollins() {
    platform = const MethodChannel('com.collins.bluetooth_app/bluetooth');
    eventChannel = const EventChannel('com.collins.bluetooth_app/bluetooth_devices_stream');
    _initBluetooth();
  }

  Future<void> _initBluetooth() async {
    try {
      await platform.invokeMethod('initBluetooth');
    } on PlatformException catch (e) {
      print(e.message);
    }
  }

  Future<List<BTDevice>> getPairedDevices() async {
    try {
      final pairedDevices =
          (await platform.invokeMethod('getPairedDevices')) as List;
      List<BTDevice> devices = List.from(pairedDevices.map((e) => BTDevice.fromMap(e as Map<dynamic, dynamic>)));
      return devices;
    } on PlatformException catch (e) {
      print(e.message);
      return [];
    }
  }

  Future<bool> connectDevice(BTDevice device) async {
    // print(device);
    // EscPosPrint.printReceipt(device);
    // return false;

    try {
      bool? response = (await platform.invokeMethod("connect", device.toMap())) as bool?;
      // if(response == true) {
      //   EscPosPrint.printReceipt(device);
      // }
      return true;
    } on PlatformException catch (e) {
      e.message;
      return false;
    }
  }

  Future<void> cancelDiscovery() async {
    try {
      await platform.invokeMethod("cancelDiscovery");
    } on PlatformException catch (e) {
      e.message;
    }
  }

  Future<Stream<List<BTDevice>>> discoverDevices() async {
    final platformUpdate = eventChannel.receiveBroadcastStream();
    platformUpdate.listen((event) {
      final list = event as List;
      List<BTDevice> devices = List.from(list.map((e) => BTDevice.fromMap(e as Map<dynamic, dynamic>)));
      streamController.add(devices);
    });
    return streamController.asBroadcastStream();
  }

//
  Future<void> makeDeviceDiscoverable() async {
    try {
      await platform.invokeMethod('makeDeviceDiscoverable');
    } on PlatformException catch (e) {
      print(e.message);
    }
  }
}
