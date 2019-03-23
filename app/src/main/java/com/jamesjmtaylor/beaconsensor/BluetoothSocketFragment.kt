package com.jamesjmtaylor.beaconsensor

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import timber.log.Timber
import java.io.IOException
import java.nio.charset.Charset
import java.util.*


class BluetoothSocketFragment : Fragment() {
    var mmSocket: BluetoothSocket? = null
    var mmDevice: BluetoothDevice? = null

    val delimiter: Byte = 33
    var readBufferPosition = 0

    fun sendBtMsg(msg2send: String) {
        //UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        val uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee") //Standard SerialPortService ID
        try {
            mmSocket = mmDevice!!.createRfcommSocketToServiceRecord(uuid)
            if (mmSocket?.isConnected == false) {
                mmSocket?.connect()
            }
            val mmOutputStream = mmSocket?.outputStream //msg += "\n";
            mmOutputStream?.write(msg2send.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
        val handler = Handler()

        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()



        if (!mBluetoothAdapter.isEnabled()) {
            val enableBluetooth = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        val pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.isNotEmpty()) {
            for (device in pairedDevices) {
                if (device.getName().equals("raspberrypi-0")) //Needs to match the name of the device
                {
                    Timber.e("Found device: $device")
                    mmDevice = device
                    break
                }
            }
        }

        class workerThread(private val btMsg: String) : Runnable {
            override fun run() {
                sendBtMsg(btMsg)
                while (!Thread.currentThread().isInterrupted) {
                    val bytesAvailable: Int
                    var workDone = false
                    try {
                        val mmInputStream = mmSocket?.inputStream
                        bytesAvailable = mmInputStream?.available() ?: 0
                        if (bytesAvailable > 0) {
                            val packetBytes = ByteArray(bytesAvailable)
                            Timber.e("Recieved bytes")
                            val readBuffer = ByteArray(1024)
                            mmInputStream?.read(packetBytes)

                            for (i in 0 until bytesAvailable) {
                                val b = packetBytes[i]
                                if (b == delimiter) {
                                    val encodedBytes = ByteArray(readBufferPosition)
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.size)
                                    val data = encodedBytes.toString(Charset.forName("US-ASCII"))
                                    readBufferPosition = 0

                                    //The variable data now contains our full command
                                    handler.post {
                                        //Update UI here
                                    }
                                    workDone = true; break
                                } else {
                                    readBuffer[readBufferPosition++] = b
                                }
                            }
                            if (workDone == true) {
                                mmSocket?.close(); break
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }

        fun tempClickHandler(v: View) {// Perform action on temp button click
            Thread(workerThread("temp")).start()
        }

        fun lightOnClickHandler(v: View) {// Perform action on temp button click
            Thread(workerThread("lightOn")).start()
        }

        fun lightOffClickHandler(v: View) {// Perform action on temp button click
            Thread(workerThread("lightOff")).start()
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bluetooth_socket, container, false)
    }


    companion object {
        fun newInstance() =
                BluetoothSocketFragment().apply {
                    arguments = Bundle().apply {}
                }
    }


}
