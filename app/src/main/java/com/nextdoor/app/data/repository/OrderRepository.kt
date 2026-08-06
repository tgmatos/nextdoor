package com.nextdoor.app.data.repository

import com.nextdoor.app.data.api.OrderApi
import com.nextdoor.app.data.dto.CreateOrderProduct
import com.nextdoor.app.data.dto.CreateOrderRequest
import com.nextdoor.app.data.dto.OrderDto
import com.nextdoor.app.data.infra.ApiResult
import com.nextdoor.app.data.infra.safeApiResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val api: OrderApi
) {
    suspend fun createOrder(
        storeId: String,
        products: List<Pair<String, Int>>,
        paymentMethod: String
    ): ApiResult<OrderDto> = safeApiResult {
        api.createOrder(
            storeId,
            CreateOrderRequest(
                products = products.map { (productId, quantity) -> CreateOrderProduct(productId, quantity) },
                paymentMethod = paymentMethod
            )
        )
    }
}
