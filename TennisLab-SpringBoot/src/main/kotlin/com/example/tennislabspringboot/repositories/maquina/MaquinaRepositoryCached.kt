package com.example.tennislabspringboot.repositories.maquina

import com.example.tennislabspringboot.models.maquina.Maquina
import com.example.tennislabspringboot.services.cache.maquina.IMaquinaCache
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class MaquinaRepositoryCached
    @Autowired constructor(
    private val repo: MaquinaRepository,
    private val cache: IMaquinaCache
): IMaquinaRepository<ObjectId> {
    private var refreshJob: Job? = null
    private var listSearches = mutableListOf<Maquina>()

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

    override suspend fun findAll(): Flow<Maquina> = withContext(Dispatchers.IO) {
        repo.findAll()
    }

    override suspend fun findByUUID(id: UUID): Maquina? = withContext(Dispatchers.IO) {
        var result: Maquina? = null

        cache.cache.asMap().forEach { if (it.key == id) result = it.value }
        if (result != null) {
            listSearches.add(result!!)
            return@withContext result
        }

        result = repo.findFirstByUuid(id).toList().firstOrNull()
        if (result != null) listSearches.add(result!!)

        result
    }

    override suspend fun save(entity: Maquina): Maquina = withContext(Dispatchers.IO) {
        listSearches.add(entity)
        repo.save(entity)
        entity
    }

    override suspend fun setInactive(id: ObjectId): Maquina? = withContext(Dispatchers.IO) {
        val entity = repo.findById(id) ?: return@withContext null

        val result = Maquina(
            id = entity.id,
            uuid = entity.uuid,
            modelo = entity.modelo,
            marca = entity.marca,
            fechaAdquisicion = entity.fechaAdquisicion,
            numeroSerie = entity.numeroSerie,
            tipo = entity.tipo,
            activa = false,
            isManual = entity.isManual,
            maxTension = entity.maxTension,
            minTension = entity.minTension,
            measuresManeuverability = entity.measuresManeuverability,
            measuresRigidity = entity.measuresRigidity,
            measuresBalance = entity.measuresBalance
        )
        repo.save(result)
        listSearches.add(result)
        result
    }

    override suspend fun delete(id: ObjectId): Maquina? = withContext(Dispatchers.IO) {
        val entity = repo.findById(id) ?: return@withContext null
        repo.delete(entity)
        listSearches.removeIf { it.uuid == entity.uuid }
        cache.cache.invalidate(entity.uuid)

        entity
    }

    override suspend fun findById(id: ObjectId): Maquina? = withContext(Dispatchers.IO) {
        val result = repo.findById(id)
        if (result != null) listSearches.add(result)
        result
    }

    suspend fun deleteAll() {
        repo.deleteAll()
    }
}