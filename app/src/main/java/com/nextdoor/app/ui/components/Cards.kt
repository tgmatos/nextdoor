package com.nextdoor.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextdoor.app.data.dto.ProductDto
import com.nextdoor.app.data.dto.StoreDto
import com.nextdoor.app.ui.theme.Emerald50
import com.nextdoor.app.ui.theme.Emerald700
import com.nextdoor.app.ui.theme.NaturalBorder
import com.nextdoor.app.ui.theme.Olive600
import com.nextdoor.app.ui.theme.TextMuted
import com.nextdoor.app.ui.theme.TextPrimary
import com.nextdoor.app.ui.util.categoryLabel
import com.nextdoor.app.ui.util.toBRL

@Composable
fun StoreCard(
    store: StoreDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, NaturalBorder, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Base64Image(
            dataUrl = store.image,
            fallbackInitial = store.name.firstOrNull()?.toString(),
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = store.name,
                fontFamily = com.nextdoor.app.ui.theme.SerifFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer4()
            Text(
                text = listOf(store.description, categoryLabel(store.category))
                    .filter { it.isNotBlank() }
                    .joinToString(" · "),
                fontSize = 11.sp,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer8()
            Row(verticalAlignment = Alignment.CenterVertically) {
                OpenPill()
            }
        }
    }
}

@Composable
fun ProductCard(
    product: ProductDto,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
    addEnabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(24.dp)
    val baseModifier = modifier
        .fillMaxWidth()
        .clip(shape)
        .background(Color.White)
        .border(1.dp, NaturalBorder, shape)
        .padding(16.dp)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    Row(
        modifier = baseModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Base64Image(
            dataUrl = product.image,
            fallbackInitial = product.name.firstOrNull()?.toString(),
            modifier = Modifier
                .size(64.dp)
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
            Spacer4()
            Text(
                text = product.description,
                fontSize = 11.sp,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer8()
            Text(
                text = product.price.toBRL(),
                fontFamily = com.nextdoor.app.ui.theme.SerifFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Olive600
            )
        }
        IconButtonCircle(
            enabled = addEnabled,
            onClick = onAdd
        )
    }
}

@Composable
private fun IconButtonCircle(enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (enabled) Olive600 else com.nextdoor.app.ui.theme.Olive200)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Adicionar",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun OpenPill(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Emerald50, RoundedCornerShape(percent = 50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(Emerald700, CircleShape)
        )
        Text(text = "Aberto", color = Emerald700, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Spacer4() = Box(modifier = Modifier.padding(top = 4.dp))
@Composable
private fun Spacer8() = Box(modifier = Modifier.padding(top = 8.dp))
