package koin.services.cache.producto

import koin.services.cache.ICache
import koin.models.producto.Producto
import java.util.*

interface IProductoCache : ICache<UUID, Producto>