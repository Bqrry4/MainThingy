package com.nyanthingy.mobileapp.modules.profile.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nyanthingy.mobileapp.LocalActivity
import com.nyanthingy.mobileapp.modules.permissions.RequireStorageRead
import com.nyanthingy.mobileapp.modules.profile.viewmodel.ProfileViewModel
import com.nyanthingy.mobileapp.modules.profile.viewmodel.SelectedProfile

@Composable
fun ProfileView() {

//    val viewModel = hiltViewModel<ProfileViewModel>()
    val profilesVM: ProfileViewModel = hiltViewModel(LocalActivity.current)
    val state by profilesVM.state.collectAsStateWithLifecycle()

    if (state.profileModelList.isEmpty()) {
        NoProfileView()
    } else {
        RequireStorageRead {
            if (profilesVM.selectedProfile == null)
                profilesVM.selectedProfile = SelectedProfile(state.profileModelList.first(), 0)
            ProfileListView(state.profileModelList)
        }
    }
}
