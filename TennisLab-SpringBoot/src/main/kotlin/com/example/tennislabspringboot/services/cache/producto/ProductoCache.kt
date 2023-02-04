package com.example.tennislabspringboot.services.cache.producto

import com.example.tennislabspringboot.models.producto.Producto
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Service
import java.util.*
import kotlin.time.Duration.Companion.minutes

@Service
class ProductoCache : IProductoCache {
    override val hasRefreshAllCacheJob: Boolean = true
    override val refreshTime: Long = 60 * 1000L
    override val cache = Cache.Builder()
        .maximumCacheSize(50)
        .expireAfterAccess(1.minutes)
        .build<UUID, Producto>()

    init { println("Initializing ProductoCache...") }
}