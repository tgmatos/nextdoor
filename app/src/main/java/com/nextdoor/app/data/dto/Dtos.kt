package com.nextdoor.app.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---------- Auth ----------

@Serializable
data class AuthResponse(val token: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegisterAddressRequest(
    val number: String,
    val street: String,
    val neighborhood: String,
    val cep: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String,
    val address: RegisterAddressRequest
)

// ---------- Account ----------

@Serializable
data class AccountDto(
    val id: String,
    val username: String,
    val email: String
)

@Serializable
data class AccountResponse(val account: AccountDto)

@Serializable
data class UpdateAccountBody(
    val email: String,
    val username: String
)

@Serializable
data class UpdateAccountRequest(val account: UpdateAccountBody)

// PATCH /api/account returns a flat {username, email} (no id).
@Serializable
data class FlatAccountDto(
    val username: String,
    val email: String
)

// ---------- Address ----------

@Serializable
data class AddressDto(
    val id: String,
    @SerialName("address_number") val addressNumber: String,
    val street: String,
    val neighborhood: String,
    val cep: String
)

@Serializable
data class UpdateAddressBody(
    @SerialName("address_number") val addressNumber: String,
    val street: String,
    val neighborhood: String,
    val cep: String
)

@Serializable
data class UpdateAddressRequest(val address: UpdateAddressBody)

// ---------- Stores & Products ----------

@Serializable
data class StoreDto(
    val id: String,
    val name: String,
    val description: String,
    val image: String,
    val category: String,
    val telephone: String
)

@Serializable
data class StoresResponse(val stores: List<StoreDto>)

@Serializable
data class ProductDto(
    val id: String,
    val name: String,
    val description: String,
    val image: String,
    val quantity: Int,
    val price: Double,
    @SerialName("inserted_at") val insertedAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class ProductsResponse(val products: List<ProductDto>)

// ---------- Orders ----------

@Serializable
data class OrderProductDto(
    val id: String,
    val name: String,
    val description: String = "",
    val image: String = "",
    val price: Double = 0.0,
    @SerialName("inserted_at") val insertedAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class OrderDto(
    val id: String,
    val total: String,
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("status_order") val statusOrder: String,
    @SerialName("inserted_at") val insertedAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("order_product") val orderProduct: List<OrderProductDto> = emptyList()
)

@Serializable
data class OrdersResponse(val orders: List<OrderDto>)

@Serializable
data class OrderResponse(val order: OrderDto)

/** Payload of the `order_updated` websocket push on the `account:order:<id>` channel. */
@Serializable
data class OrderUpdateDto(
    val id: String,
    val total: String,
    @SerialName("status_order") val statusOrder: String,
    @SerialName("payment_method") val paymentMethod: String
)

@Serializable
data class CreateOrderProduct(
    val product: String,
    val quantity: Int
)

@Serializable
data class CreateOrderRequest(
    val products: List<CreateOrderProduct>,
    @SerialName("payment_method") val paymentMethod: String
)

// ---------- Error envelope ----------
// Two shapes are possible:
//   {"errors": {"<field>": ["msg", ...]}}
//   {"errors": {"detail": "message"}}
// We capture the map as JsonElements and normalize in ApiErrorParser.
@Serializable
data class ErrorEnvelope(val errors: Map<String, kotlinx.serialization.json.JsonElement> = emptyMap())
