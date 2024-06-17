package com.nyanthingy.mobileapp.modules.location.repository

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationTracker {
    fun locationFlow() : Flow<Location>
}