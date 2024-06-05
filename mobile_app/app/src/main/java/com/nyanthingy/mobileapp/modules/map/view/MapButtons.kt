package com.nyanthingy.mobileapp.modules.map.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Map Buttons aligned at the bottom right corner
 */
@Composable
fun MapButtons(
    modifier: Modifier = Modifier,
    onLocationButtonClick: (() -> Unit) = { },
    onMapPreferencesClick: (() -> Unit) = { },
) {
    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = modifier
    ) {
        Column {
            //Location Button
            IconButton(
                colors = IconButtonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledContentColor = MaterialTheme.colorScheme.secondary
                ),
                onClick = onLocationButtonClick
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null
                )
            }

            //ChangeMapPreferences button
            IconButton(
                colors = IconButtonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledContentColor = MaterialTheme.colorScheme.secondary
                ),
                onClick = onMapPreferencesClick
            ) {
                Icon(
                    imageVector = Icons.Outlined.Map,
                    contentDescription = null
                )
            }
        }
    }
}