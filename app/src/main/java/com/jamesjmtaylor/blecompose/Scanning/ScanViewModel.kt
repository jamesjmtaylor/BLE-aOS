﻿package com.jamesjmtaylor.blecompose

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.*
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jamesjmtaylor.blecompose.services.BleListener
import com.jamesjmtaylor.blecompose.services.BleService
import kotlinx.coroutines.delay
import timber.log.Timber

//TODO: Continue porting over logic from example code
//TODO: Handle permissions request
//TODO: Create new live data to update UI with
//TODO: Update UI to display BLE data
//TODO: Think about whether VMs should be persisting scanResults & Gatt Data, or if the BleService should
//Loading, Content, Error
data class LCE<T>(val loading: Boolean, val content: T? = null, val error: Throwable? = null)
enum class ConnectionStatus { connecting, disconnecting, connected, disconnected }
data class ViewState(val scanning: Boolean = false,
                     val scanResults: List<ScanResult>? = null,
                     val state: String? = null,
                     val gatt: BluetoothGatt? = null,
                     val connectionStatus: ConnectionStatus = ConnectionStatus.disconnected)

//Constructor injection of liveData for testing & compose preview purposes
class ScanViewModel(private val viewMutableLiveData : MutableLiveData<ViewState> = MutableLiveData()): ViewModel() {
    private var deviceAddress: String? = null
    private var scanning = false
    private var scanResults = mutableListOf<ScanResult>()
    val viewLiveData: LiveData<ViewState> get() = viewMutableLiveData

    @SuppressLint("StaticFieldLeak") // OnClear() below removes circular reference memory leak
    private var bleService: BleService? = null
    override fun onCleared() {
        bleService?.clearListener()
        super.onCleared()
    }

    // Code to manage Service lifecycle.
    val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            bleService = (service as BleService.LocalBinder).service
            if (bleService?.initialize() != true) Timber.e("Unable to initialize Bluetooth")
            else bleService?.connect(deviceAddress)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bleService = null
        }
    }

    var bluetoothManager: BluetoothManager? = null
    var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothDeviceAddress: String? = null
    private var connectionState: Int = BluetoothAdapter.STATE_DISCONNECTED
    private var bleListener : BleListener? = null

    fun scan(){
        bluetoothAdapter?.bluetoothLeScanner?.startScan(object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                if (result != null && !scanResults.contains(result))  scanResults.add(result)
                var callbackTypeString = ""
                when (callbackType){
                    SCAN_FAILED_ALREADY_STARTED -> callbackTypeString = "Scan already started"
                    SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> callbackTypeString = "Registration failed"
                    SCAN_FAILED_FEATURE_UNSUPPORTED -> callbackTypeString = "Ble not supported"
                    SCAN_FAILED_INTERNAL_ERROR -> callbackTypeString = "Ble internal error"
                }
                viewMutableLiveData.value = ViewState(scanning, scanResults, callbackTypeString)
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                results?.map { if (!scanResults.contains(it)) scanResults.add(it) }
            }

            override fun onScanFailed(errorCode: Int) {
                scanning = false
                viewMutableLiveData.value = ViewState(scanning,scanResults,"onScanFailed errorCode: $errorCode")
            }
        })
    }

    fun stopScan(bleListener: BleListener){
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(bleListener.scanCallback)
    }
}