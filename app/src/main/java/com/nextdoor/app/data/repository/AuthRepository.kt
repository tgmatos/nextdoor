package com.nextdoor.app.data.repository

import com.nextdoor.app.data.api.AuthApi
import com.nextdoor.app.data.dto.LoginRequest
import com.nextdoor.app.data.dto.RegisterAddressRequest
import com.nextdoor.app.data.dto.RegisterRequest
import com.nextdoor.app.data.infra.ApiResult
import com.nextdoor.app.data.infra.TokenStore
import com.nextdoor.app.data.infra.safeApiResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val tokenStore: TokenStore
) {
    suspend fun login(email: String, password: String): ApiResult<String> = safeApiResult {
        val token = api.login(LoginRequest(email = email.trim(), password = password)).token
        tokenStore.save(token)
        token
    }

    suspend fun register(
        email: String,
        username: String,
        password: String,
        number: String,
        street: String,
        neighborhood: String,
        cep: String
    ): ApiResult<String> = safeApiResult {
        val body = RegisterRequest(
            email = email.trim(),
            username = username.trim(),
            password = password,
            address = RegisterAddressRequest(
                number = number.trim(),
                street = street.trim(),
                neighborhood = neighborhood.trim(),
                cep = cep.trim()
            )
        )
        val token = api.register(body).token
        tokenStore.save(token)
        token
    }

    suspend fun logout(): ApiResult<Unit> = safeApiResult {
        try {
            api.logout()
        } catch (_: Exception) {
            // fire-and-forget; still clear the local session below
        }
        tokenStore.clear()
    }

    suspend fun clearSession() {
        tokenStore.clear()
    }
}
