package com.jamesjmtaylor.blecompose

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jamesjmtaylor.blecompose.services.GattListener
import com.jamesjmtaylor.blecompose.services.ScanListener

enum class ConnectionStatus { connecting, disconnecting, connected, disconnected }
data class ScanViewState(val scanning: Boolean = false,
                         val scanResults: List<ScanResult> = emptyList(),
                         val state: String? = null)
data class ConnectViewState(var connectionStatus: ConnectionStatus = ConnectionStatus.disconnected,
                            val services: List<BluetoothGattService> = emptyList(),
                            val characteristics: List<BluetoothGattCharacteristic> = emptyList())

//Constructor injection of liveData for testing & compose preview purposes
class BleViewModel(private val scanViewMutableLiveData : MutableLiveData<ScanViewState> = MutableLiveData(),
                   private val connectViewMutableLiveData : MutableLiveData<ConnectViewState> = MutableLiveData()):
    ViewModel(), ScanListener, GattListener {
    var selectedDevice: BluetoothDevice? = null
    private var scanResults = listOf<ScanResult>() //immutable list is required, otherwise LiveData cannot tell that the object changed
    val scanViewLiveData: LiveData<ScanViewState> get() = scanViewMutableLiveData
    val connectViewState: LiveData<ConnectViewState> get() = connectViewMutableLiveData

    override fun setScanning(scanning: Boolean) {
        scanViewMutableLiveData.value = ScanViewState(scanning, scanResults)
    }
    override fun getScanning() : Boolean {
        return scanViewLiveData.value?.scanning ?: false
    }

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
            scanViewMutableLiveData.value  = ScanViewState(true, scanResults)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            val tempList = scanResults.toMutableList()
            results?.map { tempList.removeIf { r -> r.device.address == it.device?.address } }
            results?.let { tempList.addAll(results) }
            scanResults = tempList.toList()
            scanViewMutableLiveData.value = ScanViewState(true, scanResults)
        }

        override fun onScanFailed(errorCode: Int) {
            scanViewMutableLiveData.value = ScanViewState(false,scanResults,"onScanFailed errorCode: $errorCode")
        }
    }

    override fun setConnected(status: ConnectionStatus) {
        connectViewMutableLiveData.value = ConnectViewState(status)
    }

    override fun getConnected(): ConnectionStatus {
        return connectViewState.value?.connectionStatus ?: ConnectionStatus.disconnected
    }

    override fun updateServices(bleServices: List<BluetoothGattService>) {
        connectViewMutableLiveData.value = ConnectViewState(ConnectionStatus.connected, bleServices)
    }

    override fun updateCharacteristic(bleChar: BluetoothGattCharacteristic) {

    }


}