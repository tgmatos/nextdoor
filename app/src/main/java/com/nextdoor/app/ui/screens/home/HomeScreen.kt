package com.nextdoor.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nextdoor.app.data.dto.StoreDto
import com.nextdoor.app.ui.components.EmptyState
import com.nextdoor.app.ui.components.ErrorState
import com.nextdoor.app.ui.components.HomeTopBar
import com.nextdoor.app.ui.components.SearchBar
import com.nextdoor.app.ui.components.SkeletonBlock
import com.nextdoor.app.ui.components.StoreCard
import com.nextdoor.app.ui.theme.NaturalBg
import com.nextdoor.app.ui.theme.NaturalBorder
import com.nextdoor.app.ui.theme.TextMuted

@Composable
fun HomeScreen(
    onStoreClick: (String) -> Unit,
    onCartClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val state = viewModel.uiState
    val badgeCount by viewModel.badgeCount.collectAsStateWithLifecycle()

    // Re-fetch stores whenever this screen resumes (returning to the Home tab or
    // back-navigation) so newly added stores appear without restarting the app.
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    Scaffold(
        containerColor = NaturalBg,
        topBar = {
            HomeTopBar(
                title = "NextDoor",
                onCartClick = onCartClick,
                badgeCount = badgeCount
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Lojas perto de você".uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                color = TextMuted
            )
            Spacer(Modifier.height(12.dp))

            when (val s = state) {
                is HomeUiState.Loading -> HomeSkeleton()
                is HomeUiState.Error -> ErrorBody(message = s.message, onRetry = viewModel::load)
                is HomeUiState.Content -> ContentBody(
                    stores = s.stores,
                    onStoreClick = onStoreClick,
                    onSearchClick = onSearchClick
                )
            }
        }
    }
}

@Composable
private fun ContentBody(
    stores: List<StoreDto>,
    onStoreClick: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    SearchBar(
        placeholder = "Buscar lojas e produtos",
        onClick = onSearchClick,
        readOnly = true
    )
    Spacer(Modifier.height(12.dp))

    if (stores.isEmpty()) {
        EmptyState(
            title = "Nenhuma loja por aqui",
            hint = "Volte mais tarde para ver novidades do seu bairro.",
            modifier = Modifier.padding(top = 24.dp)
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(stores, key = { it.id }) { store ->
                StoreCard(
                    store = store,
                    onClick = { onStoreClick(store.id) }
                )
            }
        }
    }
}

@Composable
private fun ErrorBody(message: String, onRetry: () -> Unit) {
    SearchBar(placeholder = "Buscar lojas e produtos", readOnly = true)
    Spacer(Modifier.height(12.dp))
    ErrorState(title = message, onRetry = onRetry, modifier = Modifier.padding(top = 24.dp))
}

@Composable
private fun HomeSkeleton() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(percent = 50)
            )
        }
        items(3) {
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
                            .fillMaxWidth(0.6f)
                            .height(14.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    SkeletonBlock(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(11.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    SkeletonBlock(
                        modifier = Modifier
                            .width(56.dp)
                            .height(22.dp),
                        shape = RoundedCornerShape(percent = 50)
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}
