package repositories.user

import db.DBManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
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

    override suspend fun findAll(): UserResult<List<User>> {
        logger.debug { "findAll()" }

        val users: List<User> = DBManager.database.getCollection<User>().find().publisher.toList()
        return if (users.isEmpty()) {
            UserErrorNotFound("Could not find any users.")
        } else {
            UserSuccess(200, users)
        }
    }

    override suspend fun save(entity: User): UserResult<User> {
        logger.debug { "save($entity)" }

        val check = checkFieldsAreCorrect(entity)
        if (check != null) return check

        return DBManager.database.getCollection<User>().save(entity)
            .let { UserSuccess(201, entity) }
            .run { UserInternalException("There has been a problem inserting $entity.") }
    }

    override suspend fun update(entity: User): UserResult<User> {
        logger.debug { "update($entity)" }

        val check = checkFieldsAreCorrect(entity)
        if (check != null) return check

        return DBManager.database.getCollection<User>().save(entity)
            .let { UserSuccess(200, entity) }
            .run { UserInternalException("There has been a problem updating $entity.") }
    }

    override suspend fun setInactive(id: Id<User>): UserResult<User> {
        logger.debug { "setInactive($id)" }

        val entity = DBManager.database.getCollection<User>().findOneById(id)
            ?: return UserErrorNotFound("User with id $id not found.")
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
        return DBManager.database.getCollection<User>().save(updated)
            .let { UserSuccess(200, updated) }
            .run { UserInternalException("There has been a problem updating $updated.") }
    }

    override suspend fun delete(id: Id<User>): UserResult<User> {
        logger.debug { "delete($id)" }

        val entity = DBManager.database.getCollection<User>().findOneById(id)
        return if (entity == null) {
            UserErrorNotFound("Could not delete user with id $id. User not found.")
        } else {
            DBManager.database.getCollection<User>().deleteOneById(id)
                .let { UserSuccess(200, entity) }
                .run { UserInternalException("Could not delete due to unexpected exception.") }
        }
    }

    override suspend fun findById(id: Id<User>): UserResult<User> {
        logger.debug { "findById($id)" }

        val user = DBManager.database.getCollection<User>().findOneById(id)
        return if (user != null) {
            UserSuccess(200, user)
        } else {
            UserErrorNotFound("User with id $id not found.")
        }
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