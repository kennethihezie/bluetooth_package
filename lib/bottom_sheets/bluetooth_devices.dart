import 'package:flutter/material.dart';
import 'package:flutter_bluetooth_collins/model/bluetooth.dart';

Future<void> showBluetoothDevices(List<BTDevice> bluetoothDevice,
    BuildContext context, Function(BTDevice) callback,
    {bool? isBusy, bool? isConnected}) async {
  return showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.only(
              topRight: Radius.circular(20), topLeft: Radius.circular(20))),
      builder: (context) {
        return Container(
          decoration: const BoxDecoration(
              borderRadius: BorderRadius.only(
                  topRight: Radius.circular(20), topLeft: Radius.circular(20))),
          child: Column(
            children: [
              const Padding(
                padding: EdgeInsets.only(top: 16.0, bottom: 32),
                child: Text(
                  "Bluetooth Devices",
                  style: TextStyle(fontWeight: FontWeight.w600, fontSize: 16),
                ),
              ),
              Flexible(
                  child: ListView.builder(
                itemBuilder: (context, index) {
                  return InkWell(
                    onTap: () async {
                      callback.call(bluetoothDevice[index]);
                    },
                    child: Column(
                      children: [
                        Row(
                          children: [
                            Column(
                              mainAxisAlignment: MainAxisAlignment.start,
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Padding(
                                  padding:
                                  const EdgeInsets.only(top: 4.0, left: 16),
                                  child: Text(
                                    bluetoothDevice[index].name ?? '',
                                    style: const TextStyle(
                                        fontWeight: FontWeight.w800),
                                  ),
                                ),
                                Padding(
                                  padding:
                                  const EdgeInsets.only(top: 4.0, left: 16),
                                  child:
                                  Text(bluetoothDevice[index].address ?? ''),
                                ),
                                // const Divider()
                              ],
                            ),

                            const Spacer(),

                            if (isBusy == true)
                              const SizedBox(
                                width: 25,
                                height: 25,
                                child: CircularProgressIndicator.adaptive(),
                              ),

                            isConnected == true ? const Icon(Icons.check_circle_outline, color: Colors.green,) : isConnected == false ? const Icon(Icons.cancel_outlined, color: Colors.redAccent,) : Container()
                          ],
                        ),

                        Divider()
                      ],
                    ),
                  );
                },
                itemCount: bluetoothDevice.length,
              ))
            ],
          ),
        );
      });
}
