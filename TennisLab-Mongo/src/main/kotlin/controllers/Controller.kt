package controllers

import dto.maquina.MaquinaDTOcreate
import dto.pedido.PedidoDTOcreate
import dto.producto.ProductoDTOcreate
import dto.tarea.AdquisicionDTOcreate
import dto.tarea.EncordadoDTOcreate
import dto.tarea.PersonalizacionDTOcreate
import dto.tarea.TareaDTOcreate
import dto.turno.TurnoDTOcreate
import dto.user.UserDTOLogin
import dto.user.UserDTORegister
import dto.user.UserDTOcreate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mappers.fromDTO
import mappers.toDTO
import models.ResponseError
import models.ResponseSuccess
import models.maquina.Maquina
import models.pedido.Pedido
import models.pedido.PedidoState
import models.producto.Producto
import models.producto.TipoProducto
import models.tarea.Tarea
import models.turno.Turno
import models.user.*
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.litote.kmongo.Id
import repositories.maquina.IMaquinaRepository
import repositories.pedido.IPedidoRepository
import repositories.producto.IProductoRepository
import repositories.tarea.ITareaRepository
import repositories.turno.ITurnoRepository
import repositories.user.UserRepositoryCached
import services.login.checkToken
import services.utils.checkUserEmailAndPhone
import services.utils.fieldsAreIncorrect
import java.time.LocalDateTime
import java.util.*

private val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

