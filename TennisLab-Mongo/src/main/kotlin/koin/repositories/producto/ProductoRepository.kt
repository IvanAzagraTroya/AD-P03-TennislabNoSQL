package koin.repositories.producto

import koin.db.DBManager
import koin.models.producto.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext
import koin.models.producto.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import mu.KotlinLogging
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.litote.kmongo.Id
import java.util.*

private val logger = KotlinLogging.logger {}

@Single
@Named("ProductoRepository")
class ProductoRepository: IProductoRepository<Id<Producto>> {
    override suspend fun findAll(): Flow<Producto> = withContext(Dispatchers.IO) {
        logger.debug { "findAll()" }

        DBManager.database.getCollection<Producto>().find().publisher.asFlow()
    }

    override suspend fun findByUUID(id: UUID): Producto? = withContext(Dispatchers.IO) {
        logger.debug { "findByUUID($id)" }

        DBManager.database.getCollection<Producto>()
            .find().publisher.asFlow().filter { it.uuid == id }.firstOrNull()
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