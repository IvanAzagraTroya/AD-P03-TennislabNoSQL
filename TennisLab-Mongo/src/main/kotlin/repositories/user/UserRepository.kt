package repositories.user

import db.DBManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext
import models.maquina.Maquina
import models.producto.Producto
import models.user.*
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.eq
import java.util.*

private val logger = KotlinLogging.logger {}

class UserRepository: IUserRepository<Id<User>> {
    override suspend fun findAllRealTime() = flow {
        do {
            emit(DBManager.database.getCollection<User>().find().publisher.toList())
            delay(1000)
        } while (true)
    }

    override fun findAll(): Flow<User> {
        logger.debug { "findAll()" }

        return DBManager.database.getCollection<User>().find().publisher.asFlow()
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