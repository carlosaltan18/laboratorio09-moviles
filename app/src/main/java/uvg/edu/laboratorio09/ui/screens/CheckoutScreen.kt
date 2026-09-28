package uvg.edu.laboratorio09.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import uvg.edu.laboratorio09.model.BillingType
import uvg.edu.laboratorio09.model.CheckoutUiState
import uvg.edu.laboratorio09.model.PaymentMethod
import uvg.edu.laboratorio09.model.toQuetzales

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    uiState: CheckoutUiState,
    orderUnitCount: Int,
    orderTotalCents: Int,
    onFullNameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onBillingTypeChange: (BillingType) -> Unit,
    onNitChange: (String) -> Unit,
    onBusinessNameChange: (String) -> Unit,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val nitFocusRequester = remember { FocusRequester() }
    val isConfirmEnabled by remember(uiState.isFormValid, orderUnitCount) {
        derivedStateOf { uiState.isFormValid && orderUnitCount > 0 }
    }

    fun dismissKeyboard() {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    Text(
                        "Total: ${orderTotalCents.toQuetzales()}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OrderSummary(
                orderUnitCount = orderUnitCount,
                orderTotalCents = orderTotalCents
            )

            CheckoutTextField(
                value = uiState.fullName,
                onValueChange = onFullNameChange,
                label = "Nombre completo *",
                placeholder = "Ej. María Morales",
                touched = uiState.fullNameTouched,
                error = uiState.fullNameError,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                onNext = { focusManager.moveFocus(FocusDirection.Next) }
            )

            CheckoutTextField(
                value = uiState.phoneNumber,
                onValueChange = onPhoneNumberChange,
                label = "Teléfono / WhatsApp *",
                placeholder = "Ej. 55123456",
                touched = uiState.phoneNumberTouched,
                error = uiState.phoneNumberError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = if (uiState.billingType == BillingType.NIT) ImeAction.Next else ImeAction.Done
                ),
                onNext = {
                    if (uiState.billingType == BillingType.NIT) nitFocusRequester.requestFocus()
                    else dismissKeyboard()
                },
                onDone = ::dismissKeyboard
            )

            Text("Facturación *", style = MaterialTheme.typography.labelLarge)
            Column(Modifier.fillMaxWidth().selectableGroup()) {
                BillingType.entries.forEach { type ->
                    RadioOptionRow(
                        selected = uiState.billingType == type,
                        label = if (type == BillingType.CONSUMER_FINAL) {
                            "Consumidor Final (CF)"
                        } else {
                            "Factura con NIT"
                        },
                        onClick = {
                            dismissKeyboard()
                            onBillingTypeChange(type)
                        }
                    )
                }
            }

            AnimatedVisibility(visible = uiState.billingType == BillingType.NIT) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Datos de facturación fiscal",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium
                    )
                    CheckoutTextField(
                        value = uiState.nit,
                        onValueChange = onNitChange,
                        label = "NIT *",
                        placeholder = "Ej. 45123",
                        touched = uiState.nitTouched,
                        error = uiState.nitError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        onNext = { focusManager.moveFocus(FocusDirection.Next) },
                        modifier = Modifier.focusRequester(nitFocusRequester)
                    )
                    CheckoutTextField(
                        value = uiState.businessName,
                        onValueChange = onBusinessNameChange,
                        label = "Razón Social / Nombre fiscal *",
                        placeholder = "Ej. Guzmán Inversiones S.A.",
                        touched = uiState.businessNameTouched,
                        error = uiState.businessNameError,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        onDone = ::dismissKeyboard
                    )
                }
            }

            Text("Método de pago *", style = MaterialTheme.typography.labelLarge)
            Column(Modifier.fillMaxWidth().selectableGroup()) {
                PaymentMethod.entries.forEach { method ->
                    RadioOptionRow(
                        selected = uiState.paymentMethod == method,
                        label = if (method == PaymentMethod.CASH_ON_DELIVERY) {
                            "Efectivo contra entrega"
                        } else {
                            "Transferencia bancaria"
                        },
                        onClick = {
                            dismissKeyboard()
                            onPaymentMethodChange(method)
                        }
                    )
                }
            }

            Button(
                onClick = onConfirm,
                enabled = isConfirmEnabled,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Text("Confirmar pedido (Total ${orderTotalCents.toQuetzales()})")
            }

            if (!isConfirmEnabled) {
                Text(
                    "Completa los campos obligatorios para continuar.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun OrderSummary(
    orderUnitCount: Int,
    orderTotalCents: Int,
    modifier: Modifier = Modifier
) {
    Card(modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Resumen del pedido", style = MaterialTheme.typography.titleSmall)
                Text("$orderUnitCount ${if (orderUnitCount == 1) "unidad" else "unidades"}")
            }
            Text("Subtotal: ${orderTotalCents.toQuetzales()}")
        }
    }
}

@Composable
private fun CheckoutTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    touched: Boolean,
    error: String?,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
    onNext: () -> Unit = {},
    onDone: () -> Unit = {}
) {
    val showError = touched && error != null
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        isError = showError,
        supportingText = if (showError) ({ Text(error.orEmpty()) }) else null,
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(
            onNext = { onNext() },
            onDone = { onDone() }
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun RadioOptionRow(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
    HorizontalDivider()
}
