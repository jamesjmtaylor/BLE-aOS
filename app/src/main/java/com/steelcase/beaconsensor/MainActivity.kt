package com.steelcase.beaconsensor

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import android.widget.TextView
import org.altbeacon.beacon.BeaconConsumer
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.RangeNotifier
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.WindowManager
import org.altbeacon.beacon.BeaconParser

class MainActivity : AppCompatActivity(), BeaconConsumer {
    protected val TAG = "MonitoringActivity"
    private val PERMISSION_REQUEST_COARSE_LOCATION = 1
    private val MY_PERMISSIONS_REQUEST_LOCATION = 99
    var beaconManager : BeaconManager? = null

    var foundTextView : TextView? = null
    var uuidTextView : TextView? = null
    var majorTextView : TextView? = null
    var minorTextView : TextView? = null
    var txPowerTextView : TextView? = null
    var distanceTextView : TextView? = null
    var rssiTextView : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rssiTextView = findViewById(R.id.distanceTextView)
        uuidTextView = findViewById(R.id.uuidTextView)
        foundTextView = findViewById(R.id.foundTextView)
        majorTextView = findViewById(R.id.majorTextView)
        minorTextView = findViewById(R.id.minorTextView)
        txPowerTextView = findViewById(R.id.txPowerTextView)
        distanceTextView = findViewById(R.id.rssiTextView)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (checkLocationPermission()) {
            beaconManager = BeaconManager.getInstanceForApplication(this)
            beaconManager?.getBeaconParsers()?.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))
            beaconManager?.bind(this)
        }


        beaconManager = BeaconManager.getInstanceForApplication(this)
    }
    override fun onPause() {
        super.onPause()
        if (beaconManager?.isBound(this) ?: false) beaconManager?.setBackgroundMode(true)
    }
    override fun onResume() {
        super.onResume()
        if (beaconManager?.isBound(this) ?: false) beaconManager?.setBackgroundMode(false)
    }
    override fun onDestroy() {
        super.onDestroy()
        beaconManager?.unbind(this)
    }
    override fun onBeaconServiceConnect() {
        beaconManager?.addRangeNotifier(object : RangeNotifier {
            override fun didRangeBeaconsInRegion(beaconList: MutableCollection<org.altbeacon.beacon.Beacon>?, p1: Region?) {
                beaconList?.sortedBy { beacon -> beacon.distance }
                try { //catches the empty list exception that .first() throws
                    val beacon = beaconList?.first() ?: return
                    val rssi = beacon.rssi.toString()
                    val uuid = beacon.getId1()?.toString()
                    val name = beacon.bluetoothName?.toString()
                    val major = beacon.getId2()?.toString()
                    val minor = beacon.getId3()?.toString()
                    val txPower = beacon.txPower.toString()
                    val distance = beacon.distance.toString()
                    runOnUiThread {
                        rssiTextView?.text =  "RSSI: $rssi"
                        uuidTextView?.text =  "UUID: $uuid"
                        foundTextView?.text =  "Found: $name"
                        majorTextView?.text =  "Major: $major"
                        minorTextView?.text =  "Minor: $minor"
                        txPowerTextView?.text =  "TxPower: $txPower"
                        distanceTextView?.text =  "Distance: $distance meters"
                    }
                } catch (e:Exception){
                    Log.e(TAG,e.localizedMessage)
                }
            }
        })
        try {
            beaconManager?.startRangingBeaconsInRegion(Region("myRangingUniqueId", null, null, null))
        } catch (e: RemoteException) {}
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted")
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Functionality limited")
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener { }
                    builder.show()
                }
                return
            }
        }
    }
    fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)) {
                AlertDialog.Builder(this)
                        .setTitle("Location Permission")
                        .setMessage("Ranging for beacons requires location permission")
                        .setPositiveButton("OK", object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(this@MainActivity,
                                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                                        MY_PERMISSIONS_REQUEST_LOCATION);}
                        })
                        .create()
                        .show();
            } else {// No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

}
