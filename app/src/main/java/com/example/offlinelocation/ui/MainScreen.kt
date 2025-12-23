package com.example.offlinelocation.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.offlinelocation.service.LocationTrackingService

@Composable
fun MainScreen(
    modifier: Modifier = Modifier, viewModel: MainViewModel, requestPermissions: () -> Unit
) {
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val pending by viewModel.pendingCount.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionState by viewModel.permissionState.collectAsStateWithLifecycle()
    val tracking by viewModel.tracking.collectAsStateWithLifecycle()
//    val roomFlow by viewModel.roomFlow.collectAsStateWithLifecycle()
//    val locationFlow by viewModel.locationFlow.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isOnline) "🟢 Online" else "🔴 Offline",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Sync Pending: $pending", style = MaterialTheme.typography.labelMedium
        )
        Spacer(Modifier.height(16.dp))

        when (permissionState) {
            LocationPermissionState.Granted -> {
                Text("📍 Location permission granted")
            }

            LocationPermissionState.Denied -> {
                requestPermissions.invoke()
            }

            LocationPermissionState.PermanentlyDenied -> {
                Text("Permission permanently denied")
                Button(onClick = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                    )
                }) {
                    Text("Open App Settings")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        //Remove comment to configure location and room insertion intervals from UI (Milliseconds)
        /*
        Text("Location Interval")
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = { viewModel.updateLocationFlowInterval(1000) }) {
                Text("1s")
            }
            TextButton(onClick = { viewModel.updateLocationFlowInterval(5000) }) {
                Text("5s")
            }
            TextButton(onClick = { viewModel.updateLocationFlowInterval(10000) }) {
                Text("10s")
            }
        }

        Text("Room Batch Interval")
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = { viewModel.updateRoomBatchInterval(5000) }) {
                Text("5s")
            }
            TextButton(onClick = { viewModel.updateRoomBatchInterval(10000) }) {
                Text("10s")
            }
            TextButton(onClick = { viewModel.updateRoomBatchInterval(15000) }) {
                Text("15s")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))*/

        Button(onClick = {
            if (!tracking) {
                ContextCompat.startForegroundService(
                    context.applicationContext,
                    Intent(
                        context.applicationContext,
                        LocationTrackingService::class.java
                    )
                )

                viewModel.updateTrackingStatus(true)
            } else {
                context.applicationContext.stopService(
                    Intent(
                        context.applicationContext,
                        LocationTrackingService::class.java
                    )
                )

                viewModel.updateTrackingStatus(false)
            }

        }) {
            Text(if (tracking) "Stop Tracking" else "Start Tracking")
        }

        //Remove comment to observe location collection and room entries in UI
        /*
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Location Flow", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(locationFlow.size) {
                        Text("${it + 1}.) ${locationFlow[it].latitude}")
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Room Flow", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(roomFlow.size) {
                        Text("${it + 1}.) ${roomFlow[it].latitude} - sync: ${roomFlow[it].synced}")
                    }
                }
            }
        }*/
    }
}