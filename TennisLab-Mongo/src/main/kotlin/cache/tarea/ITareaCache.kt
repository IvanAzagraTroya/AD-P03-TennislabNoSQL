package cache.tarea

import cache.ICache
import models.tarea.Tarea
import java.util.*

interface ITareaCache : ICache<UUID, Tarea>