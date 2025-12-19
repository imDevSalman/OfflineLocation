package com.example.offlinelocation.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(list: List<LocationEntity>)

    @Query("SELECT * FROM location ORDER BY timestamp ASC")
    fun getAll(): Flow<List<LocationEntity>>

    @Query("SELECT COUNT(*) FROM location WHERE synced = 0")
    fun pendingCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM location WHERE synced = 0")
    suspend fun pendingCount(): Int

    @Query("SELECT * FROM location WHERE synced = 0 ORDER BY timestamp")
    suspend fun getPending(): List<LocationEntity>

    @Query("UPDATE location SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)
}