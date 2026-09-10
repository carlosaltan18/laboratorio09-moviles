package uvg.edu.laboratorio09

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import uvg.edu.laboratorio09.navigation.StoreNavKey
import uvg.edu.laboratorio09.ui.screens.CatalogScreen
import uvg.edu.laboratorio09.ui.screens.ChocolateDetailScreen
import uvg.edu.laboratorio09.ui.screens.ChocolatierProfileScreen
import uvg.edu.laboratorio09.ui.theme.Laboratorio09Theme
import uvg.edu.laboratorio09.viewmodel.StoreViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Laboratorio09Theme {
                ChocolateStoreApp()
            }
        }
    }
}

@Composable
fun ChocolateStoreApp() {
    val viewModel: StoreViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backStack = rememberNavBackStack(StoreNavKey.Catalog)

    BackHandler(enabled = backStack.size > 1) {
        backStack.removeLastOrNull()
    }

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<StoreNavKey.Catalog> {
                CatalogScreen(
                    products = uiState.products,
                    favoriteProductIds = uiState.favoriteProductIds,
                    onProductSelected = { productId ->
                        backStack.add(StoreNavKey.Detail(productId))
                    },
                    onToggleFavorite = viewModel::toggleFavorite
                )
            }
            entry<StoreNavKey.Detail> { key ->
                val product = uiState.products.find {
                    it.id == key.productId
                }

                if (product != null) {
                    ChocolateDetailScreen(
                        chocolate = product,
                        isFavorite = product.id in uiState.favoriteProductIds,
                        onToggleFavorite = viewModel::toggleFavorite,
                        onViewProfile = { profileId ->
                            backStack.add(StoreNavKey.Profile(profileId))
                        },
                        onBack = { backStack.removeLastOrNull() }
                    )
                } else {
                    MissingDestination(message = "Producto no encontrado")
                }
            }
            entry<StoreNavKey.Profile> { key ->
                val profile = uiState.profiles.find {
                    it.id == key.profileId
                }

                if (profile != null) {
                    ChocolatierProfileScreen(
                        chocolatier = profile,
                        onBack = { backStack.removeLastOrNull() }
                    )
                } else {
                    MissingDestination(message = "Perfil no encontrado")
                }
            }
        }
    )
}

@Composable
private fun MissingDestination(message: String) {
    Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(message)
    }
}
