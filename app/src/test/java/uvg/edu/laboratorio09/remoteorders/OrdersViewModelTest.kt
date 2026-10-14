package uvg.edu.laboratorio09.remoteorders

import androidx.lifecycle.viewModelScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import uvg.edu.laboratorio09.remote.OrderApi
import uvg.edu.laboratorio09.remote.OrderNetwork
import uvg.edu.laboratorio09.remote.dto.OrderDto

@OptIn(ExperimentalCoroutinesApi::class)
class OrdersViewModelTest {
    private val api = mockk<OrderApi>()
    private lateinit var viewModel: OrdersViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockkObject(OrderNetwork)
        every { OrderNetwork.api } returns api
        viewModel = OrdersViewModel()
    }

    @After
    fun tearDown() {
        viewModel.viewModelScope.cancel()
        unmockkObject(OrderNetwork)
        Dispatchers.resetMain()
    }

    private fun order(id: String) = OrderDto(
        id, 1791809817391, "Cliente ficticio", "CONSUMER_FINAL", "NOT_STORED",
        4800, "CREATED", emptyList()
    )

    @Test
    fun listKeepsServerOrderAndIgnoresDoubleTap() = runTest {
        val response = listOf(order("9"), order("11"))
        coEvery { api.getOrders("createdAtEpochMillis", "desc") } returns response
        assertEquals(OrdersUiState.Idle, viewModel.ordersState.value)
        viewModel.loadOrders()
        viewModel.loadOrders()
        assertEquals(OrdersUiState.Loading, viewModel.ordersState.value)
        advanceUntilIdle()
        assertEquals(OrdersUiState.Success(response), viewModel.ordersState.value)
        coVerify(exactly = 1) { api.getOrders("createdAtEpochMillis", "desc") }
    }

    @Test
    fun emptyListAndNextVisitFetchFreshData() = runTest {
        coEvery { api.getOrders(any(), any()) } returnsMany listOf(emptyList(), listOf(order("3")))
        viewModel.loadOrders()
        advanceUntilIdle()
        assertEquals(OrdersUiState.Empty, viewModel.ordersState.value)
        viewModel.loadOrders()
        advanceUntilIdle()
        assertEquals(OrdersUiState.Success(listOf(order("3"))), viewModel.ordersState.value)
        coVerify(exactly = 2) { api.getOrders(any(), any()) }
    }

    @Test
    fun listNetworkHttpAndFormatErrorsAllowRetry() = runTest {
        val failures = listOf(IOException("private"), HttpException(Response.error<Any>(500, "private".toResponseBody())), SerializationException("private"))
        for (failure in failures) {
            coEvery { api.getOrders(any(), any()) } throws failure
            viewModel.loadOrders()
            advanceUntilIdle()
            val state = viewModel.ordersState.value as OrdersUiState.Error
            assertFalse(state.message.contains("private"))
            coEvery { api.getOrders(any(), any()) } returns emptyList()
            viewModel.loadOrders()
            advanceUntilIdle()
            assertEquals(OrdersUiState.Empty, viewModel.ordersState.value)
        }
    }

    @Test
    fun collectingAgainDuringAndAfterLoadingDoesNotRepeatRequest() = runTest {
        val response = CompletableDeferred<List<OrderDto>>()
        coEvery { api.getOrders(any(), any()) } coAnswers { response.await() }
        viewModel.loadOrders()
        runCurrent()
        repeat(3) { assertEquals(OrdersUiState.Loading, viewModel.ordersState.first()) }
        response.complete(listOf(order("1")))
        advanceUntilIdle()
        repeat(3) { assertTrue(viewModel.ordersState.first() is OrdersUiState.Success) }
        coVerify(exactly = 1) { api.getOrders(any(), any()) }
    }

    @Test
    fun detailUsesIdAndDoesNotRepeatPendingRequest() = runTest {
        coEvery { api.getOrderById("11") } returns order("11")
        viewModel.loadOrder("11")
        viewModel.loadOrder("11")
        assertEquals(OrderDetailUiState.Loading, viewModel.detailState.value)
        advanceUntilIdle()
        assertEquals(OrderDetailUiState.Success(order("11")), viewModel.detailState.value)
        coVerify(exactly = 1) { api.getOrderById("11") }
    }

    @Test
    fun missingDetailHasDedicatedState() = runTest {
        coEvery { api.getOrderById("missing") } throws HttpException(Response.error<Any>(404, "Not found".toResponseBody()))
        viewModel.loadOrder("missing")
        advanceUntilIdle()
        assertEquals(OrderDetailUiState.NotFound, viewModel.detailState.value)
    }

    @Test
    fun detailErrorsAllowRetry() = runTest {
        val failures = listOf(IOException("private"), HttpException(Response.error<Any>(503, "private".toResponseBody())), SerializationException("private"))
        for (failure in failures) {
            coEvery { api.getOrderById("1") } throws failure
            viewModel.loadOrder("1")
            advanceUntilIdle()
            assertFalse((viewModel.detailState.value as OrderDetailUiState.Error).message.contains("private"))
            coEvery { api.getOrderById("1") } returns order("1")
            viewModel.loadOrder("1")
            advanceUntilIdle()
            assertEquals(OrderDetailUiState.Success(order("1")), viewModel.detailState.value)
        }
    }

    @Test
    fun choosingAnotherIdCancelsOldDetail() = runTest {
        val old = CompletableDeferred<OrderDto>()
        coEvery { api.getOrderById("1") } coAnswers { old.await() }
        coEvery { api.getOrderById("2") } returns order("2")
        viewModel.loadOrder("1")
        runCurrent()
        viewModel.loadOrder("2")
        advanceUntilIdle()
        old.complete(order("1"))
        advanceUntilIdle()
        assertEquals(OrderDetailUiState.Success(order("2")), viewModel.detailState.value)
    }

    @Test
    fun cancellationIsNotShownAsNetworkError() = runTest {
        coEvery { api.getOrders(any(), any()) } throws CancellationException()
        coEvery { api.getOrderById("1") } throws CancellationException()
        viewModel.loadOrders()
        viewModel.loadOrder("1")
        advanceUntilIdle()
        assertFalse(viewModel.ordersState.value is OrdersUiState.Error)
        assertFalse(viewModel.detailState.value is OrderDetailUiState.Error)
    }
}
