package repositories.pedido

import kotlinx.coroutines.flow.Flow
import models.pedido.Pedido
import models.pedido.PedidoResult

interface IPedidoRepository<ID> {
    suspend fun findAllRealTime(): Flow<List<Pedido>>
    suspend fun findAll(): PedidoResult<List<Pedido>>
    suspend fun findById(id: ID): PedidoResult<Pedido>
    suspend fun save(entity: Pedido): PedidoResult<Pedido>
    suspend fun update(entity: Pedido): PedidoResult<Pedido>
    suspend fun delete(id: ID): PedidoResult<Pedido>
}