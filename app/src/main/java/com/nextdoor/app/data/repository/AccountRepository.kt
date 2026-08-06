package com.nextdoor.app.data.repository

import com.nextdoor.app.data.api.AccountApi
import com.nextdoor.app.data.dto.AccountDto
import com.nextdoor.app.data.dto.AddressDto
import com.nextdoor.app.data.dto.FlatAccountDto
import com.nextdoor.app.data.dto.OrderDto
import com.nextdoor.app.data.dto.UpdateAccountBody
import com.nextdoor.app.data.dto.UpdateAccountRequest
import com.nextdoor.app.data.dto.UpdateAddressBody
import com.nextdoor.app.data.dto.UpdateAddressRequest
import com.nextdoor.app.data.infra.ApiResult
import com.nextdoor.app.data.infra.safeApiResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val api: AccountApi
) {
    suspend fun getAccount(): ApiResult<AccountDto> = safeApiResult {
        api.getAccount().account
    }

    suspend fun updateAccount(email: String, username: String): ApiResult<FlatAccountDto> = safeApiResult {
        api.updateAccount(UpdateAccountRequest(UpdateAccountBody(email = email.trim(), username = username.trim())))
    }

    suspend fun deleteAccount(): ApiResult<Unit> = safeApiResult {
        api.deleteAccount()
        Unit
    }

    suspend fun listOrders(): ApiResult<List<OrderDto>> = safeApiResult {
        api.listOrders().orders
    }

    suspend fun getOrder(id: String): ApiResult<OrderDto> = safeApiResult {
        api.getOrder(id).order
    }

    suspend fun listAddresses(): ApiResult<List<AddressDto>> = safeApiResult {
        api.listAddresses().body().orEmpty()
    }

    suspend fun updateAddress(id: String, addressNumber: String, street: String, neighborhood: String, cep: String): ApiResult<AddressDto> =
        safeApiResult {
            api.updateAddress(
                id,
                UpdateAddressRequest(
                    UpdateAddressBody(
                        addressNumber = addressNumber.trim(),
                        street = street.trim(),
                        neighborhood = neighborhood.trim(),
                        cep = cep.trim()
                    )
                )
            )
        }
}
