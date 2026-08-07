package com.nextdoor.app.data.repository

import android.util.Base64
import com.nextdoor.app.BuildConfig
import com.nextdoor.app.data.dto.OrderUpdateDto
import com.nextdoor.app.data.infra.PhoenixChannelClient
import com.nextdoor.app.data.infra.TokenStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Connects the customer to the Phoenix channel `account:order:<account_id>` and surfaces
 * `order_updated` pushes as a [updates] flow.
 *
 * The socket lifecycle is driven by [TokenStore.tokenFlow]: it connects whenever a JWT is
 * present and disconnects on logout / session expiry. On transport failure it reconnects with
 * capped exponential backoff; join rejections (unauthorized) stop retries until the token changes.
 */
@Singleton
class OrderUpdatesRepository @Inject constructor(
    okHttpClient: okhttp3.OkHttpClient,
    private val tokenStore: TokenStore,
    private val json: Json
) {
    private val client = PhoenixChannelClient(okHttpClient, json)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _updates = MutableSharedFlow<OrderUpdateDto>(extraBufferCapacity = 16)
    val updates: SharedFlow<OrderUpdateDto> = _updates.asSharedFlow()

    private val events = Channel<Event>(Channel.UNLIMITED)

    private var reconnectJob: Job? = null
    private var currentToken: String? = null
    private var currentTopic: String? = null
    private var retryBackoffMs = 0L

    private sealed interface Event {
        data class Arrived(val name: String, val payload: JsonObject) : Event
        object Closed : Event
        object JoinError : Event
    }

    init {
        scope.launch { consumeEvents() }
        scope.launch {
            tokenStore.tokenFlow
                .distinctUntilChanged()
                .collect { token ->
                    if (token.isNullOrBlank()) {
                        stop()
                    } else {
                        start(token)
                    }
                }
        }
    }

    private suspend fun consumeEvents() {
        for (event in events) {
            when (event) {
                is Event.Arrived -> handleEvent(event.name, event.payload)
                Event.Closed -> scheduleReconnect()
                Event.JoinError -> currentToken = null
            }
        }
    }

    private fun start(token: String) {
        currentToken = token
        retryBackoffMs = 0L
        connect()
    }

    private fun stop() {
        currentToken = null
        currentTopic = null
        reconnectJob?.cancel()
        reconnectJob = null
        client.disconnect()
    }

    private fun connect() {
        val token = currentToken ?: return
        val sub = decodeSub(token) ?: return
        currentTopic = "account:order:$sub"
        val url = wsUrl(token)
        client.connect(
            url = url,
            topic = currentTopic!!,
            listener = listener
        )
    }

    private fun scheduleReconnect() {
        if (currentToken == null) return
        val delayMs = if (retryBackoffMs == 0L) MIN_BACKOFF_MS else minOf(retryBackoffMs * 2, MAX_BACKOFF_MS)
        retryBackoffMs = delayMs
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(delayMs)
            if (currentToken != null) connect()
        }
    }

    private val listener = object : PhoenixChannelClient.Listener {
        override fun onConnected() {
            retryBackoffMs = 0L
        }

        override fun onEvent(event: String, payload: JsonObject) {
            events.trySend(Event.Arrived(event, payload))
        }

        override fun onJoinError() {
            events.trySend(Event.JoinError)
        }

        override fun onClosed() {
            events.trySend(Event.Closed)
        }
    }

    private suspend fun handleEvent(name: String, payload: JsonObject) {
        if (name != "order_updated") return
        runCatching { json.decodeFromJsonElement(OrderUpdateDto.serializer(), payload) }
            .onSuccess { _updates.emit(it) }
    }

    private fun wsUrl(token: String): String {
        val wsBase = BuildConfig.BASE_URL
            .replaceFirst("http://", "ws://")
            .replaceFirst("https://", "wss://")
        return "${wsBase.trimEnd('/')}/socket?token=$token"
    }

    private fun decodeSub(token: String): String? {
        val payload = token.split(".").getOrNull(1) ?: return null
        return runCatching {
            val decoded = Base64.decode(payload, Base64.URL_SAFE).toString(Charsets.UTF_8)
            json.parseToJsonElement(decoded).jsonObject["sub"]?.jsonPrimitive?.contentOrNull
        }.getOrNull()
    }

    private companion object {
        const val MIN_BACKOFF_MS = 1_000L
        const val MAX_BACKOFF_MS = 15_000L
    }
}
