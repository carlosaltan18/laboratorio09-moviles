package uvg.edu.laboratorio09.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import uvg.edu.laboratorio09.model.Chocolate
import uvg.edu.laboratorio09.model.ChocolateStoreUiState
import uvg.edu.laboratorio09.model.Chocolatier

class ChocolateStoreViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        ChocolateStoreUiState(
            chocolates = listOf(
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
            ),
            chocolatiers = listOf(
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
        )
    )

    val uiState: StateFlow<ChocolateStoreUiState> = _uiState.asStateFlow()

    // Publica un nuevo estado inmutable; es el único punto que modifica los favoritos.
    fun toggleFavorite(chocolateId: String) {
        _uiState.update { current ->
            val updatedFavorites = if (chocolateId in current.favoriteChocolateIds) {
                current.favoriteChocolateIds - chocolateId
            } else {
                current.favoriteChocolateIds + chocolateId
            }
            current.copy(favoriteChocolateIds = updatedFavorites)
        }
    }
}