package uvg.edu.laboratorio09.domain

import uvg.edu.laboratorio09.model.BillingType
import uvg.edu.laboratorio09.model.CheckoutUiState

fun validateFullName(value: String): String? {
    val normalized = value.trim()

    if (normalized.any { it.isDigit() }) {
        return "El nombre no puede contener números."
    }

    return if (normalized.count { it.isLetter() } >= 3) {
        null
    } else {
        "Ingrese un nombre con al menos 3 letras."
    }
}

fun validatePhoneNumber(value: String): String? =
    if (value.trim().matches(Regex("^\\d{8}$"))) {
        null
    } else {
        "Ingrese exactamente 8 dígitos."
    }

fun validateNit(value: String): String? =
    if (value.trim().matches(Regex("^\\d{5,}$"))) {
        null
    } else {
        "Ingrese al menos 5 dígitos."
    }

fun validateBusinessName(value: String): String? =
    if (value.trim().length >= 3) {
        null
    } else {
        "Ingrese al menos 3 caracteres."
    }

fun CheckoutUiState.revalidated(): CheckoutUiState {
    val currentFullNameError = validateFullName(fullName)
    val currentPhoneError = validatePhoneNumber(phoneNumber)
    val currentNitError = if (billingType == BillingType.NIT) validateNit(nit) else null
    val currentBusinessNameError =
        if (billingType == BillingType.NIT) validateBusinessName(businessName) else null

    val isValid = currentFullNameError == null &&
        currentPhoneError == null &&
        currentNitError == null &&
        currentBusinessNameError == null

    return copy(
        fullNameError = currentFullNameError,
        phoneNumberError = currentPhoneError,
        nitError = currentNitError,
        businessNameError = currentBusinessNameError,
        isFormValid = isValid
    )
}
