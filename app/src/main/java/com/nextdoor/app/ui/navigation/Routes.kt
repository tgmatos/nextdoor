package com.nextdoor.app.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val STORE = "store/{id}"
    const val PRODUCT = "product/{id}/{productId}"
    const val CHECKOUT = "checkout"
    const val ORDERS = "orders"
    const val ORDER_DETAIL = "orders/{id}"
    const val PROFILE = "profile"
    const val SEARCH = "search?query={query}"

    const val ARG_STORE_ID = "id"
    const val ARG_PRODUCT_ID = "productId"
    const val ARG_ORDER_ID = "id"
    const val ARG_QUERY = "query"

    fun store(id: String) = "store/$id"
    fun product(storeId: String, productId: String) = "product/$storeId/$productId"
    fun order(id: String) = "orders/$id"
    fun search(query: String? = null) = if (query.isNullOrBlank()) "search?query=" else "search?query=${queryUrlEncode(query)}"

    private fun queryUrlEncode(value: String): String =
        java.net.URLEncoder.encode(value, "UTF-8")
}
