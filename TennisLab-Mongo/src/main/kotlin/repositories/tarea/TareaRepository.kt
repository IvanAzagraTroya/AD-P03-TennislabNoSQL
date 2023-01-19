package repositories.tarea

import db.DBManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext
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

    override suspend fun findAll(): Flow<Tarea> {
        logger.debug { "findAll()" }

        return DBManager.database.getCollection<Tarea>().find().publisher.asFlow()
    }

    override suspend fun save(entity: Tarea): Tarea = withContext(Dispatchers.IO) {
        logger.debug { "save($entity)" }
        
        return@withContext DBManager.database.getCollection<Tarea>().save(entity).let { entity }
    }

    override suspend fun setFinalizada(id: Id<Tarea>): Tarea? {
        logger.debug { "setFinalizada($id)" }

        val entity = DBManager.database.getCollection<Tarea>().findOneById(id)
            ?: return null
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
            .let { updated }
            .run { null }
    }

    override suspend fun delete(id: Id<Tarea>): Tarea? = withContext(Dispatchers.IO){
        logger.debug { "delete($id)" }

        val entity = DBManager.database.getCollection<Tarea>().findOneById(id)
        return@withContext if (entity == null) null
            else{
            DBManager.database.getCollection<Tarea>().deleteOneById(id)
                .let { entity }
                .run { null }
        }
    }

    override suspend fun findById(id: Id<Tarea>): Tarea? = withContext(Dispatchers.IO){
        logger.debug { "findById($id)" }

        val tarea = DBManager.database.getCollection<Tarea>().findOneById(id)
        return@withContext if (tarea != null) {
            tarea
        } else {
            null
        }
    }

    /*private suspend fun checkFieldsAreCorrect(entity: Tarea): TareaResult<Tarea>? {
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
    }*/
}