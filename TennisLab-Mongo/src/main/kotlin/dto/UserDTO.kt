package dto

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import models.user.UserProfile
import java.util.*

/**
 * @author Iván Azagra Troya
 *
 * Clase DTO de User que usada para el paso de datos
 * utiliza las funciones para convertirlo a json o sacar la clase de json
 * y un método toString
 * @param id
 * @param nombre
 * @param apellido
 * @param telefono
 * @param email
 * @param password
 * @param perfil
 */
class UserDTO() {
    lateinit var id: UUID
    lateinit var nombre: String
    lateinit var apellido: String
    lateinit var telefono: String
    lateinit var email: String
//    todo: estos dos podrían no necesitarse para la visualización
    lateinit var password: String
    lateinit var perfil: UserProfile

    constructor(
        id: UUID?,
        nombre: String,
        apellido: String,
        telefono: String,
        email: String,
        password: String,
        perfil: UserProfile
    ) :this() {
        this.id = id ?: UUID.randomUUID()
        this.nombre = nombre
        this.apellido = apellido
        this.telefono = telefono
        this.email = email
        this.password = password
        this.perfil = perfil
    }

    fun fromJSON(json: String): UserDTO? {
        return Gson().fromJson(json, UserDTO::class.java)
    }

    fun toJSON(): String {
        return GsonBuilder().setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .create().toJson(this)
    }

    override fun toString(): String {
        return GsonBuilder().setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .create().toJson(this)
    }
}