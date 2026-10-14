package uvg.edu.laboratorio09.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object OrderNetwork {
    private const val BASE_URL =
        "https://6acc4ac7ef37b150407867e3.mockapi.io/api/v1/"

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                json.asConverterFactory(
                    "application/json".toMediaType()
                )
            )
            .build()
    }

    val api: OrderApi by lazy {
        retrofit.create(OrderApi::class.java)
    }
}
