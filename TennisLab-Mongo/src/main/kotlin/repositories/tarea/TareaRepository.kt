package repositories.tarea

import kotlinx.coroutines.flow.Flow
import models.tarea.Tarea
import models.tarea.TareaResult
import org.litote.kmongo.Id

class TareaRepository: ITareaRepository<Id<Tarea>> {
    override suspend fun findAllRealTime(): Flow<List<Tarea>> {
        TODO("Not yet implemented")
    }

    override suspend fun findAll(): TareaResult<List<Tarea>> {
        TODO("Not yet implemented")
    }

    override suspend fun save(entity: Tarea): TareaResult<Tarea> {
        TODO("Not yet implemented")
    }

    override suspend fun update(entity: Tarea): TareaResult<Tarea> {
        TODO("Not yet implemented")
    }

    override suspend fun setFinalizada(id: Id<Tarea>): TareaResult<Tarea> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: Id<Tarea>): TareaResult<Tarea> {
        TODO("Not yet implemented")
    }

    override suspend fun findById(id: Id<Tarea>): TareaResult<Tarea> {
        TODO("Not yet implemented")
    }
}