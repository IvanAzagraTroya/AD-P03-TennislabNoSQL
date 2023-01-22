package cache.producto

import cache.ICache
import models.producto.Producto
import java.util.*

interface IProductoCache : ICache<UUID, Producto>