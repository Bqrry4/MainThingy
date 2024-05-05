package com.nyanthingy.mobileapp.service.permission

import androidx.compose.runtime.Composable

@Composable
fun RequireLocation(
    whenNotAvailable: @Composable (BlePermissionNotAvailableReason) -> Unit = {},
    content: @Composable () -> Unit
) {

}