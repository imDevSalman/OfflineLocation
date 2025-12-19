package com.example.offlinelocation.data.repository

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.offlinelocation.data.local.LocationDao
import com.example.offlinelocation.data.local.LocationEntity
import com.example.offlinelocation.data.sync.LocationSyncWorker
import com.example.offlinelocation.domain.mappers.toDomain
import com.example.offlinelocation.domain.mappers.toEntity
import com.example.offlinelocation.domain.model.Location
import com.example.offlinelocation.domain.repository.LocationRepository
import com.example.offlinelocation.location.LocationDataSource
import com.example.offlinelocation.location.batch
import com.example.offlinelocation.utils.Constants.BACKOFF_DELAY
import com.example.offlinelocation.utils.Constants.SYNC_WORK_NAME
import com.example.offlinelocation.utils.DataStore
import com.example.offlinelocation.utils.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val dao: LocationDao,
    private val workManager: WorkManager,
    private val dataStore: DataStore,
    private val locationDataSource: LocationDataSource,
    networkMonitor: NetworkMonitor,
) : LocationRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var trackingJob: Job? = null
    private val network = networkMonitor.isOnline
        .distinctUntilChanged()
        .stateIn(
            scope, SharingStarted.Eagerly, true
        )

    private val sharedLocationFlow: SharedFlow<Location> = tracking
        .distinctUntilChanged()
        .flatMapLatest { enabled ->
            if (!enabled) {
                emptyFlow()
            } else {
                dataStore.locationFlowInterval
                    .distinctUntilChanged()
                    .flatMapLatest { interval ->
                        locationDataSource.locationFlow(interval)
                            .map { it.toDomain() }
                    }
            }
        }
        .shareIn(scope, SharingStarted.WhileSubscribed(5_000))

    override val isOnline: Flow<Boolean>
        get() = network

    override val tracking: Flow<Boolean>
        get() = dataStore.tracking

    override suspend fun setTracking(enabled: Boolean) {
        dataStore.setTracking(enabled)
    }

    override fun observeLocation(): Flow<Location> = sharedLocationFlow

    override suspend fun setLocationFlowInterval(intervalMs: Long) {
        dataStore.setLocationFlowInterval(intervalMs)
    }

    override suspend fun setRoomBatchInterval(intervalMs: Long) {
        dataStore.setRoomBatchInterval(intervalMs)
    }

    override fun startTracking() {
        trackingJob?.cancel()

        trackingJob =
            sharedLocationFlow.combine(isOnline) { loc, online -> loc to online }
                .filter { (_, online) -> !online }.map { (loc, _) -> loc.toEntity() }
                .let { flow ->
                    dataStore.roomBatchInterval
                        .distinctUntilChanged()
                        .flatMapLatest { interval ->
                            flow.batch(interval)
                        }
                }.onEach { entities -> dao.insertAll(entities) }
                .launchIn(scope)

        network
            .filter { it }
            .onEach {
                triggerSync()
            }
            .launchIn(scope)
    }


    override suspend fun save(locations: List<Location>) {
        dao.insertAll(locations.map { it.toEntity() })
    }

    override fun pendingCount(): Flow<Int> = dao.pendingCountFlow()

    override fun getAll(): Flow<List<LocationEntity>> = dao.getAll()

    override fun stopTracking() {
        scope.launch {
            dataStore.setTracking(false)
        }
        trackingJob?.cancel()
        trackingJob = null
    }

    override suspend fun triggerSync() {
        val work = OneTimeWorkRequestBuilder<LocationSyncWorker>().setConstraints(
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        ).setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_DELAY, TimeUnit.SECONDS).build()
        workManager.enqueueUniqueWork(SYNC_WORK_NAME, ExistingWorkPolicy.KEEP, work)
    }
}