package uvg.edu.laboratorio09.data.local

import uvg.edu.laboratorio09.model.OrderLine

fun FavoriteEntity.toProductId(): String = productId

fun OrderLineEntity.toOrderLine(): OrderLine = OrderLine(productId, quantity)
