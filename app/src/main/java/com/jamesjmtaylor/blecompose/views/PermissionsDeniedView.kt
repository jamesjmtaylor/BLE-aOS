package com.jamesjmtaylor.blecompose.views

import SampleData
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.jamesjmtaylor.blecompose.NavActivity
import com.jamesjmtaylor.blecompose.services.ConnectViewState
import com.jamesjmtaylor.blecompose.services.ConnectionStatus
import com.jamesjmtaylor.blecompose.ui.theme.BLEComposeTheme
import com.jamesjmtaylor.blecompose.views.componentviews.getGrantedAndDeniedPermissions
import com.jamesjmtaylor.blecompose.views.componentviews.getPermissionsRequiredByApiLevel


const val PermissionsDeniedViewRoute = "PermissionsDeniedView"

@Composable
fun PermissionsDeniedView(navController: NavController) {
    val packageName = LocalContext.current.packageName
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        val activity = LocalContext.current as? NavActivity
        val requiredPermissions = getPermissionsRequiredByApiLevel()
        val deniedPermissions =
            activity?.let { getGrantedAndDeniedPermissions(requiredPermissions, activity).second }
                ?: hashSetOf()
        Text(text = "The following permissions need to be granted manually:\n ${
            deniedPermissions
                .joinToString("") {
                    "\n• ${
                        it.split('.')
                            .last()
                            .replace("_", " ")
                            .lowercase()
                    }"
                }
        }")
        Button(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
            onClick = {
                //TODO: Handle user granting permissions.  The Settings app does not trigger the result in StartActivityForResult
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.setData(Uri.fromParts("package", packageName, null))
                activity?.startActivity(intent)
            }) {
            Text(text = "Open Settings")
        }
    }

}

@Preview(showBackground = true, name = "Light Mode", heightDp = 600, widthDp = 300)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode",
    heightDp = 600,
    widthDp = 300
)
@Composable
fun PreviewPermissionsDeniedView() {
    val navController = rememberNavController()
    val vs = MutableLiveData<ConnectViewState>()
    vs.value = ConnectViewState(ConnectionStatus.Connected, SampleData.discoveredServices)
    BLEComposeTheme {
        PermissionsDeniedView(navController)
    }
}