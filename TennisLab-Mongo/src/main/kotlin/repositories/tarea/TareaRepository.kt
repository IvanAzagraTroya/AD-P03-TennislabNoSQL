package repositories.tarea

import db.DBManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import models.producto.Producto
import models.producto.ProductoSuccess
import models.tarea.*
import models.tarea.TareaInternalException
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.coroutine.toList
import repositories.producto.ProductoRepository

private val logger = KotlinLogging.logger {}

class TareaRepository: ITareaRepository<Id<Tarea>> {
    override suspend fun findAllRealTime() = flow {
        do {
            emit(DBManager.database.getCollection<Tarea>().find().publisher.toList())
            delay(1000)
        } while (true)
    }

    override suspend fun findAll(): TareaResult<List<Tarea>> {
        logger.debug { "findAll()" }

        val tareas: List<Tarea> = DBManager.database.getCollection<Tarea>().find().publisher.toList()
        return if (tareas.isEmpty()) {
            TareaErrorNotFound("Could not find any tareas.")
        } else {
            TareaSuccess(200, tareas)
        }
    }

    override suspend fun save(entity: Tarea): TareaResult<Tarea> {
        logger.debug { "save($entity)" }
        
        val check = checkFieldsAreCorrect(entity)
        if (check != null) return check

        return DBManager.database.getCollection<Tarea>().save(entity)
            .let { TareaSuccess(201, entity) }
            .run { TareaInternalException("There has been a problem inserting $entity.") }
    }

    override suspend fun update(entity: Tarea): TareaResult<Tarea> {
        logger.debug { "update($entity)" }

        val check = checkFieldsAreCorrect(entity)
        if (check != null) return check

        return DBManager.database.getCollection<Tarea>().save(entity)
            .let { TareaSuccess(200, entity) }
            .run { TareaInternalException("There has been a problem updating $entity.") }
    }

    override suspend fun setFinalizada(id: Id<Tarea>): TareaResult<Tarea> {
        logger.debug { "setFinalizada($id)" }

        val entity = DBManager.database.getCollection<Tarea>().findOneById(id)
            ?: return TareaErrorNotFound("Tarea with id $id not found.")
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
        return DBManager.database.getCollection<Tarea>().save(updated)
            .let { TareaSuccess(200, updated) }
            .run { TareaInternalException("There has been a problem updating $updated.") }
    }

    override suspend fun delete(id: Id<Tarea>): TareaResult<Tarea> {
        logger.debug { "delete($id)" }

        val entity = DBManager.database.getCollection<Tarea>().findOneById(id)
        return if (entity == null) {
            TareaErrorNotFound("Could not delete tarea with id $id. Tarea not found.")
        } else {
            DBManager.database.getCollection<Tarea>().deleteOneById(id)
                .let { TareaSuccess(200, entity) }
                .run { TareaInternalException("Could not delete due to unexpected exception.") }
        }
    }

    override suspend fun findById(id: Id<Tarea>): TareaResult<Tarea> {
        logger.debug { "findById($id)" }

        val tarea = DBManager.database.getCollection<Tarea>().findOneById(id)
        return if (tarea != null) {
            TareaSuccess(200, tarea)
        } else {
            TareaErrorNotFound("Tarea with id $id not found.")
        }
    }

    private suspend fun checkFieldsAreCorrect(entity: Tarea): TareaResult<Tarea>? {
        if (entity.tipo == TipoTarea.ADQUISICION && entity.productoAdquiridoId == null ) { 
            return TareaErrorBadRequest("Tarea of type ADQUISICION with invalid productoAdquiridoId.") }
        if (entity.tipo == TipoTarea.PERSONALIZACION && 
            (entity.peso == null || entity.balance == null || entity.rigidez == null) ) {
            return TareaErrorBadRequest("Tarea of type PERSONALIZACION with invalid data.") }
        if (entity.tipo == TipoTarea.ENCORDADO &&
            (entity.tensionHorizontal == null || entity.cordajeHorizontalId == null 
            || entity.tensionVertical == null || entity.cordajeVerticalId == null 
            || entity.dosNudos == null) ) {
            return TareaErrorBadRequest("Tarea of type ENCORDADO with invalid data.") }
        val productosResult = ProductoRepository().findAll()
        if (entity.tipo == TipoTarea.ADQUISICION) {
            if (productosResult is ProductoSuccess<List<Producto>>) {
                val producto = productosResult.data.firstOrNull { it.uuid == entity.productoAdquiridoId }
                entity.precio = producto?.precio
                    ?: return TareaErrorBadRequest("Tarea of type ADQUISICION with invalid productoAdquiridoId.")
            } else return TareaErrorBadRequest("Error in Tarea.productoAdquiridoId : No products found.")
        }
        if (entity.tipo == TipoTarea.PERSONALIZACION) {
            entity.precio = 60.0
        }
        if (entity.tipo == TipoTarea.ENCORDADO) {
            if (productosResult is ProductoSuccess<List<Producto>>) {
                val cordajeHorizontal = productosResult.data.firstOrNull { it.uuid == entity.cordajeHorizontalId }
                val cordajeVertical = productosResult.data.firstOrNull { it.uuid == entity.cordajeVerticalId }
                if (cordajeHorizontal == null || cordajeVertical == null)
                    return TareaErrorBadRequest("Tarea of type ENCORDADO with invalid data.")
                else entity.precio = 15.0 + cordajeHorizontal.precio + cordajeVertical.precio
            } else return TareaErrorBadRequest("Error in Tarea of type ENCORDADO : No products found.")
        }
        return null
    }
}