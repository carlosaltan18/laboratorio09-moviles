package uvg.edu.laboratorio09.domain.repository

import kotlinx.coroutines.flow.Flow
import uvg.edu.laboratorio09.model.CreateRemoteOrder
import uvg.edu.laboratorio09.model.RemoteOrder

interface RemoteOrderRepository {
    suspend fun createOrder(request: CreateRemoteOrder): Result<RemoteOrder>

    fun observeOrders(): Flow<Result<List<RemoteOrder>>>

    suspend fun getOrder(orderId: String): Result<RemoteOrder>
}

class RemoteOrderNotFoundException : IllegalStateException("Pedido no encontrado")
