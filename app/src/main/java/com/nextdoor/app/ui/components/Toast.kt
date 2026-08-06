package com.nextdoor.app.ui.components

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Lightweight global toast bridge. Screens call [ToastController.show] and the
 * [ToastHost] (placed once in the app root) renders them as system toasts.
 */
object ToastController {
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages = _messages.asSharedFlow()

    fun show(message: String) {
        _messages.tryEmit(message)
    }
}

@Composable
fun ToastHost() {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        ToastController.messages.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
