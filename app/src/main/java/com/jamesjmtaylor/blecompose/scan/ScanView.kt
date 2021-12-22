package com.jamesjmtaylor.blecompose.scan

import android.bluetooth.le.ScanResult
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.jamesjmtaylor.blecompose.BleViewModel
import com.jamesjmtaylor.blecompose.NavActivity
import com.jamesjmtaylor.blecompose.ViewState
import com.jamesjmtaylor.blecompose.connect.ConnectViewRoute
import com.jamesjmtaylor.blecompose.ui.theme.BLEComposeTheme

const val ScanViewRoute = "ScanView"
@Composable
fun ScanView(vm: BleViewModel, navController: NavController) {
    val context = LocalContext.current
    val viewState by vm.viewLiveData.observeAsState()
    var checkPermissions by remember{ mutableStateOf(false) }
    if (viewState?.scanning == true) LoadingView()
    //Need a parent container to to keep children (scanButton & scanResults) from overlapping.
    // There are 3 choices:
    //Row: arranges children horizontally
    //Column: arranges children vertically
    //Box: arranges children relatively (can overlap)
    Column(Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth().padding(8.dp)){
            Button(onClick = {checkPermissions = true}) {
                Text(if (viewState?.scanning == true) "Stop Scan" else "Scan")
            }
        }
        viewState?.scanResults?.let { results -> LazyColumn { items(results) {
            ScanResult(it, Modifier.clickable {
                navController.navigate(ConnectViewRoute) { launchSingleTop = true }
            }.fillMaxWidth().padding(16.dp, 8.dp))
        }}}
    }
    if (viewState?.state?.isNotEmpty() == true) {
        Toast.makeText(LocalContext.current, viewState?.state, Toast.LENGTH_LONG).show()
    }
    if (checkPermissions) {
        checkPermissions = false
        PermissionView(context,
            {  (context as NavActivity).bleService?.toggleScan() },
            { Toast.makeText(context, "Cannot scan, permission denied", Toast.LENGTH_LONG).show() }
        )
    }
}

@Composable
fun ScanResult(s: ScanResult, modifier: Modifier) {
    Text(text = s.device?.name ?: s.device?.address ?: "No name assigned",
        fontSize = 16.sp,
        color= MaterialTheme.colors.secondaryVariant,
        modifier = modifier)
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode")
@Composable
fun PreviewScanView() {
    val navController = rememberNavController()
    val vs = MutableLiveData<ViewState>()
    vs.value = ViewState(scanResults = SampleData.scanResults)
    BLEComposeTheme {
        ScanView(BleViewModel(vs),navController)
    }
}
