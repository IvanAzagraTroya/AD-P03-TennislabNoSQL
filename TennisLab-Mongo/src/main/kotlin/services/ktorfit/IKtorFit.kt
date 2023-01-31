package services.ktorfit

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import dto.tarea.TareaDTOFromApi
import dto.user.UserDTOfromAPI
import models.tarea.Tarea

interface IKtorFit {
    @GET("users")
    suspend fun getAll(): List<UserDTOfromAPI>

    @GET("users/{id}")
    suspend fun getById(@Path("id") id: Int): UserDTOfromAPI?

    @GET("todos")
    suspend fun getAllTareas(): List<TareaDTOFromApi>

    @POST("todos")
    suspend fun saveTareas(@Body tarea: Tarea): Tarea
}