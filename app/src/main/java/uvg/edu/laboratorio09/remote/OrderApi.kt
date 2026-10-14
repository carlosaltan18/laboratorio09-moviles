package uvg.edu.laboratorio09.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import uvg.edu.laboratorio09.remote.dto.CreateOrderRequest
import uvg.edu.laboratorio09.remote.dto.OrderDto

interface OrderApi {
    @POST("orders")
    suspend fun createOrder(
        @Body request: CreateOrderRequest
    ): OrderDto

    @GET("orders")
    suspend fun getOrders(
        @Query("sortBy") sortBy: String = "createdAtEpochMillis",
        @Query("order") order: String = "desc"
    ): List<OrderDto>

    @GET("orders/{id}")
    suspend fun getOrderById(
        @Path("id") id: String
    ): OrderDto
}
