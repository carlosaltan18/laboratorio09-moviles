package uvg.edu.laboratorio09.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun observeFavoriteIds(): Flow<Set<String>>

    suspend fun toggleFavorite(productId: String): Result<Unit>
}
