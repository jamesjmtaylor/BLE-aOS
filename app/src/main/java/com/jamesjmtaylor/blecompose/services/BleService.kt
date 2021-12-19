package com.jamesjmtaylor.blecompose.services

import android.app.*
import android.bluetooth.*
import android.bluetooth.BluetoothAdapter.STATE_CONNECTED
import android.bluetooth.BluetoothAdapter.STATE_CONNECTING
import android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.nfc.NfcAdapter.EXTRA_DATA
import android.os.Binder
import android.os.IBinder
import timber.log.Timber
import java.lang.StringBuilder
import java.util.*
interface BleListener { val scanCallback: ScanCallback; val gattCallback: BluetoothGattCallback}
class BleService  : Service() {
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothDeviceAddress: String? = null
    private var connectionState: Int = STATE_DISCONNECTED
    private var bleListener : BleListener? = null

    fun clearListener(){
        bleListener = null
    }

    fun scan(bleListener: BleListener){
        bluetoothAdapter?.bluetoothLeScanner?.startScan(bleListener.scanCallback)
    }

    fun stopScan(bleListener: BleListener){
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(bleListener.scanCallback)
    }

//    private fun broadcastUpdate(action: String,characteristic: BluetoothGattCharacteristic) {
//        val intent = Intent(action)
//
//        //For all profiles write the data formatted in HEX.
//        val data = characteristic.value
//        if (data != null && data.size > 0) {
//            val stringBuilder = StringBuilder(data.size)
//            for (byteChar in data) stringBuilder.append(String.format("%02X ", byteChar))
//            intent.putExtra(EXTRA_DATA, """
//                ${String(data)}
//                $stringBuilder
//                """.trimIndent())
//        }
//        sendBroadcast(intent)
//    }

    private val binder: IBinder = LocalBinder()
    inner class LocalBinder : Binder() {
        internal val service: BleService get() = this@BleService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    // After using a given device, you should make sure that BluetoothGatt.close() is called
    override fun onUnbind(intent: Intent?): Boolean {
        close()
        return super.onUnbind(intent)
    }

    fun initialize(): Boolean { //Return true if the initialization is successful.
        if (bluetoothManager == null) {
            bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
            if (bluetoothManager == null)  return false
        }
        bluetoothAdapter = bluetoothManager?.adapter
        return bluetoothAdapter != null
    }

    fun connect(address: String?): Boolean { //Return true if the connection is successful.
        if (bluetoothAdapter == null || address == null)  return false
        val device = bluetoothAdapter?.getRemoteDevice(address) ?: return false

        bluetoothGatt = device.connectGatt(this, false, bleListener?.gattCallback)
        Timber.d("Trying to create a new connection.")
        bluetoothDeviceAddress = address
        connectionState = STATE_CONNECTING
        return true
    }

    fun disconnect() { //Disconnects an existing connection or cancel a pending connection.
        if (bluetoothGatt == null) Timber.e("disconnect error: bluetoothGatt == null")
        bluetoothGatt?.disconnect()
    }

    fun close() { //After using a given BLE device, app must call this to release resources
        if (bluetoothGatt == null) Timber.e("close error: bluetoothGatt == null")
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    fun readCharacteristic(characteristic: BluetoothGattCharacteristic?) { //Request a read on a given `BluetoothGattCharacteristic`
        if (bluetoothGatt == null) Timber.e("readCharacteristic error: bluetoothGatt == null")
        bluetoothGatt?.readCharacteristic(characteristic)
    }

    //enable notifications for a given characteristic.
    fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic, enabled: Boolean) {
        if (bluetoothGatt == null) Timber.e("setCharacteristicNotification error: bluetoothGatt == null")
        bluetoothGatt?.setCharacteristicNotification(characteristic, enabled)
    }

    fun getSupportedGattServices(): List<BluetoothGattService?>? {
        if (bluetoothGatt == null) Timber.e("getSupportedGattServices error: bluetoothGatt == null")
        return bluetoothGatt?.services
    }

    fun discoverServices() {
        bluetoothGatt?.discoverServices()
    }
}