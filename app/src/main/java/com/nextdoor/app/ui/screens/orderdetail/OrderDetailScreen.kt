package com.nextdoor.app.ui.screens.orderdetail

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nextdoor.app.data.dto.OrderDto
import com.nextdoor.app.data.dto.OrderProductDto
import com.nextdoor.app.ui.components.BackTopBar
import com.nextdoor.app.ui.components.Base64Image
import com.nextdoor.app.ui.components.ErrorState
import com.nextdoor.app.ui.components.GhostButton
import com.nextdoor.app.ui.components.SkeletonBlock
import com.nextdoor.app.ui.components.StatusPill
import com.nextdoor.app.ui.components.ToastController
import com.nextdoor.app.ui.theme.NaturalBg
import com.nextdoor.app.ui.theme.NaturalBorder
import com.nextdoor.app.ui.theme.NaturalDivider
import com.nextdoor.app.ui.theme.Olive50
import com.nextdoor.app.ui.theme.Olive200
import com.nextdoor.app.ui.theme.Olive600
import com.nextdoor.app.ui.theme.Olive700
import com.nextdoor.app.ui.theme.SerifFamily
import com.nextdoor.app.ui.theme.TextMuted
import com.nextdoor.app.ui.theme.TextPrimary
import com.nextdoor.app.ui.theme.TextStyles
import com.nextdoor.app.ui.util.PaymentMethod
import com.nextdoor.app.ui.util.shortId
import com.nextdoor.app.ui.util.toBRL
import com.nextdoor.app.ui.util.toDisplayDateTime

/** A product group derived from a flattened order_product list. */
private data class OrderLine(
    val product: OrderProductDto,
    val qty: Int,
    val lineTotal: Double
)

@Composable
fun OrderDetailScreen(
    orderId: String,
    onBack: () -> Unit
) {
    val viewModel: OrderDetailViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(orderId) {
        viewModel.load(orderId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NaturalBg)
    ) {
        BackTopBar(title = "Pedido ${orderId.shortId()}", onBack = onBack)

        when {
            state.loading -> OrderDetailSkeleton()
            state.notFound -> ErrorState(title = "Pedido não encontrado", onRetry = { viewModel.load(orderId) })
            state.error -> ErrorState(title = "Não foi possível carregar o pedido", onRetry = { viewModel.load(orderId) })
            state.order != null -> OrderDetailContent(order = state.order!!)
        }
    }
}

@Composable
private fun OrderDetailContent(order: OrderDto) {
    val lines = groupLines(order)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StatusCard(order = order)
        }
        item {
            DetailCard(title = "Itens") {
                lines.forEachIndexed { index, line ->
                    OrderLineRow(line = line)
                    if (index != lines.lastIndex) {
                        HorizontalDivider(color = NaturalDivider, modifier = Modifier.padding(vertical = 12.dp))
                    }
                }
            }
        }
        item {
            DetailCard(title = "Pagamento") {
                PaymentRow(order = order)
            }
        }
        item {
            DetailCard(title = "Resumo") {
                val subtotal = lines.sumOf { it.lineTotal }
                val total = order.total.toDoubleOrNull()
                    ?.let { it.toBRL() }
                    ?: subtotal.toBRL()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal", fontSize = 12.sp, color = TextMuted)
                    Text(subtotal.toBRL(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = NaturalDivider)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        text = total,
                        fontFamily = SerifFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = Olive600
                    )
                }
            }
        }
        item {
            GhostButton(
                text = "Ajuda",
                onClick = { ToastController.show("Em breve") },
                modifier = Modifier.fillMaxWidth(),
                textColor = Olive700
            )
        }
    }
}

/** Groups the flattened order_product list by product id; qty = group size. */
private fun groupLines(order: OrderDto): List<OrderLine> {
    val grouped = order.orderProduct.groupBy { it.id }
    return grouped.map { (_, products) ->
        val product = products.first()
        val qty = products.size
        OrderLine(product = product, qty = qty, lineTotal = product.price * qty)
    }
}

@Composable
private fun StatusCard(order: OrderDto) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, NaturalBorder, RoundedCornerShape(24.dp))
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Olive600.copy(alpha = alpha), CircleShape)
            )
            Column {
                Text(
                    text = statusSpecLabel(order.statusOrder),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = order.insertedAt.toDisplayDateTime(),
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
        StatusPill(statusOrder = order.statusOrder)
    }
}

@Composable
private fun statusSpecLabel(value: String?): String {
    return com.nextdoor.app.ui.components.statusSpec(value).label
}

@Composable
private fun OrderLineRow(line: OrderLine) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Base64Image(
            dataUrl = line.product.image,
            fallbackInitial = line.product.name.firstOrNull()?.toString(),
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = line.product.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${line.qty} × ${line.product.price.toBRL()}",
                fontSize = 10.sp,
                color = TextMuted
            )
        }
        Text(
            text = line.lineTotal.toBRL(),
            fontFamily = SerifFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Olive600
        )
    }
}

@Composable
private fun PaymentRow(order: OrderDto) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = PaymentMethod.labelOf(order.paymentMethod),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Box(
            modifier = Modifier
                .background(Olive50, RoundedCornerShape(percent = 50))
                .border(1.dp, Olive200, RoundedCornerShape(percent = 50))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = PaymentMethod.labelOf(order.paymentMethod).uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Olive700,
                letterSpacing = 0.4.sp
            )
        }
    }
}

@Composable
private fun DetailCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, NaturalBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text(text = title, style = TextStyles.Eyebrow, color = TextMuted)
        Spacer(Modifier.height(16.dp))
        content()
    }
}

@Composable
private fun OrderDetailSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp),
            shape = RoundedCornerShape(24.dp)
        )
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(24.dp)
        )
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(24.dp)
        )
    }
}
