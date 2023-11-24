package com.jamesjmtaylor.blecompose.views.componentviews

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import timber.log.Timber

/**
 * Requests permissions in a composable context.  See
 * [Bluetooth Permissions](https://developer.android.com/develop/connectivity/bluetooth/bt-permissions)
 */
@Composable
fun PermissionsRequest(
    activity: Activity,
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: (Set<String>) -> Unit
) {
    val requiredPermissions = getPermissionsRequiredByApiLevel()
    val (grantedPermissions, deniedPermissions) = getGrantedAndDeniedPermissions(
        requiredPermissions,
        activity
    )
    val request = ActivityResultContracts.RequestMultiplePermissions()
    val launcher = rememberLauncherForActivityResult(request) { grantedMap ->
        grantedMap.entries.forEach {
            if (it.value) grantedPermissions.add(it.key) else deniedPermissions.add(it.key)
            if (deniedPermissions.isNotEmpty()) onPermissionsDenied(deniedPermissions) else onPermissionsGranted()
        }
    }
    val unrequestedPermissions = requiredPermissions - deniedPermissions - grantedPermissions
    if (unrequestedPermissions.isNotEmpty()) {
        SideEffect { launcher.launch((unrequestedPermissions).toTypedArray()) }
    } else if (deniedPermissions.isNotEmpty()){
        onPermissionsDenied(deniedPermissions)
    } else {
        onPermissionsGranted()
    }
}

/**
 * Retrieves which permissions have already been approved or denied. This is important because if
 * you try & request a granted or denied permission nothing will happen.
 */
@Composable
fun getGrantedAndDeniedPermissions(
    requiredPermissions: HashSet<String>,
    activity: Activity
): Pair<HashSet<String>, HashSet<String>> {
    val deniedPermissions = hashSetOf<String>()
    val grantedPermissions = hashSetOf<String>()

    requiredPermissions.forEach { permission ->
        val status = ContextCompat.checkSelfPermission(activity, permission)
        if (status == PackageManager.PERMISSION_GRANTED) {
            Timber.d("permission granted: $permission")
            grantedPermissions.add(permission)
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            Timber.d("shouldShowRequestPermissionRationale: $permission")
            deniedPermissions.add(permission)
        }
    }
    return Pair(grantedPermissions, deniedPermissions)
}

/**
 * Retrieves the list of permissions required for BLE  for the user's Android OS version.
 */
@Composable
fun getPermissionsRequiredByApiLevel(): HashSet<String> {
    val requiredPermissions = hashSetOf(Manifest.permission.FOREGROUND_SERVICE)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        //Not strictly required after API 31, but enables you to discover eddystone beacons.
        requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
        requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
    } else { // Android 11 or lower
        requiredPermissions.add(Manifest.permission.BLUETOOTH)
    }
    return requiredPermissions
}
