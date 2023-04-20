package com.example.bluetooth_app

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import com.mazenrashed.printooth.Printooth
import com.mazenrashed.printooth.data.printable.ImagePrintable
import com.mazenrashed.printooth.data.printable.Printable
import com.mazenrashed.printooth.data.printable.RawPrintable
import com.mazenrashed.printooth.data.printable.TextPrintable
import com.mazenrashed.printooth.data.printer.DefaultPrinter
import com.mazenrashed.printooth.ui.ScanningActivity
import com.mazenrashed.printooth.utilities.Printing
import com.mazenrashed.printooth.utilities.PrintingCallback
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import net.glxn.qrgen.android.QRCode
import java.util.*

class MainActivity: FlutterActivity() {
    private var printing: Printing? = null

    companion object {
        private const val CHANNEL = "com.collins.bluetooth_app/bluetooth"
        private const val BLUETOOTH_PRINTER_REQUEST_CODE = 1003
    }

    @SuppressLint("MissingPermission")
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        Printooth.init(context)
        Printooth.removeCurrentPrinter()


        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
                call, result ->
            // This method is invoked on the main thread.
            when (call.method){
                "print" -> {
                    initListeners()
                }

                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    private fun initListeners() {
        if(!Printooth.hasPairedPrinter()){
            startActivityForResult(Intent(this@MainActivity, ScanningActivity::class.java), BLUETOOTH_PRINTER_REQUEST_CODE)
        } else {
            print()
        }

        printing?.printingCallback = object : PrintingCallback {
            override fun connectingWithPrinter() {
                Toast.makeText(this@MainActivity, "Connecting with printer", Toast.LENGTH_SHORT).show()
            }

            override fun connectionFailed(error: String) {
                Toast.makeText(this@MainActivity, "Failed to connect printer", Toast.LENGTH_SHORT).show()
            }

            override fun disconnected() {
                Toast.makeText(this@MainActivity, "Disconnected Printer", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: String) {
                Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
            }

            override fun onMessage(message: String) {
                Toast.makeText(this@MainActivity, "Message: $message", Toast.LENGTH_SHORT).show()
            }

            override fun printingOrderSentSuccessfully() {
                Toast.makeText(this@MainActivity, "Order sent to printer", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun print() {
        printing?.print(getSomePrintable())
    }

    /* Customize your printer here with text, logo and QR code */
    private fun getSomePrintable() = ArrayList<Printable>().apply {

        add(RawPrintable.Builder(byteArrayOf(27, 100, 4)).build()) // feed lines example in raw mode


        //logo
//            add(ImagePrintable.Builder(R.drawable.bold, resources)
//                    .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
//                    .build())


        add(
            TextPrintable.Builder()
                .setText("Printer")
                .setLineSpacing(DefaultPrinter.LINE_SPACING_60)
                .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
                .setFontSize(DefaultPrinter.FONT_SIZE_LARGE)
                .setEmphasizedMode(DefaultPrinter.EMPHASIZED_MODE_BOLD)
                .setUnderlined(DefaultPrinter.UNDERLINED_MODE_OFF)
                .setNewLinesAfter(1)
                .build())


        add(
            TextPrintable.Builder()
                .setText("TID: 1111123322" )
                .setCharacterCode(DefaultPrinter.CHARCODE_PC1252)
                .setNewLinesAfter(1)
                .build())

        add(
            TextPrintable.Builder()
                .setText("RRN: : 234566dfgg4456")
                .setCharacterCode(DefaultPrinter.CHARCODE_PC1252)
                .setNewLinesAfter(1)
                .build())

        add(
            TextPrintable.Builder()
                .setText("Amount: NGN$200,000")
                .setCharacterCode(DefaultPrinter.CHARCODE_PC1252)
                .setNewLinesAfter(2)
                .build())


        add(
            TextPrintable.Builder()
                .setText("APPROVED")
                .setLineSpacing(DefaultPrinter.LINE_SPACING_60)
                .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
                .setFontSize(DefaultPrinter.FONT_SIZE_LARGE)
                .setEmphasizedMode(DefaultPrinter.EMPHASIZED_MODE_BOLD)
                .setUnderlined(DefaultPrinter.UNDERLINED_MODE_OFF)
                .setNewLinesAfter(1)
                .build())


        add(
            TextPrintable.Builder()
                .setText("Transaction: Withdrawal")
                .setCharacterCode(DefaultPrinter.CHARCODE_PC1252)
                .setNewLinesAfter(1)
                .build())


        val qr: Bitmap = QRCode.from("RRN: : 234566dfgg4456\nAmount: NGN\$200,000\n")
            .withSize(200, 200).bitmap()

        add(
            ImagePrintable.Builder(qr)
                .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
                .build())


        add(TextPrintable.Builder()
            .setText("Hello World")
            .setLineSpacing(DefaultPrinter.LINE_SPACING_60)
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setEmphasizedMode(DefaultPrinter.EMPHASIZED_MODE_BOLD)
            .setUnderlined(DefaultPrinter.UNDERLINED_MODE_ON)
            .setNewLinesAfter(1)
            .build())

        add(TextPrintable.Builder()
            .setText("Hello World")
            .setAlignment(DefaultPrinter.ALIGNMENT_RIGHT)
            .setEmphasizedMode(DefaultPrinter.EMPHASIZED_MODE_BOLD)
            .setUnderlined(DefaultPrinter.UNDERLINED_MODE_ON)
            .setNewLinesAfter(1)
            .build())

        add(RawPrintable.Builder(byteArrayOf(27, 100, 4)).build())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            BLUETOOTH_PRINTER_REQUEST_CODE -> {
                if(Printooth.hasPairedPrinter())
                    printing = Printooth.printer()
            }
        }
    }
}