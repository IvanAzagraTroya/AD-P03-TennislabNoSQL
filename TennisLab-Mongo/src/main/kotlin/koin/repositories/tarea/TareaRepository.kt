package koin.repositories.tarea

import koin.db.DBManager
import koin.models.tarea.Tarea
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext
import koin.models.tarea.*
import mu.KotlinLogging
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.litote.kmongo.Id
import org.litote.kmongo.eq
import java.util.*

private val logger = KotlinLogging.logger {}

@Single
@Named("TareaRepository")
class TareaRepository: ITareaRepository<Id<Tarea>> {
    override suspend fun findAll(): Flow<Tarea> = withContext(Dispatchers.IO) {
        logger.debug { "findAll()" }

        DBManager.database.getCollection<Tarea>().find().publisher.asFlow()
    }

    override suspend fun save(entity: Tarea): Tarea = withContext(Dispatchers.IO) {
        logger.debug { "save($entity)" }
        
        DBManager.database.getCollection<Tarea>().save(entity).let { entity }
    }

    override suspend fun setFinalizada(id: Id<Tarea>): Tarea? = withContext(Dispatchers.IO) {
        logger.debug { "setFinalizada($id)" }

        val entity = DBManager.database.getCollection<Tarea>().findOneById(id)
            ?: return@withContext null
        val updated = Tarea(
            id = entity.id,
            uuid = entity.uuid,
            raquetaId = entity.raquetaId,
            precio = entity.precio,
            tipo = entity.tipo,
            finalizada = true,
            pedidoId = entity.pedidoId,
            productoAdquiridoId = entity.productoAdquiridoId,
            peso = entity.peso,
            balance = entity.balance,
            rigidez = entity.rigidez,
            tensionHorizontal = entity.tensionHorizontal,
            cordajeHorizontalId = entity.cordajeHorizontalId,
            tensionVertical = entity.tensionVertical,
            cordajeVerticalId = entity.cordajeVerticalId,
            dosNudos = entity.dosNudos
        )
        DBManager.database.getCollection<Tarea>().save(updated).let { updated }
    }

    override suspend fun delete(id: Id<Tarea>): Tarea? = withContext(Dispatchers.IO){
        logger.debug { "delete($id)" }

        val entity = DBManager.database.getCollection<Tarea>().findOneById(id)
        DBManager.database.getCollection<Tarea>().deleteOneById(id).let { entity }
    }

    override suspend fun findById(id: Id<Tarea>): Tarea? = withContext(Dispatchers.IO) {
        logger.debug { "findById($id)" }

        DBManager.database.getCollection<Tarea>().findOneById(id)
    }

    override suspend fun findByUUID(id: UUID): Tarea? = withContext(Dispatchers.IO) {
        logger.debug { "findByUUID($id)" }

        DBManager.database.getCollection<Tarea>().findOne(Tarea::uuid eq id)
    }
}