package com.jamesjmtaylor.beaconsensor

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.content.*
import android.os.Handler
import android.os.IBinder

import timber.log.Timber
import java.lang.ref.WeakReference


interface BluetoothServiceView {
    private val scanDuration get() = 5000L
    var isBluetoothScanning: Boolean
    var isBluetoothServiceBound: Boolean
    val broadcastReceiver: BroadcastReceiver
    val scanCallback: ScanCallback

    //TESTING NOTE: Ideally these would be wrapper classes that contain a private instance of each
    //final object (BluetoothService, ServiceConnection, BluetoothGatt) so that you can provide
    //overriden mock objects for hermetic unit testing.
    var bluetoothService: BluetoothService?
    var bluetoothServiceConnection: ServiceConnection?

    fun adapter(): BluetoothAdapter {
        val bluetoothManager = App.instance.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.adapter
    }

    //MARK: Service deviceConnection creation
    fun bindService(activity: Activity?) {
        if (isBluetoothServiceBound) return
        if (bluetoothServiceConnection == null) {
            createBluetoothServiceConnection()
        }
        val intent = Intent(activity, BluetoothService::class.java)
        activity?.startService(intent)
        bluetoothServiceConnection?.let { activity?.bindService(intent, it, Context.BIND_AUTO_CREATE) }
    }

    fun unbindService(activity: Activity?) {
        if (isBluetoothServiceBound) {
            bluetoothServiceConnection?.let { activity?.unbindService(it) }
            isBluetoothServiceBound = false
        }
    }

    private fun createBluetoothServiceConnection() {
        bluetoothServiceConnection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
                Timber.d("Service disconnected")
                isBluetoothServiceBound = false
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                Timber.d("Service connected")
                val binder = service as BluetoothService.LocalBinder
                bluetoothService = binder.getService()
                isBluetoothServiceBound = true
            }
        }
    }

    //MARK: Bluetooth calls
    fun enableBluetooth(activity: Activity?) {
        if (!adapter().isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity?.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }

    fun scanForDevices(newScan: Boolean, scanCallback: ScanCallback) {
        if (newScan) {
            isBluetoothScanning = true
            Handler().postDelayed(
                    {
                        isBluetoothScanning = false
                        adapter().bluetoothLeScanner.stopScan(scanCallback)
                    }, scanDuration
            )
            isBluetoothScanning = true
            adapter().bluetoothLeScanner.startScan(scanCallback)

        } else {
            isBluetoothScanning = false
            adapter().bluetoothLeScanner.stopScan(scanCallback)
        }
    }

    fun createDeviceConnection(device: BluetoothDevice, gattCallback: BluetoothGattCallback) {
        bluetoothService?.let { service ->
            val connectedGatt = device.connectGatt(App.instance, true, gattCallback)
            val connection = BluetoothDeviceConnection(connectedGatt, WeakReference(service))
            service.deviceConnection = connection
        }

    }

}

