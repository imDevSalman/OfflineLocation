package com.example.offlinelocation.ui

sealed interface LocationPermissionState {
    object Granted : LocationPermissionState
    object Denied : LocationPermissionState
    object PermanentlyDenied : LocationPermissionState
}
