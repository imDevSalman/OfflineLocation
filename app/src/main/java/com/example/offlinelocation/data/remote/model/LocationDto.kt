package com.example.offlinelocation.data.remote.model

import com.example.offlinelocation.utils.Constants.EMPLOYEE_ID

data class LocationDto(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long,
    val speed: Float,
    val employeeId: String = EMPLOYEE_ID
)
