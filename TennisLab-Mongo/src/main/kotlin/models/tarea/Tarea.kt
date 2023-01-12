package models.tarea

import kotlinx.serialization.Serializable
import serializers.UUIDSerializer
import java.util.UUID

/**
 * @Author Daniel Rodriguez Muñoz
 * Clase POKO de las tareas.
 * La primera parte de los parametros son obligatorios y son la base de las tareas,
 * los parametros opcionales son especificos para los distintos tipos de tarea.
 */
@Serializable
data class Tarea(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    @Serializable(with = UUIDSerializer::class)
    val raquetaId: UUID,
    val precio: Double,
    val tipo: TipoTarea,
    val finalizada: Boolean,

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