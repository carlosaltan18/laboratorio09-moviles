package uvg.edu.laboratorio09.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver

@Database(
    entities = [
        FavoriteEntity::class,
        OrderLineEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StoreDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDao

    companion object {
        @Volatile
        private var instance: StoreDatabase? = null

        fun getInstance(context: Context): StoreDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder<StoreDatabase>(
                    context = context.applicationContext,
                    name = "chocolate_store.db"
                )
                    .setDriver(AndroidSQLiteDriver())
                    .build()
                    .also { database ->
                        instance = database
                    }
            }
        }
    }
}
