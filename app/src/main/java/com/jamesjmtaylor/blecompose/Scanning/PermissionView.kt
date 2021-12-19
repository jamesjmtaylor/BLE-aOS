package com.jamesjmtaylor.blecompose.Scanning

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import timber.log.Timber

@Composable
fun PermissionView(context: Context, permissions: List<String>, onPermissionGranted: () -> Unit, onPermissionDenied: () -> Unit) {
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
