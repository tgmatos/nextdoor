package com.nextdoor.app.data.repository

import com.nextdoor.app.data.api.StoresApi
import com.nextdoor.app.data.dto.ProductDto
import com.nextdoor.app.data.dto.StoreDto
import com.nextdoor.app.data.infra.ApiResult
import com.nextdoor.app.data.infra.safeApiResult
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRepository @Inject constructor(
    private val api: StoresApi
) {
    // Per-store product cache so Product/Order-detail screens can resolve ids
    // without refetching in the common navigation path.
    private val productsCache = ConcurrentHashMap<String, List<ProductDto>>()

    suspend fun listStores(): ApiResult<List<StoreDto>> = safeApiResult {
        api.listStores().stores
    }

    suspend fun getStore(id: String): ApiResult<StoreDto> = safeApiResult {
        api.getStore(id)
    }

    suspend fun getProducts(storeId: String): ApiResult<List<ProductDto>> = safeApiResult {
        api.getProducts(storeId).products.also { productsCache[storeId] = it }
    }

    fun cachedProducts(storeId: String): List<ProductDto>? = productsCache[storeId]

    fun cacheProducts(storeId: String, products: List<ProductDto>) {
        productsCache[storeId] = products
    }
}