@Single
class Controller(
    @Named("UserRepositoryCached")
    private val uRepo: UserRepositoryCached,
    @Named("TurnoRepositoryCached")
    private val turRepo: ITurnoRepository<Id<Turno>>,
    @Named("TareaRepositoryCached")
    private val tarRepo: ITareaRepository<Id<Tarea>>,
    @Named("ProductoRepositoryCached")
    private val proRepo: IProductoRepository<Id<Producto>>,
    @Named("PedidoRepositoryCached")
    private val pedRepo: IPedidoRepository<Id<Pedido>>,
    @Named("MaquinaRepositoryCached")
    private val maRepo: IMaquinaRepository<Id<Maquina>>,
) {
    suspend fun findUserById(id: UUID) : String = withContext(Dispatchers.IO) {
        val user = uRepo.findByUUID(id)

        val res = if (user == null) ResponseError(404, "NOT FOUND: User with id $id not found.")
        else ResponseSuccess(200, user.toDTO())

        json.encodeToString(res)
    }

    suspend fun findUserById(id: Int) : String = withContext(Dispatchers.IO) {
        val user = uRepo.findById(id)

        val res = if (user == null) ResponseError(404, "NOT FOUND: User with id $id not found.")
        else ResponseSuccess(200, user.toDTO())

        json.encodeToString(res)
    }

    suspend fun findAllUsers() : String = withContext(Dispatchers.IO) {
        val users = uRepo.findAll().toList()

        val res = if (users.isEmpty()) ResponseError(404, "NOT FOUND: No users found.")
        else ResponseSuccess(200, toDTO(users))

        json.encodeToString(res)
    }

    suspend fun findAllUsersWithActivity(active: Boolean) : String = withContext(Dispatchers.IO) {
        val users = uRepo.findAll().toList().filter { it.activo == active }

        val res = if (users.isEmpty()) ResponseError(404, "NOT FOUND: No users found.")
        else ResponseSuccess(200, toDTO(users))

        json.encodeToString(res)
    }

    suspend fun createUser(user: UserDTOcreate, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext validated

        if (fieldsAreIncorrect(user))
            return@withContext json.encodeToString(ResponseError(400, "BAD REQUEST: Cannot insert user. Incorrect fields."))
        if (checkUserEmailAndPhone(user, uRepo))
            return@withContext json.encodeToString(ResponseError(400, "BAD REQUEST: Cannot insert user."))

        val res = uRepo.save(user.fromDTO())
        json.encodeToString(ResponseSuccess(201, res.toDTO()))
    }

    suspend fun setInactiveUser(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext validated

        val user = uRepo.findByUUID(id)
            ?: return@withContext json.encodeToString(ResponseError(404, "NOT FOUND: Cannot set inactive. User with id $id not found."))
        val result = uRepo.setInactive(user.id)
            ?: return@withContext json.encodeToString(ResponseError(500, "INTERNAL EXCEPTION: Unexpected error. Cannot find and set inactive user with id $id."))
        json.encodeToString(ResponseSuccess(200, result.toDTO()))
    }

    suspend fun deleteUser(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext validated

        val user = uRepo.findByUUID(id)
            ?: return@withContext json.encodeToString(ResponseError(404, "NOT FOUND: Cannot delete. User with id $id not found."))
        val result = uRepo.delete(user.id)
            ?: return@withContext json.encodeToString(ResponseError(500, "INTERNAL EXCEPTION: Unexpected error. Cannot delete user with id $id."))
        json.encodeToString(ResponseSuccess(200, result.toDTO()))
    }

    suspend fun findPedidoById(id: UUID) : String = withContext(Dispatchers.IO) {
        val entity = pedRepo.findByUUID(id)

        val res = if (entity == null) ResponseError(404, "NOT FOUND: Pedido with id $id not found.")
        else ResponseSuccess(200, entity.toDTO())

        json.encodeToString(res)
    }

    suspend fun findAllPedidos() : String = withContext(Dispatchers.IO) {
        val entities = pedRepo.findAll().toList()

        val res = if (entities.isEmpty()) ResponseError(404, "NOT FOUND: No pedidos found.")
        else ResponseSuccess(200, toDTO(entities))

        json.encodeToString(res)
    }

    suspend fun findAllPedidosWithState(state: PedidoState) : String = withContext(Dispatchers.IO) {
        val entities = pedRepo.findAll().toList().filter { it.state == state }

        val res = if (entities.isEmpty()) ResponseError(404, "NOT FOUND: No pedidos found with state = $state.")
        else ResponseSuccess(200, toDTO(entities))

        json.encodeToString(res)
    }

    suspend fun createPedido(entity: PedidoDTOcreate, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext validated

        if (fieldsAreIncorrect(entity))
            return@withContext json.encodeToString(ResponseError(400, "BAD REQUEST: Cannot insert pedido. Incorrect fields."))
        if (uRepo.findById(entity.user.fromDTO().id) == null)
            return@withContext json.encodeToString(ResponseError(400, "BAD REQUEST: Cannot insert pedido. User not found."))

        entity.tareas.forEach { tarRepo.save(it.fromDTO()) }
        val res = pedRepo.save(entity.fromDTO())
        json.encodeToString(ResponseSuccess(201, res.toDTO()))
    }

    suspend fun deletePedido(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext validated

        val entity = pedRepo.findByUUID(id)
            ?: return@withContext json.encodeToString(ResponseError(404, "NOT FOUND: Cannot delete. Pedido with id $id not found."))
        tarRepo.findAll().filter { it.pedidoId == id }.toList().forEach { tarRepo.delete(it.id) }
        val result = pedRepo.delete(entity.id)
            ?: return@withContext json.encodeToString(ResponseError(500, "INTERNAL EXCEPTION: Unexpected error. Cannot delete pedido with id $id."))
        json.encodeToString(ResponseSuccess(200, result.toDTO()))
    }

    suspend fun findProductoById(id: UUID) : String = withContext(Dispatchers.IO) {
        val entity = proRepo.findByUUID(id)

        val res = if (entity == null) ResponseError(404, "NOT FOUND: Producto with id $id not found.")
        else ResponseSuccess(200, entity.toDTO())

        json.encodeToString(res)
    }

    suspend fun findAllProductos() : String = withContext(Dispatchers.IO) {
        val entities = proRepo.findAll().toList()

        val res = if (entities.isEmpty()) ResponseError(404, "NOT FOUND: No productos found.")
        else ResponseSuccess(200, toDTO(entities))

        json.encodeToString(res)
    }

    suspend fun findAllProductosDisponibles() : String = withContext(Dispatchers.IO) {
        val entities = proRepo.findAll().toList().filter { it.stock > 0 }

        val res = if (entities.isEmpty()) ResponseError(404, "NOT FOUND: There are no products available.")
        else ResponseSuccess(200, toDTO(entities))

        json.encodeToString(res)
    }

    suspend fun createProducto(entity: ProductoDTOcreate, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext validated

        if (fieldsAreIncorrect(entity))
            return@withContext json.encodeToString(ResponseError(400, "BAD REQUEST: Cannot insert producto. Incorrect fields."))

        val res = proRepo.save(entity.fromDTO())
        json.encodeToString(ResponseSuccess(201, res.toDTO()))
    }

    suspend fun deleteProducto(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext validated

        val entity = proRepo.findByUUID(id)
            ?: return@withContext json.encodeToString(ResponseError(404, "NOT FOUND: Cannot delete. Producto with id $id not found."))
        val result = proRepo.delete(entity.id)
            ?: return@withContext json.encodeToString(ResponseError(500, "INTERNAL EXCEPTION: Unexpected error. Cannot delete producto with id $id."))
        json.encodeToString(ResponseSuccess(200, result.toDTO()))
    }

    suspend fun decreaseStockFromProducto(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext validated

        val entity = proRepo.findByUUID(id)
            ?: return@withContext json.encodeToString(ResponseError(404, "NOT FOUND: Cannot decrease stock. Producto with id $id not found."))
        val result = proRepo.decreaseStock(entity.id)
            ?: return@withContext json.encodeToString(ResponseError(404, "NOT FOUND: Cannot decrease stock. Producto with id $id not found."))
        json.encodeToString(ResponseSuccess(200, result.toDTO()))
    }

    suspend fun findMaquinaById(id: UUID) : String = withContext(Dispatchers.IO) {
        val entity = maRepo.findByUUID(id)

        val res = if (entity == null) ResponseError(404, "NOT FOUND: Maquina with id $id not found.")
        else ResponseSuccess(200, entity.toDTO())

        json.encodeToString(res)
    }

    suspend fun findAllMaquinas() : String = withContext(Dispatchers.IO) {
        val entities = maRepo.findAll().toList()

        val res = if (entities.isEmpty()) ResponseError(404, "NOT FOUND: No maquinas found.")
        else ResponseSuccess(200, toDTO(entities))

        json.encodeToString(res)
    }

    suspend fun createMaquina(entity: MaquinaDTOcreate, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext validated

        if (fieldsAreIncorrect(entity))
            return@withContext json.encodeToString(ResponseError(400, "BAD REQUEST: Cannot insert maquina. Incorrect fields."))

        val res = maRepo.save(entity.fromDTO())
        json.encodeToString(ResponseSuccess(201, res.toDTO()))
    }

    suspend fun deleteMaquina(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext validated

        val entity = maRepo.findByUUID(id)
            ?: return@withContext json.encodeToString(ResponseError(404, "NOT FOUND: Cannot delete. Maquina with id $id not found."))
        val result = maRepo.delete(entity.id)
            ?: return@withContext json.encodeToString(ResponseError(500, "INTERNAL EXCEPTION: Unexpected error. Cannot delete Maquina with id $id."))
        json.encodeToString(ResponseSuccess(200, result.toDTO()))
    }

    suspend fun setInactiveMaquina(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext validated

        val entity = maRepo.findByUUID(id)
            ?: return@withContext json.encodeToString(ResponseError(404, "NOT FOUND: Cannot set inactive. Maquina with id $id not found."))
        val result = maRepo.setInactive(entity.id)
            ?: return@withContext json.encodeToString(ResponseError(500, "INTERNAL EXCEPTION: Unexpected error. Cannot find and set inactive maquina with id $id."))
        json.encodeToString(ResponseSuccess(200, result.toDTO()))
    }

    suspend fun findTurnoById(id: UUID) : String = withContext(Dispatchers.IO) {
        val entity = turRepo.findByUUID(id)

        val res = if (entity == null) ResponseError(404, "NOT FOUND: Turno with id $id not found.")
        else ResponseSuccess(200, entity.toDTO())

        json.encodeToString(res)
    }

    suspend fun findAllTurnos() : String = withContext(Dispatchers.IO) {
        val entities = turRepo.findAll().toList()

        val res = if (entities.isEmpty()) ResponseError(404, "NOT FOUND: No turnos found.")
        else ResponseSuccess(200, toDTO(entities))

        json.encodeToString(res)
    }

    suspend fun findAllTurnosByFecha(horaInicio: LocalDateTime) : String = withContext(Dispatchers.IO) {
        val entities = turRepo.findAll().toList().filter { it.horaInicio == horaInicio }

        val res = if (entities.isEmpty()) ResponseError(404, "NOT FOUND: No turnos found.")
        else ResponseSuccess(200, toDTO(entities))

        json.encodeToString(res)
    }

    suspend fun createTurno(entity: TurnoDTOcreate, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.WORKER)
        if (validated != null) return@withContext validated

        if (fieldsAreIncorrect(entity))
            return@withContext json.encodeToString(ResponseError(400, "BAD REQUEST: Cannot insert turno. Incorrect fields."))

        val res = turRepo.save(entity.fromDTO())
        json.encodeToString(ResponseSuccess(201, res.toDTO()))
    }

    suspend fun deleteTurno(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext validated

        val entity = turRepo.findByUUID(id)
            ?: return@withContext json.encodeToString(ResponseError(404, "NOT FOUND: Cannot delete. Turno with id $id not found."))
        val result = turRepo.delete(entity.id)
            ?: return@withContext json.encodeToString(ResponseError(500, "INTERNAL EXCEPTION: Unexpected error. Cannot delete Turno with id $id."))
        json.encodeToString(ResponseSuccess(200, result.toDTO()))
    }

    suspend fun setFinalizadoTurno(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext validated

        val entity = turRepo.findByUUID(id)
            ?: return@withContext json.encodeToString(ResponseError(404, "NOT FOUND: Cannot set finalizado. Turno with id $id not found."))
        val result = turRepo.setFinalizado(entity.id)
            ?: return@withContext json.encodeToString(ResponseError(500, "INTERNAL EXCEPTION: Unexpected error. Cannot find and set finalizado turno with id $id."))
        json.encodeToString(ResponseSuccess(200, result.toDTO()))
    }

    suspend fun findTareaById(id: UUID) : String = withContext(Dispatchers.IO) {
        val entity = tarRepo.findByUUID(id)

        val res = if (entity == null) ResponseError(404, "NOT FOUND: Tarea with id $id not found.")
        else ResponseSuccess(200, entity.toDTO())

        json.encodeToString(res)
    }

    suspend fun findAllTareas() : String = withContext(Dispatchers.IO) {
        val entities = tarRepo.findAll().toList()

        val res = if (entities.isEmpty()) ResponseError(404, "NOT FOUND: No tareas found.")
        else ResponseSuccess(200, toDTO(entities))

        json.encodeToString(res)
    }

    suspend fun findAllTareasFinalizadas(finalizada: Boolean) : String = withContext(Dispatchers.IO) {
        val entities = tarRepo.findAll().toList().filter { it.finalizada == finalizada }

        val res = if (entities.isEmpty()) ResponseError(404, "NOT FOUND: No tareas found.")
        else ResponseSuccess(200, toDTO(entities))

        json.encodeToString(res)
    }

    suspend fun createTarea(entity: TareaDTOcreate, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext validated

        if (fieldsAreIncorrect(entity))
            return@withContext json.encodeToString(ResponseError(400, "BAD REQUEST: Cannot insert tarea. Incorrect fields."))
        if (entity is EncordadoDTOcreate
            && (
            (entity.cordajeHorizontal.uuid == entity.cordajeVertical.uuid
            && entity.cordajeHorizontal.stock < 2) ||
            (entity.cordajeHorizontal.uuid != entity.cordajeVertical.uuid
            && entity.cordajeHorizontal.stock < 1) ||
            (entity.cordajeHorizontal.uuid != entity.cordajeVertical.uuid
            && entity.cordajeVertical.stock < 1)
            ))
            return@withContext json.encodeToString(ResponseError(400, "BAD REQUEST: Cannot insert tarea. Not enough material for cordaje."))

        when (entity) {
            is EncordadoDTOcreate -> {
                if (entity.raqueta.tipo != TipoProducto.RAQUETAS ||
                    entity.cordajeHorizontal.tipo != TipoProducto.CORDAJES ||
                    entity.cordajeVertical.tipo != TipoProducto.CORDAJES)
                    return@withContext json.encodeToString(ResponseError(400, "BAD REQUEST: Cannot insert tarea. Type mismatch in product types."))
            }
            is AdquisicionDTOcreate -> {
                if (entity.raqueta.tipo != TipoProducto.RAQUETAS)
                    return@withContext json.encodeToString(ResponseError(400, "BAD REQUEST: Cannot insert tarea. Parameter raqueta is not of type Raqueta."))
            }
            is PersonalizacionDTOcreate -> {
                if (entity.raqueta.tipo != TipoProducto.RAQUETAS)
                    return@withContext json.encodeToString(ResponseError(400, "BAD REQUEST: Cannot insert tarea. Parameter raqueta is not of type Raqueta."))
            }
        }

        val res = tarRepo.save(entity.fromDTO())
        json.encodeToString(ResponseSuccess(201, res.toDTO()))
    }

    suspend fun deleteTarea(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext validated

        val entity = tarRepo.findByUUID(id)
            ?: return@withContext json.encodeToString(ResponseError(404, "NOT FOUND: Cannot delete. Tarea with id $id not found."))
        val result = tarRepo.delete(entity.id)
            ?: return@withContext json.encodeToString(ResponseError(500, "INTERNAL EXCEPTION: Unexpected error. Cannot delete tarea with id $id."))
        json.encodeToString(ResponseSuccess(200, result.toDTO()))
    }

    suspend fun setFinalizadaTarea(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext validated

        val entity = tarRepo.findByUUID(id)
            ?: return@withContext json.encodeToString(ResponseError(404, "NOT FOUND: Cannot set finalizado. Tarea with id $id not found."))
        val result = tarRepo.setFinalizada(entity.id)
            ?: return@withContext json.encodeToString(ResponseError(500, "INTERNAL EXCEPTION: Unexpected error. Cannot find and set finalizada tarea with id $id."))
        json.encodeToString(ResponseSuccess(200, result.toDTO()))
    }

    suspend fun login(user: UserDTOLogin): String = withContext(Dispatchers.IO) {
        val token = services.login.login(user, uRepo)
        if (token == null) json.encodeToString(ResponseError(404, "NOT FOUND: Unable to login. Incorrect email or password."))
        else json.encodeToString(ResponseSuccess<String>(200, token))
    }

    suspend fun register(user: UserDTORegister): String = withContext(Dispatchers.IO) {
        val token = services.login.register(user, uRepo)
        if (token == null) json.encodeToString(ResponseError(400, "BAD REQUEST: Unable to register. Incorrect parameters."))
        else json.encodeToString(ResponseSuccess<String>(200, token))
    }
}