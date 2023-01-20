package dto.turno

import dto.maquina.MaquinaDTOcreate
import dto.maquina.MaquinaDTOvisualize
import dto.tarea.TareaDTOcreate
import dto.tarea.TareaDTOvisualize
import dto.user.UserDTOcreate
import dto.user.UserDTOvisualize
import kotlinx.serialization.Serializable
import serializers.LocalDateTimeSerializer
import serializers.UUIDSerializer
import java.time.LocalDateTime
import java.util.*

@Serializable
data class TurnoDTOcreate(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    val worker: UserDTOcreate,
    val maquina: MaquinaDTOcreate,
    @Serializable(with = LocalDateTimeSerializer::class)
    val horaInicio: LocalDateTime = LocalDateTime.now(),
    @Serializable(with = LocalDateTimeSerializer::class)
    val horaFin: LocalDateTime = horaInicio,
    val tarea1: TareaDTOcreate,
    val tarea2: TareaDTOcreate?,
    val finalizado: Boolean = false
)

@Serializable
data class TurnoDTOvisualize(
    val worker: UserDTOvisualize?,
    val maquina: MaquinaDTOvisualize?,
    @Serializable(with = LocalDateTimeSerializer::class)
    val horaInicio: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val horaFin: LocalDateTime,
    val numPedidosActivos: Int,
    val tarea1: TareaDTOvisualize?,
    val tarea2: TareaDTOvisualize?,
    val finalizado: Boolean
)