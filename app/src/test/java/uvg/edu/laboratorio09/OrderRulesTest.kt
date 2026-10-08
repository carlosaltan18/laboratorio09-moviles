package uvg.edu.laboratorio09

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import uvg.edu.laboratorio09.domain.addToOrder
import uvg.edu.laboratorio09.domain.decreaseOrderLine
import uvg.edu.laboratorio09.domain.deductOrderFromInventory
import uvg.edu.laboratorio09.domain.orderTotalCents
import uvg.edu.laboratorio09.domain.removeOrderLine
import uvg.edu.laboratorio09.domain.validateOrderAvailability
import uvg.edu.laboratorio09.model.Chocolate
import uvg.edu.laboratorio09.model.OrderLine
import uvg.edu.laboratorio09.model.OrderResult
import uvg.edu.laboratorio09.model.toQuetzales

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
    fun confirmedQuantitiesAreDeductedFromInventory() {
        val lines = listOf(OrderLine("a", 3), OrderLine("b", 2))

        assertNull(validateOrderAvailability(products, lines))
        val updatedProducts = deductOrderFromInventory(products, lines)

        assertEquals(0, updatedProducts.first { it.id == "a" }.stock)
        assertEquals(6, updatedProducts.first { it.id == "b" }.stock)
        assertEquals(0, products.first { it.id == "empty" }.stock)
        assertEquals(3, products.first { it.id == "a" }.stock)
    }
}
