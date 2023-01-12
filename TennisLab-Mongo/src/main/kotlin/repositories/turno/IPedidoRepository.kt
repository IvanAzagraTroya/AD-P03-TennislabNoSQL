package repositories.turno

import kotlinx.coroutines.flow.Flow
import models.turno.Turno
import models.turno.TurnoResult

interface TurnoRepo<ID> {
    suspend fun findAllRealTime(): Flow<List<Turno>>
    fun findAll(): TurnoResult<Flow<Turno>>
    suspend fun findById(id: ID): TurnoResult<Turno>
    suspend fun save(entity: Turno): TurnoResult<Turno>
    suspend fun update(entity: Turno): TurnoResult<Turno>
    suspend fun delete(id: ID): TurnoResult<Turno>
}