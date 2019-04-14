package com.jamesjmtaylor.beaconsensor

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import timber.log.Timber

interface BluetoothServiceView {
    var bluetoothServiceIsBound: Boolean
    var bluetoothService: BluetoothService?
    var bluetoothServiceConnection: ServiceConnection?

    //MARK: Bluetooth calls
    fun enableBluetooth() {
        bluetoothService?.bluetoothAdapter
    }

    //MARK: Service connection creation
    fun bindService(activity: Activity?) {
        if (bluetoothServiceIsBound) return
        if (bluetoothServiceConnection == null) {
            createBluetoothServiceConnection()
        }
        val intent = Intent(activity, BluetoothService::class.java)
        activity?.startService(intent)
        bluetoothServiceConnection?.let { activity?.bindService(intent, it, Context.BIND_AUTO_CREATE) }
    }

    fun unbindService(activity: Activity?) {
        if (bluetoothServiceIsBound) {
            bluetoothServiceConnection?.let { activity?.unbindService(it) }
            bluetoothServiceIsBound = false
        }
    }

    private fun createBluetoothServiceConnection() {
        bluetoothServiceConnection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
                Timber.d("Disconnected")
                bluetoothServiceIsBound = false
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                Timber.d("Connected")
                val binder = service as BluetoothService.LocalBinder
                bluetoothService = binder.getService()
                bluetoothServiceIsBound = true
            }
        }
    }
}