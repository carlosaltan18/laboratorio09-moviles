package uvg.edu.laboratorio09.remoteorders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uvg.edu.laboratorio09.StoreApplication
import uvg.edu.laboratorio09.domain.repository.RemoteOrderNotFoundException
import uvg.edu.laboratorio09.domain.repository.RemoteOrderRepository

class OrdersViewModel(
    private val remoteOrderRepository: RemoteOrderRepository
) : ViewModel() {
    private val _ordersState = MutableStateFlow<OrdersUiState>(OrdersUiState.Idle)
    val ordersState: StateFlow<OrdersUiState> = _ordersState.asStateFlow()

    private val _detailState = MutableStateFlow<OrderDetailUiState>(OrderDetailUiState.Idle)
    val detailState: StateFlow<OrderDetailUiState> = _detailState.asStateFlow()

    private var detailJob: Job? = null
    private var selectedOrderId: String? = null

    fun loadOrders() {
        if (_ordersState.value is OrdersUiState.Loading) return
        _ordersState.value = OrdersUiState.Loading

        viewModelScope.launch {
            remoteOrderRepository.observeOrders().first().fold(
                onSuccess = { orders ->
                    _ordersState.value = if (orders.isEmpty()) {
                        OrdersUiState.Empty
                    } else {
                        OrdersUiState.Success(orders)
                    }
                },
                onFailure = {
                    _ordersState.value = OrdersUiState.Error(
                        "No pudimos obtener tus pedidos. Intenta de nuevo."
                    )
                }
            )
        }
    }

    fun loadOrder(orderId: String) {
        if (selectedOrderId == orderId && _detailState.value is OrderDetailUiState.Loading) {
            return
        }

        detailJob?.cancel()
        selectedOrderId = orderId
        _detailState.value = OrderDetailUiState.Loading
        detailJob = viewModelScope.launch {
            remoteOrderRepository.getOrder(orderId).fold(
                onSuccess = { order ->
                    _detailState.value = OrderDetailUiState.Success(order)
                },
                onFailure = { error ->
                    _detailState.value = if (error is RemoteOrderNotFoundException) {
                        OrderDetailUiState.NotFound
                    } else {
                        OrderDetailUiState.Error(
                            "No pudimos cargar el pedido. Intenta de nuevo."
                        )
                    }
                }
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[
                    ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                ] as StoreApplication
                OrdersViewModel(application.container.remoteOrderRepository)
            }
        }
    }
}
