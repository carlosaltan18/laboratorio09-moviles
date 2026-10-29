package uvg.edu.laboratorio09.domain.repository

import kotlinx.coroutines.flow.Flow
import uvg.edu.laboratorio09.model.OrderLine

interface CartRepository {
    fun observeOrderLines(): Flow<List<OrderLine>>

    suspend fun increase(productId: String): Result<Unit>

    suspend fun decrease(productId: String): Result<Unit>

    suspend fun remove(productId: String): Result<Unit>

    suspend fun clear(): Result<Unit>
}
