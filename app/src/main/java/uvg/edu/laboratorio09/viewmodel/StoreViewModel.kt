package uvg.edu.laboratorio09.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import uvg.edu.laboratorio09.model.Chocolate
import uvg.edu.laboratorio09.model.Chocolatier
import uvg.edu.laboratorio09.model.StoreUiState

private val storeProducts = listOf(
    Chocolate(
        id = "chocolate_01",
        chocolatierId = "chocolatier_01",
        name = "Chocolate Oscuro 70%",
        description = "Chocolate artesanal de cacao guatemalteco con sabor intenso y notas frutales.",
        price = 45.00
    ),
    Chocolate(
        id = "chocolate_02",
        chocolatierId = "chocolatier_02",
        name = "Chocolate con Café",
        description = "Chocolate semiamargo combinado con café de Antigua Guatemala.",
        price = 52.00
    ),
    Chocolate(
        id = "chocolate_03",
        chocolatierId = "chocolatier_01",
        name = "Chocolate con Cardamomo",
        description = "Chocolate con leche aromatizado con cardamomo de Alta Verapaz.",
        price = 48.00
    )
)

private val associatedProfiles = listOf(
    Chocolatier(
        id = "chocolatier_01",
        name = "Cacao Maya",
        role = "Chocolatero artesanal",
        location = "Cobán, Alta Verapaz",
        description = "Taller dedicado a producir chocolates artesanales con cacao guatemalteco y especias locales."
    ),
    Chocolatier(
        id = "chocolatier_02",
        name = "Dulce Antigua",
        role = "Fabricante de chocolate",
        location = "Antigua Guatemala, Sacatepéquez",
        description = "Chocolatería especializada en combinar cacao nacional con café y otros sabores tradicionales."
    )
)

class StoreViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        StoreUiState(
            products = storeProducts,
            profiles = associatedProfiles
        )
    )

    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    fun toggleFavorite(productId: String) {
        _uiState.update { currentState ->
            val updatedFavorites =
                if (productId in currentState.favoriteProductIds) {
                    currentState.favoriteProductIds - productId
                } else {
                    currentState.favoriteProductIds + productId
                }

            currentState.copy(
                favoriteProductIds = updatedFavorites
            )
        }
    }
}
