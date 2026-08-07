package com.nextdoor.app.data.infra

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

/**
 * Minimal Phoenix Channels (v2 JSON serializer) client over OkHttp WebSocket.
 *
 * Phoenix transport frames are JSON arrays: `[join_ref, ref, topic, event, payload]`.
 * This client connects, joins a single [topic] and surfaces pushed events to a [Listener].
 *
 * Reconnect/heartbeat scheduling is intentionally left to a scope owned by this client
 * (heartbeat) while transport failure notifications are reported via [Listener.onClosed]
 * so the caller can decide when/how to reconnect.
 */
@Singleton
class PhoenixChannelClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json
) {
    interface Listener {
        /** The socket connected and the join was sent (not yet acknowledged). */
        fun onConnected()

        /** A pushed event arrived for the joined topic. */
        fun onEvent(event: String, payload: JsonObject)

        /** The join was rejected by the server (e.g. unauthorized). */
        fun onJoinError()

        /** The socket closed or failed unexpectedly. */
        fun onClosed()
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val ref = AtomicInteger(0)

    private var webSocket: WebSocket? = null
    private var heartbeatJob: Job? = null
    private var topic: String? = null
    private var listener: Listener? = null
    private var explicitlyClosed = false

    /** Connects to [url] and joins [topic]. A previous connection (if any) is closed first. */
    @Synchronized
    fun connect(url: String, topic: String, listener: Listener) {
        closeNoNotify()
        explicitlyClosed = false
        this.topic = topic
        this.listener = listener

        val request = Request.Builder().url(url).build()
        webSocket = okHttpClient.newWebSocket(request, socketListener)
    }

    /** Closes the connection without invoking [Listener.onClosed]. */
    @Synchronized
    fun disconnect() {
        closeNoNotify()
    }

    private fun closeNoNotify() {
        explicitlyClosed = true
        heartbeatJob?.cancel()
        heartbeatJob = null
        webSocket?.close(1000, "client disconnect")
        webSocket = null
        topic = null
        listener = null
    }

    private val socketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            val topic = this@PhoenixChannelClient.topic
            if (topic == null) {
                webSocket.close(1000, "no topic")
                return
            }
            val joinRef = ref.incrementAndGet()
            webSocket.send(buildMessage(joinRef, joinRef, topic, "phx_join", JsonObject(emptyMap())))
            startHeartbeat(webSocket)
            listener?.onConnected()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            handleMessage(text)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            if (!explicitlyClosed) listener?.onClosed()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            if (!explicitlyClosed) listener?.onClosed()
        }
    }

    private fun handleMessage(text: String) {
        val frame = try {
            json.parseToJsonElement(text).jsonArray
        } catch (_: Exception) {
            return
        }
        val event = frame.getOrNull(3)?.jsonPrimitive?.contentOrNull ?: return

        when (event) {
            "phx_reply" -> {
                val payload = frame.getOrNull(4)?.jsonObject
                val status = payload?.get("status")?.jsonPrimitive?.contentOrNull
                if (status != "ok") listener?.onJoinError()
            }

            "phx_error", "phx_close" -> Unit

            else -> {
                val payload = frame.getOrNull(4)?.jsonObject ?: return
                listener?.onEvent(event, payload)
            }
        }
    }

    private fun startHeartbeat(webSocket: WebSocket) {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (isActive) {
                delay(HEARTBEAT_INTERVAL_MS)
                if (!explicitlyClosed) {
                    webSocket.send(
                        buildMessage(0, ref.incrementAndGet(), "phoenix", "heartbeat", JsonObject(emptyMap()))
                    )
                }
            }
        }
    }

    private fun buildMessage(
        joinRef: Int,
        ref: Int,
        topic: String,
        event: String,
        payload: JsonObject
    ): String {
        val joinRefElement = if (joinRef <= 0) JsonNull else JsonPrimitive(joinRef.toString())
        val frame = JsonArray(
            listOf(
                joinRefElement,
                JsonPrimitive(ref.toString()),
                JsonPrimitive(topic),
                JsonPrimitive(event),
                payload
            )
        )
        return frame.toString()
    }

    private companion object {
        const val HEARTBEAT_INTERVAL_MS = 30_000L
    }
}
