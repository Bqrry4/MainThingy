package com.nyanthingy.mobileapp.ui.screens.view.scanner

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nyanthingy.mobileapp.service.permission.RequireBluetooth
import com.nyanthingy.mobileapp.viewmodel.ScannerViewModel
import com.nyanthingy.mobileapp.viewmodel.ScanningState

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