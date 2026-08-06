package com.nextdoor.app.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nextdoor.app.data.dto.ProductDto
import com.nextdoor.app.ui.components.BackTopBar
import com.nextdoor.app.ui.components.Base64Image
import com.nextdoor.app.ui.components.ErrorBanner
import com.nextdoor.app.ui.components.GhostButton
import com.nextdoor.app.ui.components.PrimaryButton
import com.nextdoor.app.ui.components.SkeletonBlock
import com.nextdoor.app.ui.theme.NaturalBg
import com.nextdoor.app.ui.theme.NaturalBorder
import com.nextdoor.app.ui.theme.NaturalDivider
import com.nextdoor.app.ui.theme.Olive50
import com.nextdoor.app.ui.theme.Olive100
import com.nextdoor.app.ui.theme.Olive200
import com.nextdoor.app.ui.theme.Olive600
import com.nextdoor.app.ui.theme.Olive700
import com.nextdoor.app.ui.theme.SansFamily
import com.nextdoor.app.ui.theme.SerifFamily
import com.nextdoor.app.ui.theme.TextMuted
import com.nextdoor.app.ui.theme.TextPrimary
import com.nextdoor.app.ui.theme.TextStyles
import com.nextdoor.app.ui.util.PaymentMethod
import com.nextdoor.app.ui.util.toBRL

@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onOrderPlaced: (String) -> Unit
) {
    val viewModel: CheckoutViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CheckoutEvent.OrderPlaced -> onOrderPlaced(event.orderId)
            }
        }
    }

    // Empty cart (on entry or after removing last item) -> redirect home.
    // Skipped while submitting so the order-success navigation isn't overridden.
    LaunchedEffect(state.loading, state.submitting, state.items.isEmpty()) {
        if (!state.loading && !state.submitting && state.items.isEmpty()) onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NaturalBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            BackTopBar(title = "Finalizar pedido", onBack = onBack)
            if (state.loading || state.submitting) {
                CheckoutSkeleton()
            } else {
                CheckoutContent(state = state, viewModel = viewModel)
            }
        }

        if (!state.loading && !state.submitting) {
            StickyCheckoutCta(state = state, onConfirm = viewModel::confirmOrder, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun CheckoutContent(state: CheckoutUiState, viewModel: CheckoutViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp, end = 20.dp, top = 4.dp, bottom = 128.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            if (!state.errorBanner.isNullOrBlank()) {
                ErrorBanner(message = state.errorBanner)
                Spacer(Modifier.height(4.dp))
            }
        }
        item {
            CheckoutCard(title = "Itens${if (state.storeName.isNotBlank()) " · ${state.storeName}" else ""}") {
                state.items.forEachIndexed { index, (product, qty) ->
                    ItemRow(product = product, qty = qty, onRemove = { viewModel.removeItem(product) })
                    if (index != state.items.lastIndex) {
                        HorizontalDivider(color = NaturalDivider, modifier = Modifier.padding(vertical = 12.dp))
                    }
                }
            }
        }
        item {
            CheckoutCard(title = "Endereço de entrega") {
                AddressSection(
                    addresses = state.addresses,
                    addressIndex = state.addressIndex,
                    onCycle = viewModel::cycleAddress
                )
            }
        }
        item {
            CheckoutCard(title = "Pagamento") {
                PaymentGrid(
                    selected = state.selectedPayment,
                    onSelect = viewModel::selectPayment
                )
            }
        }
        item {
            CheckoutCard(title = "Resumo") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Subtotal", fontSize = 12.sp, color = TextMuted)
                    Text(state.subtotal.toBRL(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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
                        text = state.subtotal.toBRL(),
                        fontFamily = SerifFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = Olive600
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckoutCard(title: String, content: @Composable ColumnScope.() -> Unit) {
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
private fun ItemRow(product: ProductDto, qty: Int, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Base64Image(
            dataUrl = product.image,
            fallbackInitial = product.name.firstOrNull()?.toString(),
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "$qty × ${product.price.toBRL()}",
                fontSize = 10.sp,
                color = TextMuted
            )
        }
        Text(
            text = (product.price * qty).toBRL(),
            fontFamily = SerifFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Olive600
        )
        Spacer(Modifier.width(2.dp))
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Remover",
            tint = TextMuted,
            modifier = Modifier
                .size(15.dp)
                .clickable(onClick = onRemove)
        )
    }
}

@Composable
private fun AddressSection(addresses: List<com.nextdoor.app.data.dto.AddressDto>, addressIndex: Int, onCycle: () -> Unit) {
    val address = addresses.getOrNull(addressIndex)
    if (address == null) {
        Text("Nenhum endereço cadastrado", fontSize = 12.sp, color = TextMuted)
        return
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Olive50, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = Olive600, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("Endereço", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${address.street}, ${address.addressNumber}",
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 16.sp
            )
            Text(
                text = "${address.neighborhood} · CEP ${address.cep}",
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 16.sp
            )
        }
        if (addresses.size > 1) {
            GhostButton(
                text = "Trocar",
                onClick = onCycle,
                textColor = Olive600,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun PaymentGrid(selected: PaymentMethod, onSelect: (PaymentMethod) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PaymentMethod.entries.chunked(2).forEach { rowPills ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowPills.forEach { pill ->
                    PaymentPill(payment = pill, selected = pill == selected, onClick = { onSelect(pill) }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PaymentPill(payment: PaymentMethod, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bg = if (selected) Olive600 else Olive50
    val fg = if (selected) Color.White else Olive700
    val borderColor = if (selected) Olive600 else Olive200
    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(percent = 50))
            .border(1.dp, borderColor, RoundedCornerShape(percent = 50))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = payment.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StickyCheckoutCta(state: CheckoutUiState, onConfirm: () -> Unit, modifier: Modifier = Modifier) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            NaturalBg.copy(alpha = 0f),
            NaturalBg,
            NaturalBg
        )
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(gradient)
            .padding(20.dp)
    ) {
        PrimaryButton(
            text = "Confirmar pedido · ${state.subtotal.toBRL()}",
            onClick = onConfirm,
            enabled = state.items.isNotEmpty() && !state.submitting,
            loading = state.submitting,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CheckoutSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(4) {
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}
