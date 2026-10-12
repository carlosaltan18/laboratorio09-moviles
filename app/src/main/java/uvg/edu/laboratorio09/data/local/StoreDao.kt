package uvg.edu.laboratorio09.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {
    @Query("SELECT * FROM favorites")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM order_lines")
    fun observeOrderLines(): Flow<List<OrderLineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE productId = :productId")
    suspend fun deleteFavorite(productId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOrderLine(line: OrderLineEntity)

    @Query("DELETE FROM order_lines WHERE productId = :productId")
    suspend fun deleteOrderLine(productId: String)

    @Query("DELETE FROM order_lines")
    suspend fun clearOrder()
}
