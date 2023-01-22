package cache.pedido

import cache.ICache
import models.pedido.Pedido
import java.util.*

interface IPedidoCache : ICache<UUID, Pedido>