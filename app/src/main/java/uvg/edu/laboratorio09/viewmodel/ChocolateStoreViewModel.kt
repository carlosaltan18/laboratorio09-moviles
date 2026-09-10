package uvg.edu.laboratorio09.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uvg.edu.laboratorio09.data.ChocolateCatalogFactory
import uvg.edu.laboratorio09.model.Chocolate
import uvg.edu.laboratorio09.model.ChocolateStoreUiState
import uvg.edu.laboratorio09.model.Chocolatier

/** Holds the single, initial definition of the store catalog. */
class ChocolateStoreViewModel : ViewModel() {
    private val originalChocolatiers = listOf(
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

    private val initialCatalog = ChocolateCatalogFactory.createCatalog(
        originalChocolates = originalChocolates,
        chocolatierIds = originalChocolatiers.map { it.id }
    )

    init {
        check(initialCatalog.size == 500)
        check(initialCatalog.map { it.id }.distinct().size == 500)
        check(initialCatalog.all { it.priceCents > 0 })
        check(initialCatalog.all { it.stock >= 0 })
        check(initialCatalog.all { it.chocolatierId in originalChocolatiers.map { chocolatier -> chocolatier.id } })
        check(initialCatalog.all { it.imageUrl.isNotBlank() })
    }

    private val _uiState = MutableStateFlow(
        ChocolateStoreUiState(
            chocolates = initialCatalog,
            chocolatiers = originalChocolatiers
        )
    )

    val uiState: StateFlow<ChocolateStoreUiState> = _uiState.asStateFlow()
}
