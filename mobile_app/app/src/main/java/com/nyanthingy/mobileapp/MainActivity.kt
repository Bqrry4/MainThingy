package com.nyanthingy.mobileapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nyanthingy.mobileapp.config.OsmdroidConfig
import com.nyanthingy.mobileapp.modules.ble.client.service.BleService
import com.nyanthingy.mobileapp.modules.ble.client.viewmodel.BleViewModel
import com.nyanthingy.mobileapp.modules.profile.viewmodel.ProfileViewModel
import com.nyanthingy.mobileapp.modules.workers.WorkScheduler
import com.nyanthingy.mobileapp.ui.navigation.Navigation
import com.nyanthingy.mobileapp.ui.theme.NyanthingyAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


val LocalActivity = staticCompositionLocalOf<ComponentActivity> {
    error("LocalActivity is not present")
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    @Inject
    lateinit var workScheduler: WorkScheduler

    //persist viewmodels
    private val profilesVM by viewModels<ProfileViewModel>()
    private val ble by viewModels<BleViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            //Start foreground services
            val serviceIntent = Intent(LocalContext.current, BleService::class.java)
            ContextCompat.startForegroundService(LocalContext.current, serviceIntent)
            //Start background workers
            workScheduler.scheduleFenceCheckWorker()

            NyanthingyAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CompositionLocalProvider(LocalActivity provides this@MainActivity) {
                        Navigation()
                    }
                }
            }
        }

        //Osmdroid Configuration
        OsmdroidConfig.config(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()

        //Notify service about application close
        val intent = Intent(this, BleService::class.java)
        intent.setAction(BleService.CLOSE_INTENT)
        startService(intent)

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