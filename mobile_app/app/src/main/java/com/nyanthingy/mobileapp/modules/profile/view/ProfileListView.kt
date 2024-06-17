package com.nyanthingy.mobileapp.modules.profile.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nyanthingy.mobileapp.R
import com.nyanthingy.mobileapp.modules.commons.view.DraggableDropDown
import com.nyanthingy.mobileapp.modules.database.profile.model.ProfileEntry
import com.nyanthingy.mobileapp.modules.profile.model.ProfileModel
import com.nyanthingy.mobileapp.modules.profile.viewmodel.ProfileViewModel
import com.nyanthingy.mobileapp.ui.navigation.LeafRoute
import com.nyanthingy.mobileapp.ui.navigation.navigator.NavigationViewModel
import com.nyanthingy.mobileapp.ui.theme.NyanthingyAppTheme

@Composable
fun ProfileListView(
    profiles: List<ProfileModel>
) {
    val viewModel = hiltViewModel<ProfileViewModel>()
    val navigation = hiltViewModel<NavigationViewModel>()

    var dropDownItems: List<@Composable () -> Unit> = profiles.map {
        {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        CircleShape
                    )
            )
            {
                AsyncImage(
                    model = it.profileImageUri,
                    contentScale = ContentScale.Crop,
                    contentDescription = null
                )
            }
        }
    }

    val addProfileDropDownItem: @Composable () -> Unit = {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    CircleShape
                )
        )
        {
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
        }
    }

    dropDownItems = dropDownItems.plus (addProfileDropDownItem)

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
                    model = profiles.first().coverImageUri,
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
                    model = profiles.first().profileImageUri,
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
//            onCoverClick = {
//                coverImagePicker.launch(
//                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
//                )
//            },
//            onProfileClick = {
//                profileImagePicker.launch(
//                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
//                )
//            }
        )
        BasicTextField(
            value = profiles.first().name,
            onValueChange = {},
            textStyle = TextStyle(textAlign = TextAlign.Center)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ProfileStatusItem(ImageVector.vectorResource(R.drawable.gps), "GPS", true)
            Spacer(Modifier.width(5.dp))
            ProfileStatusItem(Icons.Default.NetworkCell, "Online", true)
            Spacer(Modifier.width(5.dp))
            ProfileStatusItem(ImageVector.vectorResource(R.drawable.battery), "99%", false)
        }
    }

    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = Modifier
            .padding(20.dp)
    ) {
        IconButton(onClick = {
            navigation.navigateTo(LeafRoute.Scanner.route)
        }) {
            Icon(
                Icons.Default.AddLink,
                contentDescription = "bindDevice"
            )
        }
    }

//    Box(
//        contentAlignment = Alignment.TopStart,
//        modifier = Modifier
//            .offset(x = 20.dp)
//    )
//    {
//
//    }

    DraggableDropDown(
        modifier = Modifier.zIndex(2f),
        composableList = dropDownItems,
        selectedIndex = viewModel.selectedProfile,
        onItemClick = {
            //The last one will be the add profile
            if (it == dropDownItems.count()) {
                navigation.navigateTo(LeafRoute.AddProfile.route)
            } else {
                viewModel.selectedProfile = it
            }
        }
    )
}


@Composable
fun ProfileStatusItem(
    icon: ImageVector,
    text: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.Green,
    inactiveColor: Color = Color.Red
) {

    Card(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.padding(horizontal = 40.dp, vertical = 2.dp)
        ) {
            Box {
                Icon(
                    icon,
                    text,
                    modifier = modifier
                        .size(32.dp)
                )
                Box(
                    modifier = modifier
                        .align(Alignment.TopEnd)
                        .offset(10.dp)
                ) {
                    StatusDot(
                        color = if (isActive) activeColor else inactiveColor,
                        size = 8.dp
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(1.dp),
            ) {

                Text(
                    text = text,
                    fontSize = 10.sp
                )
            }

        }
    }
}

@Composable
fun StatusDot(
    color: Color,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .size(size)
            .background(color)
    )
}


@Preview(showBackground = true)
@Composable
fun ProfileListViewPreview() {
    NyanthingyAppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ProfileListView(
                listOf(
                    ProfileModel(
                        name = "Cat1",
                        profileImageUri = "null"
                    ),
                    ProfileModel(
                        name = "Cat2",
                        profileImageUri = "null"
                    )
                )
            )
        }
    }
}
