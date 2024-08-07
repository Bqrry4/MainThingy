package com.nyanthingy.mobileapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nyanthingy.mobileapp.ui.theme.NyanthingyAppTheme
import com.nyanthingy.mobileapp.R
import com.nyanthingy.mobileapp.modules.commons.extensions.ignoreParentPadding
import com.nyanthingy.mobileapp.modules.map.view.MapsView
import com.nyanthingy.mobileapp.modules.wellness.view.WellnessView

@Composable
fun WellnessScreen() {
    NyanthingyAppTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .ignoreParentPadding(vertical = 20.dp), //make the map blend a little with the bottom bar
            color = MaterialTheme.colorScheme.background
        ) {
            WellnessView()
        }
    }
}