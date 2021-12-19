package com.jamesjmtaylor.blecompose.Scanning

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat

@Composable
fun PermissionView(context: Context, permission: String, onPermissionGranted: () -> Unit, onPermissionDenied: () -> Unit) {
    val permissionGranted = (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) onPermissionGranted()
        else onPermissionDenied()
    }

    if (permissionGranted) onPermissionGranted()
    else SideEffect { launcher.launch(permission) }
}
