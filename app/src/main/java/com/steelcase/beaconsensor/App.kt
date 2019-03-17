package com.steelcase.beaconsensor

import android.app.Application
import android.util.Log
import org.altbeacon.beacon.startup.RegionBootstrap
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.startup.BootstrapNotifier
import org.altbeacon.beacon.powersave.BackgroundPowerSaver





/**
 * Created by jtaylor on 3/8/18.
 */
class App : Application(), BootstrapNotifier {
    private val TAG = "ApplicationSingleton"
    private var regionBootstrap: RegionBootstrap? = null
    private var backgroundPowerSaver: BackgroundPowerSaver? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "App started up")
        val beaconManager = BeaconManager.getInstanceForApplication(this)
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        beaconManager.beaconParsers.add(BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")) //estimote format

        // wake up the app when any beacon is seen (you can specify specific id filers in the parameters below)
        val region = Region("com.steelcase.beaconsensor.boostrapRegion", null, null, null)
        regionBootstrap = RegionBootstrap(this, region)
        backgroundPowerSaver = BackgroundPowerSaver(this); // This reduces bluetooth power usage by about 60%

    }

    override fun didDetermineStateForRegion(state: Int, region: Region) {
        Log.i(TAG, "I just determined my state was $state for the $region region!")
    }
    override fun didEnterRegion(region: Region) {
        Log.i(TAG, "I just entered the $region region!")
    }
    override fun didExitRegion(region: Region) {
        Log.i(TAG, "I just left the $region region!")
    }
    companion object {
        private val TAG = ".BeaconSensor"
    }
}