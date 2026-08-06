package com.nextdoor.app.ui.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nextdoor.app.data.dto.ProductDto
import com.nextdoor.app.data.dto.StoreDto
import com.nextdoor.app.ui.components.BackTopBar
import com.nextdoor.app.ui.components.Base64Image
import com.nextdoor.app.ui.components.DangerButton
import com.nextdoor.app.ui.components.EmptyState
import com.nextdoor.app.ui.components.ErrorState
import com.nextdoor.app.ui.components.GhostButton
import com.nextdoor.app.ui.components.OpenPill
import com.nextdoor.app.ui.components.ProductCard
import com.nextdoor.app.ui.components.SkeletonBlock
import com.nextdoor.app.ui.theme.NaturalBg
import com.nextdoor.app.ui.theme.NaturalBorder
import com.nextdoor.app.ui.theme.Olive50
import com.nextdoor.app.ui.theme.Olive600
import com.nextdoor.app.ui.theme.SerifFamily
import com.nextdoor.app.ui.theme.TextMuted
import com.nextdoor.app.ui.theme.TextPrimary
import com.nextdoor.app.ui.util.categoryLabel

@Composable
fun StoreScreen(
    storeId: String,
    onBack: () -> Unit,
    onProductClick: (String) -> Unit,
    onCartClick: () -> Unit
) {
    val viewModel: StoreViewModel = hiltViewModel()
    val state = viewModel.uiState
    val badgeCount by viewModel.badgeCount.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()

    LaunchedEffect(storeId) {
        viewModel.load(storeId)
    }

    Scaffold(
        containerColor = NaturalBg,
        topBar = {
            BackTopBar(
                title = "Loja",
                onBack = onBack,
                onCartClick = onCartClick,
                badgeCount = badgeCount,
                centerLabel = true
            )
        }
    ) { padding ->
        when (val s = state) {
            is StoreUiState.Loading -> StoreSkeleton(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
            is StoreUiState.Error -> ErrorState(
                title = s.message,
                onRetry = { viewModel.load(storeId) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(top = 40.dp)
            )
            is StoreUiState.Content -> StoreContent(
                store = s.store,
                products = s.products,
                productsFailed = s.productsFailed,
                cartItems = cartItems,
                onProductClick = onProductClick,
                onAdd = { viewModel.addToCart(storeId, it) },
                onRetryProducts = { viewModel.retryProducts(storeId) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
    }

    if (viewModel.showClearDialog) {
        ClearCartDialog(
            onConfirm = { viewModel.confirmClearAndReplace(storeId) },
            onDismiss = viewModel::dismissDialog
        )
    }
}

@Composable
private fun StoreContent(
    store: StoreDto,
    products: List<ProductDto>,
    productsFailed: Boolean,
    cartItems: Map<ProductDto, Int>,
    onProductClick: (String) -> Unit,
    onAdd: (ProductDto) -> Unit,
    onRetryProducts: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            StoreHeaderCard(store = store)
        }
        item {
            Text(
                text = "Produtos".uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        if (productsFailed) {
            item {
                ErrorState(
                    title = "Não foi possível carregar os produtos",
                    onRetry = onRetryProducts
                )
            }
        } else if (products.isEmpty()) {
            item {
                EmptyState(
                    title = "Nenhum produto disponível",
                    hint = "Esta loja ainda não cadastrou produtos."
                )
            }
        } else {
            items(products, key = { it.id }) { product ->
                val inCart = cartItems[product] ?: 0
                ProductCard(
                    product = product,
                    onAdd = { onAdd(product) },
                    addEnabled = inCart < product.quantity,
                    onClick = { onProductClick(product.id) }
                )
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun StoreHeaderCard(store: StoreDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, NaturalBorder, RoundedCornerShape(24.dp))
    ) {
        Base64Image(
            dataUrl = store.image,
            fallbackInitial = store.name.firstOrNull()?.toString(),
            modifier = Modifier
                .fillMaxWidth()
                .height(144.dp)
        )
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = store.name,
                fontFamily = SerifFamily,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextPrimary
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .background(Olive50, RoundedCornerShape(percent = 50))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = categoryLabel(store.category),
                        color = Olive600,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                OpenPill()
            }
            if (store.telephone.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = store.telephone,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun StoreSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(24.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Produtos".uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp,
            color = TextMuted,
            modifier = Modifier.padding(top = 12.dp)
        )
        Spacer(Modifier.height(12.dp))
        repeat(4) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .border(1.dp, NaturalBorder, RoundedCornerShape(24.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SkeletonBlock(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(16.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    SkeletonBlock(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    SkeletonBlock(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(11.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    SkeletonBlock(
                        modifier = Modifier
                            .width(52.dp)
                            .height(14.dp)
                    )
                }
                SkeletonBlock(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(percent = 50)
                )
            }
        }
    }
}

@Composable
private fun ClearCartDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Limpar carrinho?", fontWeight = FontWeight.Bold) },
        text = {
            Text("Seu carrinho tem itens de outra loja. Deseja limpá-lo para adicionar este produto?")
        },
        confirmButton = {
            DangerButton(text = "Limpar e adicionar", onClick = onConfirm)
        },
        dismissButton = {
            GhostButton(text = "Cancelar", onClick = onDismiss)
        }
    )
}
