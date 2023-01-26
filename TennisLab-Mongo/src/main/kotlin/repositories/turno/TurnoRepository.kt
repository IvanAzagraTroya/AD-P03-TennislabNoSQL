package repositories.turno

import db.DBManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext
import models.turno.Turno
import mu.KotlinLogging
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.litote.kmongo.Id
import org.litote.kmongo.eq
import java.util.*

private val logger = KotlinLogging.logger {}

@Single
@Named("TurnoRepository")
class TurnoRepository: ITurnoRepository<Id<Turno>> {
    override suspend fun findAll(): Flow<Turno> = withContext(Dispatchers.IO) {
        logger.debug { "findAll()" }
        DBManager.database.getCollection<Turno>().find().publisher.asFlow()
    }

    override suspend fun save(entity: Turno): Turno = withContext(Dispatchers.IO){
        logger.debug { "save($entity)" }

        DBManager.database.getCollection<Turno>().save(entity).let { entity }
    }

    override suspend fun setFinalizado(id: Id<Turno>): Turno? = withContext(Dispatchers.IO) {
        logger.debug { "setFinalizado($id)" }

        val entity = DBManager.database.getCollection<Turno>().findOneById(id)
            ?: return@withContext null
        val updated = Turno(
            id = entity.id,
            uuid = entity.uuid,
            workerId = entity.workerId,
            maquinaId = entity.maquinaId,
            horaInicio = entity.horaInicio,
            horaFin = entity.horaFin,
            numPedidosActivos = entity.numPedidosActivos,
            tarea1Id = entity.tarea1Id,
            tarea2Id = entity.tarea2Id,
            finalizado = true
        )
        DBManager.database.getCollection<Turno>().save(updated).let { updated }
    }

    override suspend fun delete(id: Id<Turno>): Turno? = withContext(Dispatchers.IO) {
        logger.debug { "delete($id)" }

        val entity = DBManager.database.getCollection<Turno>().findOneById(id)
        DBManager.database.getCollection<Turno>().deleteOneById(id).let { entity }
    }

    override suspend fun findById(id: Id<Turno>): Turno? = withContext(Dispatchers.IO) {
        logger.debug { "findById($id)" }

        DBManager.database.getCollection<Turno>().findOneById(id)
    }

    override suspend fun findByUUID(id: UUID): Turno? = withContext(Dispatchers.IO) {
        logger.debug { "findByUUID($id)" }

        DBManager.database.getCollection<Turno>().findOne(Turno::uuid eq id)
    }
}