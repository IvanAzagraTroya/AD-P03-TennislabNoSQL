package repositories.tarea

import kotlinx.coroutines.flow.Flow
import models.tarea.Tarea
import models.tarea.TareaResult

interface ITareaRepository<ID> {
    suspend fun findAllRealTime(): Flow<List<Tarea>>
    suspend fun findAll(): Flow<Tarea>
    suspend fun findById(id: ID): Tarea?
    suspend fun save(entity: Tarea): Tarea?
    suspend fun delete(id: ID): Tarea?
    suspend fun setFinalizada(id: ID): Tarea?
}