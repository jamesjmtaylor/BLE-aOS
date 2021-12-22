package com.jamesjmtaylor.blecompose.services

import android.app.*
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
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
    val gattCallback : BluetoothGattCallback
    fun setConnected(status: ConnectionStatus)
    fun getConnected(): ConnectionStatus
}

//ViewModelStoreOwner allows Service to be custodian of the VM, cleaning it up once no longer needed
class BleService : Service(),  ViewModelStoreOwner {
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
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

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

    private val appViewModelStore: ViewModelStore by lazy { ViewModelStore() }
    override fun getViewModelStore(): ViewModelStore {
        return appViewModelStore
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
        if (gattListener?.getConnected() == ConnectionStatus.connected) {
            bluetoothGatt = device?.connectGatt(this,false, gattListener?.gattCallback)
        } else {
            bluetoothGatt
        }
    }

    companion object {
        // A global variable to let activities check if the Service is running without needing to start/bind it
        // This is the best practice as defined here: https://groups.google.com/forum/#!topic/android-developers/jEvXMWgbgzE
        var running: Boolean = false
    }
}