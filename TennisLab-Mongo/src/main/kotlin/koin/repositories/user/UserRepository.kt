package koin.repositories.user

import koin.db.DBManager
import koin.models.user.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext
import koin.models.user.*
import mu.KotlinLogging
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.litote.kmongo.Id
import org.litote.kmongo.eq
import java.util.*

private val logger = KotlinLogging.logger {}

@Single
@Named("UserRepository")
class UserRepository: IUserRepository<Id<User>> {
    override suspend fun findAll(): Flow<User> = withContext(Dispatchers.IO){
        logger.debug { "findAll()" }

        DBManager.database.getCollection<User>().find().publisher.asFlow()
    }

    override suspend fun findByUUID(id: UUID): User? = withContext(Dispatchers.IO) {
        logger.debug { "findByUUID($id)" }

        DBManager.database.getCollection<User>().findOne(User::uuid eq id)
    }

    override suspend fun save(entity: User): User = withContext(Dispatchers.IO) {
        logger.debug { "save($entity)" }

        DBManager.database.getCollection<User>().save(entity).let {entity}
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
        DBManager.database.getCollection<User>().save(updated).let { updated }
    }

    override suspend fun delete(id: Id<User>): User? = withContext(Dispatchers.IO) {
        logger.debug { "delete($id)" }

        val entity = DBManager.database.getCollection<User>().findOneById(id)
        DBManager.database.getCollection<User>().deleteOneById(id).let { entity }
    }

    override suspend fun findById(id: Id<User>): User? = withContext(Dispatchers.IO){
        logger.debug { "findById($id)" }

        DBManager.database.getCollection<User>().findOneById(id)
    }
}