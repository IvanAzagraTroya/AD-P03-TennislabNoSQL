package com.example.tennislabspringboot.repositories.turno

import com.example.tennislabspringboot.models.turno.Turno
import com.example.tennislabspringboot.services.cache.turno.ITurnoCache
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class TurnoRepositoryCached
    @Autowired constructor(
    private val repo: TurnoRepository,
    private val cache: ITurnoCache
): ITurnoRepository<ObjectId> {
    private var refreshJob: Job? = null
    private var listSearches = mutableListOf<Turno>()

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

    override suspend fun findAll(): Flow<Turno> = withContext(Dispatchers.IO) {
        repo.findAll()
    }

    override suspend fun save(entity: Turno): Turno = withContext(Dispatchers.IO) {
        listSearches.add(entity)
        repo.save(entity)
        entity
    }

    override suspend fun findByUUID(id: UUID): Turno? = withContext(Dispatchers.IO) {
        var result: Turno? = null

        cache.cache.asMap().forEach { if (it.key == id) result = it.value }
        if (result != null) {
            listSearches.add(result!!)
            return@withContext result
        }

        result = repo.findFirstByUuid(id).toList().firstOrNull()
        if (result != null) listSearches.add(result!!)

        result
    }

    override suspend fun setFinalizado(id: ObjectId): Turno? = withContext(Dispatchers.IO) {
        val entity = repo.findById(id) ?: return@withContext null

        val result = Turno(
            id = entity.id,
            uuid = entity.uuid,
            workerId = entity.workerId,
            maquinaId = entity.maquinaId,
            horaInicio = entity.horaInicio,
            horaFin = entity.horaFin,
            numPedidosActivos = entity.numPedidosActivos,
            tarea1Id = entity.tarea1Id,
            tarea2Id = entity.tarea2Id,
            finalizado = true
        )
        repo.save(result)
        listSearches.add(result)
        result
    }

    override suspend fun delete(id: ObjectId): Turno? = withContext(Dispatchers.IO) {
        val entity = repo.findById(id) ?: return@withContext null
        repo.delete(entity)
        listSearches.removeIf { it.uuid == entity.uuid }
        cache.cache.invalidate(entity.uuid)

        entity
    }

    override suspend fun findById(id: ObjectId): Turno? = withContext(Dispatchers.IO) {
        val result = repo.findById(id)
        if (result != null) listSearches.add(result)
        result
    }

    suspend fun deleteAll() {
        repo.deleteAll()
    }
}