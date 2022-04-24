package com.sonu.openapi.util

import android.util.Log
import retrofit2.Response

sealed class GenericApiResponse<T> {
    class ApiEmptyResponse<T> : GenericApiResponse<T>()
    data class ApiSuccessResponse<T>(val body: T) : GenericApiResponse<T>()
    data class ApiErrorResponse<T>(val errorMessage: String) : GenericApiResponse<T>()

    companion object {
        const val TAG = "AppDebug"

        fun <T> create(error: Throwable): ApiErrorResponse<T> {
            return ApiErrorResponse(error.message ?: "unknown error")
        }

        fun <T> create(response: Response<T>): GenericApiResponse<T> {
            Log.d(TAG, "GenericApiResponse: response: $response")
            Log.d(TAG, "GenericApiResponse: raw: ${response.raw()}")
            Log.d(TAG, "GenericApiResponse: headers: ${response.headers()}")
            Log.d(TAG, "GenericApiResponse: message: ${response.message()}")
            if (response.isSuccessful) {
                val body = response.body()
                return if (body == null || response.code() == 204) {
                    ApiEmptyResponse()
                } else if (response.code() == 401) {
                    ApiErrorResponse("401 Unauthorized. Token may be invalid.")
                } else {
                    ApiSuccessResponse(body = body)
                }
            }
            else{
                val msg =response.errorBody()?.string()
                val errorMsg = if (msg.isNullOrEmpty()) {
                    response.message()
                } else {
                    msg
                }
                return ApiErrorResponse(
                    errorMsg ?: "unknown error"
                )
            }
        }
    }
}
