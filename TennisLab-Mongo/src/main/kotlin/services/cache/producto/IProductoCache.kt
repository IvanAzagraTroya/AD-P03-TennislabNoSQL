package services.cache.producto

import services.cache.ICache
import models.producto.Producto
import java.util.*

interface IProductoCache : ICache<UUID, Producto>