package repositories.producto

import db.DBManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext
import models.producto.*
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.eq
import java.util.*

private val logger = KotlinLogging.logger {}

class ProductoRepository: IProductoRepository<Id<Producto>> {
    override suspend fun findAllRealTime() = flow {
        do {
            emit(DBManager.database.getCollection<Producto>().find().publisher.toList())
            delay(1000)
        } while (true)
    }

    override fun findAll(): Flow<Producto> {
        logger.debug { "findAll()" }

        return DBManager.database.getCollection<Producto>().find().publisher.asFlow()
    }

    override suspend fun findByUUID(id: UUID): Producto? = withContext(Dispatchers.IO) {
        logger.debug { "findByUUID($id)" }

        DBManager.database.getCollection<Producto>().findOne(Producto::uuid eq id)
    }

    override suspend fun save(entity: Producto): Producto = withContext(Dispatchers.IO) {
        logger.debug { "save($entity)" }

        DBManager.database.getCollection<Producto>().save(entity).let { entity }
    }

    override suspend fun decreaseStock(id: Id<Producto>): Producto? = withContext(Dispatchers.IO) {
        logger.debug { "decreaseStock($id)" }

        val entity = DBManager.database.getCollection<Producto>().findOneById(id)
            ?: return@withContext null
        if (entity.stock == 0) return@withContext entity
        val updated = Producto(
            id = entity.id,
            uuid = entity.uuid,
            tipo = entity.tipo,
            marca = entity.marca,
            modelo = entity.modelo,
            precio = entity.precio,
            stock = entity.stock - 1
        )
        DBManager.database.getCollection<Producto>().save(updated).let { updated }
    }

    override suspend fun delete(id: Id<Producto>): Producto? = withContext(Dispatchers.IO) {
        logger.debug { "delete($id)" }

        val entity = DBManager.database.getCollection<Producto>().findOneById(id)

        DBManager.database.getCollection<Producto>().deleteOneById(id).let { entity }
    }

    override suspend fun findById(id: Id<Producto>): Producto? = withContext(Dispatchers.IO) {
        logger.debug { "findById($id)" }

        DBManager.database.getCollection<Producto>().findOneById(id)
    }
}