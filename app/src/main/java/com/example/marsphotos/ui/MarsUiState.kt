package com.example.marsphotos.ui

import com.example.marsphotos.network.MarsPhoto

data class MarsUiState(
    val requestStatus: RequestStatus,
    val currentPhoto: MarsPhoto?
)
