package repositories.user

import db.DBManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext
import models.producto.Producto
import models.user.*
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.coroutine.toList

private val logger = KotlinLogging.logger {}

class UserRepository: IUserRepository<Id<User>> {
    override suspend fun findAllRealTime() = flow {
        do {
            emit(DBManager.database.getCollection<User>().find().publisher.toList())
            delay(1000)
        } while (true)
    }

    override suspend fun findAll(): Flow<User> {
        logger.debug { "findAll()" }

        return DBManager.database.getCollection<User>().find().publisher.asFlow()
    }

    override suspend fun save(entity: User): User = withContext(Dispatchers.IO) {
        logger.debug { "save($entity)" }

        return@withContext DBManager.database.getCollection<User>().save(entity).let {entity}
    }

    override suspend fun setInactive(id: Id<User>): User? = withContext(Dispatchers.IO) {
        logger.debug { "setInactive($id)" }

        val entity = DBManager.database.getCollection<User>().findOneById(id)
            ?: return@withContext null
        val updated = User(
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
        return@withContext DBManager.database.getCollection<User>().save(updated)
            .let { updated }
            .run { null }
    }

    override suspend fun delete(id: Id<User>): User? = withContext(Dispatchers.IO) {
        logger.debug { "delete($id)" }

        val entity = DBManager.database.getCollection<User>().findOneById(id)
        return@withContext if (entity == null) {
            null
        } else {
            DBManager.database.getCollection<User>().deleteOneById(id)
                .let { entity }
                .run { null }
        }
    }

    override suspend fun findById(id: Id<User>): User? = withContext(Dispatchers.IO){
        logger.debug { "findById($id)" }

        return@withContext DBManager.database.getCollection<User>().findOneById(id)
    }

    private fun checkFieldsAreCorrect(entity: User) : UserResult<User>? {
        if (entity.nombre.isBlank()) { return UserErrorBadRequest("Name cannot be blank.") }
        if (entity.apellido.isBlank()) { return UserErrorBadRequest("Surname cannot be blank.") }
        if (entity.telefono.isBlank() || !entity.telefono.matches("[1-9][0-9]{8}".toRegex()))
        { return UserErrorBadRequest("Incorrect format for phone number.") }
        // expresion regular del email sacada de: https://regexr.com/3e48o
        if (entity.email.isBlank() || !entity.email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}\$".toRegex()))
        { return UserErrorBadRequest("Incorrect format for email.") }
        if (entity.password.isBlank()) { return UserErrorBadRequest("Password cannot be blank.") }
        return null
    }
}