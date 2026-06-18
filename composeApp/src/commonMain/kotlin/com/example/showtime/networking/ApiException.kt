package com.example.showtime.networking

import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

class ApiException(
    val statusCode: Int?,
    override val message: String,
    cause: Throwable? = null
) : Exception(message, cause)

suspend fun Throwable.toApiException(): Throwable {
    return when (this) {
        is ApiException -> this
        is ClientRequestException -> toApiExceptionFromResponse(response, this)
        is ServerResponseException -> toApiExceptionFromResponse(response, this)
        else -> this
    }
}

private suspend fun toApiExceptionFromResponse(
    response: HttpResponse,
    cause: Throwable
): ApiException {
    val errorBody = runCatching { response.body<ApiErrorResponse>() }.getOrNull()
    val defaultMessage = when (response.status) {
        HttpStatusCode.BadRequest -> "Request is not valid."
        HttpStatusCode.Unauthorized -> "Authentication failed."
        HttpStatusCode.Conflict -> "This action conflicts with existing data."
        HttpStatusCode.NotFound -> "Requested resource was not found."
        else -> "Request failed with status ${response.status.value}."
    }

    return ApiException(
        statusCode = response.status.value,
        message = errorBody?.message ?: defaultMessage,
        cause = cause
    )
}
