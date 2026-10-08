package uvg.edu.laboratorio09.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uvg.edu.laboratorio09.model.BillingType
import uvg.edu.laboratorio09.model.OrderReceipt
import uvg.edu.laboratorio09.model.PaymentMethod
import uvg.edu.laboratorio09.model.toQuetzales

@Composable
fun OrderConfirmationScreen(
    receipt: OrderReceipt,
    onReturnToCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            "¡Pedido confirmado!",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            "Orden registrada exitosamente en su tienda.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Card(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReceiptRow("Folio", receipt.folio)
                ReceiptRow("Cliente", receipt.customerName)
                ReceiptRow("Teléfono", receipt.phoneNumber)
                ReceiptRow("Facturación", receipt.billingDescription())
                ReceiptRow("Método de pago", receipt.paymentDescription())
                ReceiptRow(
                    label = "Total del pedido",
                    value = receipt.totalCents.toQuetzales(),
                    emphasized = true
                )
            }
        }

        Button(
            onClick = onReturnToCatalog,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Volver al catálogo")
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun OrderReceipt.billingDescription(): String =
    if (billingType == BillingType.CONSUMER_FINAL) {
        "CF (Consumidor Final)"
    } else {
        "NIT $nit · $businessName"
    }

private fun OrderReceipt.paymentDescription(): String =
    if (paymentMethod == PaymentMethod.CASH_ON_DELIVERY) {
        "Efectivo contra entrega"
    } else {
        "Transferencia bancaria"
    }
