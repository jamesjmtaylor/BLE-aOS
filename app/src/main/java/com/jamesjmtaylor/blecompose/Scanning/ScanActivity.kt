package com.jamesjmtaylor.blecompose.Scanning

import SampleData
import android.Manifest
import android.bluetooth.le.ScanResult
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.jamesjmtaylor.blecompose.ScanViewModel
import com.jamesjmtaylor.blecompose.ViewState
import com.jamesjmtaylor.blecompose.services.BleListener
import com.jamesjmtaylor.blecompose.services.BleService
import com.jamesjmtaylor.blecompose.ui.theme.BLEComposeTheme

class ScanActivity : ComponentActivity() {
    var bleService: BleService? = null
    private var vm : ScanViewModel? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            bleService = (service as BleService.LocalBinder).getService()
            bleService?.bleListener = vm as BleListener
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
        vm = ViewModelProvider(this).get(ScanViewModel::class.java)
        vm?.let { setContent { BLEComposeTheme { ScanView(it) } } }
    }
}

@Composable
fun ScanView(vm: ScanViewModel) {
    ScanResults(vm)
}
@Composable
fun ScanResults(vm: ScanViewModel) {
    val context = LocalContext.current
    val permissions = mutableListOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.BLUETOOTH_ADMIN)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
    }

    val viewState by vm.viewLiveData.observeAsState()
    var checkPermissions by remember{ mutableStateOf(false) }
    if (checkPermissions) {
        checkPermissions = false
        PermissionView(context, permissions,
            onPermissionGranted = {  (context as ScanActivity).bleService?.toggleScan() },
            onPermissionDenied = { makeText(context, "Cannot scan, permission denied", Toast.LENGTH_LONG).show() }
        )
    }
    if (viewState?.scanning == true) LiveDataLoadingComponent()
    Column(Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)){
        Button(onClick = {
            checkPermissions = true
        }) {
            Text(if (viewState?.scanning == true) "Stop Scan" else "Scan")
        }}
        viewState?.scanResults?.let { results ->
            LazyColumn {
                items(results) {
                    ScanResult(it)
                }
            }
        }
    }
    if (viewState?.state?.isNotEmpty() == true) {
        makeText(LocalContext.current, viewState?.state, Toast.LENGTH_LONG).show()
    }
}

@Composable
fun LiveDataLoadingComponent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = CenterHorizontally
    ) {
        CircularProgressIndicator(modifier = Modifier.wrapContentWidth(CenterHorizontally))
    }
}

@Composable
fun ScanResult(s: ScanResult) {
        Text(text = s.device.name ?: s.device.address ?: "No name assigned",
            fontSize = 16.sp,
            color=colors.secondaryVariant,
            modifier = Modifier.clickable {
            //TODO: Connect & segue here
        }.fillMaxWidth().padding(16.dp, 8.dp))

}

@Preview(showBackground = true, name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode")
@Composable
fun PreviewConversation() {
    val vs = MutableLiveData<ViewState>()
    vs.value = ViewState(scanResults = SampleData.scanResults)
    val vm = ScanViewModel(vs)
    BLEComposeTheme {
        ScanResults(vm)
    }
}
