package repositories

import kotlinx.coroutines.runBlocking
import models.maquina.Maquina
import models.pedido.Pedido
import models.pedido.PedidoState
import models.user.User
import models.user.UserProfile
import models.producto.Producto
import models.producto.TipoProducto
import models.tarea.Tarea
import models.tarea.TipoTarea
import models.turno.Turno
import org.junit.jupiter.api.*
import repositories.turno.TurnoRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TurnoRepositoryTest {
    private val repository = TurnoRepository()

    val client = User(
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
        uuid = UUID.fromString("93a98d69-0007-48a7-b34f-05b596ea839c"),
        nombre = "Luis",
        apellido = "Martinez",
        telefono = "632950281",
        email = "email@email.com",
        password = "estacontraseñanoestaensha512",
        perfil = UserProfile.WORKER,
        activo = true
    )
    val raqueta = Producto(
        uuid = UUID.fromString("93a98d69-0001-48a7-b34f-05b596ea83ba"),
        tipo = TipoProducto.RAQUETAS,
        marca = "MarcaRaqueta",
        modelo = "ModeloRaqueta",
        precio = 150.5,
        stock = 3
    )
    val producto1 = Producto(
        uuid = UUID.fromString("93a98d69-0002-48a7-b34f-05b596ea83ac"),
        tipo = TipoProducto.ANTIVIBRADORES,
        marca = "MarcaX",
        modelo = "ModeloX",
        precio = 10.5,
        stock = 5
    )
    val personalizadora1 = Maquina(
        uuid = UUID.fromString("93a98d69-0008-48a7-b34f-05b596ea83bb"),
        modelo = "RTX-3080TI",
        marca = "Nvidia",
        fechaAdquisicion = LocalDate.parse("2022-11-10"),
        numeroSerie = "123456789X",
        measuresRigidity = false,
        measuresBalance = true,
        measuresManeuverability = true
    )
    val pedido = Pedido(
        uuid = UUID.fromString("93a98d69-0010-48a7-b34f-05b596ea8acc"),
        userId = client.uuid,
        state = PedidoState.PROCESO,
        fechaEntrada = LocalDate.parse("2013-10-10"),
        fechaSalida = LocalDate.parse("2023-12-12"),
        topeEntrega = null,
        precio = 0.0
    )
    val adquisicion1 = Tarea(
        uuid = UUID.fromString("93a98d69-0011-48a7-b34f-05b596ea83ca"),
        raquetaId = raqueta.uuid,
        finalizada = false,
        pedidoId = pedido.uuid,
        precio = producto1.precio,

    )
    val personalizacion = Tarea(
        uuid = UUID.fromString("93a98d69-0015-48a7-b34f-05b596ea8aab"),
        raquetaId = raqueta.uuid,
        precio = raqueta.precio,
        tipo = TipoTarea.PERSONALIZACION,
        finalizada = false,
        pedidoId = ,
        peso = 890,
        balance = 15.4,
        rigidez = 4
    )
    val turno = Turno(
        uuid = UUID.fromString("93a98d69-0019-48a7-b34f-05b596ea8abc"),
        workerId = worker.uuid,
        maquinaId = personalizadora1.uuid,
        horaInicio = LocalDateTime.of(2002, 10, 14, 10, 9),
        horaFin = null,
        numPedidosActivos = 2,
        tarea1Id = personalizacion.uuid,
        tarea2Id = adquisicion1.uuid,
        finalizado = false
    )

    /**
     * Inicializacion de la base de datos para testing y carga de datos necesarios.
     */
    companion object {
        @JvmStatic
        @BeforeAll
        fun initialize() = runBlocking {

        }
    }

    @BeforeEach
    fun setUp() = runBlocking {
        repository.save(turno)
        println("set up completed.")
    }

    @AfterEach
    fun tearDown() = runBlocking {
        repository.delete(turno.id)
        println("teared down successfully.")
    }

    @Test
    @DisplayName("find by id")
    fun findById() = runBlocking {
        val res = repository.findById(turno.id)
        Assertions.assertEquals(turno.id, res?.id)
    }

    @Test
    @DisplayName("find all")
    fun findAll() = runBlocking {
        val list = repository.findAll()
        val res = list.equals(it.id == turno.id)
        Assertions.assertEquals(turno.id, res?.id)
    }

    @Test
    @DisplayName("find by id inexistente")
    fun findByIdInexistente() = runBlocking {
        val res = repository.findById(UUID.fromString("93a98d69-0000-1112-0000-05b596ea83ba"))
        Assertions.assertNull(res)
    }

    @Test
    @DisplayName("insert")
    fun insert() = runBlocking {
        val res = repository.save(turno)
        Assertions.assertEquals(turno.id, res.id)
    }

    @Test
    @DisplayName("delete")
    fun delete() = runBlocking {
        val res1 = repository.delete(turno.id)
        val res2 = repository.findById(turno.id)
        assertAll({
            Assertions.assertNull(res2)
            Assertions.assertTrue(res1)
        })
    }
}