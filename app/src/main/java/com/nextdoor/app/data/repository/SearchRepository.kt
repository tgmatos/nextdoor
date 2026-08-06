package com.nextdoor.app.data.repository

import com.nextdoor.app.data.dto.ProductDto
import com.nextdoor.app.data.dto.StoreDto
import com.nextdoor.app.data.infra.ApiResult
import com.nextdoor.app.data.infra.ApiResult.Failure
import com.nextdoor.app.data.infra.ApiResult.Success
import javax.inject.Inject
import javax.inject.Singleton

data class SearchProductHit(
    val storeId: String,
    val storeName: String,
    val product: ProductDto
)

data class SearchResult(
    val stores: List<StoreDto> = emptyList(),
    val products: List<SearchProductHit> = emptyList()
)

/**
 * Customer search. The backend `GET /api/search` endpoint has not shipped yet
 * (plan.md §4 / 10-search.md), so this is a client-side stub that filters the
 * stores list and each store's cached products. Swap to the live SearchApi call
 * later without changing any call-site.
 */
@Singleton
class SearchRepository @Inject constructor(
    private val storeRepository: StoreRepository
) {
    suspend fun search(query: String): ApiResult<SearchResult> {
        val q = query.trim().lowercase()
        if (q.length < 2) return Success(SearchResult())

        val stores = when (val r = storeRepository.listStores()) {
            is Success -> r.data
            is Failure -> return r
        }

        val matchedStores = stores.filter {
            it.name.lowercase().contains(q) || it.description.lowercase().contains(q)
        }

        val matchedProducts = buildList {
            for (store in stores) {
                storeRepository.cachedProducts(store.id).orEmpty().forEach { product ->
                    if (product.name.lowercase().contains(q) || product.description.lowercase().contains(q)) {
                        add(SearchProductHit(store.id, store.name, product))
                    }
                }
            }
        }

        return Success(SearchResult(matchedStores, matchedProducts))
    }
}
