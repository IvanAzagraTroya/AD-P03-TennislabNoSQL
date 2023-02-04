package com.example.tennislabspringboot.dto.user

import com.example.tennislabspringboot.models.user.UserProfile
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*

data class UserDTOcreate(
    val uuid: UUID = UUID.randomUUID(),
    val nombre: String,
    val apellido: String,
    val telefono: String,
    val email: String,
    val password: String,
    val perfil: UserProfile = UserProfile.CLIENT,
    val activo: Boolean = true
)

data class UserDTOvisualize(
    val nombre: String,
    val apellido: String,
    val email: String,
    val perfil: UserProfile,
    val activo: Boolean
)

data class UserDTOvisualizeList(val users: List<UserDTOvisualize>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserDTOfromAPI(
    val name: String,
    val email: String,
    val phone: String
)

data class UserDTOLogin(
    val email: String,
    val password: String
)

data class UserDTORegister(
    val nombre: String,
    val apellido: String,
    val telefono: String,
    val email: String,
    val password: String
)