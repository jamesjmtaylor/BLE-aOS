package com.jamesjmtaylor.beaconsensor

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

// Defines several constants used when transmitting messages between the
// bluetoothService and the UI.
const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_TOAST: Int = 2

//Service for persistent deviceConnection to bluetooth socket, rather than IntentService which dies after handling intent
class BluetoothService : Service() {
    var deviceConnection: BluetoothDeviceConnection? = null
    //MARK: - SERVICE LOGIC
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothService = this@BluetoothService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        startForegroundNotificationService()
    }

    override fun onDestroy() {
        deviceConnection?.bluetoothGatt?.close()
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    private fun startForegroundNotificationService() {
        val pendingIntent: PendingIntent = Intent(this, BluetoothService::class.java).let {
            PendingIntent.getActivity(this, 0, it, 0)
        }
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel(BLUETOOTH_NOTIFICATION_ID, BLUETOOTH_NOTIFICATION_NAME)
        else ""
        val notificationBuilder = NotificationCompat.Builder(App.instance, channelId)
        val notification = notificationBuilder
                .setContentTitle("Bluetooth Service")
                .setContentText("A foreground bluetoothService for maintaining the bluetooth socket deviceConnection")
                .setContentIntent(pendingIntent)
                .build()

        startForeground(ONGOING_BLUETOOTH_NOTIFICATION_ID, notification)
    }
}

const val BLUETOOTH_NOTIFICATION_ID = "Bluetooth notifications channel"
const val BLUETOOTH_NOTIFICATION_NAME = "Bluetooth notifications"


/* private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

     private val mmInStream: InputStream = mmSocket.inputStream
     private val mmOutStream: OutputStream = mmSocket.outputStream
     private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream


     override fun run() {
         var numBytes: Int // bytes returned from read()

         // Keep listening to the InputStream until an exception occurs.
         while (true) {
             // Read from the InputStream.
             numBytes = try {
                 mmInStream.read(mmBuffer)
             } catch (e: IOException) {
                 Timber.e(e, BluetoothService::class.java.simpleName, "Input stream was disconnected")
                 break
             }

             // Send the obtained bytes to the UI activity.
             val readMsg = handler.obtainMessage(
                     MESSAGE_READ, numBytes, -1,
                     mmBuffer)
             readMsg.sendToTarget()
         }
     }

     // Call this from the main activity to send data to the remote device.
     fun write(bytes: ByteArray) {
         try {
             mmOutStream.write(bytes)
         } catch (e: IOException) {
             Timber.e(e, BluetoothService::class.java.simpleName, "Error occurred when sending data")

             // Send a failure message back to the activity.
             val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
             val bundle = Bundle().apply {
                 putString("toast", "Couldn't send data to the other device")
             }
             writeErrorMsg.data = bundle
             handler.sendMessage(writeErrorMsg)
             return
         }

         // Share the sent message with the UI activity.
         val writtenMsg = handler.obtainMessage(
                 MESSAGE_WRITE, -1, -1, mmBuffer)
         writtenMsg.sendToTarget()
     }

     // Call this method from the main activity to shut down the deviceConnection.
     fun cancel() {
         try {
             mmSocket.close()
         } catch (e: IOException) {
             Timber.e(e, BluetoothService::class.java.simpleName, "Could not close the connect socket")
         }
     }
 }*/
