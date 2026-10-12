package uvg.edu.laboratorio09

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreViewModelTest : StorePersistenceTestBase() {

    @Test
    fun toggleFavorite_addsAndRemovesProductId() = runBlocking {
        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.products.isNotEmpty())
        assertTrue(viewModel.uiState.value.profiles.isNotEmpty())
        assertFalse("chocolate_02" in viewModel.uiState.value.favoriteProductIds)

        toggleFavorite(viewModel, "chocolate_02")
        assertEquals(setOf("chocolate_02"), viewModel.uiState.value.favoriteProductIds)

        toggleFavorite(viewModel, "chocolate_02")
        assertTrue(viewModel.uiState.value.favoriteProductIds.isEmpty())
    }

    @Test
    fun viewModelKeepsOrderOnRejectionAndSearchPreservesCatalog() = runBlocking {
        val viewModel = createViewModel()
        val originalProducts = viewModel.uiState.value.products
        repeat(3) { addProduct(viewModel, "chocolate_02") }
        val fullOrder = viewModel.uiState.value
        assertEquals(3, fullOrder.totalOrderUnits)
        assertEquals(15600, fullOrder.orderTotalCents)
        assertEquals(15600, fullOrder.orderSubtotalsCents["chocolate_02"])
        addProduct(viewModel, "chocolate_02")
        val rejected = viewModel.uiState.value
        assertEquals(fullOrder.orderLines, rejected.orderLines)
        assertEquals(fullOrder.orderTotalCents, rejected.orderTotalCents)
        assertNotNull(rejected.orderMessage)
        viewModel.updateQuery("  cAfÉ  ")
        awaitState(viewModel) { it.query == "  cAfÉ  " }
        assertEquals(
            originalProducts.filter { it.name.contains("café", ignoreCase = true) },
            viewModel.uiState.value.filteredProducts
        )
        viewModel.updateQuery("no-existe-este-producto")
        awaitState(viewModel) { it.query == "no-existe-este-producto" }
        assertTrue(viewModel.uiState.value.filteredProducts.isEmpty())
        viewModel.clearQuery()
        awaitState(viewModel) { it.query.isEmpty() }
        assertEquals(originalProducts, viewModel.uiState.value.filteredProducts)
        assertSame(originalProducts, viewModel.uiState.value.products)
        assertEquals(fullOrder.orderLines, viewModel.uiState.value.orderLines)
        viewModel.decreaseProduct("chocolate_02")
        awaitState(viewModel) { it.totalOrderUnits == 2 }
        assertEquals(10400, viewModel.uiState.value.orderTotalCents)
        viewModel.removeProduct("chocolate_02")
        awaitState(viewModel) { it.totalOrderUnits == 0 }
        assertTrue(viewModel.uiState.value.orderLines.isEmpty())
        assertTrue(viewModel.uiState.value.orderSubtotalsCents.isEmpty())
        assertEquals(0, viewModel.uiState.value.orderTotalCents)
    }
}
