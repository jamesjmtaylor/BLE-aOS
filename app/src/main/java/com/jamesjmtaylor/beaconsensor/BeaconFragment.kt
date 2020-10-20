package com.jamesjmtaylor.beaconsensor

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.RemoteException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.altbeacon.beacon.*
import timber.log.Timber


class BeaconFragment : androidx.fragment.app.Fragment(), BeaconConsumer {
    private val PERMISSION_REQUEST_COARSE_LOCATION = 1
    private val MY_PERMISSIONS_REQUEST_LOCATION = 99
    var beaconManager: BeaconManager? = null

    //MARK: - Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}//Inflate arguments here

        if (checkLocationPermission()) {
            beaconManager = BeaconManager.getInstanceForApplication(applicationContext)
            beaconManager?.getBeaconParsers()?.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))
            beaconManager?.bind(this)
        }
        beaconManager = BeaconManager.getInstanceForApplication(applicationContext)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_beacon, container, false)
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

    override fun getApplicationContext(): Context {
        return App.instance.applicationContext
    }

    //MARK: - Beacon Logic
    override fun unbindService(p0: ServiceConnection?) {
        p0?.let { activity?.unbindService(it) }
    }

    override fun bindService(p0: Intent?, p1: ServiceConnection?, p2: Int): Boolean {
        p1?.let { return activity?.bindService(p0, it, p2) ?: false }
        return false

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
                    activity?.runOnUiThread {
                        //UPDATE UI HERE
                    }
                } catch (e: Exception) {
                    Timber.e(e, BeaconFragment::class.java.simpleName, e.localizedMessage)
                }
            }
        })
        try {
            beaconManager?.startRangingBeaconsInRegion(Region("myRangingUniqueId", null, null, null))
        } catch (e: RemoteException) {
        }
    }

    //MARK: - Permission Requests
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timber.d("coarse location permission granted")
                } else {
                    val builder = AlertDialog.Builder(applicationContext)
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

    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(applicationContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity as Activity,
                            Manifest.permission.ACCESS_COARSE_LOCATION)) {
                AlertDialog.Builder(applicationContext)
                        .setTitle("Location Permission")
                        .setMessage("Ranging for beacons requires location permission")
                        .setPositiveButton("OK", object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(activity as Activity,
                                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                                        MY_PERMISSIONS_REQUEST_LOCATION)
                            }
                        })
                        .create()
                        .show()
            } else {// No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity as Activity,
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        MY_PERMISSIONS_REQUEST_LOCATION)
            }
            return false;
        } else {
            return true;
        }
    }


    companion object {
        fun newInstance() =
                BeaconFragment().apply {
                    arguments = Bundle().apply {}//Apply constructor arguments to bundle as reqd
                }
    }
}
