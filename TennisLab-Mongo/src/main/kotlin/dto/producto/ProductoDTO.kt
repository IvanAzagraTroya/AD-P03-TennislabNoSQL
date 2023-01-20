package dto.producto

import kotlinx.serialization.Serializable
import models.producto.TipoProducto
import serializers.UUIDSerializer
import java.util.*

@Serializable
data class ProductoDTOcreate(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    val tipo: TipoProducto,
    val marca: String,
    val modelo: String,
    var precio: Double,
    val stock: Int = 0
)

@Serializable
data class ProductoDTOvisualize(
    val tipo: TipoProducto,
    val marca: String,
    val modelo: String,
    var precio: Double,
    val stock: Int
)