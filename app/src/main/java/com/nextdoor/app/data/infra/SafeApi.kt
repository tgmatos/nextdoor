package com.nextdoor.app.data.infra

import java.io.IOException
import retrofit2.HttpException

/**
 * Executes a Retrofit suspend call and normalizes the outcome into [ApiResult].
 */
suspend fun <T> safeApiResult(block: suspend () -> T): ApiResult<T> = try {
    ApiResult.Success(block())
} catch (e: HttpException) {
    e.response()?.let { ApiErrorParser.parse(it) }
        ?: ApiResult.Failure(message = "HTTP error ${e.code()}")
} catch (e: IOException) {
    ApiResult.Failure(message = "Sem conexão com o servidor. Verifique sua internet.")
} catch (e: Exception) {
    ApiResult.Failure(message = "Algo deu errado. Tente novamente.")
}

fun <T> ApiResult<T>.getOrNull(): T? = (this as? ApiResult.Success)?.data
