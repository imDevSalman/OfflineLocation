package com.example.offlinelocation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.offlinelocation.R
import com.example.offlinelocation.domain.repository.LocationRepository
import com.example.offlinelocation.utils.Constants.CHANNEL_ID
import com.example.offlinelocation.utils.Constants.CHANNEL_NAME
import com.example.offlinelocation.utils.Constants.CONTENT_TEXT
import com.example.offlinelocation.utils.Constants.CONTENT_TITLE
import com.example.offlinelocation.utils.DataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@AndroidEntryPoint
class LocationTrackingService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Inject
    lateinit var repository: LocationRepository

    @Inject
    lateinit var dataStore: DataStore

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, notification())
        observeState()
        return START_STICKY
    }

    private fun observeState() {
        dataStore.tracking
            .distinctUntilChanged()
            .onEach { enabled ->
                if (enabled) {
                    repository.startTracking()
                } else {
                    repository.stopTracking()
                    stopSelf()
                }
            }
            .launchIn(scope)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

    }

    private fun notification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(CONTENT_TITLE)
            .setContentText(CONTENT_TEXT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }
}