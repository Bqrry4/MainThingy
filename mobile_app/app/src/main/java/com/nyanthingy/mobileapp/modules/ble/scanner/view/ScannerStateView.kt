package com.nyanthingy.mobileapp.modules.ble.scanner.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nyanthingy.mobileapp.ui.theme.NyanthingyAppTheme
import com.nyanthingy.mobileapp.modules.ble.scanner.viewmodel.ScanningState
import com.nyanthingy.mobileapp.modules.commons.view.CircularProgressWithIcon
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResult

@Composable
fun ScannerStateView(
    state: ScanningState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp)
    ) {

        when (state) {
            is ScanningState.Loading -> item {
                ScanEmptyView()
            }

            is ScanningState.DevicesDiscovered -> {
                if (state.devices.isEmpty()) {
                    item {
                        ScanEmptyView()
                    }
                } else {
                    DeviceListItems(state,
                        {
                            println(it.toString())
                        },
                        {
                            ListBleDevice(it.advertisedName ?: it.device.name, it.device.address)
                        }
                    )
                }
            }

            is ScanningState.Error -> item {
                Text("error" + state.errorCode)
            }
        }
    }
}

@Composable
fun ScanEmptyView() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    )
    {
        CircularProgressWithIcon(
            imageVector = Icons.AutoMirrored.Filled.BluetoothSearching,
            progressColor = MaterialTheme.colorScheme.onPrimary
        )
    }
}


@Preview
@Composable
fun ScannerStateViewPreviewWhenLoading() {
    NyanthingyAppTheme {
        ScannerStateView(ScanningState.Loading)
    }
}

@Preview
@Composable
fun ScannerStateViewPreviewWhenError() {
    NyanthingyAppTheme {
        ScannerStateView(ScanningState.Error(0))
    }
}