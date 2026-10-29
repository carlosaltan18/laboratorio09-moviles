package uvg.edu.laboratorio09.remoteorders

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uvg.edu.laboratorio09.domain.repository.RemoteOrderNotFoundException
import uvg.edu.laboratorio09.domain.repository.RemoteOrderRepository
import uvg.edu.laboratorio09.model.CreateRemoteOrder
import uvg.edu.laboratorio09.model.RemoteOrder

@OptIn(ExperimentalCoroutinesApi::class)
class OrdersViewModelTest {
    private val repository = FakeRemoteOrderRepository()
    private lateinit var viewModel: OrdersViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel = OrdersViewModel(repository)
    }

    @After
    fun tearDown() {
        viewModel.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun listUsesRepositoryKeepsOrderAndIgnoresDoubleTap() = runTest {
        val orders = listOf(order("9"), order("11"))
        repository.ordersResult = Result.success(orders)

        viewModel.loadOrders()
        viewModel.loadOrders()
        assertEquals(OrdersUiState.Loading, viewModel.ordersState.value)

        advanceUntilIdle()

        assertEquals(OrdersUiState.Success(orders), viewModel.ordersState.value)
        assertEquals(1, repository.listRequests)
    }

    @Test
    fun emptyAndFailureStatesAllowANewRequest() = runTest {
        repository.ordersResult = Result.success(emptyList())
        viewModel.loadOrders()
        advanceUntilIdle()
        assertEquals(OrdersUiState.Empty, viewModel.ordersState.value)

        repository.ordersResult = Result.failure(IllegalStateException("private"))
        viewModel.loadOrders()
        advanceUntilIdle()
        assertTrue(viewModel.ordersState.value is OrdersUiState.Error)
        assertEquals(2, repository.listRequests)
    }

    @Test
    fun detailUsesRepositoryAndShowsNotFound() = runTest {
        repository.detailResults["missing"] = Result.failure(RemoteOrderNotFoundException())

        viewModel.loadOrder("missing")
        advanceUntilIdle()

        assertEquals(OrderDetailUiState.NotFound, viewModel.detailState.value)
        assertEquals(listOf("missing"), repository.detailRequests)
    }

    @Test
    fun detailShowsTheDomainOrderReturnedByRepository() = runTest {
        val expected = order("1")
        repository.detailResults["1"] = Result.success(expected)

        viewModel.loadOrder("1")
        advanceUntilIdle()

        assertEquals(OrderDetailUiState.Success(expected), viewModel.detailState.value)
    }

    private fun order(id: String) = RemoteOrder(
        id = id,
        createdAtEpochMillis = 1_791_809_817_391,
        customerName = "Cliente ficticio",
        billingType = "CF",
        paymentMethod = "Efectivo contra entrega",
        totalCents = 4_800,
        status = "received",
        lines = emptyList()
    )
}

private class FakeRemoteOrderRepository : RemoteOrderRepository {
    var ordersResult: Result<List<RemoteOrder>> = Result.success(emptyList())
    val detailResults = mutableMapOf<String, Result<RemoteOrder>>()
    var listRequests = 0
    val detailRequests = mutableListOf<String>()

    override suspend fun createOrder(request: CreateRemoteOrder): Result<RemoteOrder> =
        error("Not used by OrdersViewModel")

    override fun observeOrders(): Flow<Result<List<RemoteOrder>>> {
        listRequests += 1
        return flowOf(ordersResult)
    }

    override suspend fun getOrder(orderId: String): Result<RemoteOrder> {
        detailRequests += orderId
        return detailResults[orderId] ?: Result.failure(IllegalStateException())
    }
}
