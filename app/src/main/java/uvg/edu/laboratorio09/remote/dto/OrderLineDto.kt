package uvg.edu.laboratorio09.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderLineDto(
    val productId: String,
    val productName: String,
    val unitPriceCents: Int,
    val quantity: Int,
    val subtotalCents: Int
)
