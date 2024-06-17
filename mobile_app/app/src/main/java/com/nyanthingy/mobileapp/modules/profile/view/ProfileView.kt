package com.nyanthingy.mobileapp.modules.profile.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nyanthingy.mobileapp.modules.permissions.RequireStorageRead
import com.nyanthingy.mobileapp.modules.profile.viewmodel.ProfileViewModel

@Composable
fun ProfileView() {

    val viewModel = hiltViewModel<ProfileViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.profileModelList.isEmpty())
    {
        NoProfileView()
    }
    else
    {
        RequireStorageRead {
            ProfileListView(state.profileModelList)
        }
    }
}
