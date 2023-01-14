package repositories.producto

import kotlinx.coroutines.flow.Flow
import models.producto.Producto
import models.producto.ProductoResult

interface IProductoRepository<ID> {
    suspend fun findAllRealTime(): Flow<List<Producto>>
    suspend fun findAll(): ProductoResult<List<Producto>>
    suspend fun findById(id: ID): ProductoResult<Producto>
    suspend fun save(entity: Producto): ProductoResult<Producto>
    suspend fun update(entity: Producto): ProductoResult<Producto>
    suspend fun delete(id: ID): ProductoResult<Producto>
    suspend fun decreaseStock(id: ID): ProductoResult<Producto>
}