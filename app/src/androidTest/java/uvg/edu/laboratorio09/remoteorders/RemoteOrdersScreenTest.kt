package uvg.edu.laboratorio09.remoteorders

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import uvg.edu.laboratorio09.model.RemoteOrder
import uvg.edu.laboratorio09.model.RemoteOrderLine
import uvg.edu.laboratorio09.remoteorders.ui.OrdersScreen
import uvg.edu.laboratorio09.remoteorders.ui.RemoteOrderDetailScreen
import uvg.edu.laboratorio09.ui.theme.Laboratorio09Theme

class RemoteOrdersScreenTest {
    @get:Rule val compose = createComposeRule()
    private val order = RemoteOrder(
        "11", 1791809817391, "Cliente ficticio", "CONSUMER_FINAL", "NOT_STORED",
        5200, "CREATED", listOf(RemoteOrderLine("old-product", "Nombre historico", 2600, 2, 5200))
    )

    @Test fun loadingListIsVisible() {
        compose.setContent { Laboratorio09Theme { OrdersScreen(OrdersUiState.Loading, {}, {}, {}, {}) } }
        compose.onNodeWithText("Cargando pedidos…").assertIsDisplayed()
    }

    @Test fun emptyListReturnsToCatalog() {
        var clicks = 0
        compose.setContent { Laboratorio09Theme { OrdersScreen(OrdersUiState.Empty, {}, {}, { clicks++ }, {}) } }
        compose.onNodeWithText("Aún no has enviado pedidos.").assertIsDisplayed()
        compose.onNodeWithText("Ir al catálogo").performClick()
        assertEquals(1, clicks)
    }

    @Test fun listErrorCanRetry() {
        var clicks = 0
        compose.setContent { Laboratorio09Theme { OrdersScreen(OrdersUiState.Error("Revisa tu conexión."), { clicks++ }, {}, {}, {}) } }
        compose.onNodeWithText("Revisa tu conexión.").assertIsDisplayed()
        compose.onNodeWithText("Reintentar").performClick()
        assertEquals(1, clicks)
    }

    @Test fun selectingSummaryOnlyPassesId() {
        var selected = ""
        compose.setContent { Laboratorio09Theme { OrdersScreen(OrdersUiState.Success(listOf(order)), {}, { selected = it }, {}, {}) } }
        compose.onNodeWithText("Pedido #11").performClick()
        assertEquals("11", selected)
    }

    @Test fun detailUsesHistoricalNamePriceQuantitySubtotalAndTotal() {
        compose.setContent { Laboratorio09Theme { RemoteOrderDetailScreen(OrderDetailUiState.Success(order), {}, {}) } }
        compose.onNodeWithText("Pedido #11").assertIsDisplayed()
        compose.onNodeWithText("Fecha:", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Nombre historico").assertIsDisplayed()
        compose.onNodeWithText("Precio unitario: Q26.00").assertIsDisplayed()
        compose.onNodeWithText("Cantidad: 2").assertIsDisplayed()
        compose.onNodeWithText("Subtotal: Q52.00").assertIsDisplayed()
        compose.onNodeWithText("Total: Q52.00").assertIsDisplayed()
    }

    @Test fun missingDetailCanReturnToList() {
        var clicks = 0
        compose.setContent { Laboratorio09Theme { RemoteOrderDetailScreen(OrderDetailUiState.NotFound, {}, { clicks++ }) } }
        compose.onNodeWithText("Este pedido ya no existe en el servidor.").assertIsDisplayed()
        compose.onNodeWithText("Volver a Mis pedidos").performClick()
        assertEquals(1, clicks)
    }

    @Test fun detailLoadingIsVisible() {
        compose.setContent { Laboratorio09Theme { RemoteOrderDetailScreen(OrderDetailUiState.Loading, {}, {}) } }
        compose.onNodeWithText("Cargando pedido…").assertIsDisplayed()
    }

    @Test fun detailErrorCanRetry() {
        var clicks = 0
        compose.setContent { Laboratorio09Theme { RemoteOrderDetailScreen(OrderDetailUiState.Error("Revisa tu conexión."), { clicks++ }, {}) } }
        compose.onNodeWithText("Reintentar").performClick()
        assertEquals(1, clicks)
    }
}
