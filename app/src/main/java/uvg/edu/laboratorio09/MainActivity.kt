package uvg.edu.laboratorio09

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import uvg.edu.laboratorio09.model.OrderSubmissionState
import uvg.edu.laboratorio09.navigation.StoreNavKey
import uvg.edu.laboratorio09.remoteorders.OrdersViewModel
import uvg.edu.laboratorio09.remoteorders.ui.OrdersScreen
import uvg.edu.laboratorio09.remoteorders.ui.RemoteOrderDetailScreen
import uvg.edu.laboratorio09.ui.screens.CheckoutScreen
import uvg.edu.laboratorio09.ui.screens.ChocolateCatalogScreen
import uvg.edu.laboratorio09.ui.screens.ChocolateDetailScreen
import uvg.edu.laboratorio09.ui.screens.ChocolatierProfileScreen
import uvg.edu.laboratorio09.ui.screens.OrderConfirmationScreen
import uvg.edu.laboratorio09.ui.screens.OrderScreen
import uvg.edu.laboratorio09.ui.theme.Laboratorio09Theme
import uvg.edu.laboratorio09.viewmodel.ProductDetailViewModel
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
fun ChocolateStoreApp(modifier: Modifier = Modifier) {
    val viewModel: StoreViewModel = viewModel(factory = StoreViewModel.Factory)
    val ordersViewModel: OrdersViewModel = viewModel(factory = OrdersViewModel.Factory)
    val productDetailViewModel: ProductDetailViewModel =
        viewModel(factory = ProductDetailViewModel.Factory)
    val ordersState by ordersViewModel.ordersState.collectAsStateWithLifecycle()
    val orderDetailState by ordersViewModel.detailState.collectAsStateWithLifecycle()
    val storeState = viewModel.uiState.collectAsStateWithLifecycle()
    val checkoutState = viewModel.checkoutUiState.collectAsStateWithLifecycle()
    val uiState by storeState
    val checkoutUiState by checkoutState
    val orderReceipt by viewModel.orderReceipt.collectAsStateWithLifecycle()
    val submissionStateSource = viewModel.orderSubmissionState.collectAsStateWithLifecycle()
    val submissionState by submissionStateSource
    val productDetailState by productDetailViewModel.uiState.collectAsStateWithLifecycle()
    val isConfirmEnabled by remember(storeState, checkoutState, submissionStateSource) {
        derivedStateOf {
            checkoutState.value.isFormValid &&
                storeState.value.totalOrderUnits > 0 &&
                submissionStateSource.value !is OrderSubmissionState.Submitting
        }
    }
    val backStack = rememberNavBackStack(StoreNavKey.Catalog)

    LaunchedEffect(submissionState) {
        if (
            submissionState is OrderSubmissionState.Success &&
            backStack.lastOrNull() == StoreNavKey.Checkout
        ) {
            backStack.add(StoreNavKey.Confirmation)
            viewModel.consumeSubmissionSuccess()
        }
    }

    val gridState = rememberLazyGridState()
    var lastScrollQuery by rememberSaveable { mutableStateOf(uiState.query) }
    // Keeping this above NavDisplay preserves scrolling across navigation and rotation.
    LaunchedEffect(uiState.query) {
        if (lastScrollQuery != uiState.query) {
            gridState.scrollToItem(0)
            lastScrollQuery = uiState.query
        }
    }
    val openOrder: () -> Unit = {
        viewModel.clearOrderMessage()
        backStack.add(StoreNavKey.Order)
    }
    val returnToCatalog: () -> Unit = {
        while (backStack.size > 1) backStack.removeLastOrNull()
    }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = {
            if (backStack.lastOrNull() == StoreNavKey.Confirmation) {
                returnToCatalog()
            } else if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        // Adaptado de Android Developers: Animate between destinations (Navigation 3).
        transitionSpec = {
            slideInHorizontally(tween(250), initialOffsetX = { it }) togetherWith
                slideOutHorizontally(tween(250), targetOffsetX = { -it })
        },
        popTransitionSpec = {
            slideInHorizontally(tween(250), initialOffsetX = { -it }) togetherWith
                slideOutHorizontally(tween(250), targetOffsetX = { it })
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(tween(250), initialOffsetX = { -it }) togetherWith
                slideOutHorizontally(tween(250), targetOffsetX = { it })
        },
        entryProvider = entryProvider {
            entry<StoreNavKey.Catalog> {
                ChocolateCatalogScreen(
                    catalogLoadState = uiState.catalogLoadState,
                    chocolates = uiState.filteredProducts,
                    totalProducts = uiState.products.size,
                    query = uiState.query,
                    catalogSort = uiState.catalogSort,
                    orderUnitCount = uiState.totalOrderUnits,
                    gridState = gridState,
                    onQueryChange = viewModel::updateQuery,
                    onClearQuery = viewModel::clearQuery,
                    onCatalogSortChange = viewModel::updateCatalogSort,
                    onRetryCatalog = viewModel::retryCatalog,
                    onOrdersClick = {
                        ordersViewModel.loadOrders()
                        backStack.add(StoreNavKey.Orders)
                    },
                    onOrderClick = openOrder,
                    favoriteIds = uiState.favoriteProductIds,
                    onChocolateClick = { productId ->
                        viewModel.clearOrderMessage()
                        productDetailViewModel.clearTestResult()
                        backStack.add(StoreNavKey.Detail(productId))
                    },
                    onToggleFavorite = viewModel::toggleFavorite
                )
            }
            entry<StoreNavKey.Orders> {
                OrdersScreen(
                    uiState = ordersState,
                    onRetry = ordersViewModel::loadOrders,
                    onOrderClick = { orderId ->
                        ordersViewModel.loadOrder(orderId)
                        backStack.add(StoreNavKey.RemoteOrderDetail(orderId))
                    },
                    onGoToCatalog = returnToCatalog,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<StoreNavKey.RemoteOrderDetail> { key ->
                RemoteOrderDetailScreen(
                    uiState = orderDetailState,
                    onRetry = { ordersViewModel.loadOrder(key.orderId) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<StoreNavKey.Detail> { key ->
                val product = uiState.products.find {
                    it.id == key.productId
                }

                if (product != null) {
                    ChocolateDetailScreen(
                        chocolate = product,
                        quantityInOrder = uiState.orderLines.find { it.productId == product.id }?.quantity ?: 0,
                        orderUnitCount = uiState.totalOrderUnits,
                        orderMessage = uiState.orderMessage,
                        isReserving = product.id in uiState.reservingProductIds,
                        isConcurrencyTesting = productDetailState.isTesting,
                        concurrencyTestResult = productDetailState.testResult,
                        onAddToOrder = { viewModel.addProductToOrder(product.id) },
                        onConcurrencyTest = {
                            productDetailViewModel.onConcurrencyTest(product.id)
                        },
                        onOrderClick = openOrder,
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
            entry<StoreNavKey.Order> {
                OrderScreen(
                    lines = uiState.orderLines,
                    products = uiState.products,
                    subtotalsCents = uiState.orderSubtotalsCents,
                    totalCents = uiState.orderTotalCents,
                    message = uiState.orderMessage,
                    reservingProductIds = uiState.reservingProductIds,
                    onIncrease = viewModel::addProductToOrder,
                    onDecrease = viewModel::decreaseProduct,
                    onRemove = viewModel::removeProduct,
                    onBack = { backStack.removeLastOrNull() },
                    onCatalogClick = returnToCatalog,
                    onCheckoutClick = { backStack.add(StoreNavKey.Checkout) }
                )
            }
            entry<StoreNavKey.Checkout> {
                CheckoutScreen(
                    uiState = checkoutUiState,
                    orderUnitCount = uiState.totalOrderUnits,
                    orderTotalCents = uiState.orderTotalCents,
                    isConfirmEnabled = isConfirmEnabled,
                    submissionState = submissionState,
                    onFullNameChange = viewModel::onFullNameChange,
                    onPhoneNumberChange = viewModel::onPhoneNumberChange,
                    onBillingTypeChange = viewModel::onBillingTypeChange,
                    onNitChange = viewModel::onNitChange,
                    onBusinessNameChange = viewModel::onBusinessNameChange,
                    onPaymentMethodChange = viewModel::onPaymentMethodChange,
                    onConfirm = viewModel::submitOrder,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<StoreNavKey.Confirmation> {
                val receipt = orderReceipt
                if (receipt != null) {
                    OrderConfirmationScreen(
                        receipt = receipt,
                        onReturnToCatalog = returnToCatalog
                    )
                } else {
                    MissingDestination(message = "Recibo no encontrado")
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
private fun MissingDestination(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(message)
    }
}
