package com.nextdoor.app.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextdoor.app.data.dto.StoreDto
import com.nextdoor.app.data.infra.ApiResult
import com.nextdoor.app.data.repository.CartRepository
import com.nextdoor.app.data.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Error(val message: String) : HomeUiState
    data class Content(val stores: List<StoreDto>) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    cartRepository: CartRepository
) : ViewModel() {

    var uiState by mutableStateOf<HomeUiState>(HomeUiState.Loading)
        private set

    /** Live cart badge count for the top bar. */
    val badgeCount: StateFlow<Int> = cartRepository.badgeCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            uiState = HomeUiState.Loading
            when (val result = storeRepository.listStores()) {
                is ApiResult.Success -> uiState = HomeUiState.Content(result.data)
                is ApiResult.Failure ->
                    uiState = HomeUiState.Error("Não foi possível carregar as lojas")
            }
        }
    }

    fun refresh() {
        if (uiState is HomeUiState.Loading) return
        viewModelScope.launch {
            when (val result = storeRepository.listStores()) {
                is ApiResult.Success -> uiState = HomeUiState.Content(result.data)
                is ApiResult.Failure -> { /* keep current list on failure */ }
            }
        }
    }
}
