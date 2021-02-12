package com.jamesjmtaylor.beaconsensor

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ListActivity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.*
import android.widget.*
import timber.log.Timber


class DeviceScanActivity : ListActivity() {
    private var scanResultAdapter: LeDeviceListAdapter? = null
    var mBluetoothAdapter: BluetoothAdapter? = null
    private var mScanning: Boolean = false
    private var mHandler: Handler? = null

    private val REQUEST_ENABLE_BT = 1
    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.title = "Devices"
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
        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE)
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
                scanResultAdapter!!.clear()
                scanLeDevice(true)
            }
            R.id.menu_stop -> scanLeDevice(false)
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter!!.isEnabled) {
            if (!mBluetoothAdapter!!.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }

        // Initializes list view adapter.
        scanResultAdapter = LeDeviceListAdapter()
        listAdapter = scanResultAdapter
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

        unregisterReceiver(mGattUpdateReceiver)
        scanLeDevice(false)
        scanResultAdapter!!.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
        mBluetoothLeService = null
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        val result = scanResultAdapter?.getDevice(position) ?: return
        val intent = Intent(this, DeviceControlActivity::class.java)
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, result.scan.device)
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, result.scan.device.address)
        if (mScanning) {
            mBluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
            mScanning = false
        }
        startActivity(intent)
    }

    private fun scanLeDevice(enable: Boolean) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler?.postDelayed({
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

    // Class to associate connection status with scanResult
    inner class ScanResultWithConnectionStatus(val scan:  ScanResult,
                                               var connected: Boolean = false)
    // Adapter for holding devices found through scanning.
    private inner class LeDeviceListAdapter : BaseAdapter() {
        private val scanResults: ArrayList<ScanResultWithConnectionStatus> = ArrayList()
        private val inflater: LayoutInflater = this@DeviceScanActivity.layoutInflater

        fun addScanResult(device: ScanResultWithConnectionStatus) {
            if (!scanResults.contains(device)) scanResults.add(device)
        }

        fun getDevice(position: Int): ScanResultWithConnectionStatus {
            return scanResults[position]
        }

        fun setDeviceConnection(connected: Boolean, device: BluetoothDevice?) {
            scanResults.firstOrNull { it.scan.device.address == device?.address }?.connected = connected
        }

        fun clear() {
            scanResults.clear()
        }

        override fun getCount(): Int {
            return scanResults.size
        }

        override fun getItem(i: Int): Any {
            return scanResults[i]
        }

        override fun getItemId(i: Int): Long {
            return i.toLong()
        }

        @SuppressLint("SetTextI18n")
        override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
            val tempView = view ?: inflater.inflate(R.layout.row_result, null)
            val viewHolder: DeviceViewHolder
            // General ListView optimization code.
            if (view == null) {
                viewHolder = DeviceViewHolder()
                viewHolder.deviceAddress = tempView?.findViewById(R.id.macTextView) as? TextView
                viewHolder.deviceName = tempView.findViewById(R.id.nameTextView) as? TextView
                viewHolder.rssi = tempView.findViewById(R.id.rssiTextView) as? TextView
                viewHolder.txPower = tempView.findViewById(R.id.txPowerTextView) as? TextView
                viewHolder.connectButton = tempView.findViewById(R.id.connect_button) as? Button
                tempView.tag = viewHolder
            } else {
                viewHolder = tempView.tag as DeviceViewHolder
            }

            val result = scanResults[i]
            val deviceName = result.scan.device.name
            viewHolder.deviceName?.text = if (deviceName != null && deviceName.isNotEmpty()) deviceName
            else  getString(R.string.unknown_device)

            viewHolder.rssi?.text = "RSSI: ${result.scan.rssi}"
            viewHolder.txPower?.text = "Tx Power: ${result.scan.txPower}"
            viewHolder.deviceAddress?.text = "MAC: ${result.scan.device.address}"
            viewHolder.connectButton?.text = if (result.connected) "DISCONNECT" else "CONNECT"
            viewHolder.connectButton?.setOnClickListener {
                this@DeviceScanActivity.mBluetoothLeService?.connect(result.scan.device.address)
            }

            return tempView
        }
    }

    internal class DeviceViewHolder {
        var scanResult: ScanResult? = null
        var deviceName: TextView? = null
        var deviceAddress: TextView? = null
        var rssi: TextView? = null
        var txPower: TextView? = null
        var connectButton: Button? = null

        fun onClick(){
            scanResult
        }
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
                    scanResultAdapter?.addScanResult(ScanResultWithConnectionStatus(it))
                    scanResultAdapter?.notifyDataSetChanged()
                }
            }
            super.onScanResult(callbackType, result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.let {
                runOnUiThread {
                    it.forEach {
                        scanResultAdapter?.addScanResult(ScanResultWithConnectionStatus(it))
                        scanResultAdapter?.notifyDataSetChanged()
                    }
                }
            }
            super.onBatchScanResults(results)
        }
    }

    //MARK: Connection Logic:
    private var mBluetoothLeService: BluetoothLeService? = null
    val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            mBluetoothLeService = (service as BluetoothLeService.LocalBinder).service
            if (!mBluetoothLeService!!.initialize()) {
                Timber.e("Unable to initialize Bluetooth")
                finish()
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mBluetoothLeService = null
        }
    }
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val device = intent.extras?.getParcelable<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

            if (BluetoothLeService.ACTION_GATT_CONNECTED == action) {
                scanResultAdapter?.setDeviceConnection(true, device)
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED == action) {
                scanResultAdapter?.setDeviceConnection(false, device)
            }
            scanResultAdapter?.notifyDataSetChanged()
        }
    }

    companion object {
        private fun makeGattUpdateIntentFilter(): IntentFilter {
            val intentFilter = IntentFilter()
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
            intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE)
            return intentFilter
        }
    }
}
