package com.jamesjmtaylor.beaconsensor

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class BluetoothSocketFragment : Fragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val intent = Intent(activity, BluetoothService::class.java)
        activity?.startService(intent)

        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bluetooth_socket, container, false)
    }


    companion object {
        fun newInstance() = BluetoothSocketFragment().apply {
            arguments = Bundle().apply {}
        }
    }
}
