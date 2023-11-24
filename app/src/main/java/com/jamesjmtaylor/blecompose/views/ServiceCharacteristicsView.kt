package com.jamesjmtaylor.blecompose.views

import SampleData
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.jamesjmtaylor.blecompose.services.BleViewModel
import com.jamesjmtaylor.blecompose.services.ConnectViewState
import com.jamesjmtaylor.blecompose.services.ConnectionStatus
import com.jamesjmtaylor.blecompose.NavActivity
import com.jamesjmtaylor.blecompose.models.GattCharacteristic
import com.jamesjmtaylor.blecompose.ui.theme.BLEComposeTheme
import com.jamesjmtaylor.blecompose.views.componentviews.ListItem
import java.util.*


const val ServiceCharacteristicsViewRoute = "ServiceCharacteristicsView"

//Show name, then all details without connecting.  Back top left, Connect top right
@ExperimentalUnsignedTypes
@Composable
fun ServiceCharacteristicsView(vm: BleViewModel, navController: NavController) {
    val context = LocalContext.current
    val connectViewState by vm.connectViewState.observeAsState()
    val characteristicViewState by vm.characteristicViewState.observeAsState()
    var expandedCharacteristic by remember{ mutableStateOf<UUID?>(null) }
    LazyColumn(Modifier.fillMaxSize()) {
        item { Text("Service Characteristics", Modifier.padding(16.dp,8.dp), MaterialTheme.colors.primary, 24.sp) }
        connectViewState?.characteristics?.let { characteristics ->
            items(characteristics) { characteristic ->
                ListItem(
                    GattCharacteristic.getCharacteristic(characteristic.uuid.toString())?.name ?: characteristic.uuid.toString(),
                    Modifier
                        .clickable {
                            //TODO: Finish work on expanding card by using https://proandroiddev.com/expandable-lists-in-jetpack-compose-b0b78c767b4
                            expandedCharacteristic =
                                if (characteristic.uuid == expandedCharacteristic) null
                                else characteristic.uuid
                            (context as NavActivity).bleService?.setCharacteristicNotification(
                                characteristic,
                                characteristic.uuid == expandedCharacteristic
                            )
                        }
                        .fillMaxWidth()
                        .padding(16.dp, 8.dp)
                )
            }
            characteristicViewState?.let {
                Toast.makeText(context,it,Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
@Preview(showBackground = true, name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode")
@Composable
fun PreviewServiceCharacteristicsView() {
    val navController = rememberNavController()
    val vs = MutableLiveData<ConnectViewState>()
    vs.value = ConnectViewState(ConnectionStatus.Connected, SampleData.discoveredServices, SampleData.characteristics)
    BLEComposeTheme {
        ServiceCharacteristicsView(BleViewModel(connectViewMutableLiveData = vs),navController)
    }
}