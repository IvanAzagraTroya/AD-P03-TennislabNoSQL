package services.cache.user

import io.github.reactivecircus.cache4k.Cache
import models.user.User
import mu.KotlinLogging
import org.koin.core.annotation.Single
import java.util.*
import kotlin.time.Duration.Companion.minutes

private val logger = KotlinLogging.logger {  }

@Single
class UserCache : IUserCache {
    override val hasRefreshAllCacheJob: Boolean = true
    override val refreshTime: Long = 60 * 1000L
    override val cache = Cache.Builder()
        .maximumCacheSize(50)
        .expireAfterAccess(1.minutes)
        .build<UUID, User>()

    init { logger.debug { "Initializing UserCache..." } }
}