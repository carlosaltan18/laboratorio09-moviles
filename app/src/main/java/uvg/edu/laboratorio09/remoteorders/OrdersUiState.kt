package uvg.edu.laboratorio09.remoteorders

import uvg.edu.laboratorio09.remote.dto.OrderDto

sealed interface OrdersUiState {
    data object Idle : OrdersUiState
    data object Loading : OrdersUiState
    data object Empty : OrdersUiState
    data class Success(val orders: List<OrderDto>) : OrdersUiState
    data class Error(val message: String) : OrdersUiState
}
