package uvg.edu.laboratorio09.model

data class OrderReceipt(
    val folio: String,
    val customerName: String,
    val phoneNumber: String,
    val billingType: BillingType,
    val nit: String?,
    val businessName: String?,
    val paymentMethod: PaymentMethod,
    val totalCents: Int
)
