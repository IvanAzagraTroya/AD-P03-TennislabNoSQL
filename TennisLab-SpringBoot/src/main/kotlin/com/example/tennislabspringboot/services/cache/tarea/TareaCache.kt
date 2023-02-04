package com.example.tennislabspringboot.services.cache.tarea

import com.example.tennislabspringboot.models.tarea.Tarea
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Service
import java.util.*
import kotlin.time.Duration.Companion.minutes

@Service
class TareaCache : ITareaCache {
    override val hasRefreshAllCacheJob: Boolean = true
    override val refreshTime: Long = 60 * 1000L
    override val cache = Cache.Builder()
        .maximumCacheSize(50)
        .expireAfterAccess(1.minutes)
        .build<UUID, Tarea>()

    init { println("Initializing TareaCache...") }
}