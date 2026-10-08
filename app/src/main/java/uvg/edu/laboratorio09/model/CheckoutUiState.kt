package uvg.edu.laboratorio09.model

data class CheckoutUiState(
    val fullName: String = "",
    val phoneNumber: String = "",
    val billingType: BillingType = BillingType.CONSUMER_FINAL,
    val nit: String = "",
    val businessName: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH_ON_DELIVERY,
    val fullNameTouched: Boolean = false,
    val phoneNumberTouched: Boolean = false,
    val nitTouched: Boolean = false,
    val businessNameTouched: Boolean = false,
    val fullNameError: String? = null,
    val phoneNumberError: String? = null,
    val nitError: String? = null,
    val businessNameError: String? = null,
    val isFormValid: Boolean = false
)
