package com.example.tennislabspringboot.services.cache.tarea

import com.example.tennislabspringboot.models.tarea.Tarea
import com.example.tennislabspringboot.services.cache.ICache
import java.util.*

interface ITareaCache : ICache<UUID, Tarea>