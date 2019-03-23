package com.steelcase.beaconsensor

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager

class MainActivity : AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val beaconFragment = BeaconFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragmentFrameLayout, beaconFragment).addToBackStack(beaconFragment.tag).commit()
    }


}
