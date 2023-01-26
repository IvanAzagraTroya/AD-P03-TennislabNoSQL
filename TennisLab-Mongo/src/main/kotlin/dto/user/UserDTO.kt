package dto.user

import kotlinx.serialization.Serializable
import models.user.UserProfile
import serializers.UUIDSerializer
import java.util.*

@Serializable
data class UserDTOcreate(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    val nombre: String,
    val apellido: String,
    val telefono: String,
    val email: String,
    val password: String,
    val perfil: UserProfile = UserProfile.CLIENT,
    val activo: Boolean = true
)

@Serializable
data class UserDTOvisualize(
    val nombre: String,
    val apellido: String,
    val email: String,
    val perfil: UserProfile,
    val activo: Boolean
)

@Serializable
data class UserDTOfromAPI(
    val name: String,
    val email: String,
    val phone: String
)