package mappers

import dto.user.UserDTORegister
import dto.user.UserDTOcreate
import dto.user.UserDTOfromAPI
import dto.user.UserDTOvisualize
import models.user.User
import models.user.UserProfile
import services.utils.cipher

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

fun UserDTORegister.fromDTO() = UserDTOcreate (
    nombre = nombre,
    apellido = apellido,
    telefono = telefono,
    email = email,
    password = password,
    perfil = UserProfile.CLIENT,
    activo = true
)

fun UserDTOfromAPI.toVisualizeDTO() = UserDTOvisualize(
    nombre = name.substringBeforeLast(" "),
    apellido = name.substringAfterLast(" "),
    email = email,
    perfil = UserProfile.CLIENT,
    activo = true
)

fun UserDTOfromAPI.fromDTO() = User(
    nombre = name.substringBeforeLast(" "),
    apellido = name.substringAfterLast(" "),
    telefono = phone,
    email = email,
    password = cipher("password fake"),
    perfil = UserProfile.CLIENT,
    activo = true
)

fun toDTO(list: List<User>) : List<UserDTOvisualize> {
    val res = mutableListOf<UserDTOvisualize>()
    list.forEach { res.add(it.toDTO()) }
    return res
}

fun toDTOfromAPI(list: List<UserDTOfromAPI>) : List<UserDTOvisualize> {
    val res = mutableListOf<UserDTOvisualize>()
    list.forEach { res.add(it.toVisualizeDTO()) }
    return res
}

fun fromDTO(list: List<UserDTOcreate>) : List<User> {
    val res = mutableListOf<User>()
    list.forEach { res.add(it.fromDTO()) }
    return res
}

fun fromAPItoUser(list: List<UserDTOfromAPI>) : List<User> {
    val res = mutableListOf<User>()
    list.forEach { res.add(it.fromDTO()) }
    return res
}