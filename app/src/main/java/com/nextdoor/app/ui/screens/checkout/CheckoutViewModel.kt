package com.nextdoor.app.ui.screens.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextdoor.app.data.dto.AddressDto
import com.nextdoor.app.data.dto.ProductDto
import com.nextdoor.app.data.infra.ApiResult
import com.nextdoor.app.data.infra.getOrNull
import com.nextdoor.app.data.repository.AccountRepository
import com.nextdoor.app.data.repository.CartRepository
import com.nextdoor.app.data.repository.OrderRepository
import com.nextdoor.app.data.repository.StoreRepository
import com.nextdoor.app.ui.components.ToastController
import com.nextdoor.app.ui.util.PaymentMethod
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckoutUiState(
    val loading: Boolean = true,
    val storeName: String = "",
    val items: List<Pair<ProductDto, Int>> = emptyList(),
    val subtotal: Double = 0.0,
    val addresses: List<AddressDto> = emptyList(),
    val addressIndex: Int = 0,
    val selectedPayment: PaymentMethod = PaymentMethod.CC,
    val submitting: Boolean = false,
    val errorBanner: String? = null
)

sealed interface CheckoutEvent {
    data class OrderPlaced(val orderId: String) : CheckoutEvent
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val storeRepository: StoreRepository,
    private val accountRepository: AccountRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CheckoutEvent>()
    val events: SharedFlow<CheckoutEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            cartRepository.items.collect { items ->
                val entries = items.entries.map { it.key to it.value }
                val subtotal = entries.sumOf { it.first.price * it.second }
                _uiState.update {
                    it.copy(items = entries, subtotal = subtotal, loading = false)
                }
            }
        }
        viewModelScope.launch {
            cartRepository.storeId.collect { storeId ->
                if (storeId != null) {
                    viewModelScope.launch { loadStoreName(storeId) }
                    loadAddresses()
                }
            }
        }
    }

    private suspend fun loadStoreName(storeId: String) {
        val store = storeRepository.getStore(storeId).getOrNull()
        _uiState.update { it.copy(storeName = store?.name.orEmpty()) }
    }

    private fun loadAddresses() {
        viewModelScope.launch {
            val addresses = accountRepository.listAddresses().getOrNull().orEmpty()
            _uiState.update { it.copy(addresses = addresses) }
        }
    }

    fun cycleAddress() {
        _uiState.update { state ->
            if (state.addresses.size <= 1) return@update state
            val next = (state.addressIndex + 1) % state.addresses.size
            state.copy(addressIndex = next)
        }
    }

    fun selectPayment(paymentMethod: PaymentMethod) {
        _uiState.update { it.copy(selectedPayment = paymentMethod) }
    }

    fun removeItem(product: ProductDto) {
        cartRepository.remove(product)
    }

    fun confirmOrder() {
        val state = _uiState.value
        val storeId = cartRepository.storeId.value
        if (storeId == null || state.items.isEmpty() || state.submitting) return

        _uiState.update { it.copy(submitting = true, errorBanner = null) }
        viewModelScope.launch {
            val products = state.items.map { it.first.id to it.second }
            when (val result = orderRepository.createOrder(storeId, products, state.selectedPayment.apiValue)) {
                is ApiResult.Success -> {
                    cartRepository.clear()
                    _events.emit(CheckoutEvent.OrderPlaced(result.data.id))
                }
                is ApiResult.Failure -> handleOrderFailure(result, storeId)
            }
        }
    }

    private suspend fun handleOrderFailure(result: ApiResult.Failure, storeId: String) {
        when (result.code) {
            404 -> _uiState.update { it.copy(submitting = false, errorBanner = "Loja não encontrada") }
            422 -> {
                val banner = when {
                    result.message?.contains("stock", ignoreCase = true) == true ||
                        result.message?.contains("estoque", ignoreCase = true) == true ->
                        "Estoque insuficiente para um ou mais itens"
                    result.message?.contains("product", ignoreCase = true) == true ->
                        "Um ou mais produtos não foram encontrados"
                    else -> "Não foi possível concluir o pedido"
                }
                _uiState.update { it.copy(submitting = false, errorBanner = banner) }
                reconcileAfter422(storeId)
            }
            else -> {
                _uiState.update { it.copy(submitting = false) }
                ToastController.show("Não foi possível concluir o pedido")
            }
        }
    }

    /** Re-fetch products and clamp/drop cart quantities that exceed stock or vanished. */
    private fun reconcileAfter422(storeId: String) {
        viewModelScope.launch {
            val fresh = storeRepository.getProducts(storeId).getOrNull().orEmpty()
            val current = _uiState.value.items
            val newMap = linkedMapOf<ProductDto, Int>()
            for ((product, qty) in current) {
                val freshProduct = fresh.firstOrNull { it.id == product.id } ?: continue
                val available = freshProduct.quantity
                val newQty = if (available > 0) minOf(qty, available) else 0
                if (newQty > 0) newMap[freshProduct] = newQty
            }
            cartRepository.clear()
            newMap.forEach { (product, qty) -> cartRepository.add(storeId, product, qty) }
        }
    }
}
