package com.example.tennislabspringboot.repositories.pedido

import com.example.tennislabspringboot.models.pedido.Pedido
import com.example.tennislabspringboot.services.cache.pedido.IPedidoCache
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class PedidoRepositoryCached
    @Autowired constructor(
    private val repo: PedidoRepository,
    private val cache: IPedidoCache
): IPedidoRepository<ObjectId> {
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

        result = repo.findFirstByUuid(id).toList().firstOrNull()
        if (result != null) listSearches.add(result!!)

        result
    }

    override suspend fun save(entity: Pedido): Pedido = withContext(Dispatchers.IO) {
        listSearches.add(entity)
        repo.save(entity)
        entity
    }

    override suspend fun delete(id: ObjectId): Pedido? = withContext(Dispatchers.IO) {
        val entity = repo.findById(id) ?: return@withContext null
        repo.delete(entity)
        listSearches.removeIf { it.uuid == entity.uuid }
        cache.cache.invalidate(entity.uuid)
        entity
    }

    override suspend fun findById(id: ObjectId): Pedido? = withContext(Dispatchers.IO) {
        val result = repo.findById(id)
        if (result != null) listSearches.add(result)
        result
    }

    suspend fun deleteAll() {
        repo.deleteAll()
    }
}