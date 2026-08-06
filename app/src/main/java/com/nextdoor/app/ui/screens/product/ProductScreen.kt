package com.nextdoor.app.ui.screens.product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nextdoor.app.ui.components.BackTopBar
import com.nextdoor.app.ui.components.Base64Image
import com.nextdoor.app.ui.components.DangerButton
import com.nextdoor.app.ui.components.ErrorState
import com.nextdoor.app.ui.components.GhostButton
import com.nextdoor.app.ui.components.PrimaryButton
import com.nextdoor.app.ui.components.SkeletonBlock
import com.nextdoor.app.ui.theme.NaturalBg
import com.nextdoor.app.ui.theme.NaturalBorder
import com.nextdoor.app.ui.theme.NaturalDivider
import com.nextdoor.app.ui.theme.Olive200
import com.nextdoor.app.ui.theme.Olive600
import com.nextdoor.app.ui.theme.SerifFamily
import com.nextdoor.app.ui.theme.TextMuted
import com.nextdoor.app.ui.theme.TextPrimary
import com.nextdoor.app.ui.util.toBRL

@Composable
fun ProductScreen(
    storeId: String,
    productId: String,
    onBack: () -> Unit
) {
    val viewModel: ProductViewModel = hiltViewModel()
    val state = viewModel.uiState
    val badgeCount by viewModel.badgeCount.collectAsStateWithLifecycle()

    LaunchedEffect(storeId, productId) {
        viewModel.load(storeId, productId)
    }

    Scaffold(
        containerColor = NaturalBg,
        topBar = {
            BackTopBar(
                title = (state as? ProductUiState.Content)?.storeName ?: "Loja",
                onBack = onBack,
                onCartClick = null,
                badgeCount = badgeCount,
                centerLabel = true
            )
        },
        bottomBar = {
            val content = state as? ProductUiState.Content
            if (content != null) {
                StickyCta(state = content) {
                    viewModel.addToCart(storeId, onAdded = onBack)
                }
            }
        }
    ) { padding ->
        when (val s = state) {
            is ProductUiState.Loading -> ProductSkeleton(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
            is ProductUiState.Error -> ErrorState(
                title = s.message,
                onRetry = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(top = 40.dp)
            )
            is ProductUiState.Content -> ProductContent(
                product = s.product,
                qty = s.qty,
                onIncrement = viewModel::increment,
                onDecrement = viewModel::decrement,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
    }

    if (viewModel.showClearDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDialog,
            title = { Text("Limpar carrinho?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Seu carrinho tem itens de outra loja. Deseja limpá-lo para adicionar este produto?")
            },
            confirmButton = {
                DangerButton(
                    text = "Limpar e adicionar",
                    onClick = { viewModel.confirmClearAndReplace(storeId, onAdded = onBack) }
                )
            },
            dismissButton = {
                GhostButton(text = "Cancelar", onClick = viewModel::dismissDialog)
            }
        )
    }
}

@Composable
private fun ProductContent(
    product: com.nextdoor.app.data.dto.ProductDto,
    qty: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        Base64Image(
            dataUrl = product.image,
            fallbackInitial = product.name.firstOrNull()?.toString(),
            modifier = Modifier
                .fillMaxWidth()
                .height(256.dp)
        )

        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Text(
                text = product.name,
                fontFamily = SerifFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = TextPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = product.description,
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = product.price.toBRL(),
                fontFamily = SerifFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp,
                color = Olive600
            )

            Spacer(Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Stepper(
                    qty = qty,
                    stock = product.quantity,
                    onIncrement = onIncrement,
                    onDecrement = onDecrement
                )
                if (product.quantity > 0) {
                    Text(
                        text = "${product.quantity} em estoque",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun Stepper(
    qty: Int,
    stock: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val outOfStock = stock == 0
    Row(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(percent = 50))
            .border(1.dp, NaturalBorder, RoundedCornerShape(percent = 50))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepperButton(
            icon = Icons.Default.Remove,
            contentDescription = "Diminuir",
            dark = true,
            enabled = !outOfStock && qty > 1,
            onClick = onDecrement
        )
        Text(
            text = qty.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 20.dp)
        )
        StepperButton(
            icon = Icons.Default.Add,
            contentDescription = "Aumentar",
            dark = false,
            enabled = !outOfStock && qty < stock,
            onClick = onIncrement
        )
    }
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    contentDescription: String,
    dark: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(
                if (dark) Color.White else if (enabled) Olive600 else Olive200,
                CircleShape
            )
            .border(if (dark) 1.dp else 0.dp, NaturalBorder, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (dark) TextPrimary else Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun StickyCta(state: ProductUiState.Content, onAdd: () -> Unit) {
    val outOfStock = state.product.quantity == 0
    val total = state.qty * state.product.price
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, NaturalDivider)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        PrimaryButton(
            text = if (outOfStock) "Fora de estoque" else "Adicionar ao carrinho • ${total.toBRL()}",
            onClick = onAdd,
            modifier = Modifier.fillMaxWidth(),
            enabled = !outOfStock
        )
    }
}

@Composable
private fun ProductSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(256.dp)
        )
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(22.dp)
            )
            Spacer(Modifier.height(12.dp))
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(12.dp)
            )
            Spacer(Modifier.height(8.dp))
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(12.dp)
            )
            Spacer(Modifier.height(24.dp))
            SkeletonBlock(
                modifier = Modifier
                    .width(120.dp)
                    .height(28.dp)
            )
            Spacer(Modifier.height(24.dp))
            SkeletonBlock(
                modifier = Modifier
                    .width(120.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(percent = 50)
            )
        }
    }
}
