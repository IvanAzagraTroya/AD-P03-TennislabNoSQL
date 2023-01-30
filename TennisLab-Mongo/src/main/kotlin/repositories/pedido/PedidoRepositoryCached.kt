package repositories.pedido

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import models.pedido.Pedido
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.litote.kmongo.Id
import services.cache.pedido.IPedidoCache
import java.util.*

@Single
@Named("PedidoRepositoryCached")
class PedidoRepositoryCached(
    @Named("PedidoRepository")
    private val repo: IPedidoRepository<Id<Pedido>>,
    private val cache: IPedidoCache
): IPedidoRepository<Id<Pedido>> {
    private var refreshJob: Job? = null
    private var listSearches = mutableListOf<Pedido>()

    init { refreshCache() }

    private fun refreshCache() {
        if (refreshJob != null) refreshJob?.cancel()

        refreshJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                if(listSearches.isNotEmpty()) {
                    listSearches.forEach {
                        cache.cache.put(it.uuid, it)
                    }
                }

                delay(cache.refreshTime)
            }
        }
    }

    override suspend fun findAll(): Flow<Pedido> = withContext(Dispatchers.IO) {
        repo.findAll()
    }

    override suspend fun findByUUID(id: UUID): Pedido? = withContext(Dispatchers.IO) {
        var result: Pedido? = null

        cache.cache.asMap().forEach { if (it.key == id) result = it.value }
        if (result != null) {
            listSearches.add(result!!)
            return@withContext result
        }

        result = repo.findByUUID(id)
        if (result != null) listSearches.add(result!!)

        result
    }

    override suspend fun save(entity: Pedido): Pedido = withContext(Dispatchers.IO) {
        listSearches.add(entity)
        repo.save(entity)
        entity
    }

    override suspend fun delete(id: Id<Pedido>): Pedido? = withContext(Dispatchers.IO) {
        val entity = repo.delete(id)
        if (entity != null){
            listSearches.removeIf { it.uuid == entity.uuid }
            cache.cache.invalidate(entity.uuid)
        }
        entity
    }

    override suspend fun findById(id: Id<Pedido>): Pedido? = withContext(Dispatchers.IO) {
        val result = repo.findById(id)
        if (result != null) listSearches.add(result)
        result
    }
}