package uvg.edu.laboratorio09.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    val id: String,
    val createdAtEpochMillis: Long,
    val customerName: String,
    val billingType: String,
    val paymentMethod: String,
    val totalCents: Int,
    val status: String,
    val lines: List<OrderLineDto>
)
