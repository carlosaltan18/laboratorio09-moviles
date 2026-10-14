package uvg.edu.laboratorio09.remoteorders.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uvg.edu.laboratorio09.model.toQuetzales
import uvg.edu.laboratorio09.remoteorders.OrderDetailUiState

@Composable
fun RemoteOrderDetailScreen(
    uiState: OrderDetailUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    RemoteOrderScaffold("Detalle del pedido", onBack, modifier) { padding ->
        when (uiState) {
            OrderDetailUiState.Idle -> RemoteOrderMessage(
                "Consulta los datos de este pedido.", Modifier.padding(padding),
                actionLabel = "Cargar pedido", onAction = onRetry
            )
            OrderDetailUiState.Loading -> RemoteOrderMessage(
                "Cargando pedido…", Modifier.padding(padding), loading = true
            )
            OrderDetailUiState.NotFound -> RemoteOrderMessage(
                "Este pedido ya no existe en el servidor.", Modifier.padding(padding),
                actionLabel = "Volver a Mis pedidos", onAction = onBack
            )
            is OrderDetailUiState.Error -> RemoteOrderMessage(
                uiState.message, Modifier.padding(padding),
                actionLabel = "Reintentar", onAction = onRetry
            )
            is OrderDetailUiState.Success -> {
                val order = uiState.order
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Pedido #${order.id}", style = MaterialTheme.typography.headlineSmall)
                        Text("Fecha: ${formatOrderDate(order.createdAtEpochMillis)}")
                    }
                    items(order.lines) { line ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(line.productName, style = MaterialTheme.typography.titleMedium)
                                Text("Precio unitario: ${line.unitPriceCents.toQuetzales()}")
                                Text("Cantidad: ${line.quantity}")
                                Text("Subtotal: ${line.subtotalCents.toQuetzales()}")
                            }
                        }
                    }
                    item {
                        Text("Total: ${order.totalCents.toQuetzales()}", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}
