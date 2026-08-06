package com.nextdoor.app.data.infra

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import retrofit2.Response

/**
 * Unified result for all repository/data operations.
 *  - [Success] carries the parsed payload.
 *  - [Failure] carries the HTTP code, a human-readable `detail` message and any
 *    per-field validation messages parsed from the {"errors": {...}} envelope.
 */
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Failure(
        val code: Int? = null,
        val message: String? = null,
        val fieldErrors: Map<String, List<String>> = emptyMap()
    ) : ApiResult<Nothing>
}

object ApiErrorParser {
    fun parse(response: Response<*>): ApiResult.Failure {
        val code = response.code()
        var detail: String? = null
        var fieldErrors: Map<String, List<String>> = emptyMap()

        response.errorBody()?.string()?.let { raw ->
            runCatching {
                val envelope = JsonParser.decode(raw)
                envelope.errors.forEach { (key, element) ->
                    when (element) {
                        is JsonArray -> fieldErrors = fieldErrors + (key to element.mapNotNull {
                            (it as? JsonPrimitive)?.contentOrNull
                        })
                        is JsonPrimitive -> {
                            if (key == "detail") detail = element.contentOrNull
                            else fieldErrors = fieldErrors + (key to listOf(element.contentOrNull ?: ""))
                        }
                        else -> Unit
                    }
                }
            }
        }

        return ApiResult.Failure(code = code, message = detail, fieldErrors = fieldErrors)
    }
}

private object JsonParser {
    private val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
    fun decode(raw: String): com.nextdoor.app.data.dto.ErrorEnvelope {
        return json.decodeFromString(com.nextdoor.app.data.dto.ErrorEnvelope.serializer(), raw)
    }
}
