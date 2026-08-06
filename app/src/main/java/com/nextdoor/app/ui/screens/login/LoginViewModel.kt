package com.nextdoor.app.ui.screens.login

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

/** Form + login state. Single content state with an inline error banner. */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = true,
    val submitting: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value, error = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, error = null)
    }

    fun onPasswordVisibleChange(visible: Boolean) {
        uiState = uiState.copy(passwordVisible = visible)
    }

    /** Validates and submits the form; on success calls [onLoggedIn]. */
    fun login(onLoggedIn: () -> Unit) {
        if (uiState.submitting) return

        val email = uiState.email.trim()
        val password = uiState.password

        when {
            email.isBlank() || !EMAIL_REGEX.matches(email) -> {
                uiState = uiState.copy(error = "Informe um e-mail válido.")
                return
            }
            password.isBlank() -> {
                uiState = uiState.copy(error = "Informe sua senha.")
                return
            }
        }

        viewModelScope.launch {
            uiState = uiState.copy(submitting = true, error = null)
            when (val result = authRepository.login(email, password)) {
                is ApiResult.Success -> onLoggedIn()
                is ApiResult.Failure -> {
                    val message = if (result.code == 401) {
                        "E-mail ou senha incorretos"
                    } else {
                        "Não foi possível entrar. Tente novamente."
                    }
                    uiState = uiState.copy(submitting = false, error = message)
                }
            }
        }
    }

    companion object {
        private val EMAIL_REGEX =
            Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
