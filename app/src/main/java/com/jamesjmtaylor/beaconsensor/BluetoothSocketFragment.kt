package com.jamesjmtaylor.beaconsensor

import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import timber.log.Timber


class BluetoothSocketFragment : Fragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val intent = Intent(activity, BluetoothService::class.java)

        activity?.startService(intent)
        activity?.bindService(intent, bluetoothServiceConnection, BIND_AUTO_CREATE)

        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bluetooth_socket, container, false)
    }

    var bluetoothServiceIsBound = false
    var bluetoothService: BluetoothService? = null
    val bluetoothServiceConnection: ServiceConnection = object : ServiceConnection {
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

    companion object {
        fun newInstance() = BluetoothSocketFragment().apply {
            arguments = Bundle().apply {}
        }
    }
}
