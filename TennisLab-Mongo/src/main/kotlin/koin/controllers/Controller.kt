package koin.controllers

import koin.dto.maquina.*
import koin.dto.pedido.PedidoDTOcreate
import koin.dto.pedido.PedidoDTOvisualize
import koin.dto.pedido.PedidoDTOvisualizeList
import koin.dto.producto.ProductoDTOcreate
import koin.dto.producto.ProductoDTOvisualize
import koin.dto.producto.ProductoDTOvisualizeList
import koin.dto.tarea.*
import koin.dto.turno.TurnoDTOcreate
import koin.dto.turno.TurnoDTOvisualize
import koin.dto.turno.TurnoDTOvisualizeList
import koin.dto.user.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import koin.mappers.fromDTO
import koin.mappers.toDTO
import koin.models.ResponseError
import koin.models.ResponseSuccess
import koin.models.maquina.Maquina
import koin.models.pedido.Pedido
import koin.models.pedido.PedidoState
import koin.models.producto.Producto
import koin.models.producto.TipoProducto
import koin.models.tarea.Tarea
import koin.models.turno.Turno
import koin.models.user.UserProfile
import koin.models.user.*
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.litote.kmongo.Id
import koin.repositories.maquina.IMaquinaRepository
import koin.repositories.pedido.IPedidoRepository
import koin.repositories.producto.IProductoRepository
import koin.repositories.tarea.ITareaRepository
import koin.repositories.turno.ITurnoRepository
import koin.repositories.user.UserRepositoryCached
import koin.services.login.checkToken
import koin.services.utils.checkUserEmailAndPhone
import koin.services.utils.fieldsAreIncorrect
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.time.LocalDateTime
import java.util.*

private val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true

    /**
     * @author Loli
     * Esto sirve para serializar las Responses. Sin esto, no lo serializa bien.
     * Ha sido sacado de: https://github.com/Kotlin/kotlinx.serialization/issues/1341
     */
    useArrayPolymorphism = true
    encodeDefaults = true
    serializersModule = SerializersModule {
        polymorphic(Any::class) {
            subclass(String::class, String.serializer())
            subclass(UserDTOvisualize::class)
            subclass(UserDTOvisualizeList::class)
            subclass(ProductoDTOvisualize::class)
            subclass(ProductoDTOvisualizeList::class)
            subclass(PedidoDTOvisualize::class)
            subclass(PedidoDTOvisualizeList::class)
            subclass(AdquisicionDTOvisualize::class)
            subclass(EncordadoDTOvisualize::class)
            subclass(PersonalizacionDTOvisualize::class)
            subclass(TareaDTOvisualizeList::class)
            subclass(EncordadoraDTOvisualize::class)
            subclass(PersonalizadoraDTOvisualize::class)
            subclass(MaquinaDTOvisualizeList::class)
            subclass(TurnoDTOvisualize::class)
            subclass(TurnoDTOvisualizeList::class)
            subclass(List::class, ListSerializer(PolymorphicSerializer(Any::class).nullable))
        }
    }
}

