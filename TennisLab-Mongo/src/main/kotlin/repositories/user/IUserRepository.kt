package repositories.user

import kotlinx.coroutines.flow.Flow
import models.maquina.Maquina
import models.user.User
import java.util.*

interface IUserRepository<ID> {
    suspend fun findAllRealTime(): Flow<List<User>>
    fun findAll(): Flow<User>
    suspend fun findById(id: ID): User?
    suspend fun findByUUID(id: UUID): User?
    suspend fun save(entity: User): User
    suspend fun delete(id: ID): User?
    suspend fun setInactive(id: ID): User?
}