package com.jamesjmtaylor.blecompose

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jamesjmtaylor.blecompose.services.ScanListener

enum class ConnectionStatus { connecting, disconnecting, connected, disconnected }
data class ViewState(val scanning: Boolean = false,
                     val scanResults: List<ScanResult>? = null,
                     val state: String? = null,
                     val gatt: BluetoothGatt? = null,
                     val connectionStatus: ConnectionStatus = ConnectionStatus.disconnected)

//Constructor injection of liveData for testing & compose preview purposes
class BleViewModel(private val viewMutableLiveData : MutableLiveData<ViewState> = MutableLiveData()): ViewModel(), ScanListener {
    private var deviceAddress: String? = null
    private var scanning = false
    private var scanResults = listOf<ScanResult>() //immutable list is required, otherwise LiveData cannot tell that the object changed
    val viewLiveData: LiveData<ViewState> get() = viewMutableLiveData

    override fun setScanning(scanning: Boolean) {
        this.scanning = scanning
        viewMutableLiveData.value = ViewState(scanning, scanResults)
    }
    override fun getScanning() : Boolean { return scanning }

    override val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            val tempList = scanResults.toMutableList()
            tempList.removeIf { r -> r.device.address == result?.device?.address }
            result?.let { tempList.add(result) }
            scanResults = tempList.toList()
            var callbackTypeString = ""
            when (callbackType){
                SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> callbackTypeString = "Registration failed"
                SCAN_FAILED_FEATURE_UNSUPPORTED -> callbackTypeString = "Ble not supported"
                SCAN_FAILED_INTERNAL_ERROR -> callbackTypeString = "Ble internal error"
            }
            viewMutableLiveData.value = ViewState(scanning, scanResults, callbackTypeString)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            val tempList = scanResults.toMutableList()
            results?.map { tempList.removeIf { r -> r.device.address == it.device?.address } }
            results?.let { tempList.addAll(results) }
            scanResults = tempList.toList()
        }

        override fun onScanFailed(errorCode: Int) {
            scanning = false
            viewMutableLiveData.value = ViewState(scanning,scanResults,"onScanFailed errorCode: $errorCode")
        }
    }
}