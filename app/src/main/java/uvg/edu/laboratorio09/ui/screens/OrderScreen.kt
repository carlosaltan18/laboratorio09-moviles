package uvg.edu.laboratorio09.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import uvg.edu.laboratorio09.model.Chocolate
import uvg.edu.laboratorio09.model.OrderLine
import uvg.edu.laboratorio09.model.toQuetzales

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    lines: List<OrderLine>,
    products: List<Chocolate>,
    subtotalsCents: Map<String, Int>,
    totalCents: Int,
    message: String?,
    onIncrease: (String) -> Unit,
    onDecrease: (String) -> Unit,
    onRemove: (String) -> Unit,
    onBack: () -> Unit,
    onCatalogClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Mi pedido") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (message != null) {
                Text(
                    message,
                    modifier = Modifier.padding(16.dp).semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (lines.isEmpty()) {
                    item {
                        Text("No hay productos en tu pedido.")
                        TextButton(onClick = onCatalogClick) { Text("Volver al catálogo") }
                    }
                }
                items(lines, key = { it.productId }) { line ->
                    val product = products.find { it.id == line.productId }
                    if (product != null) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(product.name, style = MaterialTheme.typography.titleMedium)
                                Text("${product.priceCents.toQuetzales()} por unidad")
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    IconButton(
                                        onClick = { onDecrease(product.id) },
                                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                    ) {
                                        Icon(Icons.Filled.Remove, "Disminuir ${product.name}")
                                    }
                                    Text("Cantidad: ${line.quantity}")
                                    IconButton(
                                        onClick = { onIncrease(product.id) },
                                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, "Aumentar ${product.name}")
                                    }
                                }
                                Text("Subtotal ${subtotalsCents.getValue(product.id).toQuetzales()}")
                                TextButton(
                                    onClick = { onRemove(product.id) },
                                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                ) { Text("Eliminar") }
                            }
                        }
                    }
                }
            }
            Text(
                "Total ${totalCents.toQuetzales()}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
