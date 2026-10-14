package uvg.edu.laboratorio09.remoteorders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import uvg.edu.laboratorio09.remote.OrderNetwork

class OrdersViewModel : ViewModel() {
    private val orderApi = OrderNetwork.api
    private val _ordersState = MutableStateFlow<OrdersUiState>(OrdersUiState.Idle)
    val ordersState: StateFlow<OrdersUiState> = _ordersState.asStateFlow()
    private val _detailState = MutableStateFlow<OrderDetailUiState>(OrderDetailUiState.Idle)
    val detailState: StateFlow<OrderDetailUiState> = _detailState.asStateFlow()
    private var detailJob: Job? = null
    private var selectedOrderId: String? = null

    fun loadOrders() {
        if (_ordersState.value is OrdersUiState.Loading) return
        // Se marca antes de lanzar para ignorar un segundo toque inmediato.
        _ordersState.value = OrdersUiState.Loading
        viewModelScope.launch {
            _ordersState.value = try {
                val orders = orderApi.getOrders()
                if (orders.isEmpty()) OrdersUiState.Empty else OrdersUiState.Success(orders)
            } catch (error: CancellationException) {
                throw error
            } catch (error: IOException) {
                OrdersUiState.Error("Revisa tu conexión e intenta de nuevo.")
            } catch (error: HttpException) {
                OrdersUiState.Error("No pudimos obtener tus pedidos. Intenta de nuevo.")
            } catch (error: SerializationException) {
                OrdersUiState.Error("Recibimos una respuesta inesperada. Intenta de nuevo.")
            }
        }
    }

    fun loadOrder(orderId: String) {
        if (selectedOrderId == orderId && _detailState.value is OrderDetailUiState.Loading) return
        // Si se vuelve a la lista y se elige otro pedido, la respuesta anterior
        // no debe reemplazar el detalle del nuevo ID.
        detailJob?.cancel()
        selectedOrderId = orderId
        _detailState.value = OrderDetailUiState.Loading
        detailJob = viewModelScope.launch {
            _detailState.value = try {
                OrderDetailUiState.Success(orderApi.getOrderById(orderId))
            } catch (error: CancellationException) {
                throw error
            } catch (error: IOException) {
                OrderDetailUiState.Error("Revisa tu conexión e intenta de nuevo.")
            } catch (error: HttpException) {
                if (error.code() == 404) OrderDetailUiState.NotFound
                else OrderDetailUiState.Error("No pudimos cargar el pedido. Intenta de nuevo.")
            } catch (error: SerializationException) {
                OrderDetailUiState.Error("Recibimos una respuesta inesperada. Intenta de nuevo.")
            }
        }
    }
}
