package com.jamesjmtaylor.beaconsensor

import android.content.ServiceConnection
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class BluetoothSocketFragment : Fragment(), BluetoothServiceView {
    override var bluetoothServiceIsBound: Boolean = false
    override var bluetoothService: BluetoothService? = null
    override var bluetoothServiceConnection: ServiceConnection? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        bindService(activity)
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bluetooth_socket, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(activity)
    }


    companion object {
        fun newInstance() = BluetoothSocketFragment().apply {
            arguments = Bundle().apply {}
        }
    }
}
