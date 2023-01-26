package services.cache.pedido

import services.cache.ICache
import models.pedido.Pedido
import java.util.*

interface IPedidoCache : ICache<UUID, Pedido>