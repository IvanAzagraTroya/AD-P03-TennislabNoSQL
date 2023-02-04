package com.example.tennislabspringboot.repositories.tarea

import com.example.tennislabspringboot.dto.tarea.TareaDTOFromApi
import com.example.tennislabspringboot.mappers.fromAPItoTarea
import com.example.tennislabspringboot.mappers.toDTOapi
import com.example.tennislabspringboot.models.tarea.Tarea
import com.example.tennislabspringboot.services.cache.tarea.ITareaCache
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForEntity
import java.util.*

@Repository
class TareaRepositoryCached
    @Autowired constructor(
    private val repo: TareaRepository,
    private val cache: ITareaCache
): ITareaRepository<ObjectId> {
    private val apiUri = "https://jsonplaceholder.typicode.com/"
    private val client = RestTemplate()
    private var refreshJob: Job? = null
    private var listSearches = mutableListOf<Tarea>()

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

    override suspend fun findAll(): Flow<Tarea> = withContext(Dispatchers.IO) {
        val findAllDB = repo.findAll().toList()
        val findAllApi = fromAPItoTarea(client.getForObject("${apiUri}todos", TareaDTOFromApi::class))
        val set = mutableSetOf<Tarea>()
        set.addAll(findAllDB)
        set.addAll(findAllApi)
        set.asFlow()
    }

    override suspend fun save(entity: Tarea): Tarea = withContext(Dispatchers.IO) {
        listSearches.add(entity)
        repo.save(entity)
        client.postForEntity<TareaDTOFromApi>("${apiUri}todos", entity.toDTOapi(), TareaDTOFromApi::class)
        entity
    }

    override suspend fun findByUUID(id: UUID): Tarea? = withContext(Dispatchers.IO) {
        var result: Tarea? = null

        cache.cache.asMap().forEach { if (it.key == id) result = it.value }
        if (result != null) {
            listSearches.add(result!!)
            return@withContext result
        }

        result = repo.findFirstByUuid(id).toList().firstOrNull()
        if (result != null) listSearches.add(result!!)

        result
    }

    override suspend fun setFinalizada(id: ObjectId): Tarea? = withContext(Dispatchers.IO) {
        val entity = repo.findById(id) ?: return@withContext null

        val result = Tarea(
            id = entity.id,
            uuid = entity.uuid,
            raquetaId = entity.raquetaId,
            precio = entity.precio,
            tipo = entity.tipo,
            finalizada = true,
            pedidoId = entity.pedidoId,
            productoAdquiridoId = entity.productoAdquiridoId,
            peso = entity.peso,
            balance = entity.balance,
            rigidez = entity.rigidez,
            tensionHorizontal = entity.tensionHorizontal,
            cordajeHorizontalId = entity.cordajeHorizontalId,
            tensionVertical = entity.tensionVertical,
            cordajeVerticalId = entity.cordajeVerticalId,
            dosNudos = entity.dosNudos
        )
        repo.save(result)
        listSearches.add(result)
        result
    }

    override suspend fun delete(id: ObjectId): Tarea? = withContext(Dispatchers.IO) {
        val entity = repo.findById(id) ?: return@withContext null
        repo.delete(entity)
        listSearches.removeIf { it.uuid == entity.uuid }
        cache.cache.invalidate(entity.uuid)

        entity
    }

    override suspend fun findById(id: ObjectId): Tarea? = withContext(Dispatchers.IO) {
        val result = repo.findById(id)
        if (result != null) listSearches.add(result)
        result
    }

    suspend fun deleteAll() {
        repo.deleteAll()
    }
}