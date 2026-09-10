package uvg.edu.laboratorio09

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uvg.edu.laboratorio09.viewmodel.ChocolateStoreViewModel

class ChocolateStoreViewModelTest {
    @Test
    fun catalogIsStableAndMeetsTheProductRequirements() {
        val firstCatalog = ChocolateStoreViewModel().uiState.value.chocolates
        val secondCatalog = ChocolateStoreViewModel().uiState.value.chocolates
        val chocolatierIds = ChocolateStoreViewModel().uiState.value.chocolatiers.map { it.id }.toSet()

        assertEquals(500, firstCatalog.size)
        assertEquals(500, firstCatalog.map { it.id }.distinct().size)
        assertEquals(firstCatalog, secondCatalog)
        assertTrue(firstCatalog.all { it.priceCents > 0 })
        assertTrue(firstCatalog.all { it.stock >= 0 })
        assertTrue(firstCatalog.all { it.chocolatierId in chocolatierIds })
        assertTrue(firstCatalog.all { it.imageUrl.isNotBlank() })
        assertTrue(firstCatalog.any { it.id == "chocolate_01" })
        assertTrue(firstCatalog.any { it.id == "chocolate_02" })
        assertTrue(firstCatalog.any { it.id == "chocolate_03" })
    }
}
