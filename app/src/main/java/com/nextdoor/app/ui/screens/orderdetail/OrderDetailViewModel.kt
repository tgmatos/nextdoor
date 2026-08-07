package com.nextdoor.app.ui.screens.orderdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextdoor.app.data.dto.OrderDto
import com.nextdoor.app.data.infra.ApiResult
import com.nextdoor.app.data.repository.AccountRepository
import com.nextdoor.app.data.repository.OrderUpdatesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderDetailUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val notFound: Boolean = false,
    val order: OrderDto? = null
)

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val orderUpdatesRepository: OrderUpdatesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    init {
        // Patch the open order whenever the websocket announces a status change for it.
        viewModelScope.launch {
            orderUpdatesRepository.updates.collect { update ->
                _uiState.update { state ->
                    val order = state.order
                    if (order != null && order.id == update.id) {
                        state.copy(
                            order = order.copy(
                                statusOrder = update.statusOrder,
                                total = update.total,
                                paymentMethod = update.paymentMethod
                            )
                        )
                    } else {
                        state
                    }
                }
            }
        }
    }

    fun load(orderId: String) {
        // Ignore re-loads for the same id.
        if (!_uiState.value.loading && _uiState.value.order?.id == orderId) return
        _uiState.value = OrderDetailUiState(loading = true)
        viewModelScope.launch {
            when (val result = accountRepository.getOrder(orderId)) {
                is ApiResult.Success -> {
                    _uiState.value = OrderDetailUiState(loading = false, order = result.data)
                }
                is ApiResult.Failure -> {
                    _uiState.value = if (result.code == 404) {
                        OrderDetailUiState(loading = false, notFound = true)
                    } else {
                        OrderDetailUiState(loading = false, error = true)
                    }
                }
            }
        }
    }
}
