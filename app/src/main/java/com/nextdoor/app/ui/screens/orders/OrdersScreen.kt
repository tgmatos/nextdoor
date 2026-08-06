package com.nextdoor.app.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nextdoor.app.data.dto.OrderDto
import com.nextdoor.app.ui.components.EmptyState
import com.nextdoor.app.ui.components.ErrorState
import com.nextdoor.app.ui.components.HomeTopBar
import com.nextdoor.app.ui.components.SkeletonBlock
import com.nextdoor.app.ui.components.StatusPill
import com.nextdoor.app.ui.theme.NaturalBg
import com.nextdoor.app.ui.theme.NaturalBorder
import com.nextdoor.app.ui.theme.NaturalDivider
import com.nextdoor.app.ui.theme.Olive600
import com.nextdoor.app.ui.theme.SerifFamily
import com.nextdoor.app.ui.theme.TextMuted
import com.nextdoor.app.ui.theme.TextPrimary
import com.nextdoor.app.ui.util.shortId
import com.nextdoor.app.ui.util.toBRL
import com.nextdoor.app.ui.util.toDisplayDateTime

@Composable
fun OrdersScreen(
    onOrderClick: (String) -> Unit,
    onCartClick: () -> Unit
) {
    val viewModel: OrdersViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val badge by viewModel.badgeCount.collectAsStateWithLifecycle()

    // Re-fetch orders whenever this screen becomes resumed (entering from another
    // screen, bottom-tab switch, or back-navigation) to pick up new orders.
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NaturalBg)
    ) {
        HomeTopBar(
            title = "Meus Pedidos",
            onCartClick = onCartClick,
            badgeCount = badge
        )

        when {
            state.loading -> OrdersSkeleton()
            state.error -> ErrorState(
                title = "Não foi possível carregar seus pedidos",
                onRetry = { viewModel.load() }
            )
            state.orders.isEmpty() -> EmptyState(
                title = "Nenhum pedido ainda",
                hint = "Seus pedidos aparecerão aqui.",
                icon = Icons.Default.ReceiptLong
            )
            else -> PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.orders, key = { it.id }) { order ->
                        OrderCard(order = order, onClick = { onOrderClick(order.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: OrderDto, onClick: () -> Unit) {
    val short = order.id.shortId()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, NaturalBorder, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pedido $short",
                fontFamily = SerifFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(12.dp))
            StatusPill(statusOrder = order.statusOrder)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${order.insertedAt.toDisplayDateTime()} · Pedido $short",
            fontSize = 10.sp,
            color = TextMuted
        )
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = NaturalDivider)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (order.orderProduct.size == 1) "1 item" else "${order.orderProduct.size} itens",
                fontSize = 12.sp,
                color = TextMuted
            )
            Text(
                text = order.total.toBRL(),
                fontFamily = SerifFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Olive600
            )
        }
    }
}

@Composable
private fun OrdersSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(4) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, NaturalBorder, RoundedCornerShape(24.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SkeletonBlock(
                        modifier = Modifier
                            .width(140.dp)
                            .height(18.dp),
                        shape = RoundedCornerShape(9.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    SkeletonBlock(
                        modifier = Modifier
                            .width(70.dp)
                            .height(22.dp),
                        shape = RoundedCornerShape(percent = 50)
                    )
                }
                SkeletonBlock(
                    modifier = Modifier
                        .width(180.dp)
                        .height(12.dp),
                    shape = RoundedCornerShape(6.dp)
                )
                HorizontalDivider(color = NaturalDivider)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SkeletonBlock(
                        modifier = Modifier
                            .width(60.dp)
                            .height(12.dp),
                        shape = RoundedCornerShape(6.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    SkeletonBlock(
                        modifier = Modifier
                            .width(70.dp)
                            .height(14.dp),
                        shape = RoundedCornerShape(7.dp)
                    )
                }
            }
        }
    }
}
