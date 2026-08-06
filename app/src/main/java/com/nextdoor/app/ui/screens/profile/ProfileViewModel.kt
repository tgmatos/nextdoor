package com.nextdoor.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextdoor.app.data.dto.AddressDto
import com.nextdoor.app.data.infra.ApiResult
import com.nextdoor.app.data.infra.ApiResult.Failure
import com.nextdoor.app.data.infra.getOrNull
import com.nextdoor.app.data.infra.ApiResult.Success
import com.nextdoor.app.data.repository.AccountRepository
import com.nextdoor.app.data.repository.AuthRepository
import com.nextdoor.app.data.repository.CartRepository
import com.nextdoor.app.ui.components.ToastController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val email: String = "",
    val username: String = "",
    val addresses: List<AddressDto> = emptyList(),
    val saving: Boolean = false,
    val saveError: String? = null,
    val editingAddress: AddressDto? = null,
    val addressSaving: Boolean = false,
    val addressError: String? = null,
    val confirmDelete: Boolean = false
)

sealed interface ProfileEvent {
    data object LoggedOut : ProfileEvent
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val authRepository: AuthRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            val accountDeferred = async { accountRepository.getAccount() }
            val addressesDeferred = async { accountRepository.listAddresses() }
            val accountResult = accountDeferred.await()
            val addressesResult = addressesDeferred.await()

            val account = accountResult.getOrNull()
            if (account != null) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = false,
                        email = account.email,
                        username = account.username,
                        addresses = addressesResult.getOrNull().orEmpty()
                    )
                }
            } else {
                _uiState.update { it.copy(loading = false, error = true) }
            }
        }
    }

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, saveError = null) }
    fun onUsernameChange(value: String) = _uiState.update { it.copy(username = value, saveError = null) }

    fun saveAccount() {
        val state = _uiState.value
        val email = state.email.trim()
        val username = state.username.trim()

        if (!isValidEmail(email)) {
            _uiState.update { it.copy(saveError = "Informe um e-mail válido") }
            return
        }
        if (username.isBlank()) {
            _uiState.update { it.copy(saveError = "Informe um nome de usuário") }
            return
        }

        _uiState.update { it.copy(saving = true, saveError = null) }
        viewModelScope.launch {
            when (val result = accountRepository.updateAccount(email, username)) {
                is Success -> {
                    _uiState.update {
                        it.copy(saving = false, email = result.data.email, username = result.data.username)
                    }
                    ToastController.show("Alterações salvas")
                }
                is Failure -> {
                    val msg = fieldErrorOf(result)
                        .ifBlank { result.message ?: "Não foi possível salvar as alterações" }
                    _uiState.update { it.copy(saving = false, saveError = msg) }
                }
            }
        }
    }

    fun startEdit(address: AddressDto) {
        _uiState.update { it.copy(editingAddress = address, addressError = null) }
    }

    fun cancelEdit() {
        _uiState.update { it.copy(editingAddress = null, addressError = null) }
    }

    fun saveAddress(street: String, addressNumber: String, neighborhood: String, cep: String) {
        val editing = _uiState.value.editingAddress ?: return
        if (cep.replace(Regex("\\D"), "").length != 8) {
            _uiState.update { it.copy(addressError = "O CEP deve conter 8 dígitos") }
            return
        }
        _uiState.update { it.copy(addressSaving = true, addressError = null) }
        viewModelScope.launch {
            when (val result = accountRepository.updateAddress(editing.id, addressNumber, street, neighborhood, cep)) {
                is Success -> {
                    _uiState.update { state ->
                        state.copy(
                            addressSaving = false,
                            editingAddress = null,
                            addresses = state.addresses.map { if (it.id == editing.id) result.data else it }
                        )
                    }
                    ToastController.show("Endereço atualizado")
                }
                is Failure -> {
                    val msg = fieldErrorOf(result)
                        .ifBlank { result.message ?: "Não foi possível salvar o endereço" }
                    _uiState.update { it.copy(addressSaving = false, addressError = msg) }
                }
            }
        }
    }

    fun requestDelete() = _uiState.update { it.copy(confirmDelete = true) }
    fun cancelDelete() = _uiState.update { it.copy(confirmDelete = false) }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _events.emit(ProfileEvent.LoggedOut)
        }
    }

    fun deleteAccount() {
        _uiState.update { it.copy(confirmDelete = false) }
        viewModelScope.launch {
            accountRepository.deleteAccount()
            cartRepository.clear()
            _events.emit(ProfileEvent.LoggedOut)
        }
    }

    private fun fieldErrorOf(result: ApiResult.Failure): String =
        result.fieldErrors.values.flatten().joinToString("\n")

    private fun isValidEmail(value: String): Boolean {
        val trimmed = value.trim()
        return trimmed.isNotBlank() &&
            Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$").matches(trimmed)
    }
}
