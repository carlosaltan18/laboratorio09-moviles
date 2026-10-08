package uvg.edu.laboratorio09.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import uvg.edu.laboratorio09.data.ChocolateCatalogFactory
import uvg.edu.laboratorio09.data.local.FavoriteEntity
import uvg.edu.laboratorio09.data.local.OrderLineEntity
import uvg.edu.laboratorio09.data.local.StoreDatabase
import uvg.edu.laboratorio09.data.local.toOrderLine
import uvg.edu.laboratorio09.data.local.toProductId
import uvg.edu.laboratorio09.domain.addToOrder
import uvg.edu.laboratorio09.domain.decreaseOrderLine
import uvg.edu.laboratorio09.domain.deductOrderFromInventory
import uvg.edu.laboratorio09.domain.lineSubtotalCents
import uvg.edu.laboratorio09.domain.orderTotalCents
import uvg.edu.laboratorio09.domain.revalidated
import uvg.edu.laboratorio09.domain.validateOrderAvailability
import uvg.edu.laboratorio09.model.BillingType
import uvg.edu.laboratorio09.model.CheckoutUiState
import uvg.edu.laboratorio09.model.Chocolate
import uvg.edu.laboratorio09.model.Chocolatier
import uvg.edu.laboratorio09.model.OrderLine
import uvg.edu.laboratorio09.model.OrderReceipt
import uvg.edu.laboratorio09.model.OrderResult
import uvg.edu.laboratorio09.model.PaymentMethod
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

private data class StoreMemoryState(
    val products: List<Chocolate>,
    val profiles: List<Chocolatier>,
    val query: String = "",
    val orderMessage: String? = null
)

class StoreViewModel(application: Application) : AndroidViewModel(application) {
    private val storeDao = StoreDatabase.getInstance(application).storeDao()
    // Serializa acciones rapidas; cada regla usa el ultimo pedido de Room.
    private val mutationMutex = Mutex()
    private val favoriteIdsFlow = storeDao.observeFavorites().map { entities ->
        entities.map { it.toProductId() }.toSet()
    }
    private val orderLinesFlow = storeDao.observeOrderLines().map { entities ->
        entities.map { it.toOrderLine() }
    }

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

    private val _memoryState = MutableStateFlow(
        StoreMemoryState(products = initialCatalog, profiles = associatedProfiles)
    )

    val uiState: StateFlow<StoreUiState> = combine(
        _memoryState, favoriteIdsFlow, orderLinesFlow
    ) { memory, favoriteIds, orderLines ->
        StoreUiState(
            products = memory.products,
            profiles = memory.profiles,
            query = memory.query,
            favoriteProductIds = favoriteIds
        ).withOrderLines(orderLines, memory.orderMessage)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StoreUiState(products = initialCatalog, profiles = associatedProfiles)
    )

    private val _checkoutUiState = MutableStateFlow(CheckoutUiState().revalidated())
    val checkoutUiState: StateFlow<CheckoutUiState> = _checkoutUiState.asStateFlow()

    private val _orderReceipt = MutableStateFlow<OrderReceipt?>(null)
    val orderReceipt: StateFlow<OrderReceipt?> = _orderReceipt.asStateFlow()

    private var nextOrderNumber = 1

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

    suspend fun confirmOrder(): Boolean = viewModelScope.async {
        mutationMutex.withLock { confirmPersistedOrder() }
    }.await()

    private suspend fun confirmPersistedOrder(): Boolean {
        val currentCheckout = _checkoutUiState.value
        val validatedCheckout = currentCheckout.revalidated().copy(
            fullNameTouched = true,
            phoneNumberTouched = true,
            nitTouched = currentCheckout.billingType == BillingType.NIT,
            businessNameTouched = currentCheckout.billingType == BillingType.NIT
        )
        _checkoutUiState.value = validatedCheckout

        val memory = _memoryState.value
        val currentOrder = StoreUiState(memory.products, memory.profiles)
            .withOrderLines(orderLinesFlow.first())
        val availabilityError = validateOrderAvailability(
            products = currentOrder.products,
            lines = currentOrder.orderLines
        )
        if (!validatedCheckout.isFormValid || availabilityError != null) {
            if (availabilityError != null) {
                _memoryState.update { it.copy(orderMessage = availabilityError) }
            }
            return false
        }

        val folio = "#ORD-${nextOrderNumber.toString().padStart(5, '0')}"
        _orderReceipt.value = OrderReceipt(
            folio = folio,
            customerName = validatedCheckout.fullName.trim(),
            phoneNumber = validatedCheckout.phoneNumber.trim(),
            billingType = validatedCheckout.billingType,
            nit = validatedCheckout.nit.trim().takeIf {
                validatedCheckout.billingType == BillingType.NIT
            },
            businessName = validatedCheckout.businessName.trim().takeIf {
                validatedCheckout.billingType == BillingType.NIT
            },
            paymentMethod = validatedCheckout.paymentMethod,
            totalCents = currentOrder.orderTotalCents
        )

        val updatedProducts = deductOrderFromInventory(
            products = currentOrder.products,
            lines = currentOrder.orderLines
        )
        storeDao.clearOrder()
        _memoryState.update { it.copy(products = updatedProducts, orderMessage = null) }
        nextOrderNumber += 1
        _checkoutUiState.value = CheckoutUiState().revalidated()
        return true
    }

    fun updateQuery(query: String) {
        _memoryState.update { it.copy(query = query) }
    }

    fun clearQuery() = updateQuery("")

    fun clearOrderMessage() {
        _memoryState.update { it.copy(orderMessage = null) }
    }

    fun addProductToOrder(productId: String) {
        viewModelScope.launch {
            mutationMutex.withLock {
                when (val result = addToOrder(
                    _memoryState.value.products, orderLinesFlow.first(), productId
                )) {
                    is OrderResult.Success -> {
                        val line = result.lines.first { it.productId == productId }
                        storeDao.upsertOrderLine(OrderLineEntity(line.productId, line.quantity))
                        _memoryState.update { it.copy(orderMessage = result.message) }
                    }
                    is OrderResult.Rejected -> {
                        _memoryState.update { it.copy(orderMessage = result.reason) }
                    }
                }
            }
        }
    }

    fun decreaseProduct(productId: String) {
        viewModelScope.launch {
            mutationMutex.withLock {
                val lines = decreaseOrderLine(orderLinesFlow.first(), productId)
                val line = lines.find { it.productId == productId }
                if (line == null) {
                    storeDao.deleteOrderLine(productId)
                } else {
                    storeDao.upsertOrderLine(OrderLineEntity(line.productId, line.quantity))
                }
                clearOrderMessage()
            }
        }
    }

    fun removeProduct(productId: String) {
        viewModelScope.launch {
            mutationMutex.withLock {
                storeDao.deleteOrderLine(productId)
                clearOrderMessage()
            }
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
        viewModelScope.launch {
            mutationMutex.withLock {
                if (productId in favoriteIdsFlow.first()) {
                    storeDao.deleteFavorite(productId)
                } else {
                    storeDao.insertFavorite(FavoriteEntity(productId))
                }
            }
        }
    }
}
