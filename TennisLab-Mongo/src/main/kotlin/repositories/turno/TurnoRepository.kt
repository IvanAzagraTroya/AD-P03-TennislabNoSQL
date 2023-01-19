package repositories.turno

import db.DBManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext
import models.turno.Turno
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.eq
import java.util.*

private val logger = KotlinLogging.logger {}

class TurnoRepository: ITurnoRepository<Id<Turno>> {
    override suspend fun findAllRealTime() = flow {
        do {
            emit(DBManager.database.getCollection<Turno>().find().publisher.toList())
            delay(1000)
        } while (true)
    }

    override suspend fun findAll(): Flow<Turno> {
        logger.debug { "findAll()" }

       return DBManager.database.getCollection<Turno>().find().publisher.asFlow()
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
        return@withContext DBManager.database.getCollection<Turno>().save(updated)
            .let { updated }
            .run { null }
    }

    override suspend fun delete(id: Id<Turno>): Turno? = withContext(Dispatchers.IO) {
        logger.debug { "delete($id)" }

        val entity = DBManager.database.getCollection<Turno>().findOneById(id)
        return@withContext if (entity == null) {
            null
        } else {
            DBManager.database.getCollection<Turno>().deleteOneById(id)
                .let { entity }
                .run { null }
        }
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