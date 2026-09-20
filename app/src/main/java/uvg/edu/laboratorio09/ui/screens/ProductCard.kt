package uvg.edu.laboratorio09.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import uvg.edu.laboratorio09.model.Chocolate

enum class ImageState { Loading, Success, Error }

@Composable
fun ProductCard(
    chocolate: Chocolate,
    onChocolateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Registro del ciclo de vida en Logcat
    DisposableEffect(chocolate.id) {
        Log.d("CatalogProbe", "ENTER id-${chocolate.id}")
        onDispose {
            Log.d("CatalogProbe", "EXIT id-${chocolate.id}")
        }
    }

    var imageState by remember { mutableStateOf(ImageState.Loading) }

    Card(
        onClick = onChocolateClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://picsum.photos/seed/${chocolate.id}/400/400")
                        .crossfade(true)
                        .build(),
                    contentDescription = "Imagen de ${chocolate.name}",
                    onLoading = { imageState = ImageState.Loading },
                    onSuccess = { imageState = ImageState.Success },
                    onError = { imageState = ImageState.Error },
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (imageState == ImageState.Loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray)
                    )
                }

                if (imageState == ImageState.Error) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF5D4037)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Imagen no disponible", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = chocolate.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "Q${chocolate.priceCents}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}