package repositories.turno

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import models.turno.Turno
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.litote.kmongo.Id
import services.cache.turno.ITurnoCache
import java.util.*

@Single
@Named("TurnoRepositoryCached")
class TurnoRepositoryCached(
    @Named("TurnoRepository")
    private val repo: ITurnoRepository<Id<Turno>>,
    private val cache: ITurnoCache
): ITurnoRepository<Id<Turno>> {
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

        result = repo.findByUUID(id)
        if (result != null) listSearches.add(result!!)

        result
    }

    override suspend fun setFinalizado(id: Id<Turno>): Turno? = withContext(Dispatchers.IO) {
        val result = repo.setFinalizado(id)
        if (result != null) listSearches.add(result)
        result
    }

    override suspend fun delete(id: Id<Turno>): Turno? = withContext(Dispatchers.IO) {
        val entity = repo.delete(id)
        if (entity != null){
            listSearches.removeIf { it.uuid == entity.uuid }
            cache.cache.invalidate(entity.uuid)
        }
        entity
    }

    override suspend fun findById(id: Id<Turno>): Turno? = withContext(Dispatchers.IO) {
        val result = repo.findById(id)
        if (result != null) listSearches.add(result)
        result
    }
}