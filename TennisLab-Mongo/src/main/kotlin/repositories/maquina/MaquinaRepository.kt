package repositories.maquina

import db.DBManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext
import models.maquina.*
import models.pedido.Pedido
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.eq
import java.util.*

private val logger = KotlinLogging.logger{}

class MaquinaRepository: IMaquinaRepository<Id<Maquina>> {
    override suspend fun findAllRealTime() = flow {
        do {
            emit(DBManager.database.getCollection<Maquina>().find().publisher.toList())
            delay(1000)
        } while (true)
    }

    override fun findAll(): Flow<Maquina> {
        logger.debug { "findAll()" }
        return DBManager.database.getCollection<Maquina>().find().publisher.asFlow()
    }

    override suspend fun findByUUID(id: UUID): Maquina? = withContext(Dispatchers.IO) {
        logger.debug { "findByUUID($id)" }

        DBManager.database.getCollection<Maquina>().findOne(Maquina::uuid eq id)
    }

    override suspend fun save(entity: Maquina): Maquina = withContext(Dispatchers.IO) {
        logger.debug { "save($entity)" }

        DBManager.database.getCollection<Maquina>().save(entity).let { entity }
    }

    override suspend fun setInactive(id: Id<Maquina>): Maquina? = withContext(Dispatchers.IO) {
        logger.debug { "setInactive($id)" }

        val entity = DBManager.database.getCollection<Maquina>().findOneById(id)
            ?: return@withContext null
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
        DBManager.database.getCollection<Maquina>().save(updated).let { updated }
    }

    override suspend fun delete(id: Id<Maquina>): Maquina? = withContext(Dispatchers.IO) {
        logger.debug { "delete($id)" }

        val entity = DBManager.database.getCollection<Maquina>().findOneById(id)
        DBManager.database.getCollection<Maquina>().deleteOneById(id).let { entity }
        }

    override suspend fun findById(id: Id<Maquina>): Maquina? = withContext(Dispatchers.IO) {
        logger.debug { "findById($id)" }

        DBManager.database.getCollection<Maquina>().findOneById(id)
    }
}