package com.nextdoor.app.ui.util

/** Maps the backend `category` enum to a pt-BR display label. */
fun categoryLabel(category: String?): String = when (category) {
    "VESTUARIO" -> "Vestuário"
    "ELETRONICOS" -> "Eletrônicos"
    "COSMETICOS" -> "Cosméticos"
    "PETS" -> "Pets"
    "LIVRARIA" -> "Livraria"
    else -> category.orEmpty()
}
