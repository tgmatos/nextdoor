package com.nextdoor.app.ui.screens.search

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nextdoor.app.data.dto.StoreDto
import com.nextdoor.app.data.repository.SearchProductHit
import com.nextdoor.app.ui.components.Base64Image
import com.nextdoor.app.ui.components.EmptyState
import com.nextdoor.app.ui.components.PillTextField
import com.nextdoor.app.ui.components.SkeletonBlock
import com.nextdoor.app.ui.theme.NaturalBg
import com.nextdoor.app.ui.theme.NaturalBorder
import com.nextdoor.app.ui.theme.NaturalDivider
import com.nextdoor.app.ui.theme.Olive50
import com.nextdoor.app.ui.theme.Olive600
import com.nextdoor.app.ui.theme.SerifFamily
import com.nextdoor.app.ui.theme.TextMuted
import com.nextdoor.app.ui.theme.TextPrimary
import com.nextdoor.app.ui.theme.TextStyles
import com.nextdoor.app.ui.util.categoryLabel
import com.nextdoor.app.ui.util.toBRL

@Composable
fun SearchScreen(
    initialQuery: String,
    onBack: () -> Unit,
    onStoreClick: (String) -> Unit,
    onProductClick: (String, String) -> Unit
) {
    val viewModel: SearchViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(initialQuery) {
        viewModel.initialize(initialQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NaturalBg)
    ) {
        SearchInput(
            query = state.query,
            onQueryChange = viewModel::onQueryChange,
            onClear = viewModel::clearQuery
        )
        HorizontalDivider(color = NaturalDivider)

        when {
            state.loading -> SearchSkeleton()

            // Idle: blank or <2 chars query -> nothing rendered.
            state.query.trim().length < 2 -> Unit

            // Empty result search.
            state.searched && state.stores.isEmpty() && state.products.isEmpty() -> EmptyState(
                title = "Nenhum resultado encontrado",
                hint = "Tente buscar por outro termo ou explore as lojas próximas a você.",
                icon = Icons.Default.Search
            )

            else -> SearchResults(
                stores = state.stores,
                products = state.products,
                onStoreClick = onStoreClick,
                onProductClick = onProductClick
            )
        }
    }
}

@Composable
private fun SearchInput(query: String, onQueryChange: (String) -> Unit, onClear: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(NaturalBg)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        PillTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = "Buscar lojas e produtos",
            leadingIcon = Icons.Default.Search,
            trailingIcon = if (query.isNotBlank()) {
                {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clickable(onClick = onClear),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpar busca",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else null,
            imeAction = ImeAction.Search,
            singleLine = true
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
}

@Composable
private fun SearchResults(
    stores: List<StoreDto>,
    products: List<SearchProductHit>,
    onStoreClick: (String) -> Unit,
    onProductClick: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (stores.isNotEmpty()) {
            item { SectionEyebrow(text = "Lojas") }
            items(stores, key = { it.id }) { store ->
                StoreRow(store = store, onClick = { onStoreClick(store.id) })
                Spacer(Modifier.height(8.dp))
            }
        }
        if (products.isNotEmpty()) {
            item { SectionEyebrow(text = "Produtos") }
            items(products, key = { it.product.id }) { hit ->
                ProductRow(hit = hit, onClick = { onProductClick(hit.storeId, hit.product.id) })
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SectionEyebrow(text: String) {
    Text(text = text, style = TextStyles.Eyebrow, color = TextMuted)
}

@Composable
private fun StoreRow(store: StoreDto, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, NaturalBorder, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Base64Image(
            dataUrl = store.image,
            fallbackInitial = store.name.firstOrNull()?.toString(),
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = store.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = categoryLabel(store.category),
                fontSize = 10.sp,
                color = TextMuted
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun ProductRow(hit: SearchProductHit, onClick: () -> Unit) {
    val product = hit.product
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, NaturalBorder, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Base64Image(
            dataUrl = product.image,
            fallbackInitial = product.name.firstOrNull()?.toString(),
            modifier = Modifier
                .size(44.dp)
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
                text = hit.storeName,
                fontSize = 10.sp,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = product.price.toBRL(),
            fontFamily = SerifFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Olive600
        )
    }
}

@Composable
private fun SearchSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, NaturalBorder, RoundedCornerShape(24.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBlock(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SkeletonBlock(modifier = Modifier.width(160.dp).height(12.dp), shape = RoundedCornerShape(6.dp))
                    SkeletonBlock(modifier = Modifier.width(100.dp).height(10.dp), shape = RoundedCornerShape(5.dp))
                }
            }
        }
    }
}
