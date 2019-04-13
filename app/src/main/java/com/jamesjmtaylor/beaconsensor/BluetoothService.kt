package com.jamesjmtaylor.beaconsensor

import android.app.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import timber.log.Timber


private const val TAG = "MY_APP_DEBUG_TAG"

// Defines several constants used when transmitting messages between the
// service and the UI.
const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_TOAST: Int = 2
// ... (Add other message types here as needed.)

class BluetoothService : Service() { //Service for persistent connection to bluetooth socket, rather than IntentService which dies after handling intent
    private var serviceLooper: Looper? = null
    private var bluetoothServiceHandler: BluetoothServiceHandler? = null
    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onCreate() {
        HandlerThread("ServiceStartArguments", THREAD_PRIORITY_BACKGROUND).apply {
            start()
            serviceLooper = looper //Get the HandlerThread's looper and use it for our handler
            bluetoothServiceHandler = BluetoothServiceHandler(looper)
        }

        startForegroundNotificationService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "Starting Bluetooth Service", LENGTH_LONG).show()
        bluetoothServiceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId //Save starting Id for request tracking
            bluetoothServiceHandler?.sendMessage(msg) //Send a message to start a job
        }
        startForegroundNotificationService()
        return START_STICKY //if this service's process is killed while it is started this will try & restart it
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null //Binding not used by a background service
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
                .setContentText("A foreground service for maintaining the bluetooth socket connection")
                .setContentIntent(pendingIntent)
                .build()

        startForeground(ONGOING_BLUETOOTH_NOTIFICATION_ID, notification)
    }

    private inner class BluetoothServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            Timber.d("BluetoothService handleMessage called with message $msg")
            //Potentially stop the service here based on message content
        }
    }
}

const val BLUETOOTH_NOTIFICATION_ID = "Bluetooth notifications channel"
const val BLUETOOTH_NOTIFICATION_NAME = "Bluetooth notifications"
const val ONGOING_BLUETOOTH_NOTIFICATION_ID = 117

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

     // Call this method from the main activity to shut down the connection.
     fun cancel() {
         try {
             mmSocket.close()
         } catch (e: IOException) {
             Timber.e(e, BluetoothService::class.java.simpleName, "Could not close the connect socket")
         }
     }
 }*/
