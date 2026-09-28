package uvg.edu.laboratorio09.domain

import uvg.edu.laboratorio09.model.Chocolate
import uvg.edu.laboratorio09.model.OrderLine
import uvg.edu.laboratorio09.model.OrderResult

fun addToOrder(
    products: List<Chocolate>,
    currentLines: List<OrderLine>,
    productId: String,
    increment: Int = 1
): OrderResult {
    if (increment <= 0) {
        return OrderResult.Rejected("El incremento debe ser positivo.")
    }
    val product = products.find { it.id == productId }
        ?: return OrderResult.Rejected("El producto no existe.")
    val currentQuantity = currentLines.find { it.productId == productId }?.quantity ?: 0
    // Long evita que un incremento muy grande desborde Int y omita la validación.
    val requestedQuantity = currentQuantity.toLong() + increment
    if (requestedQuantity > product.stock) {
        return OrderResult.Rejected(
            "Solo hay ${product.stock} unidades disponibles. El pedido no cambió."
        )
    }
    val updatedLines = if (currentQuantity == 0) {
        currentLines + OrderLine(productId, increment)
    } else {
        currentLines.map { line ->
            if (line.productId == productId) {
                line.copy(quantity = requestedQuantity.toInt())
            } else {
                line
            }
        }
    }
    return OrderResult.Success(
        lines = updatedLines,
        message = if (increment == 1) "Se agregó 1 unidad al pedido."
        else "Se agregaron $increment unidades al pedido."
    )
}

fun decreaseOrderLine(currentLines: List<OrderLine>, productId: String): List<OrderLine> =
    currentLines.mapNotNull { line ->
        when {
            line.productId != productId -> line
            line.quantity > 1 -> line.copy(quantity = line.quantity - 1)
            else -> null
        }
    }

fun removeOrderLine(currentLines: List<OrderLine>, productId: String): List<OrderLine> =
    currentLines.filterNot { it.productId == productId }

fun lineSubtotalCents(product: Chocolate, line: OrderLine): Int =
    product.priceCents * line.quantity

fun orderTotalCents(products: List<Chocolate>, lines: List<OrderLine>): Int =
    lines.sumOf { line ->
        val product = products.find { it.id == line.productId } ?: return@sumOf 0
        lineSubtotalCents(product, line)
    }

fun validateOrderAvailability(
    products: List<Chocolate>,
    lines: List<OrderLine>
): String? {
    if (lines.isEmpty()) return "El pedido está vacío."

    lines.forEach { line ->
        if (line.quantity <= 0) return "El pedido contiene una cantidad inválida."
        val product = products.find { it.id == line.productId }
            ?: return "Uno de los productos ya no existe."
        if (line.quantity > product.stock) {
            return "Ya no hay suficientes unidades de ${product.name}."
        }
    }
    return null
}

fun deductOrderFromInventory(
    products: List<Chocolate>,
    lines: List<OrderLine>
): List<Chocolate> {
    require(validateOrderAvailability(products, lines) == null)
    val purchasedQuantities = lines.associate { it.productId to it.quantity }
    return products.map { product ->
        val purchasedQuantity = purchasedQuantities[product.id] ?: 0
        product.copy(stock = product.stock - purchasedQuantity)
    }
}
