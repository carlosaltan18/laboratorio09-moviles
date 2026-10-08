package uvg.edu.laboratorio09.data.local

import androidx.room3.Dao
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {
    @Query("SELECT * FROM favorites")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM order_lines")
    fun observeOrderLines(): Flow<List<OrderLineEntity>>
}
