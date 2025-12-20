package com.example.offlinelocation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.offlinelocation.data.local.LocationEntity
import com.example.offlinelocation.domain.model.Location
import com.example.offlinelocation.domain.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: LocationRepository
) : ViewModel() {
//    private val MAX_ITEMS = 30

    private val _permissionState =
        MutableStateFlow<LocationPermissionState>(LocationPermissionState.Denied)
    val permissionState: StateFlow<LocationPermissionState> = _permissionState
    fun updatePermissionState(granted: Boolean, permanentlyDenied: Boolean) {
        _permissionState.value = when {
            granted -> LocationPermissionState.Granted
            permanentlyDenied -> LocationPermissionState.PermanentlyDenied
            else -> LocationPermissionState.Denied
        }
    }

    val tracking: StateFlow<Boolean> =
        repository.tracking.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun updateTrackingStatus(enabled: Boolean) =
        viewModelScope.launch(Dispatchers.IO) { repository.setTracking(enabled) }

    val isOnline: StateFlow<Boolean> = repository.isOnline.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    val pendingCount: StateFlow<Int> = repository.pendingCount().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    //Remove comment to observe location collection and room entries in UI
    /*
    fun updateLocationFlowInterval(intervalMs: Long) = viewModelScope.launch {
        repository.setLocationFlowInterval(intervalMs)
    }

    fun updateRoomBatchInterval(intervalMs: Long) = viewModelScope.launch {
        repository.setRoomBatchInterval(intervalMs)
    }

    val roomFlow: StateFlow<List<LocationEntity>> = repository.getAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val locationFlow: StateFlow<List<Location>> = tracking.flatMapLatest { enabled ->
        if (!enabled) {
            flowOf(emptyList())
        } else {
            repository.observeLocation()
                .scan(emptyList<Location>()) { acc, location -> (acc + location).takeLast(MAX_ITEMS) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )*/
}