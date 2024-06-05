package com.nyanthingy.mobileapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.nyanthingy.mobileapp.config.OsmdroidConfig
import com.nyanthingy.mobileapp.ui.navigation.Navigation
import com.nyanthingy.mobileapp.ui.theme.NyanthingyAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NyanthingyAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigation()
                }
            }
        }

        //Osmdroid Configuration
        OsmdroidConfig.config(applicationContext)
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    NyanthingyAppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Navigation()
        }
    }
}