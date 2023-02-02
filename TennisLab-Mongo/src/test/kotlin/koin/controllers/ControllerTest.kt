package koin.controllers

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import koin.controllers.Controller
import koin.mappers.toDTO
import koin.models.maquina.Maquina
import koin.models.maquina.TipoMaquina
import koin.models.pedido.Pedido
import koin.models.pedido.PedidoState
import koin.models.producto.Producto
import koin.models.producto.TipoProducto
import koin.models.tarea.Tarea
import koin.models.tarea.TipoTarea
import koin.models.turno.Turno
import koin.models.user.User
import koin.models.user.UserProfile
import koin.repositories.maquina.MaquinaRepository
import koin.repositories.pedido.PedidoRepository
import koin.repositories.producto.ProductoRepository
import koin.repositories.tarea.TareaRepository
import koin.repositories.turno.TurnoRepository
import koin.repositories.user.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.litote.kmongo.newId
import org.litote.kmongo.util.idValue
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class ControllerTest {

    @MockK
    lateinit var repoM: MaquinaRepository

    @MockK
    lateinit var repoPedido: PedidoRepository

    @MockK
    lateinit var repoProd: ProductoRepository

    @MockK
    lateinit var repoTarea: TareaRepository

    @MockK
    lateinit var repoTurno: TurnoRepository

    @MockK
    lateinit var repoUser: UserRepository

    @InjectMockKs
    lateinit var controller: Controller

    val raqueta = Producto(
        id = newId(),
        uuid = UUID.fromString("93a98d69-0001-48a7-b34f-05b596ea83ba"),
        tipo = TipoProducto.RAQUETAS,
        marca = "MarcaRaqueta",
        modelo = "ModeloRaqueta",
        precio = 150.5,
        stock = 3
    )
    val client = User(
        id = newId(),
        uuid = UUID.fromString("93a98d69-0006-48a7-b34f-05b596ea839a"),
        nombre = "Maria",
        apellido = "Martinez",
        telefono = "632120281",
        email = "email2@email.com",
        password = "contra",
        perfil = UserProfile.CLIENT,
        activo = true
    )
    val worker = User(
        id= newId(),
        uuid = UUID.fromString("93a98d69-0007-48a7-b34f-05b596ea839c"),
        nombre = "Luis",
        apellido = "Martinez",
        telefono = "632950281",
        email = "email@email.com",
        password = "estacontraseñanoestaensha512",
        perfil = UserProfile.WORKER,
        activo = true
    )
    val producto = Producto(
        id = newId(),
        uuid = UUID.fromString("93a98d69-0004-48a7-b34f-05b596ea83aa"),
        tipo = TipoProducto.FUNDAS,
        marca = "MarcaZ",
        modelo = "ModeloZ",
        precio = 36.4,
        stock = 8
    )
    val pedido = Pedido(
        id = newId(),
        uuid = UUID.fromString("93a98d69-0010-48a7-b34f-05b596ea8acc"),
        userId = client.uuid,
        state = PedidoState.PROCESO,
        fechaEntrada = LocalDate.parse("2013-10-10"),
        fechaSalida = LocalDate.parse("2023-12-12"),
        topeEntrega = LocalDate.parse("2023-12-14"),
        precio = 0.0
    )
    val tarea = Tarea(
        id = newId(),
        uuid = UUID.fromString("93a98d69-0013-48a7-b34f-05b596ea83cc"),
        raquetaId = raqueta.uuid,
        precio = producto.precio,
        tipo = TipoTarea.PERSONALIZACION,
        finalizada = true,
        pedidoId = pedido.uuid,
        productoAdquiridoId = producto.uuid,
        peso = null,
        balance = null,
        rigidez = null,
        tensionHorizontal = null,
        cordajeHorizontalId = null,
        tensionVertical = null,
        cordajeVerticalId = null,
        dosNudos = null
    )
    val personalizadora1 = Maquina(
        id = newId(),
        uuid = UUID.fromString("93a98d69-0008-48a7-b34f-05b596ea83bb"),
        modelo = "RTX-3080TI",
        marca = "Nvidia",
        fechaAdquisicion = LocalDate.parse("2022-11-10"),
        numeroSerie = "123456789X",
        tipo = TipoMaquina.PERSONALIZADORA,
        activa = true,
        isManual = null,
        maxTension = null,
        minTension = null,
        measuresRigidity = false,
        measuresBalance = true,
        measuresManeuverability = true
    )
    val turno = Turno(
        uuid = UUID.fromString("93a98d69-0019-48a7-b34f-05b596ea8abc"),
        workerId = worker.uuid,
        maquinaId = personalizadora1.uuid,
        horaInicio = LocalDateTime.of(2002, 10, 14, 10, 9),
        horaFin = LocalDateTime.of(2002, 10, 14, 16, 49),
        numPedidosActivos = 2,
        tarea1Id = tarea.uuid,
        tarea2Id = null,
        finalizado = false
    )

    init {
        MockKAnnotations.init(this)
    }

    @Test
    fun findMaquinaById() = runTest {
        val res = """"""
        coEvery{ repoM.findByUUID(personalizadora1.uuid)}

        val result = controller.findMaquinaById(personalizadora1.uuid)


        assertAll(
            { assertEquals(res, result) }
        )
    }

    @Test
    fun findMaquinaNotExistsById() = runTest {
        val uuid = UUID.randomUUID()
        val res = """"""
        coEvery { repoM.findByUUID(uuid) }

        val result = controller.findMaquinaById(uuid)

        assertAll(
            { assertEquals(res, result)}
        )
    }


    @Test
    fun findAllMaquinasSuccess() = runTest {
        val response = """"""
        coEvery { repoM.findAll() }

        val result = controller.findAllMaquinas()

        assertAll(
            { assertEquals(response, result)}
        )
    }

    @Test
    fun findAllMaquinasError() = runTest {

    }

    @Test
    fun createMaquinaCorrect() = runTest {
        val response = """"""
        coEvery { repoM.save(personalizadora1) }
        val res = controller.createMaquina(dto, (token correcto))

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun createMaquinaIncorrect() = runTest {
        val response = """"""
        coEvery { repoM.save(personalizadora1)}
        val res = controller.createMaquina(dto, "")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun deleteMaquinaCorrect() = runTest {
        val response = """"""
        coEvery { repoM.delete(personalizadora1.id)}
        val res = controller.deleteMaquina(personalizadora1.uuid, (token correcto))

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deleteMaquinaIncorrect() = runTest {
        val response = """"""
        coEvery { repoM.delete(personalizadora1.id)}
        val res = controller.deleteMaquina(personalizadora1.uuid, "")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun setInactiveMaquinaCorrect() = runTest {
        val response = """"""
        coEvery { repoM.setInactive(personalizadora1.id)}
        val res = controller.setInactiveMaquina(personalizadora1.uuid, (tokenCorrecto))

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun setInactiveMaquinaIncorrect() = runTest {
        val response = """"""
        coEvery { repoM.setInactive(personalizadora1.id)}
        val res = controller.setInactiveMaquina(personalizadora1.uuid, "")

        assertAll(
            { assertEquals(response, res) }
        )
    }
    //-----------------------------PEDIDOS-----------------------------------
    @Test
    fun findPedidoById() = runTest {
        coEvery{ repoPedido.findByUUID(pedido.uuid)}

        val result = controller.findPedidoById(pedido.uuid)


        assertAll(
            { assertEquals("", result) }
        )
    }

    @Test
    fun findPedidoNotExistsById() = runTest {
        val uuid = UUID.randomUUID()
        val res = """"""
        coEvery { repoPedido.findByUUID(uuid) }

        val result = controller.findPedidoById(uuid)

        assertAll(
            { assertEquals("", result)}
        )
    }


    @Test
    fun findAllPedidosSuccess() = runTest {
        val response = """"""
        coEvery { repoPedido.findAll() }

        val result = controller.findAllPedidos()

        assertAll(
            { assertEquals(response, result)}
        )
    }

    @Test
    fun findAllPedidosError() = runTest {

    }

    @Test
    fun findAllPedidosWithStateCorrect() = runTest {
        val response = """"""
        coEvery { repoPedido.findAll()}

        val res = controller.findAllPedidosWithState(PedidoState.PROCESO)

        assertAll(
            { assertEquals(response, res)}
        )
    }
    @Test
    fun findAllPedidosWithStateIncorrect() = runTest {
        val response = """"""
        coEvery { repoPedido.findAll()}

        val res = controller.findAllPedidosWithState(PedidoState.PROCESO)

        assertAll(
            { assertEquals(response, res)}
        )
    }
    @Test
    fun createPedidoCorrect() = runTest {
        val response = """"""
        coEvery { repoPedido.save(pedido) }
        val res = controller.createPedido(dto, (token correcto))

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun createPedidoIncorrect() = runTest {
        val response = """"""
        coEvery { repoPedido.save(pedido)}
        val res = controller.createPedido(dto, "")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun deletePedidoCorrect() = runTest {
        val response = """"""
        coEvery { repoPedido.delete(pedido.id)}
        val res = controller.deletePedido(pedido.uuid, (token correcto))

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deletePedidoIncorrect() = runTest {
        val response = """"""
        coEvery { repoPedido.delete(pedido.id)}
        val res = controller.deletePedido(pedido.uuid, "")

        assertAll(
            { assertEquals(response, res) }
        )
    }
    //-----------------------------PRODUCTO-----------------------------------
    @Test
    fun findProductoById() = runTest {
        val res = """"""
        coEvery{ repoProd.findByUUID(raqueta.uuid)}

        val result = controller.findProductoById(raqueta.uuid)


        assertAll(
            { assertEquals(res, result) }
        )
    }

    @Test
    fun findProductoNotExistsById() = runTest {
        val uuid = UUID.randomUUID()
        val res = """"""
        coEvery { repoProd.findByUUID(uuid) }

        val result = controller.findProductoById(uuid)

        assertAll(
            { assertEquals(res, result)}
        )
    }


    @Test
    fun findAllProductosSuccess() = runTest {
        val response = """"""
        coEvery { repoProd.findAll() }

        val result = controller.findAllProductos()

        assertAll(
            { assertEquals(response, result)}
        )
    }

    @Test
    fun findAllProductosError() = runTest {

    }

    @Test
    fun findAllProductosDisponiblesCorrect() = runTest {
        val response = """"""
        coEvery { repoProd.findAll()}

        val result = controller.findAllProductosDisponibles()

        assertAll(
            { assertEquals(response, result)}
        )
    }

    @Test
    fun findAllProductosDisponiblesIncorrect() = runTest {
        val response = """"""
        coEvery { repoProd.findAll()}

        val result = controller.findAllProductosDisponibles()

        assertAll(
            { assertEquals(response, result)}
        )
    }
    @Test
    fun createProductoCorrect() = runTest {
        val response = """"""
        coEvery { repoProd.save(raqueta) }
        val res = controller.createProducto(dto, (token correcto))

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun createProductoIncorrect() = runTest {
        val response = """"""
        coEvery { repoProd.save(raqueta)}
        val res = controller.createProducto(dto, "")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun deleteProductoCorrect() = runTest {
        val response = """"""
        coEvery { repoProd.delete(raqueta.id)}
        val res = controller.deleteProducto(raqueta.uuid, (token correcto))

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deleteProductoIncorrect() = runTest {
        val response = """"""
        coEvery { repoProd.delete(raqueta.id)}
        val res = controller.deleteProducto(raqueta.uuid, "")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun decreaseStockFromProductoCorrect() = runTest {
        val response = """"""
        coEvery { repoProd.decreaseStock(raqueta.id)}
        val res = controller.decreaseStockFromProducto(raqueta.uuid, token)

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun decreaseStockFromProductoInorrect() = runTest {
        val response = """"""
        coEvery { repoProd.decreaseStock(raqueta.id)}
        val res = controller.decreaseStockFromProducto(raqueta.uuid, "")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    //-------------------------TAREA----------------------------
    @Test
    fun findTareaById() = runTest {
        val res = """"""
        coEvery{ repoTarea.findByUUID(tarea.uuid)}

        val result = controller.findTareaById(tarea.uuid)


        assertAll(
            { assertEquals(res, result) }
        )
    }

    @Test
    fun findTareaNotExistsById() = runTest {
        val uuid = UUID.randomUUID()
        val res = """"""
        coEvery { repoTarea.findByUUID(uuid) }

        val result = controller.findTareaById(uuid)

        assertAll(
            { assertEquals(res, result)}
        )
    }


    @Test
    fun findAllTareasSuccess() = runTest {
        val response = """"""
        coEvery { repoTarea.findAll() }

        val result = controller.findAllTareas()

        assertAll(
            { assertEquals(response, result)}
        )
    }

    @Test
    fun findAllTareasError() = runTest {

    }

    @Test
    fun findAllTareasFinalizadasCorrect() = runTest {
        val response = """"""
        coEvery { repoTarea.findAll()}

        val result = controller.findAllTareasFinalizadas(true)

        assertAll(
            { assertEquals(response, result)}
        )
    }

    @Test
    fun findAllTareasFinalizadasIncorrect() = runTest {
        val response = """"""
        coEvery { repoTarea.findAll()}

        val result = controller.findAllTareasFinalizadas(false)

        assertAll(
            { assertEquals(response, result)}
        )
    }
    @Test
    fun createTareaCorrect() = runTest {
        val response = """"""
        coEvery { repoTarea.save(tarea) }
        val res = controller.createTarea(dto, (token correcto))

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun createTareaIncorrect() = runTest {
        val response = """"""
        coEvery { repoTarea.save(tarea)}
        val res = controller.createTarea(dto, "")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun deleteTareaCorrect() = runTest {
        val response = """"""
        coEvery { repoTarea.delete(tarea.id)}
        val res = controller.deleteTarea(tarea.uuid, (token correcto))

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deleteTareaIncorrect() = runTest {
        val response = """"""
        coEvery { repoTarea.delete(tarea.id)}
        val res = controller.deleteTarea(tarea.uuid, "")

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun setFinalizadaTareaCorrect() = runTest {
        val response = """"""
        coEvery { repoTarea.setFinalizada(tarea.id)}
        val res = controller.setFinalizadaTarea(tarea.uuid, token)

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun setFinalizadaTareaIncorrect() = runTest {
        val response = """"""
        coEvery { repoTarea.setFinalizada(tarea.id)}
        val res = controller.setFinalizadaTarea(tarea.uuid, "")

        assertAll(
            { assertEquals(response, res) }
        )
    }
    //------------------------------TURNO----------------------------------
    @Test
    fun findTurnoById() = runTest {
        val res = """"""
        coEvery{ repoTurno.findByUUID(turno.uuid)}

        val result = controller.findTurnoById(turno.uuid)


        assertAll(
            { assertEquals(res, result) }
        )
    }

    @Test
    fun findTurnoNotExistsById() = runTest {
        val uuid = UUID.randomUUID()
        val res = """"""
        coEvery { repoTurno.findByUUID(uuid) }

        val result = controller.findTurnoById(uuid)

        assertAll(
            { assertEquals(res, result)}
        )
    }


    @Test
    fun findAllTurnosSuccess() = runTest {
        val response = """"""
        coEvery { repoTurno.findAll() }

        val result = controller.findAllTurnos()

        assertAll(
            { assertEquals(response, result)}
        )
    }

    @Test
    fun findAllTurnosError() = runTest {

    }

    @Test
    fun findAllTurnosByFechaCorrect() = runTest {
        val response = """"""
        coEvery { repoTurno.findAll()}
        val result = controller.findAllTurnosByFecha(LocalDateTime.of(2002, 10, 14, 10, 9))
        assertAll(
            { assertEquals(response, result)}
        )
    }

    @Test
    fun findAllTurnosByFechaIncorrect() = runTest {
        val response = """"""
        coEvery { repoTurno.findAll()}
        val result = controller.findAllTurnosByFecha(LocalDateTime.of(2002, 10, 14, 3, 9))
        assertAll(
            { assertEquals(response, result)}
        )
    }

    @Test
    fun createTurnoCorrect() = runTest {
        val response = """"""
        coEvery { repoTurno.save(turno) }
        val res = controller.createTurno(dto, (token correcto))

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun createTurnoIncorrect() = runTest {
        val response = """"""
        coEvery { repoTurno.save(turno)}
        val res = controller.createTurno(dto, "")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun deleteTurnoCorrect() = runTest {
        val response = """"""
        coEvery { repoTurno.delete(turno.id)}
        val res = controller.deleteTurno(turno.uuid, (token correcto))

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deleteTurnoIncorrect() = runTest {
        val response = """"""
        coEvery { repoTurno.delete(turno.id)}
        val res = controller.deleteTurno(turno.uuid, "")

        assertAll(
            { assertEquals(response, res) }
        )
    }
    //---------------------------------USER-------------------------------
    @Test
    fun findUserByUuid() = runTest {
        val res = """"""
        coEvery{ repoUser.findByUUID(client.uuid)}

        val result = controller.findUserByUuid(client.uuid)


        assertAll(
            { assertEquals(res, result) }
        )
    }

    @Test
    fun findUserNotExistsByUuid() = runTest {
        val uuid = UUID.randomUUID()
        val res = """"""
        coEvery { repoUser.findByUUID(uuid) }

        val result = controller.findUserByUuid(uuid)

        assertAll(
            { assertEquals(res, result)}
        )
    }

    @Test
    fun findUserById() = runTest {
        val res = """"""
        coEvery { repoUser.findById(5) }
        val result = controller.findUserById(5)

        assertAll(
            { assertEquals(res, result) }
        )
    }


    @Test
    fun findAllUsersSuccess() = runTest {
        val response = """"""
        coEvery { repoUser.findAll() }

        val result = controller.findAllUsers()

        assertAll(
            { assertEquals(response, result)}
        )
    }

    @Test
    fun findAllUsersError() = runTest {

    }

    @Test
    fun createUserCorrect() = runTest {
        val response = """"""
        coEvery { repoUser.save(client) }
        val res = controller.createUser(dto, (token correcto))

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun createUserIncorrect() = runTest {
        val response = """"""
        coEvery { repoUser.save(client)}
        val res = controller.createUser(dto, "")

        assertAll(
            { assertEquals(response, res)}
        )
    }

    @Test
    fun deleteUserCorrect() = runTest {
        val response = """"""
        coEvery { repoUser.delete(client.id)}
        val res = controller.deleteUser(client.uuid, (token correcto))

        assertAll(
            { assertEquals(response, res) }
        )
    }

    @Test
    fun deleteUserIncorrect() = runTest {
        val response = """"""
        coEvery { repoUser.delete(client.id)}
        val res = controller.deleteUser(client.uuid, "")

        assertAll(
            { assertEquals(response, res) }
        )
    }
}