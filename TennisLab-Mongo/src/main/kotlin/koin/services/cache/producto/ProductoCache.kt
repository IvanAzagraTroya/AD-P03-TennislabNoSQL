package koin.services.cache.producto

import io.github.reactivecircus.cache4k.Cache
import koin.models.producto.Producto
import mu.KotlinLogging
import org.koin.core.annotation.Single
import java.util.*
import kotlin.time.Duration.Companion.minutes

private val logger = KotlinLogging.logger {  }

@Single
class ProductoCache : IProductoCache {
    override val hasRefreshAllCacheJob: Boolean = true
    override val refreshTime: Long = 60 * 1000L
    override val cache = Cache.Builder()
        .maximumCacheSize(50)
        .expireAfterAccess(1.minutes)
        .build<UUID, Producto>()

    init { logger.debug { "Initializing ProductoCache..." } }
}