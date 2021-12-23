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
import com.jamesjmtaylor.blecompose.*
import com.jamesjmtaylor.blecompose.scan.PermissionView
import com.jamesjmtaylor.blecompose.scan.ListItem
import com.jamesjmtaylor.blecompose.ui.theme.BLEComposeTheme

const val ConnectViewRoute = "ConnectView"

//Show name, then all details without connecting.  Back top left, Connect top right
@Composable
fun ConnectView(vm: BleViewModel, navController: NavController) {
    val context = LocalContext.current
    val viewState by vm.connectViewState.observeAsState()
    var checkPermissions by remember{ mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth().padding(8.dp)){
            Button(onClick = {
                (context as NavActivity).bleService?.toggleConnect(vm.selectedDevice)
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
        viewState?.services?.let { services -> LazyColumn { items(services) {
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
    vs.value = ConnectViewState(ConnectionStatus.connected)
    BLEComposeTheme {
        ConnectView(BleViewModel(connectViewMutableLiveData = vs),navController)
    }
}