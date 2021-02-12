package com.jamesjmtaylor.beaconsensor

import android.app.Activity
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import kotlinx.android.synthetic.main.fragment_bluetooth_socket.view.*


class BluetoothSocketFragment : androidx.fragment.app.Fragment(), BluetoothServiceView {
    override var isBluetoothScanning: Boolean = false
    override var isBluetoothServiceBound: Boolean = false
    override var bluetoothService: BluetoothService? = null
    override var bluetoothServiceConnection: ServiceConnection? = null

    private var listener: OnListFragmentInteractionListener? = null
    var adapter: DeviceRecyclerViewAdapter? = null
    private var recyclerView: androidx.recyclerview.widget.RecyclerView? = null
    var connected = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindService(activity)
        enableBluetooth(activity)
        scanForDevices(true, scanCallback)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
            adapter = DeviceRecyclerViewAdapter(this, listener)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_bluetooth_socket, container, false)
        recyclerView = view.recyclerList
        if (recyclerView is androidx.recyclerview.widget.RecyclerView) { // Set the adapter
            recyclerView?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
            recyclerView?.adapter = adapter
        }
        return view
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(activity)
    }

    override val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Toast.makeText(activity, "Scan failed with error code $errorCode", LENGTH_LONG).show()
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let { adapter?.updateAdapterWithNewList(listOf(it)) }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results?.let { adapter?.updateAdapterWithNewList(it) }
        }
    }

    override val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_GATT_CONNECTED -> {
                    connected = true
                    updateConnectionState("Connected")
                    (context as? Activity)?.invalidateOptionsMenu()
                }
                ACTION_GATT_DISCONNECTED -> {
                    connected = false
                    updateConnectionState("Disconnected")
                    (context as? Activity)?.invalidateOptionsMenu()
                    clearUI()
                }
                ACTION_GATT_SERVICES_DISCOVERED -> {
                    // Show all the supported services and characteristics on the
                    // user interface.
                    //displayGattServices(bluetoothService.getSupportedGattServices())
                }
                ACTION_DATA_AVAILABLE -> displayData(intent.getStringExtra(EXTRA_DATA))
            }
        }
    }

    private fun displayData(stringExtra: String?) {

    }

    private fun clearUI() {

    }

    private fun updateConnectionState(connected: Any) {

    }

    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(item: ScanResult)
    }

    companion object {
        fun newInstance() = BluetoothSocketFragment().apply {
            arguments = Bundle().apply {}
        }
    }
}
