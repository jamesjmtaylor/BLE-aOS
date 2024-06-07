package com.jamesjmtaylor.blecompose.views

import SampleData
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.jamesjmtaylor.blecompose.NavActivity
import com.jamesjmtaylor.blecompose.services.BleViewModel
import com.jamesjmtaylor.blecompose.services.ScanViewState
import com.jamesjmtaylor.blecompose.ui.theme.BLEComposeTheme
import com.jamesjmtaylor.blecompose.views.componentviews.ListItem
import com.jamesjmtaylor.blecompose.views.componentviews.PermissionsRequest
import com.jamesjmtaylor.blecompose.views.componentviews.Spinner

const val ScanViewRoute = "ScanView"

@SuppressLint("MissingPermission") //Only displays results, does not fetch them.
@OptIn(ExperimentalUnsignedTypes::class)
@Composable
fun ScanView(vm: BleViewModel, navController: NavController) {
    val activity = LocalContext.current as? NavActivity
    val viewState by vm.scanViewLiveData.observeAsState()
    var checkPermissions by remember { mutableStateOf(false) }
    if (viewState?.scanning == true) Spinner()
    Column(Modifier.fillMaxSize()) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Button(onClick = {
                checkPermissions = true
            }) {
                Text(if (viewState?.scanning == true) "Stop Scan" else "Scan")
            }
        }
        viewState?.scanResults?.let { results ->
            LazyColumn {
                items(results) {
                    ListItem(it.device?.name ?: it.device?.address ?: "No name provided",
                        Modifier
                            .clickable {
                                vm.selectedDevice = it
                                navController.navigate(ConnectViewRoute) { launchSingleTop = true }
                            }
                            .fillMaxWidth()
                            .padding(16.dp, 8.dp)
                    )
                }
            }
        }
    }
    if (viewState?.state?.isNotEmpty() == true) {
        Toast.makeText(LocalContext.current, viewState?.state, Toast.LENGTH_LONG).show()
    }
    if (checkPermissions && activity != null) {
        checkPermissions = false
        PermissionsRequest(
            activity = activity,
            onPermissionsGranted = {
                activity.bleService?.launchForegroundNotification()
                activity.bleService?.toggleScan()
            },
            onPermissionsDenied = {
                navController.navigate(PermissionsDeniedViewRoute) { launchSingleTop = true }
            }
        )
    }
}


@OptIn(ExperimentalUnsignedTypes::class)
@Preview(showBackground = true, name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode")
@Composable
fun PreviewScanView() {
    val navController = rememberNavController()
    val vs = MutableLiveData<ScanViewState>()
    vs.value = ScanViewState(scanResults = SampleData.scanResults)
    BLEComposeTheme {
        ScanView(BleViewModel(vs), navController)
    }
}
