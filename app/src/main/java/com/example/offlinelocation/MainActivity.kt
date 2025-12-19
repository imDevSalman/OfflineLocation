package com.example.offlinelocation

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.offlinelocation.ui.MainScreen
import com.example.offlinelocation.ui.MainViewModel
import com.example.offlinelocation.ui.theme.OfflineLocationTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { list ->
                val permanentlyDenied = list.any { (permission, granted) ->
                    !granted && !shouldShowRequestPermissionRationale(permission)
                }

                val granted = list.values.all { it }

                viewModel.updatePermissionState(
                    granted = granted,
                    permanentlyDenied = permanentlyDenied
                )
            }

        setContent {
            viewModel = hiltViewModel()
            OfflineLocationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel,
                        requestPermissions = ::requestPermissions
                    )
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        permissionLauncher.launch(
            permissions.toTypedArray()
        )
    }
}