package com.nyanthingy.mobileapp.modules.ble.scanner.view

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nyanthingy.mobileapp.R
import com.nyanthingy.mobileapp.modules.ble.scanner.viewmodel.ScanningState
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResults

@Suppress("FunctionName")
fun LazyListScope.DeviceListItems(
    devices: ScanningState.DevicesDiscovered,
    onClick: (BleScanResults) -> Unit,
    deviceView: @Composable (BleScanResults) -> Unit,
) {
    val bondedDevices = devices.bonded
    val discoveredDevices = devices.notBonded

    if (bondedDevices.isNotEmpty()) {
        item {
            Text(
                text = stringResource(id = R.string.bonded_devices),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
        }
        items(bondedDevices.size) {
            ClickableDeviceItem(bondedDevices[it], onClick, deviceView)
        }
    }

    if (discoveredDevices.isNotEmpty()) {
        item {
            Text(
                text = stringResource(id = R.string.discovered_devices),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
        }

        items(discoveredDevices.size) {
            ClickableDeviceItem(discoveredDevices[it], onClick, deviceView)
        }
    }
    else
    {
        Log.println(Log.DEBUG, null, "empty discovered" + devices.devices.size)
    }
}

@Composable
private fun ClickableDeviceItem(
    device: BleScanResults,
    onClick: (BleScanResults) -> Unit,
    deviceView: @Composable (BleScanResults) -> Unit,
) {
    Box(modifier = Modifier
        .clip(RoundedCornerShape(10.dp))
        .clickable { onClick(device) }
        .padding(8.dp)
    ) {
        deviceView(device)
    }
}