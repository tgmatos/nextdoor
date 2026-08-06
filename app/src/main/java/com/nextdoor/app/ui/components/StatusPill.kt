package com.nextdoor.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Maps a backend `status_order` to a color pair + pt-BR label. */
data class StatusSpec(val label: String, val bg: Color, val fg: Color)

fun statusSpec(statusOrder: String?): StatusSpec = when (statusOrder?.uppercase()) {
    "ESPERANDO" -> StatusSpec("Esperando", com.nextdoor.app.ui.theme.Amber50, com.nextdoor.app.ui.theme.Amber700)
    "ACEITO" -> StatusSpec("Aceito", com.nextdoor.app.ui.theme.Blue50, com.nextdoor.app.ui.theme.Blue700)
    "PREPARACAO", "EM_PREPARACAO", "EM PREPARAÇÃO" ->
        StatusSpec("Em Preparação", com.nextdoor.app.ui.theme.Olive50, com.nextdoor.app.ui.theme.Olive700)
    "ROTA", "EM_ROTA", "EM ROTA" ->
        StatusSpec("Em Rota", com.nextdoor.app.ui.theme.Purple50, com.nextdoor.app.ui.theme.Purple700)
    "CONCLUIDO", "CONCLUÍDO" ->
        StatusSpec("Concluído", com.nextdoor.app.ui.theme.Emerald50, com.nextdoor.app.ui.theme.Emerald700)
    "CANCELADO" -> StatusSpec("Cancelado", com.nextdoor.app.ui.theme.Rose50, com.nextdoor.app.ui.theme.Rose700)
    "RECUSADO" -> StatusSpec("Recusado", com.nextdoor.app.ui.theme.Rose50, com.nextdoor.app.ui.theme.Rose700)
    else -> StatusSpec(statusOrder ?: "", com.nextdoor.app.ui.theme.Olive50, com.nextdoor.app.ui.theme.Olive700)
}

@Composable
fun StatusPill(
    statusOrder: String?,
    modifier: Modifier = Modifier
) {
    val spec = statusSpec(statusOrder)
    Box(
        modifier = modifier
            .background(spec.bg, RoundedCornerShape(percent = 50))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = spec.label,
            color = spec.fg,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp
        )
    }
}
