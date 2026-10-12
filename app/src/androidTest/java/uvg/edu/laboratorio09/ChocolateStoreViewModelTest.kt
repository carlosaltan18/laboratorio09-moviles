package uvg.edu.laboratorio09

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChocolateCatalogTest : StorePersistenceTestBase() {
    @Test
    fun catalogIsStableAndMeetsTheProductRequirements() = runBlocking {
        val firstCatalog = createViewModel().uiState.value.products
        val secondCatalog = createViewModel().uiState.value.products
        val chocolatierIds = createViewModel().uiState.value.profiles.map { it.id }.toSet()

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
