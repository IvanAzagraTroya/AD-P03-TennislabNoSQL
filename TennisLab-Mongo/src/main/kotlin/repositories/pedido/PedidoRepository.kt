package repositories.pedido

import db.DBManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import models.pedido.*
import models.pedido.Pedido
import models.pedido.PedidoErrorNotFound
import models.pedido.PedidoInternalException
import models.pedido.PedidoSuccess
import models.tarea.Tarea
import models.tarea.TareaSuccess
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.coroutine.toList
import repositories.tarea.TareaRepository

private val logger = KotlinLogging.logger {}

class PedidoRepository: IPedidoRepository<Id<Pedido>> {
    override suspend fun findAllRealTime() = flow {
        do {
            emit(DBManager.database.getCollection<Pedido>().find().publisher.toList())
            delay(1000)
        } while (true)
    }

    override suspend fun findAll(): PedidoResult<List<Pedido>> {
        logger.debug { "findAll()" }

        val pedidos: List<Pedido> = DBManager.database.getCollection<Pedido>().find().publisher.toList()
        return if (pedidos.isEmpty()) {
            PedidoErrorNotFound("Could not find any pedidos.")
        } else {
            PedidoSuccess(200, pedidos)
        }
    }

    override suspend fun save(entity: Pedido): PedidoResult<Pedido> {
        logger.debug { "save($entity)" }

        val check = checkFieldsAreCorrect(entity)
        if (check != null) return check

        return DBManager.database.getCollection<Pedido>().save(entity)
            .let { PedidoSuccess(201, entity) }
            .run { PedidoInternalException("There has been a problem inserting $entity.") }
    }

    override suspend fun update(entity: Pedido): PedidoResult<Pedido> {
        logger.debug { "update($entity)" }

        val check = checkFieldsAreCorrect(entity)
        if (check != null) return check

        return DBManager.database.getCollection<Pedido>().save(entity)
            .let { PedidoSuccess(200, entity) }
            .run { PedidoInternalException("There has been a problem updating $entity.") }
    }

    override suspend fun delete(id: Id<Pedido>): PedidoResult<Pedido> {
        logger.debug { "delete($id)" }

        val entity = DBManager.database.getCollection<Pedido>().findOneById(id)
        return if (entity == null) {
            PedidoErrorNotFound("Could not delete pedido with id $id. Pedido not found.")
        } else {
            DBManager.database.getCollection<Pedido>().deleteOneById(id)
                .let { PedidoSuccess(200, entity) }
                .run { PedidoInternalException("Could not delete due to unexpected exception.") }
        }
    }

    override suspend fun findById(id: Id<Pedido>): PedidoResult<Pedido> {
        logger.debug { "findById($id)" }

        val pedido = DBManager.database.getCollection<Pedido>().findOneById(id)
        return if (pedido != null) {
            PedidoSuccess(200, pedido)
        } else {
            PedidoErrorNotFound("Pedido with id $id not found.")
        }
    }

    private suspend fun checkFieldsAreCorrect(entity: Pedido): PedidoResult<Pedido>? {
        if (entity.fechaSalida.isBefore(entity.fechaEntrada)) {
            return PedidoErrorBadRequest("FechaSalida cannot be before FechaEntrada.") }
        if (entity.topeEntrega.isBefore(entity.fechaSalida)) {
            return PedidoErrorBadRequest("TopeEntrega cannot be before FechaSalida.") }
        val tareasResult = TareaRepository().findAll()
        if (tareasResult is TareaSuccess<List<Tarea>>) {
            entity.precio = 0.0
            tareasResult.data.filter { it.pedidoId == entity.uuid }.forEach { entity.precio += it.precio }
        } else return PedidoErrorBadRequest("Error in Pedido : No tareas found.")
        return null
    }
}