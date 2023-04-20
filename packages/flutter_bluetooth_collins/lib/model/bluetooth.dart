class BTDevice {
  String? name;
  String? address;
  int? type;
  bool? connected;

  BTDevice({this.name, this.address, this.type, this.connected});

  @override
  String toString() {
    return 'BluetoothDevice{name: $name, address: $address, type:$type, connected:$connected}';
  }

  factory BTDevice.fromMap(Map<dynamic, dynamic> map) => BTDevice(name: map["name"], address: map["address"], type: map["type"], connected: map["connected"]);

  Map<String, dynamic> toMap(){
    return {
      'name': name,
      'address': address,
      'type': type,
      'connected': connected
    };
  }
}