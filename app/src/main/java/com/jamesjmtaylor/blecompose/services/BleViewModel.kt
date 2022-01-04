package com.jamesjmtaylor.blecompose

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jamesjmtaylor.blecompose.services.GattListener
import com.jamesjmtaylor.blecompose.services.ScanListener
import timber.log.Timber

enum class ConnectionStatus { connecting, disconnecting, connected, disconnected }
data class ScanViewState(val scanning: Boolean = false,
                         val scanResults: List<ScanResult> = emptyList(),
                         val state: String? = null)
data class ConnectViewState(var connectionStatus: ConnectionStatus = ConnectionStatus.disconnected,
                            val services: List<BluetoothGattService> = emptyList(),
                            val characteristics: List<BluetoothGattCharacteristic> = emptyList())
data class CharacteristicViewState(var characteristic: BluetoothGattCharacteristic)

//Constructor injection of liveData for testing & compose preview purposes
class BleViewModel(private val scanViewMutableLiveData : MutableLiveData<ScanViewState> = MutableLiveData(),
                   private val connectViewMutableLiveData : MutableLiveData<ConnectViewState> = MutableLiveData(),
                   private val characteristicMutableLiveData : MutableLiveData<CharacteristicViewState> = MutableLiveData()
): ViewModel(), ScanListener, GattListener {
    var selectedDevice: ScanResult? = null
    private var selectedService: BluetoothGattService? = null
    private var scanResults = listOf<ScanResult>() //immutable list is required, otherwise LiveData cannot tell that the object changed
    private var services = listOf<BluetoothGattService>() //immutable list is required, otherwise LiveData cannot tell that the object changed
    private var characterics = listOf<BluetoothGattCharacteristic>() //immutable list is required, otherwise LiveData cannot tell that the object changed
    val scanViewLiveData: LiveData<ScanViewState> get() = scanViewMutableLiveData
    val connectViewState: LiveData<ConnectViewState> get() = connectViewMutableLiveData
    val characteristicViewState: LiveData<CharacteristicViewState> get() = characteristicMutableLiveData

    override fun setScanning(scanning: Boolean) {
        scanViewMutableLiveData.postValue(ScanViewState(scanning, scanResults))
    }
    override fun getScanning() : Boolean {
        return scanViewLiveData.value?.scanning ?: false
    }

    override val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            val tempList = scanResults.toMutableList()
            tempList.removeIf { r -> r.device.address == result?.device?.address } //remove old scan results
            result?.let { if (it.device.name?.isBlank() == false) tempList.add(result) } //remove scan results without device names
            scanResults = tempList.toList()
            scanViewMutableLiveData.postValue(ScanViewState(true, scanResults))
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            val tempList = scanResults.toMutableList()
            results?.map { tempList.removeIf { r -> r.device.address == it.device?.address } }
            results?.let { tempList.addAll(results) }
            scanResults = tempList.toList()
            scanViewMutableLiveData.postValue(ScanViewState(true, scanResults))
        }

        override fun onScanFailed(errorCode: Int) {
            scanViewMutableLiveData.postValue(ScanViewState(false,scanResults,"onScanFailed errorCode: $errorCode"))
        }
    }

    override fun setConnected(status: ConnectionStatus) {
        connectViewMutableLiveData.postValue(ConnectViewState(status))
    }

    override fun getConnected(): ConnectionStatus {
        return connectViewState.value?.connectionStatus ?: ConnectionStatus.disconnected
    }

    override fun updateServices(bleServices: List<BluetoothGattService>) {
        services = bleServices
        connectViewMutableLiveData.postValue(ConnectViewState(ConnectionStatus.connected, bleServices))
    }

    override fun updateCharacteristic(bleChar: BluetoothGattCharacteristic) {
        val tempList = characterics.toMutableList()
        tempList.removeIf { c -> c.uuid == bleChar.uuid } //Remove old characteristic data
        tempList.add(bleChar)
        characterics = tempList.toList()
        connectViewMutableLiveData.postValue(ConnectViewState(ConnectionStatus.connected, services, characterics))
        val sb = StringBuilder()
        for (byte in bleChar.value) sb.append(byte.toString()).append(",")
        sb.removeSuffix(",")
        Timber.d("updateCharacteristic: ${bleChar.uuid}; byte values: $sb")
    }

    fun setSelectedService(service: BluetoothGattService){
        this.selectedService = service
        this.characterics = service.characteristics
        connectViewMutableLiveData.postValue(ConnectViewState(ConnectionStatus.connected, services, characterics))
    }
}