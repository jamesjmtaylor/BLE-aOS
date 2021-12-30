package com.jamesjmtaylor.blecompose.views

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
import com.jamesjmtaylor.blecompose.ConnectViewState
import com.jamesjmtaylor.blecompose.ConnectionStatus
import com.jamesjmtaylor.blecompose.NavActivity
import com.jamesjmtaylor.blecompose.models.GattCharacteristic
import com.jamesjmtaylor.blecompose.ui.theme.BLEComposeTheme
import com.jamesjmtaylor.blecompose.views.scan.ListItem
import com.jamesjmtaylor.blecompose.views.scan.LoadingView


const val ServiceCharacteristicsViewRoute = "ServiceCharacteristicsView"

//Show name, then all details without connecting.  Back top left, Connect top right
@Composable
fun ServiceCharacteristicsView(vm: BleViewModel, navController: NavController) {
    val context = LocalContext.current
    val viewState by vm.connectViewState.observeAsState()
    val name = vm.selectedService


    LazyColumn(Modifier.fillMaxSize()) {
        item { Text("Service Characteristics", Modifier.padding(16.dp,8.dp), MaterialTheme.colors.primary, 24.sp) }
        viewState?.characteristics?.let { characteristics -> items(characteristics) {
            ListItem(
                GattCharacteristic.getCharacteristic(it.uuid.toString())?.name ?: it.uuid.toString(),
                Modifier.clickable {
                //TODO: Expand to show nested characteristics
            }.fillMaxWidth().padding(16.dp, 8.dp))
        }}

    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode")
@Composable
fun PreviewServiceCharacteristicsView() {
    val navController = rememberNavController()
    val vs = MutableLiveData<ConnectViewState>()
    vs.value = ConnectViewState(ConnectionStatus.connected, SampleData.discoveredServices)
    BLEComposeTheme {
        ServiceCharacteristicsView(BleViewModel(connectViewMutableLiveData = vs),navController)
    }
}