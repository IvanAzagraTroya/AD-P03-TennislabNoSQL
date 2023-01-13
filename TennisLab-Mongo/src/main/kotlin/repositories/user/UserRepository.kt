package repositories.user

import db.DBManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import models.user.User
import models.user.UserErrorNotFound
import models.user.UserResult
import models.user.UserSuccess
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
        val users: List<User> = DBManager.database.getCollection<User>().find().publisher.toList()
        return if (users.isEmpty()) {
            UserErrorNotFound("Could not find any users.")
        } else {
            UserSuccess(200, users)
        }
    }

    override suspend fun save(entity: User): UserResult<User> {
        TODO("Not yet implemented")
    }

    override suspend fun update(entity: User): UserResult<User> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: Id<User>): UserResult<User> {
        TODO("Not yet implemented")
    }

    override suspend fun findById(id: Id<User>): UserResult<User> {
        logger.debug { "findById($id)" }
        val user = DBManager.database.getCollection<User>()
            .findOneById(id)

        return if (user != null) {
            UserSuccess(200, user)
        } else {
            UserErrorNotFound("User with id $id not found.")
        }
    }
}