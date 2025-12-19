package com.example.offlinelocation.data.sync

import com.example.offlinelocation.data.local.LocationDao
import com.example.offlinelocation.data.remote.LocationApi
import com.example.offlinelocation.data.remote.model.SyncRequest
import com.example.offlinelocation.domain.mappers.toDto
import javax.inject.Inject

class LocationSyncEngine @Inject constructor(
    val dao: LocationDao,
    val locationApi: LocationApi
) {
    suspend fun sync(): Boolean {
        val batch = dao.getPending()
        if (batch.isEmpty()) return true

        return try {
            val request = SyncRequest(batch.map { it.toDto() })
            val res = locationApi.sync(request)
            if (res.status) {
                dao.markSynced(batch.map { it.id })
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}