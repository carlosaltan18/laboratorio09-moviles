package uvg.edu.laboratorio09

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import uvg.edu.laboratorio09.domain.revalidated
import uvg.edu.laboratorio09.domain.validateBusinessName
import uvg.edu.laboratorio09.domain.validateFullName
import uvg.edu.laboratorio09.domain.validateNit
import uvg.edu.laboratorio09.domain.validatePhoneNumber
import uvg.edu.laboratorio09.model.BillingType
import uvg.edu.laboratorio09.model.CheckoutUiState

class CheckoutValidatorsTest {
    @Test
    fun fullName_withThreeLetters_isValid() {
        assertNull(validateFullName("Ana"))
    }

    @Test
    fun fullName_withDigits_isInvalid() {
        assertTrue(validateFullName("Carlos 2") != null)
    }

    @Test
    fun fullName_withAccentsAndEnye_isValid() {
        assertNull(validateFullName("José Ñúñez"))
    }

    @Test
    fun phone_withEightDigits_isValid() {
        assertNull(validatePhoneNumber("55123456"))
    }

    @Test
    fun phone_withSpacesOrHyphens_isInvalid() {
        assertTrue(validatePhoneNumber("5512-3456") != null)
        assertTrue(validatePhoneNumber("5512 3456") != null)
    }

    @Test
    fun nit_withFourDigits_isInvalid() {
        assertTrue(validateNit("4512") != null)
    }

    @Test
    fun nit_withFiveDigits_isValid() {
        assertNull(validateNit("45123"))
    }

    @Test
    fun businessName_withLessThanThreeCharacters_isInvalid() {
        assertTrue(validateBusinessName("AB") != null)
    }

    @Test
    fun consumerFinal_doesNotRequireFiscalFields() {
        val state = CheckoutUiState(
            fullName = "Ana López",
            phoneNumber = "55123456",
            billingType = BillingType.CONSUMER_FINAL
        ).revalidated()

        assertTrue(state.isFormValid)
        assertNull(state.nitError)
        assertNull(state.businessNameError)
    }

    @Test
    fun nitBilling_requiresNitAndBusinessName() {
        val state = CheckoutUiState(
            fullName = "Ana López",
            phoneNumber = "55123456",
            billingType = BillingType.NIT
        ).revalidated()

        assertFalse(state.isFormValid)
        assertTrue(state.nitError != null)
        assertTrue(state.businessNameError != null)
    }
}
