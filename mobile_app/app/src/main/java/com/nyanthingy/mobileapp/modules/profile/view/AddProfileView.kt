package com.nyanthingy.mobileapp.modules.profile.view

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nyanthingy.mobileapp.modules.ble.scanner.viewmodel.ScannerViewModel
import com.nyanthingy.mobileapp.modules.commons.extensions.clearFocusOnTap
import com.nyanthingy.mobileapp.modules.database.model.ProfileEntry
import com.nyanthingy.mobileapp.modules.profile.viewmodel.ProfileViewModel
import com.nyanthingy.mobileapp.ui.navigation.navigator.NavigationViewModel
import com.nyanthingy.mobileapp.ui.theme.NyanthingyAppTheme
import kotlinx.coroutines.launch

@Composable
fun AddProfileView(
    modifier: Modifier = Modifier
) {
    val navigation = hiltViewModel<NavigationViewModel>()
    val profileViewModel = hiltViewModel<ProfileViewModel>()
    val coroutineScope = rememberCoroutineScope()

    var coverImage by remember {
        mutableStateOf<Uri?>(null)
    }

    var profileImage by remember {
        mutableStateOf<Uri?>(null)
    }

    var profileName by remember {
        mutableStateOf("Name")
    }

    val coverImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                coverImage = uri
            }
        }
    )

    val profileImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                profileImage = uri
            }
        }
    )


//    val contentResolver = LocalContext.current.contentResolver
//    val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//    contentResolver.takePersistableUriPermission(profileImage!!, flags)


    //function to be called on save button press
    val saveProfile = suspend {

        //checks for validity



        profileViewModel.insert(
            ProfileEntry(
                name = profileName,
                profileImageUri = profileImage.toString()
            )
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clearFocusOnTap(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            CoverAndProfileImage(
                modifier = Modifier,
                coverImage = {
                    AsyncImage(
                        model = coverImage,
                        contentDescription = "coverImage",
                        modifier = Modifier
                            .fillMaxSize()
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary))
                            .background(MaterialTheme.colorScheme.background),
                        contentScale = ContentScale.Crop
                    )
                },
                profileImage = {
                    AsyncImage(
                        model = profileImage,
                        contentDescription = "profileImage",
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                CircleShape
                            )
                            .background(MaterialTheme.colorScheme.background),
                        contentScale = ContentScale.Crop
                    )
                },
                onCoverClick = {
                    coverImagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onProfileClick = {
                    profileImagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
            Spacer(modifier = Modifier.height(5.dp))
            BasicTextField(
                value = profileName,
                onValueChange = {
                    profileName = it
                },
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            )
        }
        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = modifier.padding(20.dp)
        ) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        saveProfile()
                        navigation.navigateBack(null)
                    }
                }) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddProfileViewPreview() {
    NyanthingyAppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AddProfileView()
        }
    }
}