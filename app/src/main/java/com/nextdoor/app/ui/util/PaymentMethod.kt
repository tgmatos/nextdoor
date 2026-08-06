package com.nextdoor.app.ui.util

enum class PaymentMethod(val apiValue: String, val label: String) {
    CC("CC", "Cartão de Crédito"),
    CD("CD", "Cartão de Débito"),
    PIX("PIX", "PIX"),
    DINHEIRO("DINHEIRO", "Dinheiro");

    companion object {
        fun fromApi(value: String?): PaymentMethod? =
            entries.firstOrNull { it.apiValue == value }

        fun labelOf(value: String?): String = fromApi(value)?.label ?: (value ?: "")
    }
}
