package com.example.offlinelocation.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.offlinelocation.data.local.LocationDao
import com.example.offlinelocation.utils.Constants.BACKOFF_DELAY
import com.example.offlinelocation.utils.Constants.SYNC_WORK_NAME
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class PeriodicSyncGuardWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val dao: LocationDao,
    private val workManager: WorkManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (dao.pendingCount() > 0) {
            enqueueSync()
        }
        return Result.success()
    }

    private fun enqueueSync() {
        val syncWork =
            OneTimeWorkRequestBuilder<LocationSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    BACKOFF_DELAY,
                    TimeUnit.SECONDS
                )
                .build()

        workManager.enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            syncWork
        )
    }
}