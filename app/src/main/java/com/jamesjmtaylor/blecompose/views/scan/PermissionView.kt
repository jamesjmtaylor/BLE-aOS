package com.jamesjmtaylor.blecompose.views.scan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import timber.log.Timber




@Composable
fun PermissionView(context: Context, onPermissionGranted: () -> Unit, onPermissionDenied: () -> Unit) {
    val permissions = mutableListOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.BLUETOOTH_ADMIN)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
    }
    val permissionsGranted = permissions.map {
        val granted = (ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED)
        Timber.d("$it permission already granted: $granted")
        return@map granted
    }.reduce { p1, p2 -> p1 && p2 }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        val allGranted = results.map {
            Timber.d("${it.key} permission granted: ${it.value}")
            it.value
        }.reduce { p1, p2 -> p1 && p2 }
        if (allGranted) onPermissionGranted()
        else onPermissionDenied()
    }

    if (permissionsGranted) onPermissionGranted()
    else SideEffect { launcher.launch(permissions.toTypedArray()) }
}
