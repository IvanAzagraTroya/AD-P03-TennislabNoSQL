package koin.services.cache.tarea

import koin.services.cache.ICache
import koin.models.tarea.Tarea
import java.util.*

interface ITareaCache : ICache<UUID, Tarea>