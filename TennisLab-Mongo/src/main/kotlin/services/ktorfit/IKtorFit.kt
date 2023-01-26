package services.ktorfit

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import dto.user.UserDTOfromAPI

interface IKtorFit {
    @GET("users")
    suspend fun getAll(): List<UserDTOfromAPI>

    @GET("users/{id}")
    suspend fun getById(@Path("id") id: Int): UserDTOfromAPI?
}