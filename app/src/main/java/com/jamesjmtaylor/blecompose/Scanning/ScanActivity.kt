package com.jamesjmtaylor.blecompose.Scanning

import android.Manifest
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.lifecycle.MutableLiveData
import com.jamesjmtaylor.blecompose.ScanViewModel
import com.jamesjmtaylor.blecompose.ViewState
import com.jamesjmtaylor.blecompose.services.BleService
import com.jamesjmtaylor.blecompose.ui.theme.BLEComposeTheme

class ScanActivity : ComponentActivity() {
    val vm : ScanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val serviceIntent = Intent(this, BleService::class.java)
        bindService(serviceIntent, vm.serviceConnection, BIND_AUTO_CREATE)
        setContent { BLEComposeTheme { ScanView(vm) } }
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
        Manifest.permission.FOREGROUND_SERVICE)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
    }
    val viewState by vm.viewLiveData.observeAsState()
    var showPermissionRequest by remember{ mutableStateOf(false) }
    if (showPermissionRequest) {
        PermissionView(context, permissions,
            onPermissionGranted = { vm.toggleScan() },
            onPermissionDenied = { makeText(context, "Cannot scan, permission denied", Toast.LENGTH_LONG).show() }
        )
    }
    if (viewState?.scanning == true) LiveDataLoadingComponent()
    Column(Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)){
        Button(onClick = {
            showPermissionRequest = true
        }) {
            Text(if (viewState?.scanning == true) "Scanning" else "Scan")
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(modifier = Modifier.wrapContentWidth(CenterHorizontally))
    }
}

@Composable
fun ScanResult(s: ScanResult) {
        Column(modifier = Modifier.clickable {
            //TODO: Connect & segue here
        }) {
            Text(s.scanRecord?.deviceName ?: "No name assigned", color=colors.secondaryVariant)
        }
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
