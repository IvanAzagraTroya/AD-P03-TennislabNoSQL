package repositories.maquina

import db.DBManager
import kotlinx.coroutines.flow.Flow
import models.maquina.*
import models.turno.Turno
import models.turno.TurnoInternalException
import models.turno.TurnoSuccess
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.coroutine.toList
import java.time.LocalDate
import java.util.*

private val logger = KotlinLogging.logger{}
class MaquinaRepository: IMaquinaRepository<Id<Maquina>> {
    override suspend fun findAllRealTime(): Flow<List<Maquina>> {
        TODO("Not yet implemented")
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
        if(entity.tipo != TipoMaquina.ENCORDADORA || entity.tipo != TipoMaquina.PERSONALIZADORA){
            return MaquinaErrorBadRequest("There's no machine with type ${entity.tipo}")
        }
        if(entity.fechaAdquisicion > LocalDate.now()){
            return MaquinaErrorBadRequest("There's an error on adquisition date")
        }
        return DBManager.database.getCollection<Maquina>().save(entity)
            .let { MaquinaSuccess(201, entity) }
            .run { MaquinaInternalException("There has been a problem inserting $entity.") }
    }

    override suspend fun update(entity: Maquina): MaquinaResult<Maquina> {
        TODO("Not yet implemented")
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
        return DBManager.database.getCollection<Maquina>().save(entity)
            .let { MaquinaSuccess(200, entity) }
            .run { MaquinaInternalException("There has been a problem updating $entity.") }
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
}