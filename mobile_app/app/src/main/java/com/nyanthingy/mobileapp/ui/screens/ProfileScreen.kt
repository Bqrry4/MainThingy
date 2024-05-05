package com.nyanthingy.mobileapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyanthingy.mobileapp.R
import com.nyanthingy.mobileapp.ui.screens.view.scanner.ScannerView
import com.nyanthingy.mobileapp.ui.theme.NyanthingyAppTheme

@Composable
fun ProfileScreen() {
    NyanthingyAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {
            //list profiles which can be selected

            //button for add profile triggers scan view
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally,
//                modifier = Modifier
//                    .verticalScroll(rememberScrollState())
//                    .fillMaxSize()
//                    .padding(16.dp)
//            ) {
//                Image(
//                    painter = painterResource(R.drawable.cat),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(200.dp)
//                        .clip(CircleShape)
//                        .background(MaterialTheme.colorScheme.primary)
//                )
//                Image(
//                    painter = painterResource(R.drawable.cat),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .size(120.dp)
//                        .clip(CircleShape)
//                        .background(MaterialTheme.colorScheme.primary)
//                )
//                Text(
//                    text = "Sonya"
//                )
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(10.dp),
//
//
//                ) {
//                    ProfileStatusItem(ImageVector.vectorResource(R.drawable.gps), "GPS", true)
//                    ProfileStatusItem(Icons.Default.NetworkCell, "Online", true)
//                    ProfileStatusItem(ImageVector.vectorResource(R.drawable.battery), "99%", false)
//                }

 //           }
            ScannerView()
        }
    }
}

@Composable
fun ProfileStatusItem(
    icon: ImageVector,
    text: String,
    isActive: Boolean,
    activeColor: Color = Color.Green,
    inactiveColor: Color = Color.Red
) {
    Card(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary)

    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 40.dp, vertical = 2.dp)
        ) {

            Box{
                Icon(
                    icon,
                    text,
                    modifier = Modifier
                        .size(32.dp)
                )
                Box(
                    modifier = Modifier
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
            ){

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
fun ProfileScreenPreview() {
    NyanthingyAppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ProfileScreen()
        }
    }
}