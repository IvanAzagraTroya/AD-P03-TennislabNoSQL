package cache.turno

import cache.ICache
import models.turno.Turno
import java.util.*

interface ITurnoCache : ICache<UUID, Turno>