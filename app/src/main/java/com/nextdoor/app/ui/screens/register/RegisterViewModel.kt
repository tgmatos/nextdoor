package com.nextdoor.app.ui.screens.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextdoor.app.data.infra.ApiResult
import com.nextdoor.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/** Register form state. Single content state + inline error banner. */
data class RegisterUiState(
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val street: String = "",
    val number: String = "",
    val complement: String = "",
    val neighborhood: String = "",
    val city: String = "",
    val uf: String = "",
    val cep: String = "",
    val passwordVisible: Boolean = true,
    val submitting: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var uiState by mutableStateOf(RegisterUiState())
        private set

    fun onEmailChange(value: String) = update(error = null) { it.copy(email = value) }
    fun onUsernameChange(value: String) = update(error = null) { it.copy(username = value) }
    fun onPasswordChange(value: String) = update(error = null) { it.copy(password = value) }
    fun onStreetChange(value: String) = update(error = null) { it.copy(street = value) }
    fun onNumberChange(value: String) = update(error = null) { it.copy(number = value) }
    fun onComplementChange(value: String) = update(error = null) { it.copy(complement = value) }
    fun onNeighborhoodChange(value: String) = update(error = null) { it.copy(neighborhood = value) }
    fun onCityChange(value: String) = update(error = null) { it.copy(city = value) }
    fun onUfChange(value: String) = update(error = null) {
        it.copy(uf = value.take(2).uppercase())
    }
    fun onCepChange(value: String) = update(error = null) {
        it.copy(cep = value.filter { c -> c.isDigit() }.take(8))
    }

    fun onPasswordVisibleChange(visible: Boolean) = update { it.copy(passwordVisible = visible) }

    /** Validates and submits; on success calls [onRegistered]. */
    fun register(onRegistered: () -> Unit) {
        if (uiState.submitting) return

        val s = uiState
        val email = s.email.trim()
        val username = s.username.trim()

        when {
            email.isBlank() || !EMAIL_REGEX.matches(email) -> {
                uiState = uiState.copy(error = "Informe um e-mail válido.")
                return
            }
            username.isBlank() -> {
                uiState = uiState.copy(error = "Informe um nome de usuário.")
                return
            }
            !isPasswordValid(s.password) -> {
                uiState = uiState.copy(
                    error = "A senha deve ter no mínimo 6 caracteres, com pelo menos 1 letra maiúscula e 1 número ou símbolo."
                )
                return
            }
            s.street.isBlank() -> {
                uiState = uiState.copy(error = "Informe a rua.")
                return
            }
            s.number.isBlank() -> {
                uiState = uiState.copy(error = "Informe o número.")
                return
            }
            s.neighborhood.isBlank() -> {
                uiState = uiState.copy(error = "Informe o bairro.")
                return
            }
            !CEP_REGEX.matches(s.cep) -> {
                uiState = uiState.copy(error = "Informe um CEP válido.")
                return
            }
        }

        viewModelScope.launch {
            uiState = uiState.copy(submitting = true, error = null)
            when (val result = authRepository.register(
                email = email,
                username = username,
                password = s.password,
                number = s.number.trim(),
                street = s.street.trim(),
                neighborhood = s.neighborhood.trim(),
                cep = s.cep.trim()
            )) {
                is ApiResult.Success -> onRegistered()
                is ApiResult.Failure -> {
                    uiState = uiState.copy(submitting = false, error = mapError(result))
                }
            }
        }
    }

    /** Maps a failure into a pt-BR banner message. */
    private fun mapError(failure: ApiResult.Failure): String {
        if (failure.fieldErrors.isNotEmpty()) {
            val messages = failure.fieldErrors
                .flatMap { (_, list) -> list }
                .filter { it.isNotBlank() }
            if (messages.isNotEmpty()) return messages.joinToString(" ")
        }
        failure.message?.let { return it }
        return "Não foi possível cadastrar. Tente novamente."
    }

    private inline fun update(error: String? = null, transform: (RegisterUiState) -> RegisterUiState) {
        uiState = transform(uiState).copy(error = error)
    }

    private fun isPasswordValid(password: String): Boolean =
        password.length >= 6 &&
            password.any { it.isLowerCase() } &&
            password.any { it.isUpperCase() } &&
            password.any { it.isDigit() || !it.isLetterOrDigit() }

    companion object {
        private val EMAIL_REGEX =
            Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        private val CEP_REGEX = Regex("^\\d{8}$")
    }
}
