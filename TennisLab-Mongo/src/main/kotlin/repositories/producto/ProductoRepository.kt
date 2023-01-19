package repositories.producto

import db.DBManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactive.asFlow
import models.producto.*
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.coroutine.toList

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

    override suspend fun save(entity: Producto): Producto? {
        logger.debug { "save($entity)" }

        return DBManager.database.getCollection<Producto>().save(entity).let { entity }
    }

    override suspend fun decreaseStock(id: Id<Producto>): Producto? {
        logger.debug { "setInactive($id)" }

        val entity = DBManager.database.getCollection<Producto>().findOneById(id)
            ?: return null
        if (entity.stock == 0) return entity
        val updated = Producto(
            id = entity.id,
            uuid = entity.uuid,
            tipo = entity.tipo,
            marca = entity.marca,
            modelo = entity.modelo,
            precio = entity.precio,
            stock = entity.stock - 1
        )
        return DBManager.database.getCollection<Producto>().save(updated)
            .let { updated }
            .run { null }
    }

    override suspend fun delete(id: Id<Producto>): Producto? {
        logger.debug { "delete($id)" }

        val entity = DBManager.database.getCollection<Producto>().findOneById(id)
        return if (entity == null) {
            null
        } else {
            DBManager.database.getCollection<Producto>().deleteOneById(id)
                .let { entity }
                .run { null }
        }
    }

    override suspend fun findById(id: Id<Producto>): Producto? {
        logger.debug { "findById($id)" }

        return DBManager.database.getCollection<Producto>().findOneById(id)
    }
}