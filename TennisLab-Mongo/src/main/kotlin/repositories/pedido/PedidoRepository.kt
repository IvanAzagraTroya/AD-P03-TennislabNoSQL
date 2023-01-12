package repositories.pedido

import kotlinx.coroutines.flow.Flow
import models.pedido.Pedido
import models.producto.Producto

interface PedidoRepository<ID> {
    suspend fun findAllAsFlow(): Flow<List<Pedido>>
    suspend fun findById(id: ID): Producto?
    suspend fun save(entity: Producto): Producto?
    suspend fun delete(entity: Producto): Boolean
}