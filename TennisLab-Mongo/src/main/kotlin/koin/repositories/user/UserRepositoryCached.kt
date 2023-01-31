package koin.repositories.user

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import koin.mappers.fromAPItoUser
import koin.mappers.fromDTO
import koin.models.user.User
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.litote.kmongo.Id
import koin.services.cache.user.IUserCache
import koin.services.ktorfit.KtorFitClient
import java.util.*

@Single
@Named("UserRepositoryCached")
class UserRepositoryCached(
    @Named("UserRepository")
    private val uRepo: IUserRepository<Id<User>>,
    private val cache: IUserCache
): IUserRepository<Id<User>> {
    private val client by lazy { KtorFitClient.instance }
    private var refreshJob: Job? = null
    private var listSearches = mutableListOf<User>()

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

    override suspend fun findAll(): Flow<User> = withContext(Dispatchers.IO) {
        val res = mutableSetOf<User>()
        res.addAll(uRepo.findAll().toList())
        res.addAll(fromAPItoUser(client.getAll()))
        res.asFlow()
    }

    override suspend fun findByUUID(id: UUID): User? = withContext(Dispatchers.IO) {
        var result: User? = null

        cache.cache.asMap().forEach { if (it.key == id) result = it.value }
        if (result != null) {
            listSearches.add(result!!)
            return@withContext result
        }

        result = uRepo.findByUUID(id)
        if (result != null) listSearches.add(result!!)

        result
    }

    override suspend fun save(entity: User): User = withContext(Dispatchers.IO) {
        listSearches.add(entity)
        uRepo.save(entity)
        entity
    }

    override suspend fun setInactive(id: Id<User>): User? = withContext(Dispatchers.IO) {
        val result = uRepo.setInactive(id)
        if (result != null) listSearches.add(result)
        result
    }

    override suspend fun delete(id: Id<User>): User? = withContext(Dispatchers.IO) {
        val entity = uRepo.delete(id)
        if (entity != null) {
            listSearches.removeIf { it.uuid == entity.uuid }
            cache.cache.invalidate(entity.uuid)
        }
        entity
    }

    override suspend fun findById(id: Id<User>): User? = withContext(Dispatchers.IO) {
        val result = uRepo.findById(id)
        if (result != null) listSearches.add(result)
        result
    }

    suspend fun findById(id: Int): User? = withContext(Dispatchers.IO) {
        val result: User? = client.getById(id)?.fromDTO()
        if (result != null) listSearches.add(result)
        result
    }

    suspend fun findByEmail(email: String): User? = withContext(Dispatchers.IO) {
        findAll().toList().firstOrNull { it.email == email }
    }

    suspend fun findByPhone(phone: String): User? = withContext(Dispatchers.IO) {
        findAll().toList().firstOrNull { it.telefono == phone }
    }
}