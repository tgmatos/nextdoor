package com.nextdoor.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nextdoor.app.ui.theme.Olive50
import com.nextdoor.app.ui.theme.Olive600
import com.nextdoor.app.ui.theme.SerifFamily
import com.nextdoor.app.ui.util.ImageBase64

/**
 * Renders a base64/data-URL image, decoding off the main thread. Shows an
 * Olive50 block with a serif initial as a stable placeholder until loaded or
 * on decode failure.
 */
@Composable
fun Base64Image(
    dataUrl: String?,
    modifier: Modifier = Modifier,
    fallbackInitial: String? = null,
    contentScale: ContentScale = ContentScale.Crop
) {
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(dataUrl) {
        bitmap = ImageBase64.decode(dataUrl)
    }

    val current = bitmap
    if (current != null) {
        Image(
            bitmap = current,
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(
            modifier = modifier.background(Olive50),
            contentAlignment = Alignment.Center
        ) {
            val initial = fallbackInitial?.take(1)?.uppercase().orEmpty()
            if (initial.isNotEmpty()) {
                Text(
                    text = initial,
                    color = Olive600,
                    fontFamily = SerifFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }
    }
}
