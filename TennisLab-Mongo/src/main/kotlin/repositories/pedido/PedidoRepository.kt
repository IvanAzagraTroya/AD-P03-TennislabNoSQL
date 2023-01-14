package repositories.pedido

import kotlinx.coroutines.flow.Flow
import models.pedido.Pedido
import models.pedido.PedidoResult
import org.litote.kmongo.Id

class PedidoRepository: IPedidoRepository<Id<Pedido>> {
    override suspend fun findAllRealTime(): Flow<List<Pedido>> {
        TODO("Not yet implemented")
    }

    override suspend fun findAll(): PedidoResult<List<Pedido>> {
        TODO("Not yet implemented")
    }

    override suspend fun save(entity: Pedido): PedidoResult<Pedido> {
        TODO("Not yet implemented")
    }

    override suspend fun update(entity: Pedido): PedidoResult<Pedido> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: Id<Pedido>): PedidoResult<Pedido> {
        TODO("Not yet implemented")
    }

    override suspend fun findById(id: Id<Pedido>): PedidoResult<Pedido> {
        TODO("Not yet implemented")
    }
}