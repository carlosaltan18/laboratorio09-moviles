package uvg.edu.laboratorio09.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uvg.edu.laboratorio09.model.Chocolate
import uvg.edu.laboratorio09.model.CatalogSort

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChocolateCatalogScreen(
    chocolates: List<Chocolate>,
    totalProducts: Int,
    query: String,
    catalogSort: CatalogSort,
    favoriteIds: Set<String>,
    orderUnitCount: Int,
    gridState: LazyGridState,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onCatalogSortChange: (CatalogSort) -> Unit,
    onChocolateClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOrderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val showScrollToTop by remember(gridState) {
        derivedStateOf { gridState.firstVisibleItemIndex > 4 }
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Chocolatería") },
                actions = {
                    TextButton(onClick = onOrderClick) { Text("Pedido · $orderUnitCount") }
                }
            )
        },
        floatingActionButton = {
            if (showScrollToTop) {
                FloatingActionButton(onClick = {
                    scope.launch { gridState.animateScrollToItem(0) }
                }) {
                    Icon(Icons.Filled.ArrowUpward, contentDescription = "Volver arriba")
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("Buscar productos") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            Text(
                "${chocolates.size} de $totalProducts productos",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CatalogSort.entries.forEach { sort ->
                    FilterChip(
                        selected = catalogSort == sort,
                        onClick = { onCatalogSortChange(sort) },
                        label = {
                            Text(
                                when (sort) {
                                    CatalogSort.ORIGINAL -> "Original"
                                    CatalogSort.NAME -> "Nombre"
                                    CatalogSort.PRICE -> "Precio"
                                }
                            )
                        }
                    )
                }
            }
            if (query.isNotEmpty()) {
                TextButton(onClick = onClearQuery, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text("Limpiar búsqueda")
                }
            }
            if (chocolates.isEmpty()) {
                Text("No encontramos productos.", modifier = Modifier.padding(16.dp))
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(items = chocolates, key = { it.id }) { chocolate ->
                    ProductCard(
                        chocolate = chocolate,
                        isFavorite = chocolate.id in favoriteIds,
                        onToggleFavorite = { onToggleFavorite(chocolate.id) },
                        onChocolateClick = { onChocolateClick(chocolate.id) }
                    )
                }
            }
        }
    }
}
