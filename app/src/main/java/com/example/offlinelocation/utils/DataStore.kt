package com.example.offlinelocation.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.offlinelocation.utils.Constants.LOCATION_FLOW_INTERVAL
import com.example.offlinelocation.utils.Constants.ROOM_BATCH_INTERVAL
import com.example.offlinelocation.utils.Constants.STORE_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStore @Inject constructor(@ApplicationContext val context: Context) {
    private val Context.dataStore by preferencesDataStore(STORE_NAME)
    private val KEY_TRACKING = booleanPreferencesKey("tracking")
    private val KEY_LOCATION_FLOW_INTERVAL = longPreferencesKey("location_flow_interval")
    private val KEY_ROOM_BATCH_INTERVAL = longPreferencesKey("room_batch_interval")

    val tracking: Flow<Boolean> = context.dataStore.data.map { it[KEY_TRACKING] ?: false }
    val locationFlowInterval: Flow<Long> =
        context.dataStore.data.map { it[KEY_LOCATION_FLOW_INTERVAL] ?: LOCATION_FLOW_INTERVAL }
    val roomBatchInterval: Flow<Long> =
        context.dataStore.data.map { it[KEY_ROOM_BATCH_INTERVAL] ?: ROOM_BATCH_INTERVAL }

    fun isTrackingEnabled(): Boolean = runBlocking {
        tracking.first()
    }

    suspend fun setLocationFlowInterval(intervalMs: Long) {
        context.dataStore.edit { it[KEY_LOCATION_FLOW_INTERVAL] = intervalMs }
    }

    suspend fun setRoomBatchInterval(intervalMs: Long) {
        context.dataStore.edit { it[KEY_ROOM_BATCH_INTERVAL] = intervalMs }
    }

    suspend fun setTracking(enabled: Boolean) {
        context.dataStore.edit { it[KEY_TRACKING] = enabled }
    }
}