package uvg.edu.laboratorio09.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uvg.edu.laboratorio09.model.Chocolate
import uvg.edu.laboratorio09.model.toQuetzales

@Composable
fun ProductCard(
    chocolate: Chocolate,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onChocolateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DisposableEffect(chocolate.id) {
        Log.d("CatalogProbe", "ENTER id-${chocolate.id}")
        onDispose { Log.d("CatalogProbe", "EXIT id-${chocolate.id}") }
    }
    Card(onClick = onChocolateClick, modifier = modifier.fillMaxWidth()) {
        Column {
            ProductImage(chocolate)
            Column(modifier = Modifier.padding(12.dp)) {
                Text(chocolate.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    chocolate.priceCents.toQuetzales(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(if (chocolate.stock == 0) "Agotado" else "${chocolate.stock} disponibles")
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFavorite) "Quitar ${chocolate.name} de favoritos"
                        else "Agregar ${chocolate.name} a favoritos"
                    )
                }
            }
        }
    }
}
