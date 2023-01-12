package repositories.tarea

import kotlinx.coroutines.flow.Flow
import models.tarea.Tarea
import models.tarea.TareaResult

interface TareaRepo<ID> {
    suspend fun findAllRealTime(): Flow<List<Tarea>>
    fun findAll(): TareaResult<Flow<Tarea>>
    suspend fun findById(id: ID): TareaResult<Tarea>
    suspend fun save(entity: Tarea): TareaResult<Tarea>
    suspend fun update(entity: Tarea): TareaResult<Tarea>
    suspend fun delete(id: ID): TareaResult<Tarea>
}