package com.jamesjmtaylor.blecompose

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jamesjmtaylor.blecompose.models.GattCharacteristic
import com.jamesjmtaylor.blecompose.models.characteristics.*
import com.jamesjmtaylor.blecompose.services.GattListener
import com.jamesjmtaylor.blecompose.services.ScanListener
import timber.log.Timber
import kotlin.experimental.and

enum class ConnectionStatus { connecting, disconnecting, connected, disconnected }
data class ScanViewState(val scanning: Boolean = false,
                         val scanResults: List<ScanResult> = emptyList(),
                         val state: String? = null)
data class ConnectViewState(var connectionStatus: ConnectionStatus = ConnectionStatus.disconnected,
                            val services: List<BluetoothGattService> = emptyList(),
                            val characteristics: List<BluetoothGattCharacteristic> = emptyList())

//Constructor injection of liveData for testing & compose preview purposes
@ExperimentalUnsignedTypes
class BleViewModel(private val scanViewMutableLiveData : MutableLiveData<ScanViewState> = MutableLiveData(),
                   private val connectViewMutableLiveData : MutableLiveData<ConnectViewState> = MutableLiveData(),
                   private val characteristicMutableLiveData : MutableLiveData<String> = MutableLiveData()
): ViewModel(), ScanListener, GattListener {
    var selectedDevice: ScanResult? = null
    private var selectedService: BluetoothGattService? = null
    private var scanResults = listOf<ScanResult>() //immutable list is required, otherwise LiveData cannot tell that the object changed
    private var services = listOf<BluetoothGattService>() //immutable list is required, otherwise LiveData cannot tell that the object changed
    private var characterics = listOf<BluetoothGattCharacteristic>() //immutable list is required, otherwise LiveData cannot tell that the object changed
    val scanViewLiveData: LiveData<ScanViewState> get() = scanViewMutableLiveData
    val connectViewState: LiveData<ConnectViewState> get() = connectViewMutableLiveData
    val characteristicViewState: LiveData<String> get() = characteristicMutableLiveData

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
        characteristicMutableLiveData.postValue(getCharDataString(bleChar))
        logCharValue(bleChar)
    }

    private fun getCharDataString(it: BluetoothGattCharacteristic): String {
        return when (GattCharacteristic.getCharacteristic(it.uuid.toString())) {
            GattCharacteristic.MachineStatus -> MachineStatus.getEnum(it.value.first()).name
            GattCharacteristic.IndoorBikeData -> IndoorBikeData.convertBytesToDataString(it.value)
            GattCharacteristic.TrainingStatus -> TrainingStatus.getEnum(it.value.first()).name
            GattCharacteristic.TreadmillData -> TreadmillData.convertBytesToDataString(it.value)
            GattCharacteristic.CscMeasurement -> CscMeasurement.convertBytesToDataString(it.value)
            else -> it.value.toString()
        }
    }

    private fun logCharValue(bleChar: BluetoothGattCharacteristic) {
        val sbBits = StringBuilder()
        val sbBytes = StringBuilder()
        for (byte in bleChar.value) {
            val s = String.format("%8s", Integer.toBinaryString((byte and 0xFF.toByte()).toInt()))
                .replace(' ', '0')
                .removePrefix("111111111111111111111111") //Compensates for negative values that cause an overflow.
            sbBits.append(s)

            sbBytes.append(String.format("%02X", byte))
            if (bleChar.value.indexOf(byte) != bleChar.value.lastIndex) {
                sbBits.append("-")
                sbBytes.append(",")
            }
        }
        Timber.d("updateCharacteristic: ${bleChar.uuid}; byte values: $sbBytes; bit values: $sbBits")
    }

    fun setSelectedService(service: BluetoothGattService){
        this.selectedService = service
        this.characterics = service.characteristics
        connectViewMutableLiveData.postValue(ConnectViewState(ConnectionStatus.connected, services, characterics))
    }
}