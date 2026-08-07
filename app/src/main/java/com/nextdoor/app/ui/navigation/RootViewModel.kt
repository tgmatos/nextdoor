package com.nextdoor.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextdoor.app.data.infra.TokenStore
import com.nextdoor.app.data.repository.OrderUpdatesRepository
import com.nextdoor.app.ui.components.ToastController
import com.nextdoor.app.ui.components.statusSpec
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class RootViewModel @Inject constructor(
    tokenStore: TokenStore,
    orderUpdatesRepository: OrderUpdatesRepository
) : ViewModel() {

    /** Whether a JWT is present. Drives the start destination and logout redirects. */
    val loggedIn: StateFlow<Boolean> = tokenStore.tokenFlow
        .map { !it.isNullOrBlank() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        // Surface live status updates as a toast from a single app-wide place.
        viewModelScope.launch {
            orderUpdatesRepository.updates.collect { update ->
                ToastController.show("Pedido atualizado: ${statusSpec(update.statusOrder).label}")
            }
        }
    }
}
