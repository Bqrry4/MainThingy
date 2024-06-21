package com.nyanthingy.mobileapp.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nyanthingy.mobileapp.modules.commons.extensions.ignoreParentPadding
import com.nyanthingy.mobileapp.modules.map.view.MapsView
import com.nyanthingy.mobileapp.ui.theme.NyanthingyAppTheme

@Composable
fun MapScreen() {
    NyanthingyAppTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .ignoreParentPadding(vertical = 20.dp), //make the map blend a little with the bottom bar
            color = MaterialTheme.colorScheme.background
        ) {
            MapsView()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    NyanthingyAppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            MapScreen()
        }
    }
}