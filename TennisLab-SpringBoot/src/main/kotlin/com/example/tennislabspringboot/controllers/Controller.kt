package com.example.tennislabspringboot.controllers

import com.example.tennislabspringboot.dto.maquina.MaquinaDTOcreate
import com.example.tennislabspringboot.dto.maquina.MaquinaDTOvisualizeList
import com.example.tennislabspringboot.dto.pedido.PedidoDTOcreate
import com.example.tennislabspringboot.dto.pedido.PedidoDTOvisualizeList
import com.example.tennislabspringboot.dto.producto.ProductoDTOcreate
import com.example.tennislabspringboot.dto.producto.ProductoDTOvisualize
import com.example.tennislabspringboot.dto.producto.ProductoDTOvisualizeList
import com.example.tennislabspringboot.dto.tarea.*
import com.example.tennislabspringboot.dto.turno.TurnoDTOcreate
import com.example.tennislabspringboot.dto.turno.TurnoDTOvisualizeList
import com.example.tennislabspringboot.dto.user.UserDTOLogin
import com.example.tennislabspringboot.dto.user.UserDTORegister
import com.example.tennislabspringboot.dto.user.UserDTOcreate
import com.example.tennislabspringboot.dto.user.UserDTOvisualizeList
import com.example.tennislabspringboot.mappers.*
import com.example.tennislabspringboot.models.ResponseError
import com.example.tennislabspringboot.models.ResponseSuccess
import com.example.tennislabspringboot.models.pedido.PedidoState
import com.example.tennislabspringboot.models.producto.TipoProducto
import com.example.tennislabspringboot.models.user.UserProfile
import com.example.tennislabspringboot.repositories.maquina.MaquinaRepositoryCached
import com.example.tennislabspringboot.repositories.pedido.PedidoRepositoryCached
import com.example.tennislabspringboot.repositories.producto.ProductoRepositoryCached
import com.example.tennislabspringboot.repositories.tarea.TareaRepositoryCached
import com.example.tennislabspringboot.repositories.turno.TurnoRepositoryCached
import com.example.tennislabspringboot.repositories.user.UserRepositoryCached
import com.example.tennislabspringboot.services.login.checkToken
import com.example.tennislabspringboot.services.utils.checkUserEmailAndPhone
import com.example.tennislabspringboot.services.utils.fieldsAreIncorrect
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import java.time.LocalDateTime
import java.util.*

/**
 * @author Daniel Rodriguez Muñoz
 * Clase que actúa como controlador de los distintos repositorios haciendo uso de los métodos requeridos y
 * devolviendo en cada caso dos tipos de respuesta: Response y Response por cada caso de los métodos
 */
