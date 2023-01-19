package repositories.pedido

import kotlinx.coroutines.flow.Flow
import models.pedido.Pedido
import java.util.UUID

interface IPedidoRepository<ID> {
    suspend fun findAllRealTime(): Flow<List<Pedido>>
    fun findAll(): Flow<Pedido>
    suspend fun findById(id: ID): Pedido?
    suspend fun findByUUID(id: UUID): Pedido?
    suspend fun save(entity: Pedido): Pedido
    suspend fun delete(id: ID): Pedido?
}