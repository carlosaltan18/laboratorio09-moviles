package uvg.edu.laboratorio09.data.remote

import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import uvg.edu.laboratorio09.domain.repository.RemoteOrderNotFoundException
import uvg.edu.laboratorio09.domain.repository.RemoteOrderRepository
import uvg.edu.laboratorio09.model.CreateRemoteOrder
import uvg.edu.laboratorio09.model.RemoteOrder
import uvg.edu.laboratorio09.model.RemoteOrderLine
import uvg.edu.laboratorio09.remote.OrderApi
import uvg.edu.laboratorio09.remote.dto.CreateOrderRequest
import uvg.edu.laboratorio09.remote.dto.OrderDto
import uvg.edu.laboratorio09.remote.dto.OrderLineDto

class MockApiOrderRepository(
    private val orderApi: OrderApi
) : RemoteOrderRepository {
    override suspend fun createOrder(request: CreateRemoteOrder): Result<RemoteOrder> =
        requestResult {
            orderApi.createOrder(request.toDto()).toDomain()
        }

    override fun observeOrders(): Flow<Result<List<RemoteOrder>>> = flow {
        emit(requestResult {
            orderApi.getOrders().map { order -> order.toDomain() }
        })
    }

    override suspend fun getOrder(orderId: String): Result<RemoteOrder> =
        requestResult {
            orderApi.getOrderById(orderId).toDomain()
        }

    private suspend fun <T> requestResult(request: suspend () -> T): Result<T> = try {
        Result.success(request())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: HttpException) {
        if (exception.code() == 404) {
            Result.failure(RemoteOrderNotFoundException())
        } else {
            Result.failure(RemoteRequestException())
        }
    } catch (exception: IOException) {
        Result.failure(RemoteConnectionException())
    } catch (exception: SerializationException) {
        Result.failure(RemoteResponseException())
    } catch (exception: Exception) {
        Result.failure(RemoteRequestException())
    }
}

class RemoteConnectionException : IllegalStateException()
class RemoteResponseException : IllegalStateException()
class RemoteRequestException : IllegalStateException()

private fun CreateRemoteOrder.toDto(): CreateOrderRequest = CreateOrderRequest(
    createdAtEpochMillis = createdAtEpochMillis,
    customerName = customerName,
    billingType = billingType,
    paymentMethod = paymentMethod,
    totalCents = totalCents,
    status = status,
    lines = lines.map { line -> line.toDto() }
)

private fun RemoteOrderLine.toDto(): OrderLineDto = OrderLineDto(
    productId = productId,
    productName = productName,
    unitPriceCents = unitPriceCents,
    quantity = quantity,
    subtotalCents = subtotalCents
)

private fun OrderDto.toDomain(): RemoteOrder = RemoteOrder(
    id = id,
    createdAtEpochMillis = createdAtEpochMillis,
    customerName = customerName,
    billingType = billingType,
    paymentMethod = paymentMethod,
    totalCents = totalCents,
    status = status,
    lines = lines.map { line ->
        RemoteOrderLine(
            productId = line.productId,
            productName = line.productName,
            unitPriceCents = line.unitPriceCents,
            quantity = line.quantity,
            subtotalCents = line.subtotalCents
        )
    }
)
