package com.example.bluetooth_app

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Parcelable
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

class MainActivity: FlutterActivity() {
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private val _nearByDevices: MutableLiveData<MutableSet<BluetoothDevice>> = MutableLiveData()
    private var uuid: UUID? = null
    private var bluetoothDevice: BluetoothDevice? = null
    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when(intent?.action){
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        val devices = mutableSetOf<BluetoothDevice>()
                        _nearByDevices.value?.let { devices.addAll(it) }
                        devices.add(device)
                        _nearByDevices.value = devices
                    }
                }

                BluetoothDevice.ACTION_UUID -> {
                    // This is when we can be assured that fetchUuidsWithSdp has completed.
                    // So get the uuids and call fetchUuidsWithSdp on another device in list

                    val uuidExtra: Array<out Parcelable>? = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID)
                    Log.e("UUID_ID", "$uuidExtra")


                   // uuid = UUID.fromString(uuidExtra?.get(0)?.toString())

//                    if (bluetoothDevice != null) {
//                        CoroutineScope(Dispatchers.IO).launch {
//                            ConnectThreadX(bluetoothDevice!!).start()
//                        }
//                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.e("ACTION_DISCOVERY", "CALLED")
                }
            }
        }
    }
    private val bluetoothList: MutableSet<HashMap<String, Any>> = mutableSetOf()
    lateinit var  result: MethodChannel.Result

    companion object{
        private const val CHANNEL = "com.collins.bluetooth_app/bluetooth"
        private const val CHANNEL_STREAM = "com.collins.bluetooth_app/bluetooth_devices_stream"

        private const val REQUEST_ENABLE_BT = 1000
        private const val REQUEST_PERMISSION = 1001
        private const val REQUEST_DEVICE_DISCOVERABLE = 1002
        private const val BLUETOOTH_PRINTER_REQUEST_CODE = 1003

        private val btUuid = UUID.randomUUID()
    }

    @SuppressLint("MissingPermission")
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        _nearByDevices.value = mutableSetOf()

        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        val filterA = IntentFilter(BluetoothDevice.ACTION_UUID)
        val filterB = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

        registerReceiver(receiver, filter)
        registerReceiver(receiver, filterA)
        registerReceiver(receiver, filterB)


        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
                call, result ->
            this.result = result
            // This method is invoked on the main thread.
            when (call.method){
                "initBluetooth" -> {
                    initBluetooth()
                }

                "getPairedDevices" -> {
                    val bluetoothList = getPairedDevices()
                    result.success(bluetoothList)
                }

//                "discoverDevices" -> {
//                    discoverDevices()
//                    _nearByDevices.observe(this){
//                        Log.d("RECEIVER_DEVICE", "$it")
//                        val bluetoothList: MutableList<HashMap<String, String>> = mutableListOf()
//                        it?.forEach{ device ->
//                            bluetoothList.add(hashMapOf(Pair("deviceName", device.name), Pair("macAddress", device.address)))
//                        }
//                        //result.success(bluetoothList)
//                    }
//                }

                "makeDeviceDiscoverable" -> {
                    makeDeviceDiscoverable()
                }

                "connect" -> {
                    bluetoothAdapter?.cancelDiscovery()

                    val macAddress: String? = call.argument("address")
                    bluetoothDevice = bluetoothAdapter?.getRemoteDevice(macAddress)

                    if (bluetoothDevice != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            ConnectThreadX(bluetoothDevice!!).start()
                        }
                    }
                   // bluetoothDevice?.fetchUuidsWithSdp()
                }

                "cancelDiscovery" -> {
                    cancelDiscovery()
                }
                else -> {
                    result.notImplemented()
                }
            }
        }

        registerEventChannelStream()
    }

    private fun registerEventChannelStream(){
        val eventChannel = EventChannel(flutterEngine?.dartExecutor, CHANNEL_STREAM)
        eventChannel.setStreamHandler(object : EventChannel.StreamHandler {
            @SuppressLint("MissingPermission")
            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                //For all bluetooth device
                discoverDevices()
                //For BLE device
               // BleDevices().scanLeDevice()

                _nearByDevices.observe(this@MainActivity){
                   it?.forEach{ device ->
                       if(device.name != null){
                           bluetoothList.add(hashMapOf(Pair("name", device.name), Pair("address", device.address), Pair("type", device.type), Pair("connected", true))) }
                           events?.success(bluetoothList.toList())
                       }
                }
            }

            override fun onCancel(arguments: Any?) {

            }
        })
    }

    private fun initBluetooth(){
        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager?.adapter
        if(bluetoothAdapter != null){
            // Device support Bluetooth
            if(bluetoothAdapter?.isEnabled == false){
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(this, arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_ADVERTISE,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), REQUEST_PERMISSION)
                    return
                }
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }
        return
    }

    @SuppressLint("MissingPermission")
    private fun getPairedDevices(): List<HashMap<String, String>> {
        val pairedDevices = bluetoothAdapter?.bondedDevices
        val bluetoothList: MutableList<HashMap<String, String>> = mutableListOf()

        pairedDevices?.forEach{ device ->
            bluetoothList.add(hashMapOf(Pair("deviceName", device.name), Pair("macAddress", device.address)))
        }

        return bluetoothList
    }

    @SuppressLint("MissingPermission")
    private fun discoverDevices(){
        if(bluetoothAdapter?.startDiscovery() == true){
            Log.d("RECEIVER_DEVICE", "TRUE")
        } else {
            Log.d("RECEIVER_DEVICE", "FALSE")
        }
    }

    private fun makeDeviceDiscoverable(){
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        }
        startActivityForResult(discoverableIntent, REQUEST_DEVICE_DISCOVERABLE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_ENABLE_BT -> {
                if(resultCode == RESULT_OK){
                    Log.d("BLUE", "SUCCESS")
                }
            }

            REQUEST_PERMISSION -> {
                if(resultCode == RESULT_OK){
                    initBluetooth()
                }
            }

            REQUEST_DEVICE_DISCOVERABLE -> {
                //Make device available to connect
                CoroutineScope(Dispatchers.IO).launch {
                    AcceptThread().start()
                }
            }

            BLUETOOTH_PRINTER_REQUEST_CODE -> {
                val intent = data?.data
                Log.d("RESULT", "$intent")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
    }

    @SuppressLint("MissingPermission")
    private inner class AcceptThread : Thread(){
        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bluetoothAdapter?.listenUsingRfcommWithServiceRecord("bluetooth_app", btUuid)
        }

        override fun start() {
            //Keep listening until exception occurs or a socket is returned
            var shouldLoop = true
            while (shouldLoop){
                val socket: BluetoothSocket? = try{
                    mmServerSocket?.accept()
                } catch (e: IOException){
                    shouldLoop = false
                    null
                }
                socket?.also {
//                    manageMyConnectedSocket(it)
                    mmServerSocket?.close()
                    shouldLoop = false
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel(){
            try{
                mmServerSocket?.close()
            } catch (e: IOException){
                Log.e("ERROR", "Could not close the connect socket", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private inner class ConnectThread(device: BluetoothDevice) : Thread() {
        private val count = 0
        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("0000112f-0000-1000-8000-00805f9b34fb"))
        }

        override fun start() {
            // Cancel discovery because it otherwise slows down the connection.
            try {
                mmSocket?.connect()
                sleep(30000)
                if(mmSocket?.isConnected == true){
                    result.success(true)
//                    Log.d("CONNECT", "YES ITS CONNECTED")
                }
            } catch (e: IOException){
                Log.e("ERROR", "${e.message}")
                mmSocket?.close()
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e("ERROR", "Could not close the client socket", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private inner class ConnectThreadX(device: BluetoothDevice) {
        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(UUID.fromString("0000112f-0000-1000-8000-00805f9b34fb"))
        }

        fun start(){
            try {
                mmSocket?.connect()
                if(mmSocket?.isConnected == true){
                    result.success(true)
                }
            } catch (e: IOException){
                Log.e("ERROR", "${e.message}")
                //mmSocket?.close()
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun cancelDiscovery(){
        bluetoothAdapter?.cancelDiscovery()
    }

    private inner class BleDevices {
        private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
        private var scanning = false
        private val handler = Handler()
        private val SCAN_PERIOD: Long = 10000

        // Device scan callback.
        private val leScanCallback: ScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                val devices = mutableSetOf<BluetoothDevice>()
                _nearByDevices.value?.let { devices.addAll(it) }
                devices.add(result.device)
                _nearByDevices.value = devices
            }
        }

        @SuppressLint("MissingPermission")
        fun scanLeDevice() {
            if (!scanning) { // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    scanning = false
                    bluetoothLeScanner?.stopScan(leScanCallback)
                }, SCAN_PERIOD)
                scanning = true
                bluetoothLeScanner?.startScan(leScanCallback)
            } else {
                scanning = false
                bluetoothLeScanner?.stopScan(leScanCallback)
            }
        }
    }
}

/*
Caution: Performing device discovery consumes a lot of the Bluetooth
adapter's resources. After you have found a device to connect to, be
certain that you stop discovery with cancelDiscovery() before attempting
a connection. Also, you shouldn't perform discovery while connected to
a device because the discovery process significantly reduces the bandwidth
available for any existing connections.
 */