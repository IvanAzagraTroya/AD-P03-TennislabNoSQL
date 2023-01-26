package services.cache.user

import services.cache.ICache
import models.user.User
import java.util.*

interface IUserCache : ICache<UUID, User>