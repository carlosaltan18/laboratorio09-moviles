package uvg.edu.laboratorio09

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import uvg.edu.laboratorio09.model.BillingType
import uvg.edu.laboratorio09.model.PaymentMethod
import uvg.edu.laboratorio09.viewmodel.StoreViewModel

class StoreViewModelCheckoutTest {
    @Test
    fun returningToConsumerFinal_clearsFiscalErrorsAndTouchedFlags() {
        val viewModel = StoreViewModel()

        viewModel.onFullNameChange("Ana López")
        viewModel.onPhoneNumberChange("55123456")
        viewModel.onBillingTypeChange(BillingType.NIT)
        viewModel.onNitChange("45123")
        viewModel.onBusinessNameChange("Cacao Maya")
        viewModel.onBillingTypeChange(BillingType.CONSUMER_FINAL)

        val checkout = viewModel.checkoutUiState.value
        assertEquals(BillingType.CONSUMER_FINAL, checkout.billingType)
        assertEquals("45123", checkout.nit)
        assertEquals("Cacao Maya", checkout.businessName)
        assertFalse(checkout.nitTouched)
        assertFalse(checkout.businessNameTouched)
        assertNull(checkout.nitError)
        assertNull(checkout.businessNameError)
        assertTrue(checkout.isFormValid)
    }

    @Test
    fun invalidConfirmation_keepsOrderAndDoesNotConsumeFolio() {
        val viewModel = StoreViewModel()
        viewModel.addProductToOrder("chocolate_02")

        assertFalse(viewModel.confirmOrder())
        assertEquals(1, viewModel.uiState.value.totalOrderUnits)
        assertNull(viewModel.orderReceipt.value)
        assertTrue(viewModel.checkoutUiState.value.fullNameTouched)
        assertTrue(viewModel.checkoutUiState.value.phoneNumberTouched)

        completeConsumerFinalForm(viewModel)
        assertTrue(viewModel.confirmOrder())
        assertEquals("#ORD-00001", viewModel.orderReceipt.value?.folio)
    }

    @Test
    fun validConfirmation_savesReceiptClearsOrderAndResetsForm() {
        val viewModel = StoreViewModel()
        viewModel.addProductToOrder("chocolate_02")
        viewModel.onFullNameChange("  Ana López  ")
        viewModel.onPhoneNumberChange("55123456")
        viewModel.onPaymentMethodChange(PaymentMethod.BANK_TRANSFER)

        assertTrue(viewModel.confirmOrder())

        val receipt = viewModel.orderReceipt.value
        assertEquals("#ORD-00001", receipt?.folio)
        assertEquals("Ana López", receipt?.customerName)
        assertEquals("55123456", receipt?.phoneNumber)
        assertEquals(BillingType.CONSUMER_FINAL, receipt?.billingType)
        assertEquals(PaymentMethod.BANK_TRANSFER, receipt?.paymentMethod)
        assertEquals(5200, receipt?.totalCents)
        assertTrue(viewModel.uiState.value.orderLines.isEmpty())
        assertEquals(0, viewModel.uiState.value.orderTotalCents)
        assertEquals(
            2,
            viewModel.uiState.value.products.first { it.id == "chocolate_02" }.stock
        )

        val resetCheckout = viewModel.checkoutUiState.value
        assertEquals("", resetCheckout.fullName)
        assertEquals("", resetCheckout.phoneNumber)
        assertEquals(BillingType.CONSUMER_FINAL, resetCheckout.billingType)
        assertEquals(PaymentMethod.CASH_ON_DELIVERY, resetCheckout.paymentMethod)
        assertFalse(resetCheckout.fullNameTouched)
        assertFalse(resetCheckout.phoneNumberTouched)
    }

    @Test
    fun consecutiveValidOrders_useDeterministicSequentialFolios() {
        val viewModel = StoreViewModel()

        viewModel.addProductToOrder("chocolate_02")
        completeConsumerFinalForm(viewModel)
        assertTrue(viewModel.confirmOrder())
        val firstReceipt = viewModel.orderReceipt.value

        viewModel.addProductToOrder("chocolate_03")
        completeConsumerFinalForm(viewModel)
        assertTrue(viewModel.confirmOrder())

        assertEquals("#ORD-00001", firstReceipt?.folio)
        assertEquals("#ORD-00002", viewModel.orderReceipt.value?.folio)
        assertEquals(4800, viewModel.orderReceipt.value?.totalCents)
    }

    @Test
    fun buyingAllAvailableUnits_marksProductAsSoldOut() {
        val viewModel = StoreViewModel()
        repeat(3) { viewModel.addProductToOrder("chocolate_02") }
        completeConsumerFinalForm(viewModel)

        assertTrue(viewModel.confirmOrder())
        assertEquals(
            0,
            viewModel.uiState.value.products.first { it.id == "chocolate_02" }.stock
        )
        assertEquals(0, viewModel.uiState.value.totalOrderUnits)

        viewModel.addProductToOrder("chocolate_02")
        assertEquals(0, viewModel.uiState.value.totalOrderUnits)
        assertTrue(viewModel.uiState.value.orderMessage?.contains("0 unidades") == true)
    }

    private fun completeConsumerFinalForm(viewModel: StoreViewModel) {
        viewModel.onFullNameChange("Ana López")
        viewModel.onPhoneNumberChange("55123456")
    }
}
