package com.nyanthingy.mobileapp.modules.map.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.maps.android.compose.MapType
import com.nyanthingy.mobileapp.modules.map.viewmodel.MapPreferencesViewModel
import com.nyanthingy.mobileapp.modules.map.viewmodel.MapSelection
import com.nyanthingy.mobileapp.ui.theme.NyanthingyAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPreferencesBottomSheet(
    onDismiss: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        MapPreferencesBottomSheetContent()
    }
}

@Composable
private fun MapPreferencesBottomSheetContent() {

    val viewModel = hiltViewModel<MapPreferencesViewModel>()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    )
    {
        Text(
            text = "Map Preferences",
            fontSize = 20.sp
        )
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(50.dp)
        )
        {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = {
                    viewModel.mapType = MapSelection.Google(MapType.NORMAL)
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null
                    )
                }
                Text(text = "Standard")
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = {
                    viewModel.mapType = MapSelection.Google(MapType.SATELLITE)
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null
                    )
                }
                Text(text = "Satellite")
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = {
                    viewModel.mapType = MapSelection.OsmDroid
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null
                    )
                }
                Text(text = "OpenStreetMap")
            }

        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(10.dp)
        ){
            Text(text ="Wllfsd")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MapPreferencesBottomSheetPreview() {
    NyanthingyAppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            MapPreferencesBottomSheetContent()
        }
    }
}
