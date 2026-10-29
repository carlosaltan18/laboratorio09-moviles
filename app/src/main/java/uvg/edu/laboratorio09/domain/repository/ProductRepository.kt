package uvg.edu.laboratorio09.domain.repository

import kotlinx.coroutines.flow.Flow
import uvg.edu.laboratorio09.model.Chocolate

interface ProductRepository {
    fun observeProducts(): Flow<List<Chocolate>>

    suspend fun reserveUnit(productId: String): Result<Unit>

    suspend fun releaseUnit(productId: String): Result<Unit>
}

class OutOfStockException : IllegalStateException("Producto agotado")
