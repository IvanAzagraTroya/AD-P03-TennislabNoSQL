package koin.repositories.pedido

import koin.db.DBManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext
import koin.models.pedido.Pedido
import mu.KotlinLogging
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.litote.kmongo.Id
import org.litote.kmongo.eq
import java.util.*

private val logger = KotlinLogging.logger {}

@Single
@Named("PedidoRepository")
class PedidoRepository: IPedidoRepository<Id<Pedido>> {
    override suspend fun findAll(): Flow<Pedido> = withContext(Dispatchers.IO) {
        logger.debug { "findAll()" }

        DBManager.database.getCollection<Pedido>().find().publisher.asFlow()
    }

    override suspend fun findByUUID(id: UUID): Pedido? = withContext(Dispatchers.IO) {
        logger.debug { "findByUUID($id)" }

        DBManager.database.getCollection<Pedido>().findOne(Pedido::uuid eq id)
    }

    override suspend fun save(entity: Pedido): Pedido = withContext(Dispatchers.IO) {
        logger.debug { "save($entity)" }

        DBManager.database.getCollection<Pedido>().save(entity).let { entity }
    }

    override suspend fun delete(id: Id<Pedido>): Pedido? = withContext(Dispatchers.IO) {
        logger.debug { "delete($id)" }

        val entity = DBManager.database.getCollection<Pedido>().findOneById(id)
        DBManager.database.getCollection<Pedido>().deleteOneById(id).let { entity }
    }

    override suspend fun findById(id: Id<Pedido>): Pedido? = withContext(Dispatchers.IO) {
        logger.debug { "findById($id)" }

        DBManager.database.getCollection<Pedido>().findOneById(id)
    }
}