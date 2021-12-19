package com.jamesjmtaylor.blecompose.Scanning

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat

@Composable
fun PermissionView(context: Context, permissions: List<String>, onPermissionGranted: () -> Unit, onPermissionDenied: () -> Unit) {
    val permissionsGranted = permissions.map {
        (ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED)
    }.reduce { p1, p2 -> p1 && p2 }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        val allGranted = results.map { it.value }.reduce { p1, p2 -> p1 && p2 }
        if (allGranted) onPermissionGranted()
        else onPermissionDenied()
    }

    if (permissionsGranted) onPermissionGranted()
    else SideEffect { launcher.launch(permissions.toTypedArray()) }
}
