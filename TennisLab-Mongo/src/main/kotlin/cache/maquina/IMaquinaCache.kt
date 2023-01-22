package cache.maquina

import cache.ICache
import models.maquina.Maquina
import java.util.*

interface IMaquinaCache : ICache<UUID, Maquina>