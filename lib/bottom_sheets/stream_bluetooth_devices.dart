import 'package:flutter/material.dart';
import 'package:flutter_bluetooth_collins/model/bluetooth.dart';

Future<void> showStreamBluetoothDevices(Stream<List<BTDevice>> stream,BuildContext context, Function(BTDevice) callback) async {
  return showModalBottomSheet(context: context,
      shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.only(topRight: Radius.circular(20), topLeft:  Radius.circular(20))
      ),      builder: (context){
        return Container(
          decoration: const BoxDecoration(
              borderRadius: BorderRadius.only(topRight: Radius.circular(20), topLeft:  Radius.circular(20))
          ),
          child: Column(
            children: [
              const Padding(
                padding: EdgeInsets.only(top: 16.0, bottom: 32),
                child: Text("Bluetooth Devices", style: TextStyle(fontWeight: FontWeight.w600, fontSize: 16),),
              ),

              StreamBuilder<List<BTDevice>>(
                stream: stream,
                initialData: [],
                builder: (context, snapshot){
                  final bluetoothDevice = snapshot.data;

                  if(bluetoothDevice?.isEmpty == true){
                    return const Center(child: CircularProgressIndicator.adaptive());
                  } else {
                   return Flexible(child: ListView.builder(itemBuilder: (context, index){
                      return InkWell(
                        onTap: (){
                          callback.call(bluetoothDevice[index]);
                        },
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.start,
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Padding(
                              padding: const EdgeInsets.only(top: 4.0, left: 16),
                              child: Text(bluetoothDevice![index].name ?? '', style: const TextStyle(fontWeight: FontWeight.w800),),
                            ),

                            Padding(
                              padding: const EdgeInsets.only(top: 4.0, left: 16),
                              child: Text(bluetoothDevice[index].address ?? ''),
                            ),

                            const Divider()
                          ],
                        ),
                      );
                    }, itemCount: bluetoothDevice?.length,));
                  }
                },
              )
            ],
          ),
        );
      });
}