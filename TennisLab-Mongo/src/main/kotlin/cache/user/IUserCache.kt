package cache.user

import cache.ICache
import models.user.User
import java.util.*

interface IUserCache : ICache<UUID, User>