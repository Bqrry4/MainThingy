package com.nyanthingy.mobileapp.modules.profile.view

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nyanthingy.mobileapp.R
import com.nyanthingy.mobileapp.modules.commons.view.DraggableDropDown
import com.nyanthingy.mobileapp.modules.commons.view.DropDownSelect
import com.nyanthingy.mobileapp.modules.commons.view.OverlappingBoxes
import com.nyanthingy.mobileapp.modules.database.model.ProfileEntry
import com.nyanthingy.mobileapp.modules.profile.viewmodel.ProfileViewModel
import com.nyanthingy.mobileapp.ui.navigation.LeafRoute
import com.nyanthingy.mobileapp.ui.navigation.navigator.NavigationViewModel
import com.nyanthingy.mobileapp.ui.theme.NyanthingyAppTheme
import kotlin.math.roundToInt

@Composable
fun ProfileListView(
    profiles: List<ProfileEntry>
) {
    val viewModel = hiltViewModel<ProfileViewModel>()
    val navigation = hiltViewModel<NavigationViewModel>()

    val dropDownItems: List<@Composable () -> Unit> = profiles.map {
        {
//                val contentResolver = LocalContext.current.contentResolver
//                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//                contentResolver.takePersistableUriPermission(Uri.parse(it.profileImageUri), flags)

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
            )
            {
                AsyncImage(
                    model = it.profileImageUri,
                    contentDescription = null
                )
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
    ) {
        CoverAndProfileImage()
        BasicTextField(
            value = "Sonya",
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
            Icon(Icons.Default.AddLink, contentDescription = "bindDevice")
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
            viewModel.selectedProfile = it
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
                    ProfileEntry(
                        name = "Cat1",
                        profileImageUri = "null"
                    ),
                    ProfileEntry(
                        name = "Cat2",
                        profileImageUri = "null"
                    )
                )
            )
        }
    }
}
