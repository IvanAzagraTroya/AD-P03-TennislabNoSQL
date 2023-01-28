package repositories.tarea

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import mappers.fromDTO
import models.tarea.Tarea
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.litote.kmongo.Id
import services.cache.tarea.ITareaCache
import services.ktorfit.KtorFitClient
import java.util.*

@Single
@Named("TareaRepositoryCached")
class TareaRepositoryCached(
    @Named("TareaRepository")
    private val repo: ITareaRepository<Id<Tarea>>,
    private val cache: ITareaCache
): ITareaRepository<Id<Tarea>> {
    private val client by lazy { KtorFitClient.instance }
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
        val findAllApi = fromDTO(client.getAllTareas())
        val set = mutableSetOf<Tarea>()
        set.addAll(findAllDB)
        set.addAll(findAllApi)
        set.asFlow()
    }

    override suspend fun save(entity: Tarea): Tarea = withContext(Dispatchers.IO) {
        listSearches.add(entity)
        repo.save(entity)
        client.saveTareas(entity)
        entity
    }

    override suspend fun findByUUID(id: UUID): Tarea? = withContext(Dispatchers.IO) {
        var result: Tarea? = null

        cache.cache.asMap().forEach { if (it.key == id) result = it.value }
        if (result != null) {
            listSearches.add(result!!)
            return@withContext result
        }

        result = repo.findByUUID(id)
        if (result != null) listSearches.add(result!!)

        result
    }

    override suspend fun setFinalizada(id: Id<Tarea>): Tarea? = withContext(Dispatchers.IO) {
        val result = repo.setFinalizada(id)
        if (result != null) listSearches.add(result)
        result
    }

    override suspend fun delete(id: Id<Tarea>): Tarea? = withContext(Dispatchers.IO) {
        val entity = repo.delete(id)
        if (entity != null){
            listSearches.removeIf { it.uuid == entity.uuid }
            cache.cache.invalidate(entity.uuid)
        }
        entity
    }

    override suspend fun findById(id: Id<Tarea>): Tarea? = withContext(Dispatchers.IO) {
        val result = repo.findById(id)
        if (result != null) listSearches.add(result)
        result
    }
}