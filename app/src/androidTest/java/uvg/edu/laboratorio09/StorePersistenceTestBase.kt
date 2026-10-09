package uvg.edu.laboratorio09

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import uvg.edu.laboratorio09.data.local.StoreDatabase
import uvg.edu.laboratorio09.model.StoreUiState
import uvg.edu.laboratorio09.viewmodel.StoreViewModel

// Pruebas con Room y SQLite reales; cada caso empieza sin favoritos ni pedido.
abstract class StorePersistenceTestBase {
    protected val application: Application
        get() = InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext as Application
    protected val dao get() = StoreDatabase.getInstance(application).storeDao()
    private val stores = mutableListOf<ViewModelStore>()
    private lateinit var collectors: CoroutineScope

    @Before
    fun prepareDatabase() = runBlocking {
        collectors = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        dao.clearOrder()
        dao.observeFavorites().first().forEach { dao.deleteFavorite(it.productId) }
    }

    @After
    fun releaseViewModels() = runBlocking {
        collectors.cancel()
        withContext(Dispatchers.Main) { stores.forEach { it.clear() } }
        dao.clearOrder()
        dao.observeFavorites().first().forEach { dao.deleteFavorite(it.productId) }
    }

    protected fun createViewModel(): StoreViewModel {
        val viewModel = StoreViewModel(application)
        stores += ViewModelStore().apply { put("store", viewModel) }
        collectors.launch { viewModel.uiState.collect {} }
        return viewModel
    }

    protected suspend fun awaitState(
        viewModel: StoreViewModel,
        predicate: (StoreUiState) -> Boolean
    ): StoreUiState = withTimeout(10_000) { viewModel.uiState.first(predicate) }

    protected suspend fun addProduct(viewModel: StoreViewModel, id: String) {
        val before = viewModel.uiState.value
        val product = before.products.first { it.id == id }
        val quantity = before.orderLines.find { it.productId == id }?.quantity ?: 0
        viewModel.clearOrderMessage()
        viewModel.addProductToOrder(id)
        if (quantity < product.stock) {
            awaitState(viewModel) {
                it.orderLines.find { line -> line.productId == id }?.quantity == quantity + 1
            }
        } else {
            awaitState(viewModel) { it.orderMessage?.contains("${product.stock} unidades") == true }
        }
    }

    protected suspend fun toggleFavorite(viewModel: StoreViewModel, id: String) {
        val wasFavorite = id in viewModel.uiState.value.favoriteProductIds
        viewModel.toggleFavorite(id)
        awaitState(viewModel) { (id in it.favoriteProductIds) != wasFavorite }
    }

    protected suspend fun confirmOrder(viewModel: StoreViewModel): Boolean {
        val before = viewModel.uiState.value
        val confirmed = viewModel.confirmOrder()
        if (confirmed) {
            awaitState(viewModel) { state ->
                state.orderLines.isEmpty() && before.orderLines.all { line ->
                    state.products.first { it.id == line.productId }.stock ==
                        before.products.first { it.id == line.productId }.stock - line.quantity
                }
            }
        }
        return confirmed
    }
}
