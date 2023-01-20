package mappers

import dto.user.UserDTOcreate
import dto.user.UserDTOvisualize
import models.user.User
import services.cipher

fun User.toDTO() =
    UserDTOvisualize (nombre, apellido, email, perfil, activo)

fun UserDTOcreate.fromDTO() = User (
    uuid = uuid,
    nombre = nombre,
    apellido = apellido,
    telefono = telefono,
    email = email,
    password = cipher(password),
    perfil = perfil,
    activo = activo
)

fun toDTO(list: List<User>) : List<UserDTOvisualize> {
    val res = mutableListOf<UserDTOvisualize>()
    list.forEach { res.add(it.toDTO()) }
    return res
}

fun fromDTO(list: List<UserDTOcreate>) : List<User> {
    val res = mutableListOf<User>()
    list.forEach { res.add(it.fromDTO()) }
    return res
}