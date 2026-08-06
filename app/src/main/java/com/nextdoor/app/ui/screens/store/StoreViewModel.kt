package com.nextdoor.app.ui.screens.store

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextdoor.app.data.dto.ProductDto
import com.nextdoor.app.data.dto.StoreDto
import com.nextdoor.app.data.infra.ApiResult
import com.nextdoor.app.data.repository.CartRepository
import com.nextdoor.app.data.repository.StoreRepository
import com.nextdoor.app.ui.components.ToastController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface StoreUiState {
    object Loading : StoreUiState
    data class Error(val message: String) : StoreUiState
    data class Content(
        val store: StoreDto,
        val products: List<ProductDto>,
        val productsFailed: Boolean = false
    ) : StoreUiState
}

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    var uiState by mutableStateOf<StoreUiState>(StoreUiState.Loading)
        private set

    var showClearDialog by mutableStateOf(false)
        private set

    private var pendingAdd: ProductDto? = null

    val badgeCount: StateFlow<Int> = cartRepository.badgeCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Live cart contents so "+" buttons react to the current stock limit. */
    val cartItems: StateFlow<Map<ProductDto, Int>> = cartRepository.items

    fun load(storeId: String) {
        viewModelScope.launch {
            uiState = StoreUiState.Loading
            coroutineScope {
                val storeDeferred = async { storeRepository.getStore(storeId) }
                val productsDeferred = async { storeRepository.getProducts(storeId) }
                val storeRes = storeDeferred.await()
                val productsRes = productsDeferred.await()

                when (storeRes) {
                    is ApiResult.Success -> when (productsRes) {
                        is ApiResult.Success -> uiState = StoreUiState.Content(
                            store = storeRes.data,
                            products = productsRes.data
                        )
                        is ApiResult.Failure -> uiState = StoreUiState.Content(
                            store = storeRes.data,
                            products = emptyList(),
                            productsFailed = true
                        )
                    }
                    is ApiResult.Failure -> uiState = StoreUiState.Error(
                        message = if (storeRes.code == 400) "Loja não encontrada"
                        else "Não foi possível carregar a loja"
                    )
                }
            }
        }
    }

    /** Refetches only the product list after a partial failure, keeping the header. */
    fun retryProducts(storeId: String) {
        val store = (uiState as? StoreUiState.Content)?.store ?: return
        viewModelScope.launch {
            when (val result = storeRepository.getProducts(storeId)) {
                is ApiResult.Success -> uiState = StoreUiState.Content(store, result.data)
                is ApiResult.Failure -> uiState = StoreUiState.Content(
                    store,
                    emptyList(),
                    productsFailed = true
                )
            }
        }
    }

    /** Adds a unit of [product]; on a cross-store conflict shows the confirm dialog. */
    fun addToCart(storeId: String, product: ProductDto) {
        val added = cartRepository.add(storeId, product, 1)
        if (added) {
            ToastController.show("Adicionado ao carrinho")
        } else {
            pendingAdd = product
            showClearDialog = true
        }
    }

    fun confirmClearAndReplace(storeId: String) {
        val product = pendingAdd ?: return
        cartRepository.replaceAndAdd(storeId, product, 1)
        pendingAdd = null
        showClearDialog = false
        ToastController.show("Adicionado ao carrinho")
    }

    fun dismissDialog() {
        pendingAdd = null
        showClearDialog = false
    }
}
