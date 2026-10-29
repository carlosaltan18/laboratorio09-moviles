package uvg.edu.laboratorio09.data.local

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import uvg.edu.laboratorio09.domain.repository.FavoriteRepository

class RoomFavoriteRepository(
    private val storeDao: StoreDao
) : FavoriteRepository {
    override fun observeFavoriteIds(): Flow<Set<String>> =
        storeDao.observeFavorites().map { favorites ->
            favorites.map { it.toProductId() }.toSet()
        }

    override suspend fun toggleFavorite(productId: String): Result<Unit> = try {
        if (productId in observeFavoriteIds().first()) {
            storeDao.deleteFavorite(productId)
        } else {
            storeDao.insertFavorite(FavoriteEntity(productId))
        }
        Result.success(Unit)
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}
