package uvg.edu.laboratorio09.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import uvg.edu.laboratorio09.model.Chocolate

private enum class ImageState { Loading, Success, Error }

@Composable
fun ProductImage(chocolate: Chocolate, modifier: Modifier = Modifier) {
    var imageState by remember(chocolate.imageUrl) { mutableStateOf(ImageState.Loading) }
    Box(
        modifier = modifier.fillMaxWidth().height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(chocolate.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Imagen de ${chocolate.name}",
            onLoading = { imageState = ImageState.Loading },
            onSuccess = { imageState = ImageState.Success },
            onError = { imageState = ImageState.Error },
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        if (imageState != ImageState.Success) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (imageState == ImageState.Error) {
                    Text("Imagen no disponible", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
