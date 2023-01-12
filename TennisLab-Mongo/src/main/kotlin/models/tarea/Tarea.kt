package models.tarea

import kotlinx.serialization.Serializable
import serializers.UUIDSerializer
import java.util.UUID

@Serializable
data class Tarea(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    @Serializable(with = UUIDSerializer::class)
    val raquetaId: UUID,
    val precio: Double,
    val tipo: TipoTarea,
    // esto es para adquisiciones
    @Serializable(with = UUIDSerializer::class)
    val productoAdquiridoId: UUID?,
    // esto es para personalizaciones
    val peso: Int?,
    val balance: Double?,
    val rigidez: Int?,
    // esto es para encordados
    val tensionHorizontal: Double?,
    @Serializable(with = UUIDSerializer::class)
    val cordajeHorizontalId: UUID?,
    val tensionVertical: Double?,
    @Serializable(with = UUIDSerializer::class)
    val cordajeVerticalId: UUID?,
    val dosNudos: Boolean?
)