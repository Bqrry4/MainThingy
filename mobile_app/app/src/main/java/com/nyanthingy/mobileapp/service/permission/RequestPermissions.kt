package com.nyanthingy.mobileapp.service.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestPermissions(
    permissions: List<String>,
    notGranted: @Composable () -> Unit = {},
    granted: @Composable () -> Unit
) {
    val context = LocalContext.current
    val state = rememberMultiplePermissionsState(permissions = permissions)
//    { it ->
//        val needToSetting = it.filter {
//            !ActivityCompat.shouldShowRequestPermissionRationale(
//                context.findActivity(),
//                it.key
//            ) && !it.value
//        }.isNotEmpty()
//        if (needToSetting) {
//            val intent = Intent(
//                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
//                Uri.parse("package:" + context.packageName)
//            )
//            context.startActivity(intent)
//        }
//    }
    Log.println(Log.DEBUG, null, state.allPermissionsGranted.toString())
    state.revokedPermissions.forEach{
        Log.println(Log.DEBUG, null, it.permission + it.status.toString())
    }
    if (state.allPermissionsGranted) {
        granted()
    } else {
        LaunchedEffect(state) {
//            state.permissions.forEach{
//                it.launchPermissionRequest()
//            }
            state.launchMultiplePermissionRequest()
        }
        notGranted()
    }

//    if (ActivityCompat.checkSelfPermission(
//            LocalContext.current,
//            Manifest.permission.BLUETOOTH_SCAN
//        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
//            LocalContext.current,
//            Manifest.permission.BLUETOOTH_CONNECT
//        ) != PackageManager.PERMISSION_GRANTED
//    ) {
//
//        ActivityCompat.requestPermissions(LocalContext.current.findActivity(), arrayOf(Manifest.permission_group.NEARBY_DEVICES), 0)
//    }

}

internal fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Permissions should be called in the context of an Activity")
}