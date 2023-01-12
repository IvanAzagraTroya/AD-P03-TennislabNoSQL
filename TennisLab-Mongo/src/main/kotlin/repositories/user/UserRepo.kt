package repositories.user

import kotlinx.coroutines.flow.Flow
import models.user.User
import models.user.UserResult

interface UserRepo<ID> {
    suspend fun findAllRealTime(): Flow<List<User>>
    fun findAll(): UserResult<Flow<User>>
    suspend fun findById(id: ID): UserResult<User>
    suspend fun save(entity: User): UserResult<User>
    suspend fun update(entity: User): UserResult<User>
    suspend fun delete(id: ID): UserResult<User>
}