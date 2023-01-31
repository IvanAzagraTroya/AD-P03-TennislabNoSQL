package koin.dto.pedido

import koin.dto.tarea.TareaDTOcreate
import koin.dto.tarea.TareaDTOvisualize
import koin.dto.user.UserDTOcreate
import koin.dto.user.UserDTOvisualize
import kotlinx.serialization.Serializable
import koin.models.pedido.PedidoState
import koin.serializers.LocalDateSerializer
import koin.serializers.UUIDSerializer
import java.time.LocalDate
import java.util.*

@Serializable
data class PedidoDTOcreate(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    @Serializable(with = UUIDSerializer::class)
    val user: UserDTOcreate,
    val state: PedidoState = PedidoState.PROCESO,
    @Serializable(with = LocalDateSerializer::class)
    val fechaEntrada: LocalDate = LocalDate.now(),
    @Serializable(with = LocalDateSerializer::class)
    val fechaSalida: LocalDate = fechaEntrada,
    @Serializable(with = LocalDateSerializer::class)
    val topeEntrega: LocalDate = fechaSalida.plusMonths(1L),
    val tareas: List<TareaDTOcreate> = listOf()
)

@Serializable
data class PedidoDTOvisualize(
    val user: UserDTOvisualize?,
    val state: PedidoState,
    @Serializable(with = LocalDateSerializer::class)
    val fechaEntrada: LocalDate,
    @Serializable(with = LocalDateSerializer::class)
    val fechaSalida: LocalDate,
    @Serializable(with = LocalDateSerializer::class)
    val topeEntrega: LocalDate,
    val tareas: List<TareaDTOvisualize>,
    val precio: Double
)