package uvg.edu.laboratorio09.model

data class RemoteOrderLine(
    val productId: String,
    val productName: String,
    val unitPriceCents: Int,
    val quantity: Int,
    val subtotalCents: Int
)

data class CreateRemoteOrder(
    val createdAtEpochMillis: Long,
    val customerName: String,
    val billingType: String,
    val paymentMethod: String,
    val totalCents: Int,
    val status: String,
    val lines: List<RemoteOrderLine>
)

data class RemoteOrder(
    val id: String,
    val createdAtEpochMillis: Long,
    val customerName: String,
    val billingType: String,
    val paymentMethod: String,
    val totalCents: Int,
    val status: String,
    val lines: List<RemoteOrderLine>
)
