package services.cache.maquina

import services.cache.ICache
import models.maquina.Maquina
import java.util.*

interface IMaquinaCache : ICache<UUID, Maquina>