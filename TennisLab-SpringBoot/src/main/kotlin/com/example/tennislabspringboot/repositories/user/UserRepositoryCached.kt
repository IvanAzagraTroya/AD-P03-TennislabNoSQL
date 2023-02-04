package com.example.tennislabspringboot.repositories.user

import com.example.tennislabspringboot.dto.user.UserDTOfromAPI
import com.example.tennislabspringboot.mappers.fromAPItoUser
import com.example.tennislabspringboot.mappers.fromDTO
import com.example.tennislabspringboot.models.user.User
import com.example.tennislabspringboot.services.cache.user.IUserCache
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import java.util.*

@Repository
class UserRepositoryCached
    @Autowired constructor(
    private val uRepo: UserRepository,
    private val cache: IUserCache
): IUserRepository<ObjectId> {
    private val apiUri = "https://jsonplaceholder.typicode.com/"
    private val client = RestTemplate()
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
        res.addAll(fromAPItoUser(client.getForObject("${apiUri}users", UserDTOfromAPI::class)))
        res.asFlow()
    }

    override suspend fun findByUUID(id: UUID): User? = withContext(Dispatchers.IO) {
        var result: User? = null

        cache.cache.asMap().forEach { if (it.key == id) result = it.value }
        if (result != null) {
            listSearches.add(result!!)
            return@withContext result
        }

        result = uRepo.findFirstByUuid(id).toList().firstOrNull()
        if (result != null) listSearches.add(result!!)

        result
    }

    override suspend fun save(entity: User): User = withContext(Dispatchers.IO) {
        listSearches.add(entity)
        uRepo.save(entity)
        entity
    }

    override suspend fun setInactive(id: ObjectId): User? = withContext(Dispatchers.IO) {
        val entity = uRepo.findById(id) ?: return@withContext null

        val result = User(
            id = entity.id,
            uuid = entity.uuid,
            nombre = entity.nombre,
            apellido = entity.apellido,
            telefono = entity.telefono,
            email = entity.email,
            password = entity.password,
            perfil = entity.perfil,
            activo = false
        )
        uRepo.save(result)
        listSearches.add(result)
        result
    }

    override suspend fun delete(id: ObjectId): User? = withContext(Dispatchers.IO) {
        val entity = uRepo.findById(id) ?: return@withContext null
        uRepo.delete(entity)
        listSearches.removeIf { it.uuid == entity.uuid }
        cache.cache.invalidate(entity.uuid)

        entity
    }

    override suspend fun findById(id: ObjectId): User? = withContext(Dispatchers.IO) {
        val result = uRepo.findById(id)
        if (result != null) listSearches.add(result)
        result
    }

    override suspend fun findById(id: Int): User? = withContext(Dispatchers.IO) {
        try {
            val result = client.getForObject<UserDTOfromAPI>("${apiUri}users/$id", UserDTOfromAPI::class).fromDTO()
            listSearches.add(result)
            result
        }
        catch (e: Exception) {
            null
        }
    }

    override suspend fun findByEmail(email: String): User? = withContext(Dispatchers.IO) {
        uRepo.findFirstByEmail(email).toList().firstOrNull()
        //findAll().toList().firstOrNull { it.email == email }
    }

    override suspend fun findByPhone(phone: String): User? = withContext(Dispatchers.IO) {
        uRepo.findFirstByTelefono(phone).toList().firstOrNull()
        //findAll().toList().firstOrNull { it.telefono == phone }
    }

    suspend fun deleteAll() {
        uRepo.deleteAll()
    }
}