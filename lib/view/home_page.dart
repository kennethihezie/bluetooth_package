import 'dart:async';
import 'package:bluetooth_app/bottom_sheets/bluetooth_devices.dart';
import 'package:flutter_bluetooth_collins/esc_pos_print.dart';
import 'package:flutter_bluetooth_collins/model/bluetooth.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bluetooth_collins/flutter_bluetooth_collins.dart';

import '../bottom_sheets/stream_bluetooth_devices.dart';


class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});
  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  final flutterBluetoothCollins = FlutterBluetoothCollins();
  bool _isBusy = false;
  bool? _isConnected;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Center(child: ElevatedButton(onPressed: () async {
              final devices = await flutterBluetoothCollins.getPairedDevices();

              if(devices.isNotEmpty){
               await showBluetoothDevices(devices, context, (device) async {
                  setState(() {
                    _isBusy = true;
                  });
                  await flutterBluetoothCollins.connectDevice(device).then((value) {
                    setState(() {
                      _isConnected = value;
                      _isBusy = false;
                    });
                  });
                }, isBusy: _isBusy, isConnected: _isConnected);
              }
            }, child: const Text("Get Paired Devices"))),
          ),
          //
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Center(child: ElevatedButton(onPressed: () async {
              showStreamBluetoothDevices(await flutterBluetoothCollins.discoverDevices(), context, (btDevice){
                flutterBluetoothCollins.connectDevice(btDevice);
              }).then((value) => flutterBluetoothCollins.cancelDiscovery());
            }, child: const Text("Discover Devices"))),
          ),


          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Center(child: ElevatedButton(onPressed: () async {
              flutterBluetoothCollins.makeDeviceDiscoverable();
            }, child: const Text("Make Device Discoverable"))),
          ),

          // Padding(
          //   padding: const EdgeInsets.all(8.0),
          //   child: Center(child: ElevatedButton(onPressed: () async {
          //     EscPosPrint.printReceipt();
          //   }, child: const Text("Print Receipt"))),
          // ),
        ],
      ),

      // floatingActionButton: FloatingActionButton(onPressed: , child: const Icon(Icons.bluetooth),),
    );
  }
}
