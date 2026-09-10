package uvg.edu.laboratorio09

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uvg.edu.laboratorio09.viewmodel.StoreViewModel

class StoreViewModelTest {

    @Test
    fun toggleFavorite_addsAndRemovesProductId() {
        val viewModel = StoreViewModel()

        assertTrue(viewModel.uiState.value.products.isNotEmpty())
        assertTrue(viewModel.uiState.value.profiles.isNotEmpty())
        assertFalse("chocolate_02" in viewModel.uiState.value.favoriteProductIds)

        viewModel.toggleFavorite("chocolate_02")
        assertEquals(setOf("chocolate_02"), viewModel.uiState.value.favoriteProductIds)

        viewModel.toggleFavorite("chocolate_02")
        assertTrue(viewModel.uiState.value.favoriteProductIds.isEmpty())
    }
}
