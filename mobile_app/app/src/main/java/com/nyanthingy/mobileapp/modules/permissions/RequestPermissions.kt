package com.nyanthingy.mobileapp.modules.permissions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.nyanthingy.mobileapp.modules.permissions.features.PermissionManager

/**
 * Broadcasts PermissionManager.PERMISSIONS_REFRESH Intent
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestPermissions(
    permissions: List<String>,
    contentWhenRejected: @Composable () -> Unit = {},
    onPermissionChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val permissionState = rememberMultiplePermissionsState(permissions = permissions)
    { it ->
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

        //broadcast that the permission state was probably changed
        context.sendBroadcast(Intent().setAction(PermissionManager.PERMISSIONS_REFRESH))
    }

    if (!permissionState.allPermissionsGranted) {
        LaunchedEffect(permissionState) {
            permissionState.launchMultiplePermissionRequest()
        }
        contentWhenRejected()
    }

    //notify about the permission state
    onPermissionChange(permissionState.allPermissionsGranted)
}


internal fun Context.asActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Permissions request called outside of the context of an Activity")
}
