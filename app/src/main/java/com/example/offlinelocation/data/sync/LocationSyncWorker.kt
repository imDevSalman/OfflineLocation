package com.example.offlinelocation.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.offlinelocation.data.local.AppDatabase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.random.Random

@HiltWorker
class LocationSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parameters: WorkerParameters,
    val engine: LocationSyncEngine
) :
    CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        return if (engine.sync()) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}