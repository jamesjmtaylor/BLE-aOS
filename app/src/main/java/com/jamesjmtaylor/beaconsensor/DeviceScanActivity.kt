package com.jamesjmtaylor.beaconsensor

import android.app.Activity
import android.app.ListActivity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import timber.log.Timber


class DeviceScanActivity : ListActivity() {

    private var mLeDeviceListAdapter: LeDeviceListAdapter? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mScanning: Boolean = false
    private var mHandler: Handler? = null

    private val REQUEST_ENABLE_BT = 1
    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.setTitle("Devices")
        mHandler = Handler()

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false)
            menu.findItem(R.id.menu_scan).setVisible(true)
            menu.findItem(R.id.menu_refresh).setActionView(null)
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true)
            menu.findItem(R.id.menu_scan).setVisible(false)
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.menu_scan -> {
                mLeDeviceListAdapter!!.clear()
                scanLeDevice(true)
            }
            R.id.menu_stop -> scanLeDevice(false)
        }
        return true
    }

    override fun onResume() {
        super.onResume()

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter!!.isEnabled) {
            if (!mBluetoothAdapter!!.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = LeDeviceListAdapter()
        setListAdapter(mLeDeviceListAdapter)
        scanLeDevice(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish()
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        super.onPause()
        scanLeDevice(false)
        mLeDeviceListAdapter!!.clear()
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        val device = mLeDeviceListAdapter!!.getDevice(position) ?: return
        val intent = Intent(this, DeviceControlActivity::class.java)
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.name)
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.address)
        if (mScanning) {
            mBluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
            mScanning = false
        }
        startActivity(intent)
    }

    private fun scanLeDevice(enable: Boolean) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler!!.postDelayed({
                mScanning = false
                mBluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
                invalidateOptionsMenu()
            }, SCAN_PERIOD)

            mScanning = true
            mBluetoothAdapter?.bluetoothLeScanner?.startScan(mLeScanCallback)
        } else {
            mScanning = false
            mBluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
        }
        invalidateOptionsMenu()
    }

    // Adapter for holding devices found through scanning.
    private inner class LeDeviceListAdapter : BaseAdapter() {


        private val mLeDevices: ArrayList<BluetoothDevice>
        private val mInflator: LayoutInflater

        init {
            mLeDevices = ArrayList()
            mInflator = this@DeviceScanActivity.layoutInflater
        }

        fun addDevice(device: BluetoothDevice) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device)
            }
        }

        fun getDevice(position: Int): BluetoothDevice? {
            return mLeDevices[position]
        }

        fun clear() {
            mLeDevices.clear()
        }

        override fun getCount(): Int {
            return mLeDevices.size
        }

        override fun getItem(i: Int): Any {
            return mLeDevices[i]
        }

        override fun getItemId(i: Int): Long {
            return i.toLong()
        }

        override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
            var view = view
            val viewHolder: ViewHolder
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null)
                viewHolder = ViewHolder()
                viewHolder.deviceAddress = view?.findViewById(R.id.device_address) as TextView
                viewHolder.deviceName = view.findViewById(R.id.device_name) as TextView
                view.tag = viewHolder
            } else {
                viewHolder = view.tag as ViewHolder
            }

            val device = mLeDevices[i]
            val deviceName = device.name
            if (deviceName != null && deviceName.length > 0)
                viewHolder.deviceName?.setText(deviceName)
            else
                viewHolder.deviceName?.setText("UNKNOWN DEVICE")
            viewHolder.deviceAddress?.setText(device.address)

            return view
        }


    }

    internal class ViewHolder {
        var deviceName: TextView? = null
        var deviceAddress: TextView? = null
    }

    // Device scan callback.
    private val mLeScanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            Timber.e("Error Code: $errorCode")
            super.onScanFailed(errorCode)
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let {
                runOnUiThread {
                    mLeDeviceListAdapter?.addDevice(it.device)
                    mLeDeviceListAdapter?.notifyDataSetChanged()
                }
            }
            super.onScanResult(callbackType, result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.let {
                runOnUiThread {
                    it.forEach {
                        mLeDeviceListAdapter?.addDevice(it.device)
                        mLeDeviceListAdapter?.notifyDataSetChanged()
                    }
                }
            }
            super.onBatchScanResults(results)
        }
    }

/*            ScanCallback { device, rssi, scanRecord ->
        runOnUiThread {
            mLeDeviceListAdapter?.addDevice(device)
            mLeDeviceListAdapter?.notifyDataSetChanged()
        }
    }*/

}
