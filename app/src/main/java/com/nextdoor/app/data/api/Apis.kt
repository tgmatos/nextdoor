package com.nextdoor.app.data.api

import com.nextdoor.app.data.dto.AccountResponse
import com.nextdoor.app.data.dto.AddressDto
import com.nextdoor.app.data.dto.AuthResponse
import com.nextdoor.app.data.dto.CreateOrderRequest
import com.nextdoor.app.data.dto.FlatAccountDto
import com.nextdoor.app.data.dto.LoginRequest
import com.nextdoor.app.data.dto.OrderDto
import com.nextdoor.app.data.dto.OrderResponse
import com.nextdoor.app.data.dto.OrdersResponse
import com.nextdoor.app.data.dto.ProductDto
import com.nextdoor.app.data.dto.ProductsResponse
import com.nextdoor.app.data.dto.RegisterRequest
import com.nextdoor.app.data.dto.StoreDto
import com.nextdoor.app.data.dto.StoresResponse
import com.nextdoor.app.data.dto.UpdateAccountRequest
import com.nextdoor.app.data.dto.UpdateAddressRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {
    @POST("api/account/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("api/account/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("api/account/logout")
    suspend fun logout(): retrofit2.Response<Unit>
}

interface StoresApi {
    @GET("api/stores")
    suspend fun listStores(): StoresResponse

    @GET("api/stores/{id}")
    suspend fun getStore(@Path("id") id: String): StoreDto

    @GET("api/stores/{id}/product")
    suspend fun getProducts(@Path("id") id: String): ProductsResponse
}

interface AccountApi {
    @GET("api/account")
    suspend fun getAccount(): AccountResponse

    @PATCH("api/account")
    suspend fun updateAccount(@Body body: UpdateAccountRequest): FlatAccountDto

    @DELETE("api/account")
    suspend fun deleteAccount(): retrofit2.Response<Unit>

    @GET("api/account/order")
    suspend fun listOrders(): OrdersResponse

    @GET("api/account/order/{id}")
    suspend fun getOrder(@Path("id") id: String): OrderResponse

    @GET("api/account/address")
    suspend fun listAddresses(): retrofit2.Response<List<AddressDto>>

    @GET("api/account/address/{id}")
    suspend fun getAddress(@Path("id") id: String): AddressDto

    @PATCH("api/account/address/{id}")
    suspend fun updateAddress(
        @Path("id") id: String,
        @Body body: UpdateAddressRequest
    ): AddressDto
}

interface OrderApi {
    @POST("api/store/order/{storeId}")
    suspend fun createOrder(
        @Path("storeId") storeId: String,
        @Body body: CreateOrderRequest
    ): OrderDto
}

/**
 * Search is not yet shipped on the backend (see plan.md §4 / 10-search.md).
 * The interface is declared so SearchRepository can be swapped to the live
 * endpoint later without UI changes. Kept happy for the stub path.
 */
interface SearchApi {
    @GET("api/search")
    suspend fun search(
        @Query("query") query: String
    ): SearchResponse
}

@kotlinx.serialization.Serializable
data class SearchResponse(
    val stores: List<StoreDto> = emptyList(),
    val products: List<SearchProductDto> = emptyList()
)

@kotlinx.serialization.Serializable
data class SearchProductDto(
    val id: String,
    val name: String,
    val description: String = "",
    val image: String = "",
    val price: Double = 0.0,
    @kotlinx.serialization.SerialName("store_id") val storeId: String? = null
)
