package repositories.user

import kotlinx.coroutines.flow.Flow
import models.user.User
import models.user.UserResult

interface IUserRepository<ID> {
    suspend fun findAllRealTime(): Flow<List<User>>
    suspend fun findAll(): Flow<User>
    suspend fun findById(id: ID): User?
    suspend fun save(entity: User): User?
    suspend fun delete(id: ID): User?
    suspend fun setInactive(id: ID): User?
}