/**
 * @author Loli
 * Clase que actúa como controlador de los distintos repositorios haciendo uso de los métodos requeridos y
 * devolviendo en cada caso dos tipos de respuesta: ResponseError y ResponseSuccess por cada caso de los métodos
 */

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
    /**
     * @param id Identificador de tipo UUID del objeto User
     * Este método sirve para buscar un objeto de tipo User con el id pasado por parámetro
     * @return ResponseError en caso de que no exista el usuario con ese identificador
     * @return ResponseSuccess si encuentra un usuario con ese identificador
     */
    suspend fun findUserByUuid(id: UUID) : String = withContext(Dispatchers.IO) {
        val user = uRepo.findByUUID(id)

        val res = if (user == null) ResponseError(404, "NOT FOUND: User with id $id not found.")
        else ResponseSuccess(200, user.toDTO())

        json.encodeToString(res)
    }

    /**
     * @param id Identificador de tipo int del objeto User
     * Este método sirve para buscar un objeto de tipo User con el id pasado por parámetro
     * @return ResponseError en caso de que no exista el usuario con ese identificador
     * @return ResponseSuccess si encuentra un usuario con ese identificador
     */
    suspend fun findUserById(id: Int) : String = withContext(Dispatchers.IO) {
        val user = uRepo.findById(id)

        val res = if (user == null) ResponseError(404, "NOT FOUND: User with id $id not found.")
        else ResponseSuccess(200, user.toDTO())

        json.encodeToString(res)
    }

    /**
     * Este método devuelve todos los usuarios que se encuentren registrados en la base de datos
     * @return ResponseError en caso de que no existan usuarios
     * @return ResponseSuccess con los datos de un UserDTOVisualizeList con la lista de usuarios
     * Por último coge el valor devuelto y le aplica un encode para tenerlo en formato json
     */
    suspend fun findAllUsers() : String = withContext(Dispatchers.IO) {
        val users = uRepo.findAll().toList()

        val res = if (users.isEmpty()) ResponseError(404, "NOT FOUND: No users found.")
        else ResponseSuccess(200, UserDTOvisualizeList(toDTO(users)))

        json.encodeToString(res)
    }

    /**
     * Este método devuelve todos los usuarios que se encuentren activos o inactivos dependiendo del parámetro pasado
     * @param active de tipo Boolean se usa para buscar a los usuarios que tengan el parametro "active" = true :? false
     * @return ResponseError en caso de que no existan usuarios
     * @return ResponseSuccess con los datos de un UserDTOVisualizeList con la lista de usuarios
     * Por último coge el valor devuelto y le aplica un encode para tenerlo en formato json
     */
    suspend fun findAllUsersWithActivity(active: Boolean) : String = withContext(Dispatchers.IO) {
        val users = uRepo.findAll().toList().filter { it.activo == active }

        val res = if (users.isEmpty()) ResponseError(404, "NOT FOUND: No users found.")
        else ResponseSuccess(200, UserDTOvisualizeList(toDTO(users)))

        json.encodeToString(res)
    }

    /**
     * Este método sirve para crear usuarios
     * @param user de tipo UserDTOCreate
     * @param token de tipo String
     * Comprueba que el token es válido y si se trata de un usuario de tipo administrador en caso de que validated no sea null
     * @return validated respuesta de error por acceso no autorizado
     * Si validated es null se comprueba los campos del UserDTOCreate y devuelve
     * @return ResponseError en formato json en caso de que el usuario se haya instalado de forma incorrecta
     * @return ResponseSuccess si todos los campos son correctos y se aplica el guardado de forma correcta, devuelve un json
     */
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

    /**
     * Este método sirve para establecer un usuario como inactivo
     * @param id de tipo UUID del usuario
     * @param token el token es una cadena de texto que se pasa el método por parámetro
     * @return ResponseError en caso de que no se encuentre el id o que no se pueda establecer como inactivo
     * @return ResponseSuccess con un enconde a String con formato json
     */
    suspend fun setInactiveUser(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext validated

        val user = uRepo.findByUUID(id)
            ?: return@withContext json.encodeToString(ResponseError(404, "NOT FOUND: Cannot set inactive. User with id $id not found."))
        val result = uRepo.setInactive(user.id)
            ?: return@withContext json.encodeToString(ResponseError(500, "INTERNAL EXCEPTION: Unexpected error. Cannot find and set inactive user with id $id."))
        json.encodeToString(ResponseSuccess(200, result.toDTO()))
    }

    /**
     * Este método sirve para borrar un usuario
     * @param id de tipo UUID del usuario que se quiera buscar
     * @param token de tipo String para validar el acceso al método
     * @return validated si no es null devolverá el ResponseError correspondiente
     * @return ResponseError en json del mensaje de error en caso de que el usuario no sea encontrado por el repositorio
     * @return ResponseError en json en caso de que no se pueda aplicar el borrado al usuario encontrado
     * @return ResponseSuccess con formato json
     */
    suspend fun deleteUser(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext validated

        val user = uRepo.findByUUID(id)
            ?: return@withContext json.encodeToString(ResponseError(404, "NOT FOUND: Cannot delete. User with id $id not found."))
        val result = uRepo.delete(user.id)
            ?: return@withContext json.encodeToString(ResponseError(500, "INTERNAL EXCEPTION: Unexpected error. Cannot delete user with id $id."))
        json.encodeToString(ResponseSuccess(200, result.toDTO()))
    }

    /**
     * @param id Identificador de tipo UUID del objeto Pedido
     * Este método sirve para buscar un objeto de tipo Pedido con el id pasado por parámetro
     * @return ResponseError en caso de que no exista el pedido con ese identificador
     * @return ResponseSuccess si encuentra un pedido con ese identificador
     */
    suspend fun findPedidoById(id: UUID) : String = withContext(Dispatchers.IO) {
        val entity = pedRepo.findByUUID(id)

        val res = if (entity == null) ResponseError(404, "NOT FOUND: Pedido with id $id not found.")
        else ResponseSuccess(200, entity.toDTO())

        json.encodeToString(res)
    }

    /**
     * Este método devuelve todos los pedidos que se encuentren registrados en la base de datos
     * @return ResponseError en caso de que no existan pedidos
     * @return ResponseSuccess con los datos de un PedidoDTOVisualizeList con la lista de pedidos
     * Por último coge el valor devuelto y le aplica un encode para tenerlo en formato json
     */
    suspend fun findAllPedidos() : String = withContext(Dispatchers.IO) {
        val entities = pedRepo.findAll().toList()

        val res = if (entities.isEmpty()) ResponseError(404, "NOT FOUND: No pedidos found.")
        else ResponseSuccess(200, PedidoDTOvisualizeList(toDTO(entities)))

        json.encodeToString(res)
    }

    suspend fun findAllPedidosWithState(state: PedidoState) : String = withContext(Dispatchers.IO) {
        val entities = pedRepo.findAll().toList().filter { it.state == state }

        val res = if (entities.isEmpty()) ResponseError(404, "NOT FOUND: No pedidos found with state = $state.")
        else ResponseSuccess(200, PedidoDTOvisualizeList(toDTO(entities)))

        json.encodeToString(res)
    }

    suspend fun createPedido(entity: PedidoDTOcreate, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext validated

        if (fieldsAreIncorrect(entity))
            return@withContext json.encodeToString(ResponseError(400, "BAD REQUEST: Cannot insert pedido. Incorrect fields."))
        if (uRepo.findByUUID(entity.user.fromDTO().uuid) == null)
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
        else ResponseSuccess(200, ProductoDTOvisualizeList(toDTO(entities)))

        json.encodeToString(res)
    }

    suspend fun findAllProductosDisponibles() : String = withContext(Dispatchers.IO) {
        val entities = proRepo.findAll().toList().filter { it.stock > 0 }

        val res = if (entities.isEmpty()) ResponseError(404, "NOT FOUND: There are no products available.")
        else ResponseSuccess(200, ProductoDTOvisualizeList(toDTO(entities)))

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
        else ResponseSuccess(200, MaquinaDTOvisualizeList(toDTO(entities)))

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
        else ResponseSuccess(200, TurnoDTOvisualizeList(toDTO(entities)))

        json.encodeToString(res)
    }

    suspend fun findAllTurnosByFecha(horaInicio: LocalDateTime) : String = withContext(Dispatchers.IO) {
        val entities = turRepo.findAll().toList().filter { it.horaInicio == horaInicio }

        val res = if (entities.isEmpty()) ResponseError(404, "NOT FOUND: No turnos found.")
        else ResponseSuccess(200, TurnoDTOvisualizeList(toDTO(entities)))

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
        val entities = tarRepo.findAll().toList().subList(0, 25)

        val res = if (entities.isEmpty()) ResponseError(404, "NOT FOUND: No tareas found.")
        else ResponseSuccess(200, TareaDTOvisualizeList(toDTO(entities)))

        json.encodeToString(res)
    }

    suspend fun findAllTareasFinalizadas(finalizada: Boolean) : String = withContext(Dispatchers.IO) {
        val entities = tarRepo.findAll().toList().filter { it.finalizada == finalizada }.subList(0, 25)

        val res = if (entities.isEmpty()) ResponseError(404, "NOT FOUND: No tareas found.")
        else ResponseSuccess(200, TareaDTOvisualizeList(toDTO(entities)))

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
            else -> {}
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
        val token = koin.services.login.login(user, uRepo)
        if (token == null) json.encodeToString(ResponseError(404, "NOT FOUND: Unable to login. Incorrect email or password."))
        else json.encodeToString(ResponseSuccess(200, token))
    }

    suspend fun register(user: UserDTORegister): String = withContext(Dispatchers.IO) {
        val token = koin.services.login.register(user, uRepo)
        if (token == null) json.encodeToString(ResponseError(400, "BAD REQUEST: Unable to register. Incorrect parameters."))
        else json.encodeToString(ResponseSuccess(200, token))
    }
}