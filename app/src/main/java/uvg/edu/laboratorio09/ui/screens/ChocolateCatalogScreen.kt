package uvg.edu.laboratorio09.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uvg.edu.laboratorio09.model.Chocolate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChocolateCatalogScreen(
    chocolates: List<Chocolate>,
    favoriteIds: Set<String>,
    onChocolateClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo de Chocolatería") }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Asignación de claves por ID para un scroll lazy óptimo
            items(items = chocolates, key = { it.id }) { chocolate ->
                ProductCard(
                    chocolate = chocolate,
                    onChocolateClick = { onChocolateClick(chocolate.id) }
                )
            }
        }
    }
}