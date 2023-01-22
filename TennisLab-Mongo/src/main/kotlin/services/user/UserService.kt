package services.user

import cache.user.IUserCache
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import models.user.User
import mu.KotlinLogging
import org.koin.core.annotation.Single
import org.litote.kmongo.Id
import repositories.user.IUserRepository
import java.util.*

private val logger = KotlinLogging.logger {}

@Single
class UserService(
    private val repo: IUserRepository<Id<User>>,
    private val cache: IUserCache
) : IUserService<Id<User>> {
    private val refreshJob: Job? = null

    init {
        logger.debug { "Initializing cache. AutoRefreshAll: ${cache.hasRefreshAllCacheJob}" }
        if (cache.hasRefreshAllCacheJob)
            refreshCache()
    }

    private fun refreshCache() {
        TODO("Not yet implemented")
    }

    override fun findAll(): Flow<User> {
        TODO("Not yet implemented")
    }

    override suspend fun findByUUID(id: UUID): User? {
        TODO("Not yet implemented")
    }

    override suspend fun save(entity: User): User {
        TODO("Not yet implemented")
    }

    override suspend fun setInactive(id: Id<User>): User? {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: Id<User>): User? {
        TODO("Not yet implemented")
    }

    override suspend fun findById(id: Id<User>): User? {
        TODO("Not yet implemented")
    }
}