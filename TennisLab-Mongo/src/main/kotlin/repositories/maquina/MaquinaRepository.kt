package repositories.maquina

import kotlinx.coroutines.flow.Flow
import models.maquina.Maquina
import models.maquina.MaquinaResult
import org.litote.kmongo.Id

class MaquinaRepository: IMaquinaRepository<Id<Maquina>> {
    override suspend fun findAllRealTime(): Flow<List<Maquina>> {
        TODO("Not yet implemented")
    }

    override suspend fun findAll(): MaquinaResult<List<Maquina>> {
        TODO("Not yet implemented")
    }

    override suspend fun save(entity: Maquina): MaquinaResult<Maquina> {
        TODO("Not yet implemented")
    }

    override suspend fun update(entity: Maquina): MaquinaResult<Maquina> {
        TODO("Not yet implemented")
    }

    override suspend fun setInactive(id: Id<Maquina>): MaquinaResult<Maquina> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: Id<Maquina>): MaquinaResult<Maquina> {
        TODO("Not yet implemented")
    }

    override suspend fun findById(id: Id<Maquina>): MaquinaResult<Maquina> {
        TODO("Not yet implemented")
    }
}