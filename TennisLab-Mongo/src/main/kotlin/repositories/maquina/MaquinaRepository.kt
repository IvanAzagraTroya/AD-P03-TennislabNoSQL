package repositories.maquina

import db.DBManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import models.maquina.*
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.coroutine.toList
import java.time.LocalDate

private val logger = KotlinLogging.logger{}

class MaquinaRepository: IMaquinaRepository<Id<Maquina>> {
    override suspend fun findAllRealTime() = flow {
        do {
            emit(DBManager.database.getCollection<Maquina>().find().publisher.toList())
            delay(1000)
        } while (true)
    }

    override suspend fun findAll(): MaquinaResult<List<Maquina>> {
        logger.debug { "findAll()" }
        val maquina: List<Maquina> = DBManager.database.getCollection<Maquina>().find().publisher.toList()
        return if (maquina.isEmpty()){
            MaquinaErrorNotFound("Could not find any máquinas")
        } else{
            MaquinaSuccess(200, maquina)
        }
    }

    override suspend fun save(entity: Maquina): MaquinaResult<Maquina> {
        logger.debug { "save($entity)" }

        val check = checkFieldsAreCorrect(entity)
        if (check != null) return check

        return DBManager.database.getCollection<Maquina>().save(entity)
            .let { MaquinaSuccess(201, entity) }
            .run { MaquinaInternalException("There has been a problem inserting $entity.") }
    }

    override suspend fun update(entity: Maquina): MaquinaResult<Maquina> {
        logger.debug { "update($entity)" }

        val check = checkFieldsAreCorrect(entity)
        if (check != null) return check

        return DBManager.database.getCollection<Maquina>().save(entity)
            .let { MaquinaSuccess(200, entity) }
            .run { MaquinaInternalException("There has been a problem updating $entity.") }
    }

    override suspend fun setInactive(id: Id<Maquina>): MaquinaResult<Maquina> {
        logger.debug { "setInactive($id)" }

        val entity = DBManager.database.getCollection<Maquina>().findOneById(id)
            ?: return MaquinaErrorNotFound("Máquina with id $id not found")
        val updated = Maquina(
            id = entity.id,
            uuid = entity.uuid,
            modelo = entity.modelo,
            marca = entity.marca,
            fechaAdquisicion = entity.fechaAdquisicion,
            numeroSerie = entity.numeroSerie,
            tipo = entity.tipo,
            activa = false,
            isManual = entity.isManual,
            maxTension = entity.maxTension,
            minTension = entity.minTension,
            measuresManeuverability = entity.measuresManeuverability,
            measuresRigidity = entity.measuresRigidity,
            measuresBalance = entity.measuresBalance
        )
        return DBManager.database.getCollection<Maquina>().save(updated)
            .let { MaquinaSuccess(200, updated) }
            .run { MaquinaInternalException("There has been a problem updating $updated.") }
    }

    override suspend fun delete(id: Id<Maquina>): MaquinaResult<Maquina> {
        logger.debug { "delete($id)" }

        val entity = DBManager.database.getCollection<Maquina>().findOneById(id)
        return if (entity == null) {
            MaquinaErrorNotFound("Could not delete maquina with id: $id. Maquina not found")
        }else {
            DBManager.database.getCollection<Maquina>().deleteOneById(id)
                .let { MaquinaSuccess(200, entity) }
                .run { MaquinaInternalException("Could not delete due to unexpected exception.") }
        }
    }

    override suspend fun findById(id: Id<Maquina>): MaquinaResult<Maquina> {
        logger.debug { "findById($id)" }

        val maquina = DBManager.database.getCollection<Maquina>().findOneById(id)
        return if (maquina != null) {
            MaquinaSuccess(200, maquina)
        } else {
            MaquinaErrorNotFound("Turno with id $id not found.")
        }
    }

    private fun checkFieldsAreCorrect(entity: Maquina): MaquinaResult<Maquina>? {
        if (entity.modelo.isBlank()) { return MaquinaErrorBadRequest("Model cannot be blank.") }
        if (entity.marca.isBlank()) { return MaquinaErrorBadRequest("Brand cannot be blank.") }
        if (entity.numeroSerie.isBlank()) { return MaquinaErrorBadRequest("Serial number cannot be blank.") }
        if(entity.fechaAdquisicion > LocalDate.now()) {
            return MaquinaErrorBadRequest("Acquisition date cannot be in the future.") }
        if (entity.tipo == TipoMaquina.ENCORDADORA &&
            (entity.isManual == null || entity.minTension == null || entity.maxTension == null) ) {
            return MaquinaErrorBadRequest("Maquina of type ENCORDADORA with invalid data.") }
        if (entity.tipo == TipoMaquina.PERSONALIZADORA &&
            (entity.measuresManeuverability == null || entity.measuresBalance == null ||
            entity.measuresRigidity == null) ) {
            return MaquinaErrorBadRequest("Maquina of type PERSONALIZADORA with invalid data.") }
        if (entity.tipo == TipoMaquina.ENCORDADORA && entity.maxTension!! <= entity.minTension!!) {
            return MaquinaErrorBadRequest("ENCORDADORA: Max tension cannot be lower or equal than min tension.") }
        if (entity.tipo == TipoMaquina.ENCORDADORA && entity.minTension!! <= 0 ) {
            return MaquinaErrorBadRequest("ENCORDADORA: There must not be negative values in tension parameters.") }
        return null
    }
}