package services.user

import kotlinx.coroutines.flow.Flow
import models.user.User
import java.util.*

interface IUserService<ID> {
    fun findAll(): Flow<User>
    suspend fun findById(id: ID): User?
    suspend fun findByUUID(id: UUID): User?
    suspend fun save(entity: User): User
    suspend fun delete(id: ID): User?
    suspend fun setInactive(id: ID) : User?
}