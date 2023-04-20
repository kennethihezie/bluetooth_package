import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bluetooth_collins/flutter_bluetooth_collins.dart';

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});
  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  final flutterBluetoothCollins = FlutterBluetoothCollins();


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [

          //
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Center(child: ElevatedButton(onPressed: () async {
              await flutterBluetoothCollins.print();
            }, child: const Text("Print"))),
          ),
        ],
      ),
    );
  }
}
