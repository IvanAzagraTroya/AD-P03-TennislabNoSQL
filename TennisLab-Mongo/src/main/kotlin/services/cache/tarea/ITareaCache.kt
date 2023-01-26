package services.cache.tarea

import services.cache.ICache
import models.tarea.Tarea
import java.util.*

interface ITareaCache : ICache<UUID, Tarea>