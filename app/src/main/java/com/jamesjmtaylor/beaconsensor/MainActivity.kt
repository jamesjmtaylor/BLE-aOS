package com.jamesjmtaylor.beaconsensor

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        createFragments()
    }

    private fun createFragments() {
        val beaconFragment = BeaconFragment.newInstance()
        val bluetoothSocketFragment = BluetoothSocketFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragmentFrameLayout, bluetoothSocketFragment).addToBackStack(bluetoothSocketFragment.tag).commit()
    }
}
