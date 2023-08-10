package com.example.marsphotos.ui

import com.example.marsphotos.network.MarsPhoto
import com.example.marsphotos.ui.screens.RequestStatus

data class MarsUiState(
    val requestStatus: RequestStatus,
    val currentPhoto: MarsPhoto?
)
