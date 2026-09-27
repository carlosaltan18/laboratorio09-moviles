package uvg.edu.laboratorio09

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import uvg.edu.laboratorio09.model.BillingType
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
}
