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
import uvg.edu.laboratorio09.model.RemoteOrder
import uvg.edu.laboratorio09.remoteorders.OrdersUiState

@Composable
fun OrdersScreen(
    uiState: OrdersUiState,
    onRetry: () -> Unit,
    onOrderClick: (String) -> Unit,
    onGoToCatalog: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    RemoteOrderScaffold("Mis pedidos", onBack, modifier) { padding ->
        when (uiState) {
            OrdersUiState.Idle -> RemoteOrderMessage(
                "Consulta tus pedidos enviados.", Modifier.padding(padding),
                actionLabel = "Cargar pedidos", onAction = onRetry
            )
            OrdersUiState.Loading -> RemoteOrderMessage(
                "Cargando pedidos…", Modifier.padding(padding), loading = true
            )
            OrdersUiState.Empty -> RemoteOrderMessage(
                "Aún no has enviado pedidos.", Modifier.padding(padding),
                actionLabel = "Ir al catálogo", onAction = onGoToCatalog
            )
            is OrdersUiState.Error -> RemoteOrderMessage(
                uiState.message, Modifier.padding(padding),
                actionLabel = "Reintentar", onAction = onRetry
            )
            is OrdersUiState.Success -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Se conserva el orden recibido del servidor.
                items(uiState.orders, key = { it.id }) { order ->
                    OrderSummaryCard(order, onClick = { onOrderClick(order.id) })
                }
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(
    order: RemoteOrder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Pedido #${order.id}", style = MaterialTheme.typography.titleMedium)
            Text(formatOrderDate(order.createdAtEpochMillis))
            Text("${order.lines.sumOf { it.quantity }} unidades")
            Text("Total: ${order.totalCents.toQuetzales()}", style = MaterialTheme.typography.titleLarge)
        }
    }
}
