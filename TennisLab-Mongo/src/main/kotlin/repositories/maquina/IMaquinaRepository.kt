package repositories.maquina

import kotlinx.coroutines.flow.Flow
import models.maquina.Maquina
import models.maquina.MaquinaResult

interface IMaquinaRepository<ID> {
    suspend fun findAllRealTime(): Flow<List<Maquina>>
    fun findAll(): MaquinaResult<Flow<Maquina>>
    suspend fun findById(id: ID): MaquinaResult<Maquina>
    suspend fun save(entity: Maquina): MaquinaResult<Maquina>
    suspend fun update(entity: Maquina): MaquinaResult<Maquina>
    suspend fun delete(id: ID): MaquinaResult<Maquina>
}