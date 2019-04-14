package com.jamesjmtaylor.beaconsensor

import android.content.ServiceConnection
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager

class MainActivity : AppCompatActivity(), BluetoothServiceView {
    override var bluetoothServiceIsBound: Boolean = false
    override var bluetoothService: BluetoothService? = null
    override var bluetoothServiceConnection: ServiceConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

//        val beaconFragment = BeaconFragment.newInstance()
        val bluetoothSocketFragment = BluetoothSocketFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragmentFrameLayout, bluetoothSocketFragment).addToBackStack(bluetoothSocketFragment.tag).commit()
        bindService(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(this)
    }
}
