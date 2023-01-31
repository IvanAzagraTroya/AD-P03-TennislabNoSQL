package koin.repositories.maquina

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import koin.models.maquina.Maquina
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.litote.kmongo.Id
import koin.services.cache.maquina.IMaquinaCache
import java.util.*

@Single
@Named("MaquinaRepositoryCached")
class MaquinaRepositoryCached(
    @Named("MaquinaRepository")
    private val repo: IMaquinaRepository<Id<Maquina>>,
    private val cache: IMaquinaCache
): IMaquinaRepository<Id<Maquina>> {
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

        result = repo.findByUUID(id)
        if (result != null) listSearches.add(result!!)

        result
    }

    override suspend fun save(entity: Maquina): Maquina = withContext(Dispatchers.IO) {
        listSearches.add(entity)
        repo.save(entity)
        entity
    }

    override suspend fun setInactive(id: Id<Maquina>): Maquina? = withContext(Dispatchers.IO) {
        val result = repo.setInactive(id)
        if (result != null) listSearches.add(result)
        result
    }

    override suspend fun delete(id: Id<Maquina>): Maquina? = withContext(Dispatchers.IO) {
        val entity = repo.delete(id)
        if (entity != null){
            listSearches.removeIf { it.uuid == entity.uuid }
            cache.cache.invalidate(entity.uuid)
        }
        entity
    }

    override suspend fun findById(id: Id<Maquina>): Maquina? = withContext(Dispatchers.IO) {
        val result = repo.findById(id)
        if (result != null) listSearches.add(result)
        result
    }
}