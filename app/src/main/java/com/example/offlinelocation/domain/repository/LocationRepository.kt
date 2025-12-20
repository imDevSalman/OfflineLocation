package com.example.offlinelocation.domain.repository

import com.example.offlinelocation.data.local.LocationEntity
import com.example.offlinelocation.domain.model.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LocationRepository {
    val isOnline: StateFlow<Boolean>
    val tracking: StateFlow<Boolean>
    suspend fun setTracking(enabled: Boolean)
    suspend fun setLocationFlowInterval(intervalMs: Long)
    suspend fun setRoomBatchInterval(intervalMs: Long)
    fun observeLocation(): Flow<Location>
    fun startTracking()
    fun stopTracking()
    suspend fun save(locations: List<Location>)
    fun pendingCount(): Flow<Int>
    fun getAll(): Flow<List<LocationEntity>>
    suspend fun triggerSync()
}