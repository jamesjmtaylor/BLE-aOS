package com.jamesjmtaylor.beaconsensor

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Intent
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*

class BluetoothDeviceConnection(val bluetoothGatt: BluetoothGatt,
                                val weakService: WeakReference<BluetoothService>) {
    private var connectionState = STATE_DISCONNECTED
    // Various callback methods defined by the BLE API.
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val intentAction: String
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    intentAction = ACTION_GATT_CONNECTED
                    connectionState = STATE_CONNECTED
                    val services = bluetoothGatt.discoverServices()
                    broadcastUpdate(intentAction)
                    Timber.i("Connected to GATT server.")
                    Timber.i("Attempting to start service discovery: $services")
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    intentAction = ACTION_GATT_DISCONNECTED
                    connectionState = STATE_DISCONNECTED
                    Timber.i("Disconnected from GATT server.")
                    broadcastUpdate(intentAction)
                }
            }
        }

        // New services discovered
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
                else -> Timber.w("onServicesDiscovered received: $status")
            }
        }

        // Result of a characteristic read operation
        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
                }
            }
        }

        //MARK: Message Broadcasters back to activity
        private fun broadcastUpdate(action: String) {
            val intent = Intent(action)
            weakService.get()?.sendBroadcast(intent)
        }

        private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic) {
            val intent = Intent(action)

            // This is special handling for the Heart Rate Measurement profile. Data
            // parsing is carried out as per profile specifications.
            when (characteristic.uuid) {
                UUID_HEART_RATE_MEASUREMENT -> {
                    val flag = characteristic.properties
                    val format = when (flag and 0x01) {
                        0x01 -> {
                            Timber.d("Heart rate format UINT16.")
                            BluetoothGattCharacteristic.FORMAT_UINT16
                        }
                        else -> {
                            Timber.d("Heart rate format UINT8.")
                            BluetoothGattCharacteristic.FORMAT_UINT8
                        }
                    }
                    val heartRate = characteristic.getIntValue(format, 1)
                    Timber.d(String.format("Received heart rate: %d", heartRate))
                    intent.putExtra(EXTRA_DATA, (heartRate).toString())
                }
                else -> {
                    // For all other profiles, writes the data formatted in HEX.
                    val data: ByteArray? = characteristic.value
                    if (data?.isNotEmpty() == true) {
                        val hexString: String = data.joinToString(separator = " ") {
                            String.format("%02X", it)
                        }
                        intent.putExtra(EXTRA_DATA, "$data\n$hexString")
                    }
                }
            }
            weakService.get()?.sendBroadcast(intent)
        }
    }
}

private const val STATE_DISCONNECTED = 0
private const val STATE_CONNECTING = 1
private const val STATE_CONNECTED = 2

const val ONGOING_BLUETOOTH_NOTIFICATION_ID = 117
const val REQUEST_ENABLE_BT = 118

const val ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED"
const val ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED"
const val ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED"
const val ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE"
const val EXTRA_DATA = "EXTRA_DATA"
val UUID_HEART_RATE_MEASUREMENT = UUID.fromString("Test")