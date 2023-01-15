package repositories.producto

import db.DBManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
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

    override suspend fun findAll(): ProductoResult<List<Producto>> {
        logger.debug { "findAll()" }

        val productos: List<Producto> = DBManager.database.getCollection<Producto>().find().publisher.toList()
        return if (productos.isEmpty()) {
            ProductoErrorNotFound("Could not find any productos.")
        } else {
            ProductoSuccess(200, productos)
        }
    }

    override suspend fun save(entity: Producto): ProductoResult<Producto> {
        logger.debug { "save($entity)" }

        val check = checkFieldsAreCorrect(entity)
        if (check != null) return check

        return DBManager.database.getCollection<Producto>().save(entity)
            .let { ProductoSuccess(201, entity) }
            .run { ProductoInternalException("There has been a problem inserting $entity.") }
    }

    override suspend fun update(entity: Producto): ProductoResult<Producto> {
        logger.debug { "update($entity)" }

        val check = checkFieldsAreCorrect(entity)
        if (check != null) return check

        return DBManager.database.getCollection<Producto>().save(entity)
            .let { ProductoSuccess(200, entity) }
            .run { ProductoInternalException("There has been a problem updating $entity.") }
    }

    override suspend fun decreaseStock(id: Id<Producto>): ProductoResult<Producto> {
        logger.debug { "setInactive($id)" }

        val entity = DBManager.database.getCollection<Producto>().findOneById(id)
            ?: return ProductoErrorNotFound("Producto with id $id not found.")
        if (entity.stock == 0) return ProductoSuccess(200, entity)
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
            .let { ProductoSuccess(200, updated) }
            .run { ProductoInternalException("There has been a problem updating $updated.") }
    }

    override suspend fun delete(id: Id<Producto>): ProductoResult<Producto> {
        logger.debug { "delete($id)" }

        val entity = DBManager.database.getCollection<Producto>().findOneById(id)
        return if (entity == null) {
            ProductoErrorNotFound("Could not delete producto with id $id. Producto not found.")
        } else {
            DBManager.database.getCollection<Producto>().deleteOneById(id)
                .let { ProductoSuccess(200, entity) }
                .run { ProductoInternalException("Could not delete due to unexpected exception.") }
        }
    }

    override suspend fun findById(id: Id<Producto>): ProductoResult<Producto> {
        logger.debug { "findById($id)" }

        val producto = DBManager.database.getCollection<Producto>().findOneById(id)
        return if (producto != null) {
            ProductoSuccess(200, producto)
        } else {
            ProductoErrorNotFound("Producto with id $id not found.")
        }
    }

    private fun checkFieldsAreCorrect(entity: Producto): ProductoResult<Producto>? {
        if (entity.marca.isBlank()) { return ProductoErrorBadRequest("Marca cannot be blank.") }
        if (entity.modelo.isBlank()) { return ProductoErrorBadRequest("Model cannot be blank.") }
        if (entity.precio <= 0.0 ) { return ProductoErrorBadRequest("Price must be greater than 0.") }
        if (entity.stock < 0 ) { return ProductoErrorBadRequest("Stock cannot be a negative value.") }
        return null
    }
}