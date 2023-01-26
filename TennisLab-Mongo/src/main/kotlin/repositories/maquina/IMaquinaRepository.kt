package repositories.maquina

import kotlinx.coroutines.flow.Flow
import models.maquina.Maquina
import java.util.*

interface IMaquinaRepository<ID> {
    suspend fun findAll(): Flow<Maquina>
    suspend fun findById(id: ID): Maquina?
    suspend fun findByUUID(id: UUID): Maquina?
    suspend fun save(entity: Maquina): Maquina
    suspend fun delete(id: ID): Maquina?
    suspend fun setInactive(id: ID): Maquina?
}