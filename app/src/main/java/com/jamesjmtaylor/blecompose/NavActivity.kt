package com.jamesjmtaylor.blecompose

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jamesjmtaylor.blecompose.connect.ConnectView
import com.jamesjmtaylor.blecompose.connect.ConnectViewRoute
import com.jamesjmtaylor.blecompose.scan.ScanView
import com.jamesjmtaylor.blecompose.scan.ScanViewRoute
import com.jamesjmtaylor.blecompose.services.ScanListener
import com.jamesjmtaylor.blecompose.services.BleService
import com.jamesjmtaylor.blecompose.ui.theme.BLEComposeTheme

//Show name, then all details without connecting.  Back top left, Connect top right
class NavActivity : ComponentActivity() {
    var bleService: BleService? = null
    private var bleViewModel : BleViewModel? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            bleService = (service as BleService.LocalBinder).getService()
            bleService?.scanListener = bleViewModel as ScanListener
        }
        override fun onServiceDisconnected(p0: ComponentName?) {}
    }

    override fun onStart() {
        super.onStart()
        Intent(this, BleService::class.java).also { intent ->
            bindService(intent,connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bleViewModel = ViewModelProvider(this).get(BleViewModel::class.java)
        bleViewModel?.let { vm -> setContent { BLEComposeTheme {
            val navController = rememberNavController()
            val navHost = NavHost(navController, ScanViewRoute){
                composable(ScanViewRoute) { ScanView(vm,navController) }
                composable(ConnectViewRoute) { ConnectView(vm,navController)}
            }
        }}}
    }
}
