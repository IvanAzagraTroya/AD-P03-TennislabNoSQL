package com.example.tennislabspringboot.services.cache.producto

import com.example.tennislabspringboot.models.producto.Producto
import com.example.tennislabspringboot.services.cache.ICache
import java.util.*

interface IProductoCache : ICache<UUID, Producto>