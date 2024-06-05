package com.nyanthingy.mobileapp.modules.profile.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nyanthingy.mobileapp.R
import com.nyanthingy.mobileapp.modules.commons.view.OverlappingBoxes

@Composable
fun CoverAndProfileImage(
    modifier: Modifier = Modifier,
    coverImage: @Composable () -> Unit = {},
    profileImage: @Composable () -> Unit = {},
    onCoverClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    OverlappingBoxes(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(150.dp)
                .clickable { onCoverClick() }
        ) {
            coverImage()
        }

        Box(
            modifier = modifier
                .size(100.dp)
                .clip(CircleShape)
                .clickable { onProfileClick() }
        ) {
            profileImage()
        }
    }
}