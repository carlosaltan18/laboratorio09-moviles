package uvg.edu.laboratorio09.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import uvg.edu.laboratorio09.StoreApplication
import uvg.edu.laboratorio09.domain.lineSubtotalCents
import uvg.edu.laboratorio09.domain.orderTotalCents
import uvg.edu.laboratorio09.domain.revalidated
import uvg.edu.laboratorio09.domain.repository.AuthenticationRepository
import uvg.edu.laboratorio09.domain.repository.CartRepository
import uvg.edu.laboratorio09.domain.repository.CatalogPreferencesRepository
import uvg.edu.laboratorio09.domain.repository.ChocolatierRepository
import uvg.edu.laboratorio09.domain.repository.FavoriteRepository
import uvg.edu.laboratorio09.domain.repository.OutOfStockException
import uvg.edu.laboratorio09.domain.repository.ProductRepository
import uvg.edu.laboratorio09.domain.repository.RemoteOrderRepository
import uvg.edu.laboratorio09.model.BillingType
import uvg.edu.laboratorio09.model.CatalogLoadState
import uvg.edu.laboratorio09.model.CatalogSort
import uvg.edu.laboratorio09.model.CheckoutUiState
import uvg.edu.laboratorio09.model.Chocolatier
import uvg.edu.laboratorio09.model.CreateRemoteOrder
import uvg.edu.laboratorio09.model.OrderLine
import uvg.edu.laboratorio09.model.OrderReceipt
import uvg.edu.laboratorio09.model.OrderSubmissionState
import uvg.edu.laboratorio09.model.PaymentMethod
import uvg.edu.laboratorio09.model.RemoteOrderLine
import uvg.edu.laboratorio09.model.StoreUiState

private data class StoreMemoryState(
    val query: String = "",
    val orderMessage: String? = null
)

private data class CatalogData(
    val loadState: CatalogLoadState,
    val profiles: List<Chocolatier>
)

private data class LocalStoreData(
    val favoriteIds: Set<String>,
    val orderLines: List<OrderLine>,
    val catalogSort: CatalogSort
)

