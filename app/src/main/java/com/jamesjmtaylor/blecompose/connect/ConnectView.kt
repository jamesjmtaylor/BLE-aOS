package com.jamesjmtaylor.blecompose.connect

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
import com.jamesjmtaylor.blecompose.*
import com.jamesjmtaylor.blecompose.scan.PermissionView
import com.jamesjmtaylor.blecompose.scan.ListItem
import com.jamesjmtaylor.blecompose.scan.LoadingView
import com.jamesjmtaylor.blecompose.ui.theme.BLEComposeTheme
import timber.log.Timber

const val ConnectViewRoute = "ConnectView"

//Show name, then all details without connecting.  Back top left, Connect top right
@Composable
fun ConnectView(vm: BleViewModel, navController: NavController) {
    val context = LocalContext.current
    val viewState by vm.connectViewState.observeAsState()
    val name = vm.selectedDevice?.device?.name ?: vm.selectedDevice?.device?.address ?: "No name assigned"
    var checkPermissions by remember{ mutableStateOf(false) }
    if (viewState?.connectionStatus == ConnectionStatus.connecting
        || viewState?.connectionStatus == ConnectionStatus.disconnecting ) LoadingView()
    Column(Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth().padding(8.dp)){
            Button(onClick = {
                val svc = (context as NavActivity).bleService
                if (vm.selectedDevice?.isConnectable == true) svc?.toggleConnect(vm.selectedDevice?.device)
                else Toast.makeText(context,"Device is not connectable", Toast.LENGTH_LONG).show()
            }) {
                Text(when (viewState?.connectionStatus) {
                    ConnectionStatus.disconnected-> "Connect"
                    ConnectionStatus.disconnecting -> "Disconnecting"
                    ConnectionStatus.connected -> "Disconnect"
                    ConnectionStatus.connecting -> "Connecting"
                    else -> "Connect"
                })
            }
        }
        LazyColumn {
            item { Text("Scan Data", Modifier.padding(16.dp,8.dp), MaterialTheme.colors.primary, 24.sp) }
            item { Text("Name: $name", Modifier.padding(16.dp,8.dp), color= MaterialTheme.colors.onBackground) }
            item { Text("RSSI: ${vm.selectedDevice?.rssi}", Modifier.padding(16.dp,8.dp), color= MaterialTheme.colors.onBackground) }
            item { Text("Timestamp Nanos: ${vm.selectedDevice?.timestampNanos}", Modifier.padding(16.dp,8.dp), color= MaterialTheme.colors.onBackground) }
            item { Text("Tx Power: ${vm.selectedDevice?.txPower}", Modifier.padding(16.dp,8.dp), color= MaterialTheme.colors.onBackground) }
            item { Text("Is Connectable: ${vm.selectedDevice?.isConnectable}", Modifier.padding(16.dp,8.dp), color= MaterialTheme.colors.onBackground) }
            item { Text("Primary Phy: ${vm.selectedDevice?.primaryPhy}", Modifier.padding(16.dp,8.dp), color= MaterialTheme.colors.onBackground) }
            item { Text("Secondary Phy: ${vm.selectedDevice?.secondaryPhy}", Modifier.padding(16.dp,8.dp), color= MaterialTheme.colors.onBackground) }
            item { Text("Discovered Services", Modifier.padding(16.dp,8.dp), MaterialTheme.colors.primary, 24.sp) }
            viewState?.services?.let { services -> items(services) {
                ListItem(it.uuid.toString(), Modifier.clickable {
                    //TODO: Expand to show nested characteristics
                }.fillMaxWidth().padding(16.dp, 8.dp))
            }}
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode")
@Composable
fun PreviewConnectView() {
    val navController = rememberNavController()
    val vs = MutableLiveData<ConnectViewState>()
    vs.value = ConnectViewState(ConnectionStatus.connected, SampleData.discoveredServices)
    BLEComposeTheme {
        ConnectView(BleViewModel(connectViewMutableLiveData = vs),navController)
    }
}