package com.nextdoor.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextdoor.app.data.dto.StoreDto
import com.nextdoor.app.data.infra.ApiResult
import com.nextdoor.app.data.repository.SearchProductHit
import com.nextdoor.app.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val loading: Boolean = false,
    val searched: Boolean = false,
    val stores: List<StoreDto> = emptyList(),
    val products: List<SearchProductHit> = emptyList()
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var initialized = false

    init {
        viewModelScope.launch {
            _query
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { query ->
                    val q = query.trim()
                    if (q.length < 2) {
                        _uiState.update {
                            it.copy(loading = false, searched = false, stores = emptyList(), products = emptyList())
                        }
                    } else {
                        _uiState.update { it.copy(loading = true, searched = true) }
                        when (val result = searchRepository.search(q)) {
                            is ApiResult.Success -> _uiState.update {
                                it.copy(
                                    loading = false,
                                    stores = result.data.stores,
                                    products = result.data.products
                                )
                            }
                            is ApiResult.Failure -> _uiState.update {
                                it.copy(loading = false, stores = emptyList(), products = emptyList())
                            }
                        }
                    }
                }
        }
    }

    /** Seeds the query from the navigation argument (once). */
    fun initialize(initialQuery: String) {
        if (initialized) return
        initialized = true
        _query.value = initialQuery
        _uiState.update { it.copy(query = initialQuery) }
    }

    fun onQueryChange(value: String) {
        _query.value = value
        _uiState.update { it.copy(query = value) }
    }

    fun clearQuery() {
        _query.value = ""
        _uiState.update { it.copy(query = "", searched = false, stores = emptyList(), products = emptyList()) }
    }
}
