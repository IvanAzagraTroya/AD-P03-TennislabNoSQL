package repositories.producto

import kotlinx.coroutines.flow.Flow
import models.producto.Producto

interface IProductoRepository<ID> {
    suspend fun findAllRealTime(): Flow<List<Producto>>
    fun findAll(): Flow<Producto>
    suspend fun findById(id: ID): Producto?
    suspend fun save(entity: Producto): Producto?
    suspend fun delete(id: ID): Producto?
    suspend fun decreaseStock(id: ID): Producto?
}