package koin.dto.turno

import koin.dto.maquina.MaquinaDTOcreate
import koin.dto.maquina.MaquinaDTOvisualize
import koin.dto.tarea.TareaDTOcreate
import koin.dto.tarea.TareaDTOvisualize
import koin.dto.user.UserDTOcreate
import koin.dto.user.UserDTOvisualize
import kotlinx.serialization.Serializable
import koin.serializers.LocalDateTimeSerializer
import koin.serializers.UUIDSerializer
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