package repositories.turno

import kotlinx.coroutines.flow.Flow
import models.tarea.Tarea
import models.turno.Turno
import models.turno.TurnoResult
import java.util.*

interface ITurnoRepository<ID> {
    suspend fun findAllRealTime(): Flow<List<Turno>>
    suspend fun findAll(): TurnoResult<List<Turno>>
    suspend fun findById(id: ID): TurnoResult<Turno>
    suspend fun save(entity: Turno): TurnoResult<Turno>
    suspend fun update(entity: Turno): TurnoResult<Turno>
    suspend fun delete(id: ID): TurnoResult<Turno>
    suspend fun setFinalizado(id: ID): TurnoResult<Turno>
    suspend fun findByUUID(id: UUID): Turno?
}