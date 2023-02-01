package koin.client

import kotlinx.serialization.Serializable

@Serializable
data class ServerResponse<T: Any>(
    val code: Int,
    val data: T? = null,
    val message: String? = null
)
