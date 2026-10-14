package uvg.edu.laboratorio09.remoteorders

import uvg.edu.laboratorio09.remote.dto.OrderDto

sealed interface OrderDetailUiState {
    data object Idle : OrderDetailUiState
    data object Loading : OrderDetailUiState
    data class Success(val order: OrderDto) : OrderDetailUiState
    data object NotFound : OrderDetailUiState
    data class Error(val message: String) : OrderDetailUiState
}
