package com.nextdoor.app.ui.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageBase64 {
    /**
     * Decodes a base64 or data-URL image string ("data:image/png;base64,....")
     * into an [ImageBitmap], or null on failure / blank input.
     */
    suspend fun decode(dataUrl: String?): ImageBitmap? = withContext(Dispatchers.Default) {
        if (dataUrl.isNullOrBlank()) return@withContext null
        runCatching {
            val base64 = if (dataUrl.contains("base64,")) dataUrl.substringAfter("base64,") else dataUrl
            val bytes = Base64.decode(base64.trim(), Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }.getOrNull()
    }

    /** Synchronous variant for previews/tests. */
    fun decodeSync(dataUrl: String?): Bitmap? {
        if (dataUrl.isNullOrBlank()) return null
        return runCatching {
            val base64 = if (dataUrl.contains("base64,")) dataUrl.substringAfter("base64,") else dataUrl
            val bytes = Base64.decode(base64.trim(), Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()
    }
}
