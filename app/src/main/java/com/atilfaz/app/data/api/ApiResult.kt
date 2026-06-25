package com.atilfaz.app.data.api

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()

    val isSuccess get() = this is Success
    val isError get() = this is Error
    val isLoading get() = this is Loading

    fun getOrNull(): T? = if (this is Success) data else null

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw Exception(message)
        is Loading -> throw IllegalStateException("Still loading")
    }

    inline fun onSuccess(block: (T) -> Unit): ApiResult<T> {
        if (this is Success) block(data)
        return this
    }

    inline fun onError(block: (String, Int?) -> Unit): ApiResult<T> {
        if (this is Error) block(message, code)
        return this
    }

    inline fun <R> map(transform: (T) -> R): ApiResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> Loading
    }
}

suspend fun <T> safeApiCall(call: suspend () -> T): ApiResult<T> = try {
    ApiResult.Success(call())
} catch (e: retrofit2.HttpException) {
    ApiResult.Error(
        message = e.response()?.errorBody()?.string() ?: e.message(),
        code = e.code()
    )
} catch (e: java.io.IOException) {
    ApiResult.Error(message = "Network error: ${e.localizedMessage ?: "Unknown network error"}")
} catch (e: Exception) {
    ApiResult.Error(message = e.localizedMessage ?: "Unknown error")
}
