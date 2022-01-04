import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_READ
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_READ
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothGattService.SERVICE_TYPE_PRIMARY
import android.bluetooth.BluetoothGattService.SERVICE_TYPE_SECONDARY
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import java.util.*

object SampleData {

    val scanResults : List<ScanResult> = listOf(
        //ScanResult(BluetoothDevice device, int eventType, int primaryPhy, int secondaryPhy, int advertisingSid, int txPower, int rssi, int periodicAdvertisingInterval, ScanRecord scanRecord, long timestampNanos)
        ScanResult(null, 0, 1,2,3,4,5,6, null, 1L ),
        ScanResult(null, 0, 1,2,3,4,5,6, null, 1L ),
        ScanResult(null, 0, 1,2,3,4,5,6, null, 1L ),
        ScanResult(null, 0, 1,2,3,4,5,6, null, 1L )
    )

    val discoveredServices : List<BluetoothGattService> = listOf(
        BluetoothGattService(UUID.randomUUID(),SERVICE_TYPE_PRIMARY),
        BluetoothGattService(UUID.randomUUID(), SERVICE_TYPE_SECONDARY),
        BluetoothGattService(UUID.randomUUID(), SERVICE_TYPE_SECONDARY)
    )

    val characteristics : List<BluetoothGattCharacteristic> = listOf(
        BluetoothGattCharacteristic(UUID.randomUUID(),PROPERTY_READ,PERMISSION_READ),
        BluetoothGattCharacteristic(UUID.randomUUID(),PROPERTY_READ,PERMISSION_READ),
        BluetoothGattCharacteristic(UUID.randomUUID(),PROPERTY_READ,PERMISSION_READ)
    )
}