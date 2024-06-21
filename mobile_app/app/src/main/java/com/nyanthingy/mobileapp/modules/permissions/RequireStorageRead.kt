package com.nyanthingy.mobileapp.modules.permissions

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.nyanthingy.mobileapp.modules.permissions.features.PermissionManager

/**
 * @note This is not bonded to a feature
 */
@Composable
fun RequireStorageRead(
    whenNotAvailable: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val permissions = buildList {

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
                add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                add(Manifest.permission.READ_MEDIA_IMAGES)

            else -> add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    val context = LocalContext.current
    var permissionState by remember {
        mutableStateOf(
            PermissionManager.arePermissionsGranted(context, permissions)
        )
    }

    if (!permissionState) {
        RequestPermissions(
            permissions = permissions,
            contentWhenRejected = whenNotAvailable,
            onPermissionChange = {
                permissionState = it
            }
        )
    } else {
        content()
    }
}
