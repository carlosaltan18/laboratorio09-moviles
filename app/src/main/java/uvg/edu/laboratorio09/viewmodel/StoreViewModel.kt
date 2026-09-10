package uvg.edu.laboratorio09.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import uvg.edu.laboratorio09.data.ChocolateCatalogFactory
import uvg.edu.laboratorio09.model.Chocolate
import uvg.edu.laboratorio09.model.Chocolatier
import uvg.edu.laboratorio09.model.StoreUiState

private val originalChocolates = listOf(
    Chocolate(
        id = "chocolate_01",
        chocolatierId = "chocolatier_01",
        name = "Chocolate Oscuro 70%",
        description = "Chocolate artesanal de cacao guatemalteco con sabor intenso y notas frutales.",
        priceCents = 4500,
        stock = 0,
        imageUrl = "https://picsum.photos/seed/chocolate-01/400/400"
    ),
    Chocolate(
        id = "chocolate_02",
        chocolatierId = "chocolatier_02",
        name = "Chocolate con Café",
        description = "Chocolate semiamargo combinado con café de Antigua Guatemala.",
        priceCents = 5200,
        stock = 3,
        imageUrl = "https://picsum.photos/seed/chocolate-02/400/400"
    ),
    Chocolate(
        id = "chocolate_03",
        chocolatierId = "chocolatier_01",
        name = "Chocolate con Cardamomo",
        description = "Chocolate con leche aromatizado con cardamomo de Alta Verapaz.",
        priceCents = 4800,
        stock = 8,
        imageUrl = "https://picsum.photos/seed/chocolate-03/400/400"
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

    private val initialCatalog = ChocolateCatalogFactory.createCatalog(
        originalChocolates = originalChocolates,
        chocolatierIds = associatedProfiles.map { it.id }
    )

    init {
        val profileIds = associatedProfiles.map { it.id }.toSet()
        check(initialCatalog.size == 500)
        check(initialCatalog.map { it.id }.distinct().size == 500)
        check(initialCatalog.all { it.priceCents > 0 })
        check(initialCatalog.all { it.stock >= 0 })
        check(initialCatalog.all { it.chocolatierId in profileIds })
        check(initialCatalog.all { it.imageUrl.isNotBlank() })
    }

    private val _uiState = MutableStateFlow(
        StoreUiState(
            products = initialCatalog,
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
