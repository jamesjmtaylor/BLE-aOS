package com.jamesjmtaylor.blecompose.services

import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.jamesjmtaylor.blecompose.ConnectionStatus
import com.jamesjmtaylor.blecompose.R
import com.jamesjmtaylor.blecompose.NavActivity
import timber.log.Timber
import java.util.*

//NOTE: Based on AdvertiserService in Bluetooth Advertisements Kotlin
//Also references https://stackoverflow.com/questions/53382320/boundservice-livedata-viewmodel-best-practice-in-new-android-recommended-arc
const val FOREGROUND_NOTIFICATION_ID = 3
const val BLE_NOTIFICATION_CHANNEL_ID = "bleChl"
//Interface is to keep access to vm methods to a bare minimum
interface ScanListener {
    val scanCallback : ScanCallback
    fun setScanning(scanning: Boolean)
    fun getScanning(): Boolean
}
interface GattListener {
    fun setConnected(status: ConnectionStatus)
    fun getConnected(): ConnectionStatus
    fun updateCharacteristic(bleChar: BluetoothGattCharacteristic)
    fun updateServices(bleServices: List<BluetoothGattService>)
}

//ViewModelStoreOwner allows Service to be custodian of the VM, cleaning it up once no longer needed
class BleService : Service() {
    var scanListener: ScanListener? = null
    var gattListener: GattListener? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var handler: Handler? = null
    //Binding is needed if you want your views to be able to invoke service methods, i.e. scan()
    var binder: IBinder = LocalBinder()
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    inner class LocalBinder : Binder() {
        fun getService(): BleService = this@BleService
    }

    override fun onCreate() {
        running = true
        initialize()
        super.onCreate()
    }

    override fun onDestroy() {
        running = false
        handler?.removeCallbacksAndMessages(null) // this is a generic way for removing tasks
        stopForeground(true)
        super.onDestroy()
    }

    private fun initialize() {
        if (bluetoothLeScanner == null) {
            val manager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothLeScanner = manager.adapter.bluetoothLeScanner
        }
        launchForegroundNotification()
    }

    private fun launchForegroundNotification() {
        val notificationIntent = Intent(this, NavActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, FLAG_IMMUTABLE)

        val bleNotificationChannel = NotificationChannel(
            BLE_NOTIFICATION_CHANNEL_ID, "BLE",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val nManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nManager.createNotificationChannel(bleNotificationChannel)
        val nBuilder = Notification.Builder(this, BLE_NOTIFICATION_CHANNEL_ID)

        val notification = nBuilder.setContentTitle("BLE Service")
            .setContentText("BLE Scanner for the BLE Compose app is active.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(FOREGROUND_NOTIFICATION_ID, notification)
    }

    fun toggleScan() {
        if (scanListener?.getScanning() == false) {
            bluetoothLeScanner?.startScan(scanListener?.scanCallback)
            scanListener?.setScanning(true)
        } else {
            bluetoothLeScanner?.stopScan(scanListener?.scanCallback)
            scanListener?.setScanning(false)
        }
    }

    fun toggleConnect(device: BluetoothDevice?) {
        Timber.d("toggleConnect() device: ${device?.address?.toString()}; status: ${gattListener?.getConnected()?.name}")

        if (gattListener?.getConnected() != ConnectionStatus.connected) {
            gattListener?.setConnected(ConnectionStatus.connecting)
            bluetoothGatt = device?.connectGatt(this,false, object : BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt?,status: Int,newState: Int) {
                    Timber.d("toggleConnect() onConnectionStateChange")
                    when (val connectionStatus = intToConnectionStatus(newState)) {
                        ConnectionStatus.connected -> bluetoothGatt?.discoverServices()
                        else -> gattListener?.setConnected(connectionStatus)
                    }
                }
                override fun onServiceChanged(gatt: BluetoothGatt) {
                    Timber.d("toggleConnect() onServiceChanged")
                    gatt.services?.let { gattListener?.updateServices(it.toList())}
                }
                override fun onCharacteristicChanged(gatt: BluetoothGatt?,characteristic: BluetoothGattCharacteristic?) {
                    Timber.d("toggleConnect() onCharacteristicChanged")
                    characteristic?.let { gattListener?.updateCharacteristic(it) }
                }
                override fun onCharacteristicRead(gatt: BluetoothGatt?,characteristic: BluetoothGattCharacteristic?, status: Int) {
                    Timber.d("toggleConnect() onCharacteristicRead")
                    characteristic?.let { gattListener?.updateCharacteristic(it) }
                }
                override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                    Timber.d("toggleConnect() onServicesDiscovered")
                    gatt?.services?.let { gattListener?.updateServices(it) }
                }
            })
        } else {
            gattListener?.setConnected(ConnectionStatus.disconnecting)
            bluetoothGatt?.disconnect()
        }
    }

    val cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")//Client Characteristic Configuration Descriptor
    //This descriptor type (often abbreviated CCCD) is without a doubt the most important and commonly used,
    // and it is essential for the operation of most of the profiles and use cases. Its function is simple:
    // it acts as a switch, enabling or disabling server-initiated updates
    fun setCharacteristicNotification(bleChar: BluetoothGattCharacteristic, enabled: Boolean) {
        bluetoothGatt?.setCharacteristicNotification(bleChar,enabled)
        bleChar.getDescriptor(cccdUuid)?.let { cccd ->
            cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            bluetoothGatt?.writeDescriptor(cccd)
        }
    }

    private fun intToConnectionStatus(int: Int) : ConnectionStatus {
        return when (int) {
            BluetoothProfile.STATE_CONNECTED -> ConnectionStatus.connected
            BluetoothProfile.STATE_CONNECTING -> ConnectionStatus.connecting
            BluetoothProfile.STATE_DISCONNECTING -> ConnectionStatus.disconnecting
            BluetoothProfile.STATE_DISCONNECTED -> ConnectionStatus.disconnected
            else -> ConnectionStatus.disconnected
        }
    }

    companion object {
        // A global variable to let activities check if the Service is running without needing to start/bind it
        // This is the best practice as defined here: https://groups.google.com/forum/#!topic/android-developers/jEvXMWgbgzE
        var running: Boolean = false
    }
}