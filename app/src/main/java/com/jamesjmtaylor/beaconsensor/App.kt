package com.jamesjmtaylor.beaconsensor

import android.app.Application
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.powersave.BackgroundPowerSaver
import org.altbeacon.beacon.startup.BootstrapNotifier
import org.altbeacon.beacon.startup.RegionBootstrap
import timber.log.Timber


/**
 * Created by jtaylor on 3/8/18.
 */
class App : Application(), BootstrapNotifier {
    private var regionBootstrap: RegionBootstrap? = null
    private var backgroundPowerSaver: BackgroundPowerSaver? = null

    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        Timber.d("App started up")

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
        Timber.d("I just determined my state was $state for the $region region!")
    }

    override fun didEnterRegion(region: Region) {
        Timber.d("I just entered the $region region!")
    }

    override fun didExitRegion(region: Region) {
        Timber.i("I just left the $region region!")
    }
}
