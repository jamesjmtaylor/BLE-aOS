package com.jamesjmtaylor.blecompose.connect

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.jamesjmtaylor.blecompose.BleViewModel
import com.jamesjmtaylor.blecompose.ConnectionStatus
import com.jamesjmtaylor.blecompose.NavActivity
import com.jamesjmtaylor.blecompose.ViewState
import com.jamesjmtaylor.blecompose.scan.PermissionView
import com.jamesjmtaylor.blecompose.scan.ScanResult
import com.jamesjmtaylor.blecompose.scan.ScanView
import com.jamesjmtaylor.blecompose.ui.theme.BLEComposeTheme

const val ConnectViewRoute = "ConnectView"

//Show name, then all details without connecting.  Back top left, Connect top right
@Composable
fun ConnectView(vm: BleViewModel, navController: NavController) {
    val context = LocalContext.current
    val viewState by vm.viewLiveData.observeAsState()
    var checkPermissions by remember{ mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth().padding(8.dp)){
            Button(onClick = {checkPermissions = true}) {
                Text(if (viewState?.connectionStatus == ConnectionStatus.disconnected) "Connect"
                    else "Disconnect")
            }
        }
        viewState?.scanResults?.let { results -> LazyColumn { items(results) {
            ScanResult(it, Modifier.clickable {
                navController.navigate(ConnectViewRoute) { launchSingleTop = true }
            }.fillMaxWidth().padding(16.dp, 8.dp))
        }}
        }
    }
    if (viewState?.state?.isNotEmpty() == true) {
        Toast.makeText(LocalContext.current, viewState?.state, Toast.LENGTH_LONG).show()
    }
    if (checkPermissions) {
        checkPermissions = false
        PermissionView(context,
            {  (context as NavActivity).bleService?.toggleConnect() },
            { Toast.makeText(context, "Cannot scan, permission denied", Toast.LENGTH_LONG).show() }
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode")
@Composable
fun PreviewConnectView() {
    val navController = rememberNavController()
    val vs = MutableLiveData<ViewState>()
    vs.value = ViewState(scanResults = SampleData.scanResults)
    BLEComposeTheme {
        ConnectView(BleViewModel(vs),navController)
    }
}