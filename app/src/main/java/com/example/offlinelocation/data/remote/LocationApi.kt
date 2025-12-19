package com.example.offlinelocation.data.remote

import com.example.offlinelocation.data.remote.model.SyncRequest
import com.example.offlinelocation.data.remote.model.SyncResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface LocationApi {
    @POST("sync")
    suspend fun sync(@Body request: SyncRequest): SyncResponse
}