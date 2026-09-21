package uvg.edu.laboratorio09

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import uvg.edu.laboratorio09.domain.addToOrder
import uvg.edu.laboratorio09.domain.decreaseOrderLine
import uvg.edu.laboratorio09.domain.orderTotalCents
import uvg.edu.laboratorio09.domain.removeOrderLine
import uvg.edu.laboratorio09.model.Chocolate
import uvg.edu.laboratorio09.model.OrderLine
import uvg.edu.laboratorio09.model.OrderResult
import uvg.edu.laboratorio09.model.toQuetzales
import uvg.edu.laboratorio09.viewmodel.StoreViewModel

class OrderRulesTest {
    private val products = listOf(
        Chocolate("a", "profile", "Chocolate", "", 2590, 3, ""),
        Chocolate("b", "profile", "Bombón", "", 1050, 8, ""),
        Chocolate("empty", "profile", "Agotado", "", 1000, 0, "")
    )

    @Test
    fun repeatedAddsAccumulateAndStockRejectionPreservesOrder() {
        val first = addToOrder(products, emptyList(), "a") as OrderResult.Success
        val second = addToOrder(products, first.lines, "a") as OrderResult.Success
        assertEquals(listOf(OrderLine("a", 2)), second.lines)
        val third = addToOrder(products, second.lines, "a") as OrderResult.Success
        assertTrue(addToOrder(products, third.lines, "a") is OrderResult.Rejected)
        assertEquals(listOf(OrderLine("a", 3)), third.lines)
        assertEquals(7770, orderTotalCents(products, third.lines))
        assertEquals(3, products.first().stock)
    }

    @Test
    fun rejectsInvalidIdsIncrementsAndSoldOutProducts() {
        val lines = listOf(OrderLine("a", 1))
        assertTrue(addToOrder(products, lines, "missing") is OrderResult.Rejected)
        assertTrue(addToOrder(products, lines, "a", 0) is OrderResult.Rejected)
        assertTrue(addToOrder(products, lines, "a", -1) is OrderResult.Rejected)
        assertTrue(addToOrder(products, lines, "a", Int.MAX_VALUE) is OrderResult.Rejected)
        assertTrue(addToOrder(products, lines, "empty") is OrderResult.Rejected)
        assertEquals(listOf(OrderLine("a", 1)), lines)
    }

    @Test
    fun editsPreserveOtherLinesAndTotalsUseCents() {
        val lines = listOf(OrderLine("a", 2), OrderLine("b", 1))
        assertEquals(6230, orderTotalCents(products, lines))
        assertEquals("Q62.30", orderTotalCents(products, lines).toQuetzales())
        val decreased = decreaseOrderLine(lines, "a")
        assertEquals(listOf(OrderLine("a", 1), OrderLine("b", 1)), decreased)
        assertEquals(listOf(OrderLine("b", 1)), decreaseOrderLine(decreased, "a"))
        assertEquals(listOf(OrderLine("b", 1)), removeOrderLine(lines, "a"))
        assertEquals(0, orderTotalCents(products, emptyList()))
        assertEquals("Q0.00", 0.toQuetzales())
    }

    @Test
    fun viewModelKeepsOrderOnRejectionAndSearchPreservesCatalog() {
        val viewModel = StoreViewModel()
        val originalProducts = viewModel.uiState.value.products
        repeat(3) { viewModel.addProductToOrder("chocolate_02") }
        val fullOrder = viewModel.uiState.value
        assertEquals(3, fullOrder.totalOrderUnits)
        assertEquals(15600, fullOrder.orderTotalCents)
        assertEquals(15600, fullOrder.orderSubtotalsCents["chocolate_02"])
        viewModel.addProductToOrder("chocolate_02")
        val rejected = viewModel.uiState.value
        assertSame(fullOrder.orderLines, rejected.orderLines)
        assertEquals(fullOrder.orderTotalCents, rejected.orderTotalCents)
        assertNotNull(rejected.orderMessage)
        viewModel.updateQuery("  cAfÉ  ")
        assertEquals(
            originalProducts.filter { it.name.contains("café", ignoreCase = true) },
            viewModel.uiState.value.filteredProducts
        )
        viewModel.updateQuery("no-existe-este-producto")
        assertTrue(viewModel.uiState.value.filteredProducts.isEmpty())
        viewModel.clearQuery()
        assertEquals(originalProducts, viewModel.uiState.value.filteredProducts)
        assertSame(originalProducts, viewModel.uiState.value.products)
        assertEquals(fullOrder.orderLines, viewModel.uiState.value.orderLines)
        viewModel.decreaseProduct("chocolate_02")
        assertEquals(10400, viewModel.uiState.value.orderTotalCents)
        viewModel.removeProduct("chocolate_02")
        assertTrue(viewModel.uiState.value.orderLines.isEmpty())
        assertTrue(viewModel.uiState.value.orderSubtotalsCents.isEmpty())
        assertEquals(0, viewModel.uiState.value.orderTotalCents)
    }
}
