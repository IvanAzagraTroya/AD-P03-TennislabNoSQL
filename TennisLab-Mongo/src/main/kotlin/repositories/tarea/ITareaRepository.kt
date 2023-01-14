package repositories.tarea

import kotlinx.coroutines.flow.Flow
import models.tarea.Tarea
import models.tarea.TareaResult

interface ITareaRepository<ID> {
    suspend fun findAllRealTime(): Flow<List<Tarea>>
    suspend fun findAll(): TareaResult<List<Tarea>>
    suspend fun findById(id: ID): TareaResult<Tarea>
    suspend fun save(entity: Tarea): TareaResult<Tarea>
    suspend fun update(entity: Tarea): TareaResult<Tarea>
    suspend fun delete(id: ID): TareaResult<Tarea>
    suspend fun setFinalizada(id: ID): TareaResult<Tarea>
}