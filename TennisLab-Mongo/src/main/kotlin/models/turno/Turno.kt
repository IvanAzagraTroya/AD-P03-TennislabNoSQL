package models.turno

import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import serializers.LocalDateTimeSerializer
import serializers.UUIDSerializer
import java.time.LocalDateTime
import java.util.UUID

/**
 * @author Iván Azagra
 * Clase POKO de Turno
 */
@Serializable
data class Turno(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    @Serializable(with = UUIDSerializer::class)
    val workerId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val maquinaId: UUID,
    @Serializable(with = LocalDateTimeSerializer::class)
    val horaInicio: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val horaFin: LocalDateTime,
    val numPedidosActivos: Int,
    @Serializable(with = UUIDSerializer::class)
    val tarea1Id: UUID,
    @Serializable(with = UUIDSerializer::class)
    val tarea2Id: UUID?,
    val hasFinished: Boolean

)
