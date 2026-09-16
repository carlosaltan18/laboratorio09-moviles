package uvg.edu.laboratorio09.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uvg.edu.laboratorio09.model.Chocolate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChocolateDetailScreen(
    chocolate: Chocolate,
    isFavorite: Boolean,
    onToggleFavorite: (String) -> Unit,
    onViewProfile: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    var showTechnicalSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(chocolate.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(chocolate.name, style = MaterialTheme.typography.headlineSmall)
            Text("Q%.2f".format(chocolate.price), style = MaterialTheme.typography.titleMedium)
            Text(chocolate.description, style = MaterialTheme.typography.bodyMedium)

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onToggleFavorite(chocolate.id) }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorito"
                    )
                }
                Text(if (isFavorite) "En tus favoritos" else "Agregar a favoritos")
            }

            TextButton(onClick = { showTechnicalSheet = !showTechnicalSheet }) {
                Text(if (showTechnicalSheet) "Ocultar ficha técnica" else "Ver ficha técnica")
            }
            if (showTechnicalSheet) {
                Text(
                    "Origen del cacao y notas de elaboración detalladas de ${chocolate.name}.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(onClick = { onViewProfile(chocolate.chocolatierId) }) {
                Text("Ver chocolatero")
            }
        }
    }
}