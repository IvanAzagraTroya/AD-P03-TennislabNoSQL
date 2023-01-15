package repositories.turno

import db.DBManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import models.turno.*
import models.turno.Turno
import models.turno.TurnoErrorNotFound
import models.turno.TurnoSuccess
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.coroutine.toList

private val logger = KotlinLogging.logger {}

class TurnoRepository: ITurnoRepository<Id<Turno>> {
    override suspend fun findAllRealTime() = flow {
        do {
            emit(DBManager.database.getCollection<Turno>().find().publisher.toList())
            delay(1000)
        } while (true)
    }

    override suspend fun findAll(): TurnoResult<List<Turno>> {
        logger.debug { "findAll()" }

        val turnos: List<Turno> = DBManager.database.getCollection<Turno>().find().publisher.toList()
        return if (turnos.isEmpty()) {
            TurnoErrorNotFound("Could not find any turnos.")
        } else {
            TurnoSuccess(200, turnos)
        }
    }

    override suspend fun save(entity: Turno): TurnoResult<Turno> {
        logger.debug { "save($entity)" }

        if (entity.numPedidosActivos < 0 || entity.numPedidosActivos > 2) {
            return TurnoErrorBadRequest("Number of active pedidos cannot exceed 2 or be lower than 0.")
        }
        if (entity.tarea2Id == null && entity.numPedidosActivos > 1) {
            return TurnoErrorBadRequest("Number of active pedidos cannot exceed 1 when there is only 1 task.")
        }

        return DBManager.database.getCollection<Turno>().save(entity)
            .let { TurnoSuccess(201, entity) }
            .run { TurnoInternalException("There has been a problem inserting $entity.") }
    }

    override suspend fun update(entity: Turno): TurnoResult<Turno> {
        logger.debug { "update($entity)" }

        if (entity.numPedidosActivos < 0 || entity.numPedidosActivos > 2) {
            return TurnoErrorBadRequest("Number of active pedidos cannot exceed 2 or be lower than 0.")
        }
        if (entity.tarea2Id == null && entity.numPedidosActivos > 1) {
            return TurnoErrorBadRequest("Number of active pedidos cannot exceed 1 when there is only 1 task.")
        }

        return DBManager.database.getCollection<Turno>().save(entity)
            .let { TurnoSuccess(200, entity) }
            .run { TurnoInternalException("There has been a problem updating $entity.") }
    }

    override suspend fun setFinalizado(id: Id<Turno>): TurnoResult<Turno> {
        logger.debug { "setFinalizado($id)" }

        val entity = DBManager.database.getCollection<Turno>().findOneById(id)
            ?: return TurnoErrorNotFound("Turno with id $id not found.")
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
        return DBManager.database.getCollection<Turno>().save(updated)
            .let { TurnoSuccess(200, updated) }
            .run { TurnoInternalException("There has been a problem updating $updated.") }
    }

    override suspend fun delete(id: Id<Turno>): TurnoResult<Turno> {
        logger.debug { "delete($id)" }

        val entity = DBManager.database.getCollection<Turno>().findOneById(id)
        return if (entity == null) {
            TurnoErrorNotFound("Could not delete turno with id $id. Turno not found.")
        } else {
            DBManager.database.getCollection<Turno>().deleteOneById(id)
                .let { TurnoSuccess(200, entity) }
                .run { TurnoInternalException("Could not delete due to unexpected exception.") }
        }
    }

    override suspend fun findById(id: Id<Turno>): TurnoResult<Turno> {
        logger.debug { "findById($id)" }

        val turno = DBManager.database.getCollection<Turno>().findOneById(id)
        return if (turno != null) {
            TurnoSuccess(200, turno)
        } else {
            TurnoErrorNotFound("Turno with id $id not found.")
        }
    }
}