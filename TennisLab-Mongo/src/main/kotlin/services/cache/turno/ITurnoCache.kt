package services.cache.turno

import services.cache.ICache
import models.turno.Turno
import java.util.*

interface ITurnoCache : ICache<UUID, Turno>