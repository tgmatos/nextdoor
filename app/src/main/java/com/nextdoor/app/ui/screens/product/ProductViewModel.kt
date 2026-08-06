package com.nextdoor.app.ui.screens.product

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextdoor.app.data.dto.ProductDto
import com.nextdoor.app.data.infra.ApiResult
import com.nextdoor.app.data.infra.getOrNull
import com.nextdoor.app.data.repository.CartRepository
import com.nextdoor.app.data.repository.StoreRepository
import com.nextdoor.app.ui.components.ToastController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ProductUiState {
    object Loading : ProductUiState
    data class Error(val message: String) : ProductUiState
    data class Content(
        val storeName: String,
        val product: ProductDto,
        val qty: Int
    ) : ProductUiState
}

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    var uiState by mutableStateOf<ProductUiState>(ProductUiState.Loading)
        private set

    var showClearDialog by mutableStateOf(false)
        private set

    private var pendingContent: ProductUiState.Content? = null

    val badgeCount: StateFlow<Int> = cartRepository.badgeCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Resolves [productId] from the store cache first, refetching only if missing. */
    fun load(storeId: String, productId: String) {
        viewModelScope.launch {
            val cached = storeRepository.cachedProducts(storeId)?.find { it.id == productId }

            if (cached != null) {
                uiState = ProductUiState.Content("Loja", cached, initialQty(cached))
                // Best-effort store name refresh (cache holds only products).
                storeRepository.getStore(storeId).getOrNull()?.name?.let { name ->
                    val current = uiState
                    if (current is ProductUiState.Content) {
                        uiState = current.copy(storeName = name)
                    }
                }
                return@launch
            }

            // Refetch path (process death / deep link).
            uiState = ProductUiState.Loading
            val storeName = storeRepository.getStore(storeId).getOrNull()?.name
            when (val result = storeRepository.getProducts(storeId)) {
                is ApiResult.Success -> {
                    val product = result.data.find { it.id == productId }
                    if (product != null) {
                        uiState = ProductUiState.Content(
                            storeName = storeName ?: "Loja",
                            product = product,
                            qty = initialQty(product)
                        )
                    } else {
                        uiState = ProductUiState.Error("Produto não encontrado")
                    }
                }
                is ApiResult.Failure -> uiState = if (result.code == 400) {
                    ProductUiState.Error("Produto não encontrado")
                } else {
                    ProductUiState.Error("Não foi possível carregar o produto")
                }
            }
        }
    }

    fun increment() {
        val content = uiState as? ProductUiState.Content ?: return
        if (content.qty >= content.product.quantity) return
        uiState = content.copy(qty = content.qty + 1)
    }

    fun decrement() {
        val content = uiState as? ProductUiState.Content ?: return
        if (content.qty <= 1) return
        uiState = content.copy(qty = content.qty - 1)
    }

    /** Adds the selected quantity; on a cross-store conflict shows the confirm dialog. */
    fun addToCart(storeId: String, onAdded: () -> Unit) {
        val content = uiState as? ProductUiState.Content ?: return
        if (content.product.quantity == 0) return
        val added = cartRepository.add(storeId, content.product, content.qty)
        if (added) {
            ToastController.show("Adicionado ao carrinho")
            onAdded()
        } else {
            pendingContent = content
            showClearDialog = true
        }
    }

    fun confirmClearAndReplace(storeId: String, onAdded: () -> Unit) {
        val content = pendingContent ?: return
        cartRepository.replaceAndAdd(storeId, content.product, content.qty)
        pendingContent = null
        showClearDialog = false
        ToastController.show("Adicionado ao carrinho")
        onAdded()
    }

    fun dismissDialog() {
        pendingContent = null
        showClearDialog = false
    }

    private fun initialQty(product: ProductDto): Int = if (product.quantity >= 1) 1 else 0
}
