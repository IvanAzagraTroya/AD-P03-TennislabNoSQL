package koin.services.cache.user

import koin.services.cache.ICache
import koin.models.user.User
import java.util.*

interface IUserCache : ICache<UUID, User>