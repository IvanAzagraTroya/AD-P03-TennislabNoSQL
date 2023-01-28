package controllers

import TurnoResponseError
import TurnoResponseSuccess
import dto.maquina.EncordadoraDTOcreate
import dto.maquina.MaquinaDTOcreate
import dto.pedido.PedidoDTOcreate
import dto.producto.ProductoDTOcreate
import dto.tarea.AdquisicionDTOcreate
import dto.tarea.EncordadoDTOcreate
import dto.tarea.PersonalizacionDTOcreate
import dto.tarea.TareaDTOcreate
import dto.turno.TurnoDTOcreate
import dto.user.UserDTOcreate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mappers.fromDTO
import mappers.toDTO
import models.maquina.Maquina
import models.maquina.MaquinaResponseError
import models.maquina.MaquinaResponseSuccess
import models.pedido.Pedido
import models.pedido.PedidoResponseError
import models.pedido.PedidoResponseSuccess
import models.pedido.PedidoState
import models.producto.Producto
import models.producto.ProductoResponseError
import models.producto.ProductoResponseSuccess
import models.producto.TipoProducto
import models.tarea.Tarea
import models.tarea.TareaResponseError
import models.tarea.TareaResponseSuccess
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
import services.fieldsAreIncorrect
import java.time.LocalDateTime
import java.util.UUID

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

        val res = if (user == null) UserResponseError(404, "NOT FOUND: User with id $id not found.")
        else UserResponseSuccess(200, user.toDTO())

        Json.encodeToString(res)
    }

    suspend fun findUserById(id: Int) : String = withContext(Dispatchers.IO) {
        val user = uRepo.findById(id)

        val res = if (user == null) UserResponseError(404, "NOT FOUND: User with id $id not found.")
        else UserResponseSuccess(200, user.toDTO())

        Json.encodeToString(res)
    }

    suspend fun findAllUsers() : String = withContext(Dispatchers.IO) {
        val users = uRepo.findAll().toList()

        val res = if (users.isEmpty()) UserResponseError(404, "NOT FOUND: No users found.")
        else UserResponseSuccess(200, toDTO(users))

        Json.encodeToString(res)
    }

    suspend fun findAllUsersWithActivity(active: Boolean) : String = withContext(Dispatchers.IO) {
        val users = uRepo.findAll().toList().filter { it.activo == active }

        val res = if (users.isEmpty()) UserResponseError(404, "NOT FOUND: No users found.")
        else UserResponseSuccess(200, toDTO(users))

        Json.encodeToString(res)
    }

    suspend fun createUser(user: UserDTOcreate) : String = withContext(Dispatchers.IO) {
        if (fieldsAreIncorrect(user))
            return@withContext Json.encodeToString(UserResponseError(400, "BAD REQUEST: Cannot insert user. Incorrect fields."))

        val res = uRepo.save(user.fromDTO())
        Json.encodeToString(UserResponseSuccess(201, res.toDTO()))
    }

    suspend fun setInactiveUser(id: UUID) : String = withContext(Dispatchers.IO) {
        val user = uRepo.findByUUID(id)
            ?: return@withContext Json.encodeToString(UserResponseError(404, "NOT FOUND: Cannot set inactive. User with id $id not found."))
        val result = uRepo.setInactive(user.id)
            ?: return@withContext Json.encodeToString(UserResponseError(500, "INTERNAL EXCEPTION: Unexpected error. Cannot find and set inactive user with id $id."))
        Json.encodeToString(UserResponseSuccess(200, result.toDTO()))
    }

    suspend fun deleteUser(id: UUID) : String = withContext(Dispatchers.IO) {
        val user = uRepo.findByUUID(id)
            ?: return@withContext Json.encodeToString(UserResponseError(404, "NOT FOUND: Cannot delete. User with id $id not found."))
        val result = uRepo.delete(user.id)
            ?: return@withContext Json.encodeToString(UserResponseError(500, "INTERNAL EXCEPTION: Unexpected error. Cannot delete user with id $id."))
        Json.encodeToString(UserResponseSuccess(200, result.toDTO()))
    }

    suspend fun findPedidoById(id: UUID) : String = withContext(Dispatchers.IO) {
        val entity = pedRepo.findByUUID(id)

        val res = if (entity == null) PedidoResponseError(404, "NOT FOUND: Pedido with id $id not found.")
        else PedidoResponseSuccess(200, entity.toDTO())

        Json.encodeToString(res)
    }

    suspend fun findAllPedidos() : String = withContext(Dispatchers.IO) {
        val entities = pedRepo.findAll().toList()

        val res = if (entities.isEmpty()) PedidoResponseError(404, "NOT FOUND: No pedidos found.")
        else PedidoResponseSuccess(200, toDTO(entities))

        Json.encodeToString(res)
    }

    suspend fun findAllPedidosWithState(state: PedidoState) : String = withContext(Dispatchers.IO) {
        val entities = pedRepo.findAll().toList().filter { it.state == state }

        val res = if (entities.isEmpty()) PedidoResponseError(404, "NOT FOUND: No pedidos found with state = $state.")
        else PedidoResponseSuccess(200, toDTO(entities))

        Json.encodeToString(res)
    }

    suspend fun createPedido(entity: PedidoDTOcreate) : String = withContext(Dispatchers.IO) {
        if (fieldsAreIncorrect(entity))
            return@withContext Json.encodeToString(PedidoResponseError(400, "BAD REQUEST: Cannot insert pedido. Incorrect fields."))
        if (uRepo.findById(entity.user.fromDTO().id) == null)
            return@withContext Json.encodeToString(PedidoResponseError(400, "BAD REQUEST: Cannot insert pedido. User not found."))

        entity.tareas.forEach { tarRepo.save(it.fromDTO()) }
        val res = pedRepo.save(entity.fromDTO())
        Json.encodeToString(PedidoResponseSuccess(201, res.toDTO()))
    }

    suspend fun deletePedido(id: UUID) : String = withContext(Dispatchers.IO) {
        val entity = pedRepo.findByUUID(id)
            ?: return@withContext Json.encodeToString(PedidoResponseError(404, "NOT FOUND: Cannot delete. Pedido with id $id not found."))
        tarRepo.findAll().filter { it.pedidoId == id }.toList().forEach { tarRepo.delete(it.id) }
        val result = pedRepo.delete(entity.id)
            ?: return@withContext Json.encodeToString(PedidoResponseError(500, "INTERNAL EXCEPTION: Unexpected error. Cannot delete pedido with id $id."))
        Json.encodeToString(PedidoResponseSuccess(200, result.toDTO()))
    }

    suspend fun findProductoById(id: UUID) : String = withContext(Dispatchers.IO) {
        val entity = proRepo.findByUUID(id)

        val res = if (entity == null) ProductoResponseError(404, "NOT FOUND: Producto with id $id not found.")
        else ProductoResponseSuccess(200, entity.toDTO())

        Json.encodeToString(res)
    }

    suspend fun findAllProductos() : String = withContext(Dispatchers.IO) {
        val entities = proRepo.findAll().toList()

        val res = if (entities.isEmpty()) ProductoResponseError(404, "NOT FOUND: No productos found.")
        else ProductoResponseSuccess(200, toDTO(entities))

        Json.encodeToString(res)
    }

    suspend fun findAllProductosDisponibles() : String = withContext(Dispatchers.IO) {
        val entities = proRepo.findAll().toList().filter { it.stock > 0 }

        val res = if (entities.isEmpty()) ProductoResponseError(404, "NOT FOUND: There are no products available.")
        else ProductoResponseSuccess(200, toDTO(entities))

        Json.encodeToString(res)
    }

    suspend fun createProducto(entity: ProductoDTOcreate) : String = withContext(Dispatchers.IO) {
        if (fieldsAreIncorrect(entity))
            return@withContext Json.encodeToString(ProductoResponseError(400, "BAD REQUEST: Cannot insert producto. Incorrect fields."))

        val res = proRepo.save(entity.fromDTO())
        Json.encodeToString(ProductoResponseSuccess(201, res.toDTO()))
    }

    suspend fun deleteProducto(id: UUID) : String = withContext(Dispatchers.IO) {
        val entity = proRepo.findByUUID(id)
            ?: return@withContext Json.encodeToString(ProductoResponseError(404, "NOT FOUND: Cannot delete. Producto with id $id not found."))
        val result = proRepo.delete(entity.id)
            ?: return@withContext Json.encodeToString(ProductoResponseError(500, "INTERNAL EXCEPTION: Unexpected error. Cannot delete producto with id $id."))
        Json.encodeToString(ProductoResponseSuccess(200, result.toDTO()))
    }

    suspend fun decreaseStockFromProducto(id: UUID) : String = withContext(Dispatchers.IO) {
        val entity = proRepo.findByUUID(id)
            ?: return@withContext Json.encodeToString(ProductoResponseError(404, "NOT FOUND: Cannot decrease stock. Producto with id $id not found."))
        val result = proRepo.decreaseStock(entity.id)
            ?: return@withContext Json.encodeToString(ProductoResponseError(404, "NOT FOUND: Cannot decrease stock. Producto with id $id not found."))
        Json.encodeToString(ProductoResponseSuccess(200, result.toDTO()))
    }

    suspend fun findMaquinaById(id: UUID) : String = withContext(Dispatchers.IO) {
        val entity = maRepo.findByUUID(id)

        val res = if (entity == null) MaquinaResponseError(404, "NOT FOUND: Maquina with id $id not found.")
        else MaquinaResponseSuccess(200, entity.toDTO())

        Json.encodeToString(res)
    }

    suspend fun findAllMaquinas() : String = withContext(Dispatchers.IO) {
        val entities = maRepo.findAll().toList()

        val res = if (entities.isEmpty()) MaquinaResponseError(404, "NOT FOUND: No maquinas found.")
        else MaquinaResponseSuccess(200, toDTO(entities))

        Json.encodeToString(res)
    }

    suspend fun createMaquina(entity: MaquinaDTOcreate) : String = withContext(Dispatchers.IO) {
        if (fieldsAreIncorrect(entity))
            return@withContext Json.encodeToString(MaquinaResponseError(400, "BAD REQUEST: Cannot insert maquina. Incorrect fields."))

        val res = maRepo.save(entity.fromDTO())
        Json.encodeToString(MaquinaResponseSuccess(201, res.toDTO()))
    }

    suspend fun deleteMaquina(id: UUID) : String = withContext(Dispatchers.IO) {
        val entity = maRepo.findByUUID(id)
            ?: return@withContext Json.encodeToString(MaquinaResponseError(404, "NOT FOUND: Cannot delete. Maquina with id $id not found."))
        val result = maRepo.delete(entity.id)
            ?: return@withContext Json.encodeToString(MaquinaResponseError(500, "INTERNAL EXCEPTION: Unexpected error. Cannot delete Maquina with id $id."))
        Json.encodeToString(MaquinaResponseSuccess(200, result.toDTO()))
    }

    suspend fun setInactiveMaquina(id: UUID) : String = withContext(Dispatchers.IO) {
        val entity = maRepo.findByUUID(id)
            ?: return@withContext Json.encodeToString(MaquinaResponseError(404, "NOT FOUND: Cannot set inactive. Maquina with id $id not found."))
        val result = maRepo.setInactive(entity.id)
            ?: return@withContext Json.encodeToString(MaquinaResponseError(500, "INTERNAL EXCEPTION: Unexpected error. Cannot find and set inactive maquina with id $id."))
        Json.encodeToString(MaquinaResponseSuccess(200, result.toDTO()))
    }

    suspend fun findTurnoById(id: UUID) : String = withContext(Dispatchers.IO) {
        val entity = turRepo.findByUUID(id)

        val res = if (entity == null) TurnoResponseError(404, "NOT FOUND: Turno with id $id not found.")
        else TurnoResponseSuccess(200, entity.toDTO())

        Json.encodeToString(res)
    }

    suspend fun findAllTurnos() : String = withContext(Dispatchers.IO) {
        val entities = turRepo.findAll().toList()

        val res = if (entities.isEmpty()) TurnoResponseError(404, "NOT FOUND: No turnos found.")
        else TurnoResponseSuccess(200, toDTO(entities))

        Json.encodeToString(res)
    }

    suspend fun findAllTurnosByFecha(horaInicio: LocalDateTime) : String = withContext(Dispatchers.IO) {
        val entities = turRepo.findAll().toList().filter { it.horaInicio == horaInicio }

        val res = if (entities.isEmpty()) TurnoResponseError(404, "NOT FOUND: No turnos found.")
        else TurnoResponseSuccess(200, toDTO(entities))

        Json.encodeToString(res)
    }

    suspend fun createTurno(entity: TurnoDTOcreate) : String = withContext(Dispatchers.IO) {
        if (fieldsAreIncorrect(entity))
            return@withContext Json.encodeToString(TurnoResponseError(400, "BAD REQUEST: Cannot insert turno. Incorrect fields."))

        val res = turRepo.save(entity.fromDTO())
        Json.encodeToString(TurnoResponseSuccess(201, res.toDTO()))
    }

    suspend fun deleteTurno(id: UUID) : String = withContext(Dispatchers.IO) {
        val entity = turRepo.findByUUID(id)
            ?: return@withContext Json.encodeToString(TurnoResponseError(404, "NOT FOUND: Cannot delete. Turno with id $id not found."))
        val result = turRepo.delete(entity.id)
            ?: return@withContext Json.encodeToString(TurnoResponseError(500, "INTERNAL EXCEPTION: Unexpected error. Cannot delete Turno with id $id."))
        Json.encodeToString(TurnoResponseSuccess(200, result.toDTO()))
    }

    suspend fun setFinalizadoTurno(id: UUID) : String = withContext(Dispatchers.IO) {
        val entity = turRepo.findByUUID(id)
            ?: return@withContext Json.encodeToString(TurnoResponseError(404, "NOT FOUND: Cannot set finalizado. Turno with id $id not found."))
        val result = turRepo.setFinalizado(entity.id)
            ?: return@withContext Json.encodeToString(TurnoResponseError(500, "INTERNAL EXCEPTION: Unexpected error. Cannot find and set finalizado turno with id $id."))
        Json.encodeToString(TurnoResponseSuccess(200, result.toDTO()))
    }

    suspend fun findTareaById(id: UUID) : String = withContext(Dispatchers.IO) {
        val entity = tarRepo.findByUUID(id)

        val res = if (entity == null) TareaResponseError(404, "NOT FOUND: Tarea with id $id not found.")
        else TareaResponseSuccess(200, entity.toDTO())

        Json.encodeToString(res)
    }

    suspend fun findAllTareas() : String = withContext(Dispatchers.IO) {
        val entities = tarRepo.findAll().toList()

        val res = if (entities.isEmpty()) TareaResponseError(404, "NOT FOUND: No tareas found.")
        else TareaResponseSuccess(200, toDTO(entities))

        Json.encodeToString(res)
    }

    suspend fun findAllTareasFinalizadas(finalizada: Boolean) : String = withContext(Dispatchers.IO) {
        val entities = tarRepo.findAll().toList().filter { it.finalizada == finalizada }

        val res = if (entities.isEmpty()) TareaResponseError(404, "NOT FOUND: No tareas found.")
        else TareaResponseSuccess(200, toDTO(entities))

        Json.encodeToString(res)
    }

    suspend fun createTarea(entity: TareaDTOcreate) : String = withContext(Dispatchers.IO) {
        if (fieldsAreIncorrect(entity))
            return@withContext Json.encodeToString(TareaResponseError(400, "BAD REQUEST: Cannot insert tarea. Incorrect fields."))
        if (entity is EncordadoDTOcreate
            && (
            (entity.cordajeHorizontal.uuid == entity.cordajeVertical.uuid
            && entity.cordajeHorizontal.stock < 2) ||
            (entity.cordajeHorizontal.uuid != entity.cordajeVertical.uuid
            && entity.cordajeHorizontal.stock < 1) ||
            (entity.cordajeHorizontal.uuid != entity.cordajeVertical.uuid
            && entity.cordajeVertical.stock < 1)
            ))
            return@withContext Json.encodeToString(TareaResponseError(400, "BAD REQUEST: Cannot insert tarea. Not enough material for cordaje."))

        when (entity) {
            is EncordadoDTOcreate -> {
                if (entity.raqueta.tipo != TipoProducto.RAQUETAS ||
                    entity.cordajeHorizontal.tipo != TipoProducto.CORDAJES ||
                    entity.cordajeVertical.tipo != TipoProducto.CORDAJES)
                    return@withContext Json.encodeToString(TareaResponseError(400, "BAD REQUEST: Cannot insert tarea. Type mismatch in product types."))
            }
            is AdquisicionDTOcreate -> {
                if (entity.raqueta.tipo != TipoProducto.RAQUETAS)
                    return@withContext Json.encodeToString(TareaResponseError(400, "BAD REQUEST: Cannot insert tarea. Parameter raqueta is not of type Raqueta."))
            }
            is PersonalizacionDTOcreate -> {
                if (entity.raqueta.tipo != TipoProducto.RAQUETAS)
                    return@withContext Json.encodeToString(TareaResponseError(400, "BAD REQUEST: Cannot insert tarea. Parameter raqueta is not of type Raqueta."))
            }
        }

        val res = tarRepo.save(entity.fromDTO())
        Json.encodeToString(TareaResponseSuccess(201, res.toDTO()))
    }

    suspend fun deleteTarea(id: UUID) : String = withContext(Dispatchers.IO) {
        val entity = tarRepo.findByUUID(id)
            ?: return@withContext Json.encodeToString(TareaResponseError(404, "NOT FOUND: Cannot delete. Tarea with id $id not found."))
        val result = tarRepo.delete(entity.id)
            ?: return@withContext Json.encodeToString(TareaResponseError(500, "INTERNAL EXCEPTION: Unexpected error. Cannot delete tarea with id $id."))
        Json.encodeToString(TareaResponseSuccess(200, result.toDTO()))
    }

    suspend fun setFinalizadaTarea(id: UUID) : String = withContext(Dispatchers.IO) {
        val entity = tarRepo.findByUUID(id)
            ?: return@withContext Json.encodeToString(TareaResponseError(404, "NOT FOUND: Cannot set finalizado. Tarea with id $id not found."))
        val result = tarRepo.setFinalizada(entity.id)
            ?: return@withContext Json.encodeToString(TareaResponseError(500, "INTERNAL EXCEPTION: Unexpected error. Cannot find and set finalizada tarea with id $id."))
        Json.encodeToString(TareaResponseSuccess(200, result.toDTO()))
    }
}