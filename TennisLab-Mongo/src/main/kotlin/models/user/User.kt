package models.user

import kotlinx.serialization.Serializable
import serializers.UUIDSerializer
import java.util.*

@Serializable
data class User(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    val nombre: String,
    val apellido: String,
    val telefono: String,
    val email: String,
    val password: String,
    val perfil: UserProfile,
    val activo: Boolean
)