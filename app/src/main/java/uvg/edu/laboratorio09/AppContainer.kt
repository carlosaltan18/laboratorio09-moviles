package uvg.edu.laboratorio09

import android.content.Context
import uvg.edu.laboratorio09.data.firebase.FirebaseAuthRepository
import uvg.edu.laboratorio09.data.firebase.FirestoreProductRepository
import uvg.edu.laboratorio09.data.inmemory.InMemoryChocolatierRepository
import uvg.edu.laboratorio09.data.local.DataStoreCatalogPreferencesRepository
import uvg.edu.laboratorio09.data.local.RoomCartRepository
import uvg.edu.laboratorio09.data.local.RoomFavoriteRepository
import uvg.edu.laboratorio09.data.local.StoreDatabase
import uvg.edu.laboratorio09.data.local.storePreferencesDataStore
import uvg.edu.laboratorio09.data.remote.MockApiOrderRepository
import uvg.edu.laboratorio09.domain.repository.AuthenticationRepository
import uvg.edu.laboratorio09.domain.repository.CartRepository
import uvg.edu.laboratorio09.domain.repository.CatalogPreferencesRepository
import uvg.edu.laboratorio09.domain.repository.ChocolatierRepository
import uvg.edu.laboratorio09.domain.repository.FavoriteRepository
import uvg.edu.laboratorio09.domain.repository.ProductRepository
import uvg.edu.laboratorio09.domain.repository.RemoteOrderRepository
import uvg.edu.laboratorio09.remote.OrderNetwork

class AppContainer(context: Context) {
    private val database = StoreDatabase.getInstance(context)

    val authenticationRepository: AuthenticationRepository = FirebaseAuthRepository()
    val productRepository: ProductRepository = FirestoreProductRepository()
    val favoriteRepository: FavoriteRepository = RoomFavoriteRepository(database.storeDao())
    val cartRepository: CartRepository = RoomCartRepository(database.storeDao())
    val catalogPreferencesRepository: CatalogPreferencesRepository =
        DataStoreCatalogPreferencesRepository(context.storePreferencesDataStore)
    val remoteOrderRepository: RemoteOrderRepository = MockApiOrderRepository(OrderNetwork.api)
    val chocolatierRepository: ChocolatierRepository = InMemoryChocolatierRepository()
}
