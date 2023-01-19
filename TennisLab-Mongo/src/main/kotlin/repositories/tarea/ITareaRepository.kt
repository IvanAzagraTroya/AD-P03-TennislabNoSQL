package repositories.tarea

import kotlinx.coroutines.flow.Flow
import models.producto.Producto
import models.tarea.Tarea
import models.tarea.TareaResult
import java.util.*

interface ITareaRepository<ID> {
    suspend fun findAllRealTime(): Flow<List<Tarea>>
    fun findAll(): Flow<Tarea>
    suspend fun findById(id: ID): Tarea?
    suspend fun save(entity: Tarea): Tarea
    suspend fun delete(id: ID): Tarea?
    suspend fun setFinalizada(id: ID): Tarea?
    suspend fun findByUUID(id: UUID): Tarea?
}