class StoreViewModel(
    private val authenticationRepository: AuthenticationRepository,
    private val productRepository: ProductRepository,
    private val favoriteRepository: FavoriteRepository,
    private val cartRepository: CartRepository,
    private val catalogPreferencesRepository: CatalogPreferencesRepository,
    private val remoteOrderRepository: RemoteOrderRepository,
    chocolatierRepository: ChocolatierRepository
) : ViewModel() {
    private val mutationMutex = Mutex()
    private val _memoryState = MutableStateFlow(StoreMemoryState())
    private val _catalogLoadState = MutableStateFlow<CatalogLoadState>(CatalogLoadState.Loading)
    private val _reservingProductIds = MutableStateFlow<Set<String>>(emptySet())
    private var catalogObservationJob: Job? = null

    private val catalogData = combine(
        _catalogLoadState,
        chocolatierRepository.observeChocolatiers()
    ) { loadState, profiles ->
        CatalogData(loadState, profiles)
    }

    private val localStoreData = combine(
        favoriteRepository.observeFavoriteIds(),
        cartRepository.observeOrderLines(),
        catalogPreferencesRepository.observeCatalogSort()
    ) { favoriteIds, orderLines, catalogSort ->
        LocalStoreData(favoriteIds, orderLines, catalogSort)
    }

    val uiState: StateFlow<StoreUiState> = combine(
        _memoryState,
        catalogData,
        localStoreData,
        _reservingProductIds
    ) { memory, catalog, local, reservingProductIds ->
        val products = (catalog.loadState as? CatalogLoadState.Content)
            ?.products
            .orEmpty()
        StoreUiState(
            products = products,
            profiles = catalog.profiles,
            favoriteProductIds = local.favoriteIds,
            query = memory.query,
            catalogSort = local.catalogSort,
            catalogLoadState = catalog.loadState,
            reservingProductIds = reservingProductIds,
            orderLines = local.orderLines,
            orderMessage = memory.orderMessage
        ).withOrderCalculations()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StoreUiState()
    )

    private val _checkoutUiState = MutableStateFlow(CheckoutUiState().revalidated())
    val checkoutUiState: StateFlow<CheckoutUiState> = _checkoutUiState.asStateFlow()

    private val _orderReceipt = MutableStateFlow<OrderReceipt?>(null)
    val orderReceipt: StateFlow<OrderReceipt?> = _orderReceipt.asStateFlow()

    private val _orderSubmissionState = MutableStateFlow<OrderSubmissionState>(
        OrderSubmissionState.Idle
    )
    val orderSubmissionState: StateFlow<OrderSubmissionState> =
        _orderSubmissionState.asStateFlow()

    private var nextOrderNumber = 1

    init {
        loadCatalog()
    }

    fun retryCatalog() {
        loadCatalog()
    }

    private fun loadCatalog() {
        catalogObservationJob?.cancel()
        catalogObservationJob = viewModelScope.launch {
            _catalogLoadState.value = CatalogLoadState.Loading
            val authentication = authenticationRepository.ensureSignedIn()
            if (authentication.isFailure) {
                _catalogLoadState.value = CatalogLoadState.Error(
                    "No pudimos iniciar la sesión del catálogo. Intenta de nuevo."
                )
                return@launch
            }

            productRepository.observeProducts()
                .catch { error ->
                    if (error is CancellationException) throw error
                    _catalogLoadState.value = CatalogLoadState.Error(
                        "No pudimos cargar el catálogo. Revisa tu conexión e intenta de nuevo."
                    )
                }
                .collectLatest { products ->
                    _catalogLoadState.value = CatalogLoadState.Content(products)
                }
        }
    }

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

    fun submitOrder() {
        if (_orderSubmissionState.value is OrderSubmissionState.Submitting) return

        viewModelScope.launch {
            mutationMutex.withLock {
                submitReservedOrder()
            }
        }
    }

    suspend fun confirmOrder(): Boolean {
        submitOrder()
        return orderSubmissionState.first { state ->
            state !is OrderSubmissionState.Idle &&
                state !is OrderSubmissionState.Submitting
        } is OrderSubmissionState.Success
    }

    private suspend fun submitReservedOrder() {
        val currentCheckout = _checkoutUiState.value
        val validatedCheckout = currentCheckout.revalidated().copy(
            fullNameTouched = true,
            phoneNumberTouched = true,
            nitTouched = currentCheckout.billingType == BillingType.NIT,
            businessNameTouched = currentCheckout.billingType == BillingType.NIT
        )
        _checkoutUiState.value = validatedCheckout

        val storeSnapshot = uiState.value
        if (!validatedCheckout.isFormValid) {
            _orderSubmissionState.value = OrderSubmissionState.Error(
                "Revisa los campos obligatorios antes de continuar."
            )
            return
        }
        if (storeSnapshot.orderLines.isEmpty()) {
            _orderSubmissionState.value = OrderSubmissionState.Error("Tu pedido está vacío.")
            return
        }

        val request = storeSnapshot.toRemoteOrderRequest(validatedCheckout)
        _orderSubmissionState.value = OrderSubmissionState.Submitting
        val submission = remoteOrderRepository.createOrder(request)
        val remoteOrder = submission.getOrElse {
            _orderSubmissionState.value = OrderSubmissionState.Error(
                "No pudimos enviar tu pedido. Revisa tu conexión e intenta nuevamente."
            )
            return
        }

        val clearResult = cartRepository.clear()
        if (clearResult.isFailure) {
            _orderSubmissionState.value = OrderSubmissionState.Error(
                "El pedido fue recibido, pero no pudimos actualizar el carrito."
            )
            return
        }

        _orderReceipt.value = OrderReceipt(
            folio = "#ORD-${nextOrderNumber.toString().padStart(5, '0')}",
            serverId = remoteOrder.id,
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
            totalCents = remoteOrder.totalCents
        )
        nextOrderNumber += 1
        _checkoutUiState.value = CheckoutUiState().revalidated()
        _memoryState.update { it.copy(orderMessage = null) }
        _orderSubmissionState.value = OrderSubmissionState.Success(remoteOrder.id)
    }

    fun consumeSubmissionSuccess() {
        if (_orderSubmissionState.value is OrderSubmissionState.Success) {
            _orderSubmissionState.value = OrderSubmissionState.Idle
        }
    }

    fun updateQuery(query: String) {
        _memoryState.update { it.copy(query = query) }
    }

    fun clearQuery() = updateQuery("")

    fun updateCatalogSort(sort: CatalogSort) {
        viewModelScope.launch {
            catalogPreferencesRepository.setCatalogSort(sort).onFailure {
                setOrderMessage("No pudimos guardar el orden del catálogo.")
            }
        }
    }

    fun clearOrderMessage() {
        setOrderMessage(null)
    }

    fun addProductToOrder(productId: String) {
        if (productId in _reservingProductIds.value) return
        setReserving(productId, true)

        viewModelScope.launch {
            mutationMutex.withLock {
                try {
                    val product = uiState.value.products.find { it.id == productId }
                    if (product == null || product.stock <= 0) {
                        setOrderMessage("Este producto ya está agotado.")
                        return@withLock
                    }

                    val reserveResult = productRepository.reserveUnit(productId)
                    if (reserveResult.isFailure) {
                        setOrderMessage(reserveResult.outOfStockMessage())
                        return@withLock
                    }

                    val cartResult = cartRepository.increase(productId)
                    if (cartResult.isFailure) {
                        productRepository.releaseUnit(productId)
                        setOrderMessage("No pudimos agregar el producto al pedido. Intenta de nuevo.")
                    } else {
                        setOrderMessage("Se reservó 1 unidad para tu pedido.")
                    }
                } finally {
                    setReserving(productId, false)
                }
            }
        }
    }

    fun decreaseProduct(productId: String) {
        viewModelScope.launch {
            mutationMutex.withLock {
                val line = cartRepository.observeOrderLines().first()
                    .find { it.productId == productId }
                    ?: return@withLock
                setReserving(productId, true)
                val releaseResult = productRepository.releaseUnit(productId)
                if (releaseResult.isFailure) {
                    setReserving(productId, false)
                    setOrderMessage("No pudimos devolver la reserva. Intenta de nuevo.")
                    return@withLock
                }

                val cartResult = cartRepository.decrease(line.productId)
                if (cartResult.isFailure) {
                    productRepository.reserveUnit(productId)
                    setOrderMessage("No pudimos actualizar el pedido. Intenta de nuevo.")
                } else {
                    clearOrderMessage()
                }
                setReserving(productId, false)
            }
        }
    }

    fun removeProduct(productId: String) {
        viewModelScope.launch {
            mutationMutex.withLock {
                val line = cartRepository.observeOrderLines().first()
                    .find { it.productId == productId }
                    ?: return@withLock
                setReserving(productId, true)
                val releases = List(line.quantity) {
                    productRepository.releaseUnit(productId)
                }
                if (releases.any { it.isFailure }) {
                    repeat(releases.count { it.isSuccess }) {
                        productRepository.reserveUnit(productId)
                    }
                    setOrderMessage("No pudimos devolver todas las reservas. Intenta de nuevo.")
                    setReserving(productId, false)
                    return@withLock
                }

                val cartResult = cartRepository.remove(productId)
                if (cartResult.isFailure) {
                    repeat(line.quantity) { productRepository.reserveUnit(productId) }
                    setOrderMessage("No pudimos actualizar el pedido. Intenta de nuevo.")
                } else {
                    clearOrderMessage()
                }
                setReserving(productId, false)
            }
        }
    }

    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            favoriteRepository.toggleFavorite(productId).onFailure {
                setOrderMessage("No pudimos actualizar tus favoritos.")
            }
        }
    }

    private fun setReserving(productId: String, isReserving: Boolean) {
        _reservingProductIds.update { current ->
            if (isReserving) current + productId else current - productId
        }
    }

    private fun setOrderMessage(message: String?) {
        _memoryState.update { it.copy(orderMessage = message) }
    }

    private fun Result<Unit>.outOfStockMessage(): String = when (exceptionOrNull()) {
        is OutOfStockException -> "Este producto ya está agotado."
        else -> "No pudimos reservar el producto. Intenta de nuevo."
    }

    private fun StoreUiState.withOrderCalculations(): StoreUiState {
        val validLines = orderLines.filter { line -> products.any { it.id == line.productId } }
        val subtotals = validLines.associate { line ->
            val product = products.first { it.id == line.productId }
            line.productId to lineSubtotalCents(product, line)
        }
        return copy(
            orderSubtotalsCents = subtotals,
            orderTotalCents = orderTotalCents(products, validLines)
        )
    }

    private fun StoreUiState.toRemoteOrderRequest(
        checkoutState: CheckoutUiState
    ): CreateRemoteOrder = CreateRemoteOrder(
        createdAtEpochMillis = System.currentTimeMillis(),
        customerName = "Cliente de tienda",
        billingType = if (checkoutState.billingType == BillingType.CONSUMER_FINAL) {
            "CF"
        } else {
            "NIT"
        },
        paymentMethod = when (checkoutState.paymentMethod) {
            PaymentMethod.CASH_ON_DELIVERY -> "Efectivo contra entrega"
            PaymentMethod.BANK_TRANSFER -> "Transferencia bancaria"
        },
        totalCents = orderTotalCents,
        status = "received",
        lines = orderLines.mapNotNull { line ->
            products.find { it.id == line.productId }?.let { product ->
                RemoteOrderLine(
                    productId = product.id,
                    productName = product.name,
                    unitPriceCents = product.priceCents,
                    quantity = line.quantity,
                    subtotalCents = lineSubtotalCents(product, line)
                )
            }
        }
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[
                    ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                ] as StoreApplication
                val container = application.container
                StoreViewModel(
                    authenticationRepository = container.authenticationRepository,
                    productRepository = container.productRepository,
                    favoriteRepository = container.favoriteRepository,
                    cartRepository = container.cartRepository,
                    catalogPreferencesRepository = container.catalogPreferencesRepository,
                    remoteOrderRepository = container.remoteOrderRepository,
                    chocolatierRepository = container.chocolatierRepository
                )
            }
        }
    }
}
