package models.producto

import kotlinx.serialization.Serializable
import serializers.UUIDSerializer
import java.util.UUID

@Serializable
data class Producto(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    val tipo: TipoProducto,
    val marca: String,
    val modelo: String,
    val precio: Double,
    val stock: Int
)