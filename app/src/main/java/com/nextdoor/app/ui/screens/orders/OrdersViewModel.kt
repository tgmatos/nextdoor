package com.nextdoor.app.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextdoor.app.data.dto.OrderDto
import com.nextdoor.app.data.infra.ApiResult
import com.nextdoor.app.data.repository.AccountRepository
import com.nextdoor.app.data.repository.CartRepository
import com.nextdoor.app.data.repository.OrderUpdatesRepository
import com.nextdoor.app.ui.components.ToastController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrdersUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val refreshing: Boolean = false,
    val orders: List<OrderDto> = emptyList()
)

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val orderUpdatesRepository: OrderUpdatesRepository,
    cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    /** Cart badge count surfaced to the top bar. */
    val badgeCount: StateFlow<Int> = cartRepository.badgeCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        load()
        observeOrderUpdates()
    }

    /** Patches the matching order in the list whenever the websocket announces a status change. */
    private fun observeOrderUpdates() {
        viewModelScope.launch {
            orderUpdatesRepository.updates.collect { update ->
                _uiState.update { state ->
                    val orders = state.orders.map { order ->
                        if (order.id == update.id) {
                            order.copy(
                                statusOrder = update.statusOrder,
                                total = update.total,
                                paymentMethod = update.paymentMethod
                            )
                        } else {
                            order
                        }
                    }
                    state.copy(orders = orders)
                }
            }
        }
    }

    fun load() {
        _uiState.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            when (val result = accountRepository.listOrders()) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            error = false,
                            refreshing = false,
                            orders = result.data.sortedByDescending { s -> s.insertedAt }
                        )
                    }
                }
                is ApiResult.Failure -> {
                    _uiState.update { it.copy(loading = false, error = true, refreshing = false) }
                }
            }
        }
    }

    fun refresh() {
        val current = _uiState.value
        if (current.loading) return
        // Keep stale list visible while refreshing.
        _uiState.update { it.copy(refreshing = true) }
        viewModelScope.launch {
            when (val result = accountRepository.listOrders()) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            error = false,
                            refreshing = false,
                            orders = result.data.sortedByDescending { s -> s.insertedAt }
                        )
                    }
                }
                is ApiResult.Failure -> {
                    _uiState.update { it.copy(refreshing = false) }
                    ToastController.show("Não foi possível carregar seus pedidos")
                }
            }
        }
    }
}
