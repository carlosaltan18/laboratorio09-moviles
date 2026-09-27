package uvg.edu.laboratorio09.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import uvg.edu.laboratorio09.data.ChocolateCatalogFactory
import uvg.edu.laboratorio09.domain.revalidated
import uvg.edu.laboratorio09.model.Chocolate
import uvg.edu.laboratorio09.model.Chocolatier
import uvg.edu.laboratorio09.model.CheckoutUiState
import uvg.edu.laboratorio09.model.BillingType
import uvg.edu.laboratorio09.model.PaymentMethod
import uvg.edu.laboratorio09.domain.addToOrder
import uvg.edu.laboratorio09.domain.decreaseOrderLine
import uvg.edu.laboratorio09.domain.lineSubtotalCents
import uvg.edu.laboratorio09.domain.orderTotalCents
import uvg.edu.laboratorio09.domain.removeOrderLine
import uvg.edu.laboratorio09.model.OrderLine
import uvg.edu.laboratorio09.model.OrderResult
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

    private val _checkoutUiState = MutableStateFlow(CheckoutUiState().revalidated())
    val checkoutUiState: StateFlow<CheckoutUiState> = _checkoutUiState.asStateFlow()

    fun onFullNameChange(value: String) {
        _checkoutUiState.update {
            it.copy(fullName = value, fullNameTouched = true).revalidated()
        }
    }

    fun onPhoneNumberChange(value: String) {
        _checkoutUiState.update {
            it.copy(phoneNumber = value, phoneNumberTouched = true).revalidated()
        }
    }

    fun onNitChange(value: String) {
        _checkoutUiState.update {
            it.copy(nit = value, nitTouched = true).revalidated()
        }
    }

    fun onBusinessNameChange(value: String) {
        _checkoutUiState.update {
            it.copy(businessName = value, businessNameTouched = true).revalidated()
        }
    }

    fun onPaymentMethodChange(method: PaymentMethod) {
        _checkoutUiState.update { it.copy(paymentMethod = method).revalidated() }
    }

    fun onBillingTypeChange(type: BillingType) {
        _checkoutUiState.update { currentState ->
            if (type == BillingType.CONSUMER_FINAL) {
                currentState.copy(
                    billingType = type,
                    nitTouched = false,
                    businessNameTouched = false,
                    nitError = null,
                    businessNameError = null
                ).revalidated()
            } else {
                currentState.copy(billingType = type).revalidated()
            }
        }
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun clearQuery() = updateQuery("")

    fun clearOrderMessage() {
        _uiState.update { it.copy(orderMessage = null) }
    }

    fun addProductToOrder(productId: String) {
        _uiState.update { state ->
            when (val result = addToOrder(state.products, state.orderLines, productId)) {
                is OrderResult.Success -> state.withOrderLines(result.lines, result.message)
                is OrderResult.Rejected -> state.copy(orderMessage = result.reason)
            }
        }
    }

    fun decreaseProduct(productId: String) {
        _uiState.update { state ->
            state.withOrderLines(decreaseOrderLine(state.orderLines, productId))
        }
    }

    fun removeProduct(productId: String) {
        _uiState.update { state ->
            state.withOrderLines(removeOrderLine(state.orderLines, productId))
        }
    }

    private fun StoreUiState.withOrderLines(
        lines: List<OrderLine>,
        message: String? = null
    ): StoreUiState = copy(
        orderLines = lines,
        orderMessage = message,
        orderSubtotalsCents = lines.associate { line ->
            val product = products.first { it.id == line.productId }
            line.productId to lineSubtotalCents(product, line)
        },
        orderTotalCents = orderTotalCents(products, lines)
    )

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
