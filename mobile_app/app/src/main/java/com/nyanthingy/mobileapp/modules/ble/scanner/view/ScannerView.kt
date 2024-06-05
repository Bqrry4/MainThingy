package com.nyanthingy.mobileapp.modules.ble.scanner.view

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nyanthingy.mobileapp.modules.permissions.RequireBluetooth
import com.nyanthingy.mobileapp.modules.ble.scanner.viewmodel.ScannerViewModel
import com.nyanthingy.mobileapp.modules.ble.scanner.viewmodel.ScanningState

@Composable
fun ScannerView(
) {
    RequireBluetooth(whenNotAvailable = {
        Text(text = it.toString())
    }) {
        val viewModel = hiltViewModel<ScannerViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle(ScanningState.Loading)

        ScannerStateView(state)
    }
}