package com.nextdoor.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextdoor.app.ui.theme.NaturalBorder
import com.nextdoor.app.ui.theme.Olive50
import com.nextdoor.app.ui.theme.Olive200
import com.nextdoor.app.ui.theme.Olive600
import com.nextdoor.app.ui.theme.Olive700
import com.nextdoor.app.ui.theme.Rose200
import com.nextdoor.app.ui.theme.Rose50
import com.nextdoor.app.ui.theme.Rose700

private val Pill = RoundedCornerShape(percent = 50)

/**
 * Pill-shaped, scale-on-press buttons matching the front-end styling.
 */

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    PressScale(target = 0.95f) { scale, interaction ->
        Box(
            modifier = modifier
                .graphicsLayer { this.scaleX = scale; this.scaleY = scale }
                .background(if (enabled) Olive600 else Olive200, Pill)
                .clickable(enabled = enabled && !loading, interactionSource = interaction, indication = null, onClick = onClick)
                .padding(horizontal = 24.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textColor: Color = Olive700
) {
    PressScale(target = 0.96f) { scale, interaction ->
        Box(
            modifier = modifier
                .graphicsLayer { this.scaleX = scale; this.scaleY = scale }
                .background(Olive50, Pill)
                .border(1.dp, NaturalBorder, Pill)
                .clickable(enabled = enabled, interactionSource = interaction, indication = null, onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (enabled) textColor else com.nextdoor.app.ui.theme.TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    filled: Boolean = false
) {
    PressScale(target = 0.96f) { scale, interaction ->
        val bg = if (filled) Rose700 else Rose50
        val fg = if (filled) Color.White else Rose700
        val borderColor = if (filled) Rose700 else Rose200
        Box(
            modifier = modifier
                .graphicsLayer { this.scaleX = scale; this.scaleY = scale }
                .background(bg, Pill)
                .border(1.dp, borderColor, Pill)
                .clickable(enabled = enabled, interactionSource = interaction, indication = null, onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Rose700
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

/** Fades the scale to [target] while pressed; exposes current scale + interaction source. */
@Composable
private fun PressScale(
    target: Float,
    content: @Composable (scale: Float, interaction: MutableInteractionSource) -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) target else 1f)
    content(scale, interaction)
}
