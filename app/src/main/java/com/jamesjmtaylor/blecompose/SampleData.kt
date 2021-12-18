import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult

object SampleData {

    val scanResults : List<ScanResult> = listOf(
        //ScanResult(BluetoothDevice device, int eventType, int primaryPhy, int secondaryPhy, int advertisingSid, int txPower, int rssi, int periodicAdvertisingInterval, ScanRecord scanRecord, long timestampNanos)
        ScanResult(null, 0, 1,2,3,4,5,6, null, 1L ),
        ScanResult(null, 0, 1,2,3,4,5,6, null, 1L ),
        ScanResult(null, 0, 1,2,3,4,5,6, null, 1L ),
        ScanResult(null, 0, 1,2,3,4,5,6, null, 1L )
    )
}