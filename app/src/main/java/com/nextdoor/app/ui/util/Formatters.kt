package com.nextdoor.app.ui.util

import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** "99.80" or 49.9 -> "R$ 24,90" (pt-BR style, non-breaking space after R$). */
fun Double.toBRL(): String {
    val nf = NumberFormat.getNumberInstance(Locale("pt", "BR")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "R$ ${nf.format(this)}"
}

fun String?.toBRL(): String = (this?.toDoubleOrNull() ?: 0.0).toBRL()

const val MONEY_REGEX = "^(?:R\\$\\s?)?\\d{1,9}(?:[.,]\\d{1,2})?$"

/**
 * ISO datetime (e.g. "2026-08-03T20:33:36") -> "Hoje, 20:33" / "Ontem, 20:33" /
 * "3 ago. 2026, 20:33".
 */
fun String?.toDisplayDateTime(): String {
    if (this.isNullOrBlank()) return ""
    return runCatching {
        val dt = LocalDateTime.parse(this, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val now = LocalDateTime.now()
        val time = dt.format(DateTimeFormatter.ofPattern("HH:mm"))
        when (dt.toLocalDate()) {
            now.toLocalDate() -> "Hoje, $time"
            now.toLocalDate().minusDays(1) -> "Ontem, $time"
            else -> dt.format(
                DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale("pt", "BR"))
            )
        }
    }.getOrElse { "" }
}

/** "8609ad7d-3401-43bc-a7da-a2a60f3a7cbf" -> "#8609ad7d" (first 8). */
fun String?.shortId(): String {
    if (this == null) return ""
    return if (length > 8) "#${take(8)}" else "#$this"
}
