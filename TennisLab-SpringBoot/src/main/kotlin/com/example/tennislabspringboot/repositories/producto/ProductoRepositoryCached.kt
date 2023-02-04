package com.example.tennislabspringboot.repositories.producto

import com.example.tennislabspringboot.mappers.toDTO
import com.example.tennislabspringboot.models.producto.Producto
import com.example.tennislabspringboot.services.cache.producto.IProductoCache
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ProductoRepositoryCached
    @Autowired constructor(
    private val repo: ProductoRepository,
    private val cache: IProductoCache
): IProductoRepository<ObjectId> {
    private var refreshJob: Job? = null
    private var listSearches = mutableListOf<Producto>()

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

    override suspend fun findAllAsFlow() = flow {
        do {
            emit(toDTO(repo.findAll().toList()))
            delay(1000)
        } while (true)
    }

    override suspend fun findAll(): Flow<Producto> = withContext(Dispatchers.IO) {
        repo.findAll()
    }

    override suspend fun findByUUID(id: UUID): Producto? = withContext(Dispatchers.IO) {
        var result: Producto? = null

        cache.cache.asMap().forEach { if (it.key == id) result = it.value }
        if (result != null) {
            listSearches.add(result!!)
            return@withContext result
        }

        result = repo.findFirstByUuid(id).toList().firstOrNull()
        if (result != null) listSearches.add(result!!)

        result
    }

    override suspend fun save(entity: Producto): Producto = withContext(Dispatchers.IO) {
        listSearches.add(entity)
        repo.save(entity)
        entity
    }

    override suspend fun decreaseStock(id: ObjectId): Producto? = withContext(Dispatchers.IO) {
        val entity = repo.findById(id) ?: return@withContext null

        val result = Producto(
            id = entity.id,
            uuid = entity.uuid,
            tipo = entity.tipo,
            marca = entity.marca,
            modelo = entity.modelo,
            precio = entity.precio,
            stock = entity.stock - 1
        )
        repo.save(result)
        listSearches.add(result)
        result
    }

    override suspend fun delete(id: ObjectId): Producto? = withContext(Dispatchers.IO) {
        val entity = repo.findById(id) ?: return@withContext null
        repo.delete(entity)
        listSearches.removeIf { it.uuid == entity.uuid }
        cache.cache.invalidate(entity.uuid)

        entity
    }

    override suspend fun findById(id: ObjectId): Producto? = withContext(Dispatchers.IO) {
        val result = repo.findById(id)
        if (result != null) listSearches.add(result)
        result
    }

    suspend fun deleteAll() {
        repo.deleteAll()
    }
}