@Controller
class Controller
    @Autowired constructor(
        private val uRepo: UserRepositoryCached,
        private val turRepo: TurnoRepositoryCached,
        private val tarRepo: TareaRepositoryCached,
        private val proRepo: ProductoRepositoryCached,
        private val pedRepo: PedidoRepositoryCached,
        private val maRepo: MaquinaRepositoryCached,
        private val turMapper: TurnoMapper,
        private val tarMapper: TareaMapper,
        private val pedMapper: PedidoMapper,
) {
    private val json = ObjectMapper()
        .registerModule(JavaTimeModule())
        .writerWithDefaultPrettyPrinter()
    /**
     * @param id Identificador de tipo String
     * Este método sirve para buscar un objeto de tipo User con el id pasado por parámetro
     * @return cadena de texto con los datos de Response en caso de que no exista el usuario con ese identificador
     * @return cadena de texto con los datos de Response si encuentra un usuario con ese identificador
     */
    suspend fun findUserByUuid(id: UUID) : String = withContext(Dispatchers.IO) {
        val user = uRepo.findByUUID(id)

        if (user == null) json.writeValueAsString(ResponseError(404, "User with id $id not found."))
        else json.writeValueAsString(ResponseSuccess(200, user.toDTO()))
    }

    /**
     * @param id Identificador de tipo int del objeto User
     * Este método sirve para buscar un objeto de tipo User con el id pasado por parámetro
     * @return cadena de texto con los datos de cadena de texto con el error en caso de que no exista el usuario con ese identificador
     * @return cadena de texto con los datos de cadena de texto en formato JSON con el usuario y es estatus
     */
    suspend fun findUserById(id: Int) : String = withContext(Dispatchers.IO) {
        val user = uRepo.findById(id)

        if (user == null) json.writeValueAsString(ResponseError(404, "User with id $id not found."))
        else json.writeValueAsString(ResponseSuccess(200, user.toDTO()))
    }

    /**
     * Este método devuelve todos los usuarios que se encuentren registrados en la base de datos
     * @return cadena de texto con los datos de Response en caso de que no existan usuarios
     * @return cadena de texto con los datos de Response con los datos de un UserDTOVisualizeList con la lista de usuarios
     * Por último coge el valor devuelto y le aplica un encode para tenerlo en formato json
     */
    suspend fun findAllUsers() : String = withContext(Dispatchers.IO) {
        val users = uRepo.findAll().toList()

        if (users.isEmpty()) json.writeValueAsString(ResponseError(404, "No users found."))
        else json.writeValueAsString(ResponseSuccess(200, UserDTOvisualizeList(toDTO(users))))
    }

    /**
     * Este método devuelve todos los usuarios que se encuentren activos o inactivos dependiendo del parámetro pasado
     * @param active de tipo Boolean se usa para buscar a los usuarios que tengan el parametro "active" = true :? false
     * @return cadena de texto con los datos de Response en caso de que no existan usuarios
     * @return cadena de texto con los datos de Response con los datos de un UserDTOVisualizeList con la lista de usuarios
     *
     */
    suspend fun findAllUsersWithActivity(active: Boolean) : String = withContext(Dispatchers.IO) {
        val users = uRepo.findAll().toList().filter { it.activo == active }

        if (users.isEmpty()) json.writeValueAsString(ResponseError(404, "No users found with activity: $active."))
        else json.writeValueAsString(ResponseSuccess(200, UserDTOvisualizeList(toDTO(users))))
    }

    /**
     * Este método sirve para crear usuarios
     * @param user de tipo UserDTOCreate
     * @param token de tipo String
     * Comprueba que el token es válido y si se trata de un usuario de tipo administrador en caso de que validated no sea null
     * @return cadena de texto con los datos de validated respuesta de error por acceso no autorizado
     * Si validated es null se comprueba los campos del UserDTOCreate y devuelve
     * @return cadena de texto con los datos de Response en formato json en caso de que el usuario se haya introducido de forma incorrecta
     * @return cadena de texto con los datos de Response si todos los campos son correctos y se aplica el guardado de forma correcta, devuelve un json
     */
    suspend fun createUser(user: UserDTOcreate, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        if (fieldsAreIncorrect(user))
            return@withContext json.writeValueAsString(ResponseError(400, "Cannot insert user. Incorrect fields."))
        if (checkUserEmailAndPhone(user, uRepo))
            return@withContext json.writeValueAsString(ResponseError(400, "Cannot insert user."))

        val res = uRepo.save(user.fromDTO())
        json.writeValueAsString(ResponseSuccess(201, res.toDTO()))
    }

    /**
     * Este método sirve para establecer un usuario como inactivo
     * @param id de tipo UUID del usuario
     * @param token el token es una cadena de texto que se pasa el método por parámetro
     * @return cadena de texto con los datos de Response en caso de que no se encuentre el id o que no se pueda establecer como inactivo
     * @return cadena de texto con los datos de Response con un enconde a String con formato json
     */
    suspend fun setInactiveUser(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        val user = uRepo.findByUUID(id)
            ?: return@withContext json.writeValueAsString(
                ResponseError(404, "Cannot set inactive. User with id $id not found."))
        val result = uRepo.setInactive(user.id)
            ?: return@withContext json.writeValueAsString(
                ResponseError(500, "Unexpected error. Cannot find and set inactive user with id $id."))
        json.writeValueAsString(ResponseSuccess(200, result.toDTO()))
    }

    /**
     * Este método sirve para borrar un usuario
     * @param id de tipo UUID del usuario que se quiera buscar
     * @param token de tipo String para validar el acceso al método
     * @return cadena de texto con los datos de validated si no es null devolverá el Response correspondiente
     * @return cadena de texto con los datos de Response en json del mensaje de error en caso de que el usuario no sea encontrado por el repositorio
     * @return cadena de texto con los datos de Response en json en caso de que no se pueda aplicar el borrado al usuario encontrado
     * @return cadena de texto con los datos de Response con formato json
     */
    suspend fun deleteUser(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        val user = uRepo.findByUUID(id)
            ?: return@withContext json.writeValueAsString(
                ResponseError(404, "NOT FOUND: Cannot delete. User with id $id not found."))
        val result = uRepo.delete(user.id)
            ?: return@withContext json.writeValueAsString(
                ResponseError(500, "Unexpected error. Cannot delete user with id $id."))
        json.writeValueAsString(ResponseSuccess(200, result.toDTO()))
    }

    /**
     * @param id Identificador de tipo UUID del objeto Pedido
     * Este método sirve para buscar un objeto de tipo Pedido con el id pasado por parámetro
     * @return cadena de texto con los datos de Response en caso de que no exista el pedido con ese identificador
     * @return cadena de texto con los datos de Response si encuentra un pedido con ese identificador
     */
    suspend fun findPedidoById(id: UUID) : String = withContext(Dispatchers.IO) {
        val entity = pedRepo.findByUUID(id)

        if (entity == null) json.writeValueAsString(ResponseError(404, "Pedido with id $id not found."))
        else json.writeValueAsString(ResponseSuccess(200, pedMapper.toDTO(entity)))
    }

    /**
     * Este método devuelve todos los pedidos que se encuentren registrados en la base de datos
     * @return cadena de texto con los datos de Response en caso de que no existan pedidos
     * @return cadena de texto con los datos de Response con los datos de un PedidoDTOVisualizeList con la lista de pedidos
     * Por último coge el valor devuelto y le aplica un encode para tenerlo en formato json
     */
    suspend fun findAllPedidos() : String = withContext(Dispatchers.IO) {
        val entities = pedRepo.findAll().toList()

        if (entities.isEmpty()) json.writeValueAsString(ResponseError(404, "No pedidos found."))
        else json.writeValueAsString(ResponseSuccess(200, PedidoDTOvisualizeList(pedMapper.toDTO(entities))))
    }

    /**
     * Este método devuelve todos los pedidos que con el estado requerido dependiendo del parámetro pasado
     * @param state el estado en el que se encuentra la lista de pedidos que devuelve el método
     * @return cadena de texto con los datos de Response en caso de que no existan pedidos con ese estado
     * @return cadena de texto con los datos de Response con los datos de un PedidoDTOVisualizeList con la lista de pedidos con el estado
     * Por último coge el valor devuelto y le aplica un encode para devolverlo en formato json
     */
    suspend fun findAllPedidosWithState(state: PedidoState) : String = withContext(Dispatchers.IO) {
            val entities = pedRepo.findAll().toList().filter { it.state == state }

            if (entities.isEmpty()) json.writeValueAsString(
                ResponseError(404, "No pedidos found with state = $state."))
            else json.writeValueAsString(
                ResponseSuccess(200, PedidoDTOvisualizeList(pedMapper.toDTO(entities))))
    }

    /**
     * Este método sirve para crear pedidos
     * @param entity de tipo PedidoDTOCreate
     * @param token de tipo String
     * Comprueba que el token es válido y si se trata de un token de tipo administrador en caso de que validated no sea null
     * @return cadena de texto con los datos de validated respuesta de error por acceso no autorizado
     * Si validated es null se comprueba los campos del PedidoDTOCreate y devuelve
     * @return cadena de texto con los datos de Response en formato json en caso de que el pedido se haya introducido de forma incorrecta o no haya sido encontrado el usuario
     * en caso de no dar error recoge las tareas del pedido  las guarda usando el repositorio de tareas y después guarda el pedido
     * @return cadena de texto con los datos de Response si todos los campos son correctos y se aplica el guardado de forma correcta, devuelve un json
     */
    suspend fun createPedido(entity: PedidoDTOcreate, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        if (fieldsAreIncorrect(entity))
            return@withContext json.writeValueAsString(
                ResponseError(400, "Cannot insert pedido. Incorrect fields."))
        if (uRepo.findByUUID(entity.user.fromDTO().uuid) == null)
            return@withContext json.writeValueAsString(
                ResponseError(404, "Cannot insert pedido. User not found."))

        entity.tareas.forEach { tarRepo.save(it.fromDTO()) }
        val res = pedRepo.save(entity.fromDTO())
        json.writeValueAsString(ResponseSuccess(201, pedMapper.toDTO(res)))
    }

    /**
     * Este método sirve para borrar un pedido
     * @param id de tipo UUID del pedido que se quiera buscar
     * @param token de tipo String para validar el acceso al método
     * @return cadena de texto con los datos de validated si no es null devolverá el Response correspondiente
     * @return cadena de texto con los datos de Response en json del mensaje de error en caso de que el pedido no sea encontrado por el repositorio
     * @return cadena de texto con los datos de Response en json en caso de que no se pueda aplicar el borrado al pedido encontrado
     * @return cadena de texto con los datos de Response con formato json
     */
    suspend fun deletePedido(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        val entity = pedRepo.findByUUID(id)
            ?: return@withContext json.writeValueAsString(
                ResponseError(404, "Cannot delete. Pedido with id $id not found."))
        tarRepo.findAll().filter { it.pedidoId == id }.toList().forEach { tarRepo.delete(it.id) }
        val result = pedRepo.delete(entity.id)
            ?: return@withContext json.writeValueAsString(
                ResponseError(500, "Unexpected error. Cannot delete pedido with id $id."))
        json.writeValueAsString(ResponseSuccess(200, pedMapper.toDTO(result)))
    }

    /**
     * metodo para devolver los productos en tiempo real
     */
    suspend fun findAllProductosAsFlow() : Flow<List<ProductoDTOvisualize>> {
        return proRepo.findAllAsFlow()
    }

    /**
     * @param id Identificador de tipo UUID del objeto Producto
     * Este método sirve para buscar un objeto de tipo Producto con el id pasado por parámetro
     * @return cadena de texto con los datos de Response en caso de que no exista el producto con ese identificador
     * @return cadena de texto con los datos de Response si encuentra un producto con ese identificador
     */
    suspend fun findProductoById(id: UUID) : String = withContext(Dispatchers.IO) {
            val entity = proRepo.findByUUID(id)

            if (entity == null) json.writeValueAsString(
                ResponseError(404, "Producto with id $id not found."))
            else json.writeValueAsString(
                ResponseSuccess(200, entity.toDTO()))
    }

    /**
     * Este método devuelve todos los productos que se encuentren registrados en la base de datos
     * @return cadena de texto con los datos de Response en caso de que no existan productos
     * @return cadena de texto con los datos de Response con los datos de un ProductosDTOVisualizeList con la lista de productos
     * Por último coge el valor devuelto y le aplica un encode para tenerlo en formato json
     */
    suspend fun findAllProductos() : String = withContext(Dispatchers.IO) {
        val entities = proRepo.findAll().toList()

        if (entities.isEmpty()) json.writeValueAsString(
            ResponseError(404, "No productos found."))
        else json.writeValueAsString(
            ResponseSuccess(200, ProductoDTOvisualizeList(toDTO(entities))))
    }

    /**
     * Este método devuelve todos los productos que se encuentren disponibles
     * @return cadena de texto con los datos de Response en caso de que no existan productos con ese estado
     * @return cadena de texto con los datos de Response con los datos de un ProductosDTOVisualizeList con la lista de productos con el estado
     * Por último coge el valor devuelto y le aplica un encode para devolverlo en formato json
     */
    suspend fun findAllProductosDisponibles(disponibles: Boolean) : String = withContext(Dispatchers.IO) {
        val entities = if (disponibles) proRepo.findAll().toList().filter { it.stock > 0 }
        else proRepo.findAll().toList().filter { it.stock == 0 }

        if (entities.isEmpty()) json.writeValueAsString(
            ResponseError(404, "There are no products available."))
        else json.writeValueAsString(
            ResponseSuccess(200, ProductoDTOvisualizeList(toDTO(entities))))
    }

    /**
     * Este método sirve para crear productos
     * @param entity de tipo ProductosDTOCreate
     * @param token de tipo String
     * Comprueba que el token es válido y si se trata de un token de tipo administrador en caso de que validated no sea null
     * @return cadena de texto con los datos de validated respuesta de error por acceso no autorizado
     * Si validated es null se comprueba los campos del ProductosDTOCreate y devuelve
     * @return cadena de texto con los datos de Response en formato json en caso de que el producto se haya introducido de forma incorrecta
     * @return cadena de texto con los datos de Response si todos los campos son correctos y se aplica el guardado de forma correcta, devuelve un json
     */
    suspend fun createProducto(entity: ProductoDTOcreate, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        if (fieldsAreIncorrect(entity))
            return@withContext json.writeValueAsString(
                ResponseError(400, "Cannot insert producto. Incorrect fields."))

        val res = proRepo.save(entity.fromDTO())
        json.writeValueAsString(ResponseSuccess(201, res.toDTO()))
    }

    /**
     * Este método sirve para borrar un producto
     * @param id de tipo UUID del pedido que se quiera buscar
     * @param token de tipo String para validar el acceso al método
     * @return cadena de texto con los datos de validated si no es null devolverá el Response correspondiente
     * @return cadena de texto con los datos de Response en json del mensaje de error en caso de que el producto no sea encontrado por el repositorio
     * @return cadena de texto con los datos de Response en json en caso de que no se pueda aplicar el borrado al producto encontrado
     * @return cadena de texto con los datos de Response con formato json
     */
    suspend fun deleteProducto(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        val entity = proRepo.findByUUID(id)
            ?: return@withContext json.writeValueAsString(
                ResponseError(404, "Cannot delete. Producto with id $id not found."))
        val result = proRepo.delete(entity.id)
            ?: return@withContext json.writeValueAsString(
                ResponseError(500, "Unexpected error. Cannot delete producto with id $id."))
        json.writeValueAsString(ResponseSuccess(200, result.toDTO()))
    }

    /**
     * Este método sirve para bajar el stock del producto con el id pasado por parámetro
     * @param id de tipo UUID del producto
     * @param token de tipo String para validar el acceso al método
     * @return cadena de texto con los datos de validated si no es null devolverá el Response correspondiente
     * @return cadena de texto con los datos de Response en json del mensaje de error en caso de que el producto no sea encontrado por el repositorio
     * @return cadena de texto con los datos de Response en json en caso de que no se pueda aplicar el borrado al producto encontrado
     * @return cadena de texto con los datos de Response con formato json
     */
    suspend fun decreaseStockFromProducto(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        val entity = proRepo.findByUUID(id)
            ?: return@withContext json.writeValueAsString(
                ResponseError(404, "Cannot decrease stock. Producto with id $id not found."))
        val result = proRepo.decreaseStock(entity.id)
            ?: return@withContext json.writeValueAsString(
                ResponseError(404, "Cannot decrease stock. Producto with id $id not found."))
        json.writeValueAsString(ResponseSuccess(200, result.toDTO()))
    }

    /**
     * @param id identificador de tipo UUID del objeto Máquina
     * Este método sirve para buscar un objeto de tipo Máquina con el id pasado por parámetro
     * @return cadena de texto con los datos de Response en caso de que no exista la máquina con ese identificador
     * @return cadena de texto con los datos de Response si encuentra una máquina con ese identificador
     * devuelve la respuesta en formato json
     */
    suspend fun findMaquinaById(id: UUID) : String = withContext(Dispatchers.IO) {
            val entity = maRepo.findByUUID(id)

            if (entity == null) json.writeValueAsString(
                ResponseError(404, "Maquina with id $id not found."))
            else json.writeValueAsString(
                ResponseSuccess(200, entity.toDTO()))
    }

    /**
     * Este método devuelve todos las máquinas que se encuentren registradas en la base de datos
     * @return cadena de texto con los datos de Response en caso de que no existan máquinas
     * @return cadena de texto con los datos de Response con los datos de un MaquinaDTOVisualizeList con la lista de máquinas
     * Por último coge el valor devuelto y le aplica un encode para devolverlo en formato json
     */
    suspend fun findAllMaquinas() : String = withContext(Dispatchers.IO) {
        val entities = maRepo.findAll().toList()

        if (entities.isEmpty()) json.writeValueAsString(
            ResponseError(404, "No maquinas found."))
        else json.writeValueAsString(
            ResponseSuccess(200, MaquinaDTOvisualizeList(toDTO(entities))))
    }

    /**
     * Este método sirve para crear productos
     * @param entity de tipo MaquinaDTOCreate
     * @param token de tipo String
     * Comprueba que el token es válido y si se trata de un token de tipo administrador en caso de que validated no sea null
     * @return cadena de texto con los datos de validated respuesta de error por acceso no autorizado
     * Si validated es null se comprueba los campos del TurnoDTOCreate y devuelve
     * @return cadena de texto con los datos de Response en formato json en caso de que la maquina se haya introducido de forma incorrecta
     * @return cadena de texto con los datos de Response si todos los campos son correctos y se aplica el guardado de forma correcta, devuelve un json
     */
    suspend fun createMaquina(entity: MaquinaDTOcreate, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        if (fieldsAreIncorrect(entity))
            return@withContext json.writeValueAsString(
                ResponseError(400, "Cannot insert maquina. Incorrect fields."))

        val res = maRepo.save(entity.fromDTO())
        json.writeValueAsString(ResponseSuccess(201, res.toDTO()))
    }

    /**
     * Este método sirve para borrar un máquina
     * @param id de tipo UUID de la máquina que se quiera buscar
     * @param token de tipo String para validar el acceso al método
     * @return cadena de texto con los datos de validated si no es null devolverá el Response correspondiente
     * @return cadena de texto con los datos de Response en json del mensaje de error en caso de que la máquina no sea encontrada por el repositorio
     * @return cadena de texto con los datos de Response en json en caso de que no se pueda aplicar el borrado a la máquina encontrada
     * @return cadena de texto con los datos de Response con formato json
     */
    suspend fun deleteMaquina(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        val entity = maRepo.findByUUID(id)
            ?: return@withContext json.writeValueAsString(
                ResponseError(404, "Cannot delete. Maquina with id $id not found."))
        val result = maRepo.delete(entity.id)
            ?: return@withContext json.writeValueAsString(
                ResponseError(500, "Unexpected error. Cannot delete Maquina with id $id."))
        json.writeValueAsString(ResponseSuccess(200, result.toDTO()))
    }

    /**
     * Este método sirve para poner en estado inactivo la máquina con el identificador pasado por parámetro
     * @param id de tipo UUID de la máquina
     * @param token de tipo String para validar el acceso al método
     * @return cadena de texto con los datos de validated si no es null devolverá el Response correspondiente
     * @return cadena de texto con los datos de Response en json del mensaje de error en caso de que la máquina no sea encontrada por el repositorio
     * @return cadena de texto con los datos de Response en json en caso de que no se pueda aplicar el cambio a inactivo de la máquina encontrada
     * @return cadena de texto con los datos de Response con formato json
     */
    suspend fun setInactiveMaquina(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        val entity = maRepo.findByUUID(id)
            ?: return@withContext json.writeValueAsString(
                ResponseError(404, "Cannot set inactive. Maquina with id $id not found."))
        val result = maRepo.setInactive(entity.id)
            ?: return@withContext json.writeValueAsString(
                ResponseError(500, "Unexpected error. Cannot find and set inactive maquina with id $id."))
        json.writeValueAsString(ResponseSuccess(200, result.toDTO()))
    }

    /**
     * @param id identificador de tipo UUID del objeto Turno
     * Este método sirve para buscar un objeto de tipo Turno con el id pasado por parámetro
     * @return cadena de texto con los datos de Response en caso de que no exista el turno con ese identificador
     * @return cadena de texto con los datos de Response si encuentra un turno con ese identificador
     * devuelve la respuesta en formato json
     */
    suspend fun findTurnoById(id: UUID) : String = withContext(Dispatchers.IO) {
        val entity = turRepo.findByUUID(id)

        if (entity == null) json.writeValueAsString(
            ResponseError(404, "Turno with id $id not found."))
        else json.writeValueAsString(ResponseSuccess(200, turMapper.toDTO(entity)))
    }

    /**
     * Este método devuelve todos los turnos que se encuentren registrados en la base de datos
     * @return cadena de texto con los datos de Response en caso de que no existan turnos
     * @return cadena de texto con los datos de Response con los datos de un TurnoDTOVisualizeList con la lista de turnos
     * Por último coge el valor devuelto y le aplica un encode para devolverlo en formato json
     */
    suspend fun findAllTurnos() : String = withContext(Dispatchers.IO) {
        val entities = turRepo.findAll().toList()

        if (entities.isEmpty()) json.writeValueAsString(
            ResponseError(404, "No turnos found."))
        else json.writeValueAsString(
            ResponseSuccess(200, TurnoDTOvisualizeList(turMapper.toDTO(entities))))
    }

    /**
     * Este método sirve para buscar turnos por fecha y hora de inicio
     * @param horaInicio la fecha y hora del turno o turnos que se quieran buscar
     * Este método sirve para buscar un objeto de tipo Turno con la fecha pasada por parámetro
     * @return cadena de texto con los datos de Response en caso de que no existan turnos con esa fecha
     * @return cadena de texto con los datos de Response si encuentra un turno con esa fecha
     * devuelve la respuesta en formato json
     */
    suspend fun findAllTurnosByFecha(horaInicio: LocalDateTime) : String = withContext(Dispatchers.IO) {
        val entities = turRepo.findAll().toList().filter { it.horaInicio == horaInicio }

        if (entities.isEmpty()) json.writeValueAsString(
            ResponseError(404, "No turnos found."))
        else json.writeValueAsString(
                ResponseSuccess(200, TurnoDTOvisualizeList(turMapper.toDTO(entities))))
    }

    /**
     * Este método sirve para crear turnos
     * @param entity de tipo TurnoDTOCreate
     * @param token de tipo String
     * Comprueba que el token es válido y si se trata de un token de tipo administrador en caso de que validated no sea null
     * @return cadena de texto con los datos de validated respuesta de error por acceso no autorizado
     * Si validated es null se comprueba los campos del TurnoDTOCreate y devuelve
     * @return cadena de texto con los datos de Response en formato json en caso de que el turno se haya introducido de forma incorrecta
     * @return cadena de texto con los datos de Response si todos los campos son correctos y se aplica el guardado de forma correcta, devuelve un json
     */
    suspend fun createTurno(entity: TurnoDTOcreate, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.WORKER)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        if (fieldsAreIncorrect(entity))
            return@withContext json.writeValueAsString(
                ResponseError(400, "Cannot insert turno. Incorrect fields."))

        val res = turRepo.save(turMapper.fromDTO(entity))
        json.writeValueAsString(ResponseSuccess(201, turMapper.toDTO(res)))
    }

    /**
     * Este método sirve para borrar un turno
     * @param id de tipo UUID del turno que se quiera buscar
     * @param token de tipo String para validar el acceso al método
     * @return cadena de texto con los datos de validated si no es null devolverá el Response correspondiente
     * @return cadena de texto con los datos de Response en json del mensaje de error en caso de que el turno no sea encontrada por el repositorio
     * @return cadena de texto con los datos de Response en json en caso de que no se pueda aplicar el borrado al turno encontrado
     * @return cadena de texto con los datos de Response con formato json
     */
    suspend fun deleteTurno(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        val entity = turRepo.findByUUID(id)
            ?: return@withContext json.writeValueAsString(
                ResponseError(404, "Cannot delete. Turno with id $id not found."))
        val result = turRepo.delete(entity.id)
            ?: return@withContext json.writeValueAsString(
                ResponseError(500, "Unexpected error. Cannot delete Turno with id $id."))
        json.writeValueAsString(ResponseSuccess(200, turMapper.toDTO(result)))
    }

    /**
     * Este método sirve para poner en estado finalizado el turno con el identificador pasado por parámetro
     * @param id de tipo UUID del turno
     * @param token de tipo String para validar el acceso al método
     * @return cadena de texto con los datos de validated si no es null devolverá el Response correspondiente
     * @return cadena de texto con los datos de Response en json del mensaje de error en caso de que el turno no sea encontrada por el repositorio
     * @return cadena de texto con los datos de Response en json en caso de que no se pueda aplicar el cambio a inactivo del turno encontrado
     * @return cadena de texto con los datos de Response con formato json
     */
    suspend fun setFinalizadoTurno(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        val entity = turRepo.findByUUID(id)
            ?: return@withContext json.writeValueAsString(
                ResponseError(404, "Cannot set finalizado. Turno with id $id not found."))
        val result = turRepo.setFinalizado(entity.id)
            ?: return@withContext json.writeValueAsString(
                ResponseError(500, "Unexpected error. Cannot find and set finalizado turno with id $id."))
        json.writeValueAsString(ResponseSuccess(200, turMapper.toDTO(result)))
    }

    /**
     * @param id identificador de tipo UUID del objeto Tarea
     * Este método sirve para buscar un objeto de tipo Tarea con el id pasado por parámetro
     * @return cadena de texto con los datos de Response en caso de que no exista la tarea con ese identificador
     * @return cadena de texto con los datos de Response si encuentra una tarea con ese identificador
     * devuelve la respuesta en formato json
     */
    suspend fun findTareaById(id: UUID) : String = withContext(Dispatchers.IO) {
        val entity = tarRepo.findByUUID(id)

        if (entity == null) json.writeValueAsString(
            ResponseError(404, "Tarea with id $id not found."))
        else json.writeValueAsString(
            ResponseSuccess(200, tarMapper.toDTO(entity)))
    }

    /**
     * Este método devuelve todos las tareas que se encuentren registrados en la base de datos
     * @return cadena de texto con los datos de Response en caso de que no existan tareas
     * @return cadena de texto con los datos de Response con los datos de un TareaDTOVisualizeList con la lista de tareas
     * Por último coge el valor devuelto y le aplica un encode para devolverlo en formato json
     */
    suspend fun findAllTareas() : String = withContext(Dispatchers.IO) {
        var entities = tarRepo.findAll().toList()
        if (entities.size > 25) entities = entities.subList(0,24)

        if (entities.isEmpty()) json.writeValueAsString(
            ResponseError(404, "No tareas found."))
        else json.writeValueAsString(
            ResponseSuccess(200, TareaDTOvisualizeList(tarMapper.toDTO(entities))))
    }

    /**
     * Este método sirve para buscar tareas finalizadas
     * @param finalizada de tipo Boolean para definir el estado de las tareas a buscar
     * @return cadena de texto con los datos de Response en caso de que no existan tareas con ese estado
     * @return cadena de texto con los datos de Response si encuentra una tarea con ese estado true/false
     * devuelve la respuesta en formato json
     */
    suspend fun findAllTareasFinalizadas(finalizada: Boolean) : String = withContext(Dispatchers.IO) {
        var entities = tarRepo.findAll().toList().filter { it.finalizada == finalizada }
        if (entities.size > 25) entities = entities.subList(0,24)

        if (entities.isEmpty()) json.writeValueAsString(
            ResponseError(404, "No tareas found."))
        else json.writeValueAsString(
            ResponseSuccess(200, TareaDTOvisualizeList(tarMapper.toDTO(entities))))
    }

    /**
     * Este método sirve para crear una Tarea
     * @param entity de tipo TareaDTOCreate
     * @param token para validar el método
     * Comprueba que el token es válido y si se trata de un token de tipo administrador en caso de que validated no sea null
     * @return cadena de texto con los datos de validated respuesta de error por acceso no autorizado
     * Si validated es null se comprueba los campos del TareaDTOCreate y devuelve
     * @return cadena de texto con los datos de Response en formato json en caso de que la tarea se haya introducido de forma incorrecta
     * @return cadena de texto con los datos de Response en caso de que no se hayan pasado datos suficientes para el cordaje
     * @return cadena de texto con los datos de Response en caso de que haya una incoherencia de tipos en el EncordadoDTOCreate
     * @return cadena de texto con los datos de Response en caso de que el parámetro no sea del tipo requerido en AdquisicionDTOCreate
     * @return cadena de texto con los datos de Response en caso de que el parámetro no sea del tipo requerido en PersonalizacionDTOCreate
     * @return cadena de texto con los datos de Response si todos los campos son correctos y se aplica el guardado de forma correcta, devuelve un json
     */
    suspend fun createTarea(entity: TareaDTOcreate, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

        if (fieldsAreIncorrect(entity))
            return@withContext json.writeValueAsString(
                ResponseError(400, "Cannot insert tarea. Incorrect fields."))
        if (entity is EncordadoDTOcreate
            && (
            (entity.cordajeHorizontal.uuid == entity.cordajeVertical.uuid
            && entity.cordajeHorizontal.stock < 2) ||
            (entity.cordajeHorizontal.uuid != entity.cordajeVertical.uuid
            && entity.cordajeHorizontal.stock < 1) ||
            (entity.cordajeHorizontal.uuid != entity.cordajeVertical.uuid
            && entity.cordajeVertical.stock < 1)
            ))
            return@withContext json.writeValueAsString(
                ResponseError(400, "Cannot insert tarea. Not enough material for cordaje."))

        when (entity) {
            is EncordadoDTOcreate -> {
                if (entity.raqueta.tipo != TipoProducto.RAQUETAS ||
                    entity.cordajeHorizontal.tipo != TipoProducto.CORDAJES ||
                    entity.cordajeVertical.tipo != TipoProducto.CORDAJES)
                    return@withContext json.writeValueAsString(
                        ResponseError(400, "Cannot insert tarea. Type mismatch in product types."))
            }
            is AdquisicionDTOcreate -> {
                if (entity.raqueta.tipo != TipoProducto.RAQUETAS)
                    return@withContext json.writeValueAsString(
                        ResponseError(400, "Cannot insert tarea. Parameter is not of type Raqueta."))
            }
            is PersonalizacionDTOcreate -> {
                if (entity.raqueta.tipo != TipoProducto.RAQUETAS)
                    return@withContext json.writeValueAsString(
                        ResponseError(400, "Cannot insert tarea. Parameter is not of type Raqueta."))
            }
        }

        val res = tarRepo.save(entity.fromDTO())
        json.writeValueAsString(ResponseSuccess(201, tarMapper.toDTO(res)))
    }

    /**
     * Este método sirve para borrar una tarea
     * @param id de tipo UUID del turno que se quiera buscar
     * @param token de tipo String para validar el acceso al método
     * @return cadena de texto con los datos de validated si no es null devolverá el Response correspondiente
     * @return cadena de texto con los datos de Response en json del mensaje de error en caso de que la tarea no sea encontrada por el repositorio
     * @return cadena de texto con los datos de Response en json en caso de que no se pueda aplicar el borrado a la tarea encontrada
     * @return cadena de texto con los datos de Response con formato json
     */
    suspend fun deleteTarea(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

            val entity = tarRepo.findByUUID(id)
                ?: return@withContext json.writeValueAsString(
                    ResponseError(404, "Cannot delete. Tarea with id $id not found."))
            val result = tarRepo.delete(entity.id)
                ?: return@withContext json.writeValueAsString(
                    ResponseError(500, "Unexpected error. Cannot delete tarea with id $id."))
            json.writeValueAsString(ResponseSuccess(200, tarMapper.toDTO(result)))
    }

    /**
     * Este método sirve para poner en estado finalizado la tarea con el identificador pasado por parámetro
     * @param id de tipo UUID del turno
     * @param token de tipo String para validar el acceso al método
     * @return cadena de texto con los datos de validated si no es null devolverá el Response correspondiente
     * @return cadena de texto con los datos de Response en json del mensaje de error en caso de que la tarea no sea encontrada por el repositorio
     * @return cadena de texto con los datos de Response en json en caso de que no se pueda aplicar el cambio a finalizada de la tarea encontrada
     * @return cadena de texto con los datos de Response con formato json
     */
    suspend fun setFinalizadaTarea(id: UUID, token: String) : String = withContext(Dispatchers.IO) {
        val validated = checkToken(token, UserProfile.ADMIN)
        if (validated != null) return@withContext json.writeValueAsString(validated)

            val entity = tarRepo.findByUUID(id)
                ?: return@withContext json.writeValueAsString(
                    ResponseError(404, "Cannot set finalizado. Tarea with id $id not found."))
            val result = tarRepo.setFinalizada(entity.id)
                ?: return@withContext json.writeValueAsString(
                    ResponseError(500, "Unexpected error. Cannot find and set finalizada tarea with id $id."))
            json.writeValueAsString(ResponseSuccess(200, tarMapper.toDTO(result)))
    }

    /**
     * Este método sirve para iniciar sesión de un usuario
     * @param user de tipo UserDTOLogin
     * @return Response en caso de que el token sea null
     * @return Response si el token no es null
     */
    suspend fun login(user: UserDTOLogin): String = withContext(Dispatchers.IO) {
        val token = com.example.tennislabspringboot.services.login.login(user, uRepo)
        if (token == null) json.writeValueAsString(
            ResponseError(400, "Unable to login. Incorrect email or password."))
        else json.writeValueAsString(ResponseSuccess(200, token))
    }

    /**
     * Este método sirve para registrar un usuario
     * @param user de tipo UserDTORegister
     * @return Response en caso de que el token sea null
     * @return Response si el token no es null
     */
    suspend fun register(user: UserDTORegister): String = withContext(Dispatchers.IO) {
        val token = com.example.tennislabspringboot.services.login.register(user, uRepo)
        if (token == null) json.writeValueAsString(
            ResponseError(400, "Unable to register. Incorrect parameters."))
        else json.writeValueAsString(ResponseSuccess(200, token))
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        val j1 = launch { uRepo.deleteAll() }
        val j2 = launch { maRepo.deleteAll() }
        val j3 = launch { proRepo.deleteAll() }
        val j4 = launch { pedRepo.deleteAll() }
        val j5 = launch { turRepo.deleteAll() }
        val j6 = launch { tarRepo.deleteAll() }
        joinAll(j1, j2, j3, j4, j5, j6)
    }
}