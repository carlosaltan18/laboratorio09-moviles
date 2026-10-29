package uvg.edu.laboratorio09.data.local

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import uvg.edu.laboratorio09.domain.repository.CartRepository
import uvg.edu.laboratorio09.model.OrderLine

class RoomCartRepository(
    private val storeDao: StoreDao
) : CartRepository {
    override fun observeOrderLines(): Flow<List<OrderLine>> =
        storeDao.observeOrderLines().map { entities ->
            entities.map { it.toOrderLine() }
        }

    override suspend fun increase(productId: String): Result<Unit> = perform {
        val currentLine = observeOrderLines().first().find { it.productId == productId }
        storeDao.upsertOrderLine(
            OrderLineEntity(
                productId = productId,
                quantity = (currentLine?.quantity ?: 0) + 1
            )
        )
    }

    override suspend fun decrease(productId: String): Result<Unit> = perform {
        val currentLine = observeOrderLines().first().find { it.productId == productId }
            ?: return@perform
        if (currentLine.quantity <= 1) {
            storeDao.deleteOrderLine(productId)
        } else {
            storeDao.upsertOrderLine(
                OrderLineEntity(productId, currentLine.quantity - 1)
            )
        }
    }

    override suspend fun remove(productId: String): Result<Unit> = perform {
        storeDao.deleteOrderLine(productId)
    }

    override suspend fun clear(): Result<Unit> = perform {
        storeDao.clearOrder()
    }

    private suspend fun perform(action: suspend () -> Unit): Result<Unit> = try {
        action()
        Result.success(Unit)
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}
