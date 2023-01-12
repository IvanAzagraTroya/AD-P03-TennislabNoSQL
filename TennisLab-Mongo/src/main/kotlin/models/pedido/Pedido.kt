package models.pedido

import kotlinx.serialization.Serializable
import serializers.LocalDateSerializer
import serializers.UUIDSerializer
import java.time.LocalDate
import java.util.UUID

/**
 * @author Iván Azagra Troya
 * Clase POKO Pedido
 */
@Serializable
data class Pedido(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID(),
    val state: PedidoState,
    @Serializable(with = LocalDateSerializer::class)
    val fechaEntrada: LocalDate,
    @Serializable(with = LocalDateSerializer::class)
    val fechaSalida: LocalDate,
    @Serializable(with = LocalDateSerializer::class)
    val topeEntrega: LocalDate,
    val precio: Double
)