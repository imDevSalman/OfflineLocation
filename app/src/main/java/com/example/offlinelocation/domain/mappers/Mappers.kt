package com.example.offlinelocation.domain.mappers

import com.example.offlinelocation.data.local.LocationEntity
import com.example.offlinelocation.data.remote.model.LocationDto
import com.example.offlinelocation.domain.model.Location

fun android.location.Location.toDomain() = Location(
    latitude = latitude,
    longitude = longitude,
    accuracy = accuracy,
    timestamp = time,
    speed = speed
)

fun Location.toEntity() = LocationEntity(
    latitude = latitude,
    longitude = longitude,
    accuracy = accuracy,
    timestamp = timestamp,
    speed = speed
)

fun LocationEntity.toDto() = LocationDto(
    latitude = latitude,
    longitude = longitude,
    accuracy = accuracy,
    timestamp = timestamp,
    speed = speed
)

fun LocationEntity.toDomain() = Location(
    latitude = latitude,
    longitude = longitude,
    accuracy = accuracy,
    timestamp = timestamp,
    speed = speed
)

fun List<LocationEntity>.toDomainList() = this.map { it.toDomain() }