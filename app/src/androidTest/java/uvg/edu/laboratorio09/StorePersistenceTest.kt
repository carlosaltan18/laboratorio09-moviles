package uvg.edu.laboratorio09

import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import uvg.edu.laboratorio09.data.local.FavoriteEntity
import uvg.edu.laboratorio09.data.local.OrderLineEntity
import uvg.edu.laboratorio09.data.local.StoreDatabase

class StorePersistenceTest : StorePersistenceTestBase() {
    @Test
    fun rapidActions_preserveQuantitiesAndRejectStockOverflow() = runBlocking {
        val viewModel = createViewModel()
        repeat(4) { viewModel.addProductToOrder("chocolate_02") }
        val state = awaitState(viewModel) {
            it.totalOrderUnits == 3 && it.orderMessage?.contains("Solo hay 3") == true
        }
        assertEquals(15600, state.orderTotalCents)
        assertEquals(listOf(OrderLineEntity("chocolate_02", 3)), dao.observeOrderLines().first())
        repeat(2) { viewModel.toggleFavorite("chocolate_02") }
        // Una accion posterior actua como barrera de las mutaciones serializadas.
        viewModel.addProductToOrder("chocolate_03")
        awaitState(viewModel) { it.totalOrderUnits == 4 }
        assertTrue(dao.observeFavorites().first().isEmpty())
    }

    @Test
    fun decreaseAndRemove_deleteOnlyTheSelectedLine() = runBlocking {
        val viewModel = createViewModel()
        addProduct(viewModel, "chocolate_02")
        addProduct(viewModel, "chocolate_02")
        addProduct(viewModel, "chocolate_03")
        viewModel.decreaseProduct("chocolate_02")
        awaitState(viewModel) { it.totalOrderUnits == 2 }
        assertEquals(1, dao.observeOrderLines().first().first { it.productId == "chocolate_02" }.quantity)
        viewModel.decreaseProduct("chocolate_02")
        awaitState(viewModel) { it.totalOrderUnits == 1 }
        assertEquals(listOf(OrderLineEntity("chocolate_03", 1)), dao.observeOrderLines().first())
        addProduct(viewModel, "chocolate_02")
        viewModel.removeProduct("chocolate_03")
        awaitState(viewModel) { it.orderLines.size == 1 && it.orderLines.first().productId == "chocolate_02" }
        assertEquals(listOf(OrderLineEntity("chocolate_02", 1)), dao.observeOrderLines().first())
    }

    @Test
    fun newViewModel_restoresOrderTotalsAndFavorites_thenConfirmationClearsOnlyOrder() = runBlocking {
        val first = createViewModel()
        toggleFavorite(first, "chocolate_02")
        addProduct(first, "chocolate_02")
        addProduct(first, "chocolate_02")
        addProduct(first, "chocolate_03")
        val restored = createViewModel()
        val state = awaitState(restored) { it.totalOrderUnits == 3 && "chocolate_02" in it.favoriteProductIds }
        assertEquals(15200, state.orderTotalCents)
        assertEquals(mapOf("chocolate_02" to 10400, "chocolate_03" to 4800), state.orderSubtotalsCents)
        restored.onFullNameChange("Ana Lopez")
        restored.onPhoneNumberChange("55123456")
        assertTrue(confirmOrder(restored))
        assertEquals(15200, restored.orderReceipt.value?.totalCents)
        assertTrue(dao.observeOrderLines().first().isEmpty())
        assertEquals(listOf(FavoriteEntity("chocolate_02")), dao.observeFavorites().first())
        val reopened = createViewModel()
        awaitState(reopened) { "chocolate_02" in it.favoriteProductIds }
        assertEquals(0, reopened.uiState.value.totalOrderUnits)
    }

    @Test
    fun databaseReopen_keepsRowsAndDeletionsOnDisk() = runBlocking {
        val name = "store-persistence-test.db"
        fun openDatabase() = Room.databaseBuilder<StoreDatabase>(application, name)
            .setDriver(AndroidSQLiteDriver()).build()
        application.deleteDatabase(name)
        var database = openDatabase()
        try {
            database.storeDao().insertFavorite(FavoriteEntity("chocolate_02"))
            database.storeDao().insertFavorite(FavoriteEntity("chocolate_03"))
            database.storeDao().upsertOrderLine(OrderLineEntity("chocolate_02", 1))
            database.storeDao().upsertOrderLine(OrderLineEntity("chocolate_02", 2))
            database.storeDao().upsertOrderLine(OrderLineEntity("chocolate_03", 1))
            database.close()
            database = openDatabase()
            assertEquals(2, database.storeDao().observeFavorites().first().size)
            assertEquals(3, database.storeDao().observeOrderLines().first().sumOf { it.quantity })
            database.storeDao().deleteFavorite("chocolate_03")
            database.storeDao().clearOrder()
            database.close()
            database = openDatabase()
            assertEquals(listOf(FavoriteEntity("chocolate_02")), database.storeDao().observeFavorites().first())
            assertTrue(database.storeDao().observeOrderLines().first().isEmpty())
        } finally {
            database.close()
            application.deleteDatabase(name)
        }
    }
}
