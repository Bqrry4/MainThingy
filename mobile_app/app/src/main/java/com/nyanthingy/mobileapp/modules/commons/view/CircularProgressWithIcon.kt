package com.nyanthingy.mobileapp.modules.commons.view

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun CircularProgressWithIcon(
    imageVector: ImageVector,
    modifier: Modifier = Modifier
    ) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Icon(imageVector = imageVector, null)
        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
    }
}