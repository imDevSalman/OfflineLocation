package com.example.offlinelocation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.offlinelocation.service.LocationTrackingService
import com.example.offlinelocation.utils.DataStore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject
    lateinit var dataStore: DataStore

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val enabled = dataStore.isTrackingEnabled()
        if (!enabled) return

        ContextCompat.startForegroundService(
            context,
            Intent(context, LocationTrackingService::class.java)
        )
    }

}