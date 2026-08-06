package com.nextdoor.app.data.repository

import com.nextdoor.app.data.dto.ProductDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory cart. Holds a single store per order (per API design) — items from
 * another store are rejected with `add` returning false so the UI can prompt.
 */
@Singleton
class CartRepository @Inject constructor() {

    private val _storeId = MutableStateFlow<String?>(null)
    private val _items = MutableStateFlow<Map<ProductDto, Int>>(emptyMap())

    val storeId: StateFlow<String?> = _storeId.asStateFlow()
    val items: StateFlow<Map<ProductDto, Int>> = _items.asStateFlow()

    val badgeCount = _items.map { entries -> entries.values.sum() }
    val subtotal = _items.map { entries ->
        entries.entries.sumOf { (product, qty) -> product.price * qty }
    }

    fun quantityOf(productId: String): Int =
        _items.value.entries.firstOrNull { it.key.id == productId }?.value ?: 0

    /**
     * Adds [qty] of [product] from [storeId].
     * @return true when added (cart empty or same store), false when blocked by
     * a different store already in the cart (item NOT added).
     */
    fun add(storeId: String, product: ProductDto, qty: Int): Boolean {
        val current = _storeId.value
        if (current != null && current != storeId) return false
        _storeId.value = storeId
        _items.update { map ->
            val existing = map[product] ?: 0
            map + (product to (existing + qty))
        }
        return true
    }

    /** Replaces the whole cart with [qty] of [product] from [storeId] (after user confirms). */
    fun replaceAndAdd(storeId: String, product: ProductDto, qty: Int) {
        _storeId.value = storeId
        _items.value = mapOf(product to qty)
    }

    fun remove(product: ProductDto) {
        val newItems = _items.value - product
        _items.value = newItems
        if (newItems.isEmpty()) _storeId.value = null
    }

    fun setQuantity(product: ProductDto, qty: Int) {
        if (qty <= 0) {
            remove(product)
        } else {
            _items.update { it + (product to qty) }
        }
    }

    fun clear() {
        _storeId.value = null
        _items.value = emptyMap()
    }
}
