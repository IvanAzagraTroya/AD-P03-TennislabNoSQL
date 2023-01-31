package koin.common

import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val token: String?,
    val code: Int?,
    val body: String?,
    val type: Type
) {
    enum class Type {
        LOGIN,REGISTER,REQUEST
    }
}