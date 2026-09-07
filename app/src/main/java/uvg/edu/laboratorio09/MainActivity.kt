package uvg.edu.laboratorio09

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import uvg.edu.laboratorio09.viewmodel.ChocolateStoreViewModel

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
fun ChocolateStoreApp(
    viewModel: ChocolateStoreViewModel = viewModel()
) {
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
                    chocolates = uiState.chocolates,
                    favoriteIds = uiState.favoriteChocolateIds,
                    onChocolateSelected = { productId ->
                        backStack.add(StoreNavKey.Detail(productId))
                    },
                    onToggleFavorite = viewModel::toggleFavorite
                )
            }
            entry<StoreNavKey.Detail> { key ->
                val chocolate = uiState.chocolates.first { it.id == key.productId }
                ChocolateDetailScreen(
                    chocolate = chocolate,
                    isFavorite = chocolate.id in uiState.favoriteChocolateIds,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onViewProfile = { profileId ->
                        backStack.add(StoreNavKey.Profile(profileId))
                    },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<StoreNavKey.Profile> { key ->
                val chocolatier = uiState.chocolatiers.first { it.id == key.profileId }
                ChocolatierProfileScreen(
                    chocolatier = chocolatier,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}