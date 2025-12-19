package com.example.offlinelocation.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "location", indices = [Index(value = ["timestamp"], unique = true)])
data class LocationEntity(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long,
    val speed: Float,
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val synced: Boolean = false
)
