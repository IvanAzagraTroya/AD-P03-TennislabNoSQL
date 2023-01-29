package repositories

import db.DBManager
import db.readProperties
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import models.maquina.Maquina
import models.maquina.TipoMaquina
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.litote.kmongo.newId
import repositories.turno.TurnoRepository
import utils.toUUID
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TurnoRepositoryTest {
    private val repository = TurnoRepository()

    val client = User(
        id= newId(),
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
    val raqueta = Producto(
        id= newId(),
        uuid = UUID.fromString("93a98d69-0001-48a7-b34f-05b596ea83ba"),
        tipo = TipoProducto.RAQUETAS,
        marca = "MarcaRaqueta",
        modelo = "ModeloRaqueta",
        precio = 150.5,
        stock = 3
    )
    val producto1 = Producto(
        id= newId(),
        uuid = UUID.fromString("93a98d69-0002-48a7-b34f-05b596ea83ac"),
        tipo = TipoProducto.ANTIVIBRADORES,
        marca = "MarcaX",
        modelo = "ModeloX",
        precio = 10.5,
        stock = 5
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
    val adquisicion1 = Tarea(
        id= newId(),
        uuid = UUID.fromString("93a98d69-0011-48a7-b34f-05b596ea83ca"),
        raquetaId = raqueta.uuid,
        precio = raqueta.precio,
        tipo = TipoTarea.ADQUISICION,
        finalizada = false,
        pedidoId = pedido.uuid,
        productoAdquiridoId = raqueta.uuid,
        peso = null,
        balance = null,
        rigidez = null,
        tensionHorizontal = null,
        cordajeHorizontalId = null,
        tensionVertical = null,
        cordajeVerticalId = null,
        dosNudos = null

    )
    val personalizacion = Tarea(
        uuid = UUID.fromString("93a98d69-0015-48a7-b34f-05b596ea8aab"),
        raquetaId = producto1.uuid,
        precio = producto1.precio,
        tipo = TipoTarea.PERSONALIZACION,
        finalizada = false,
        pedidoId = pedido.uuid,
        productoAdquiridoId = producto1.uuid,
        peso = 890,
        balance = 15.4,
        rigidez = 4,
        tensionHorizontal = null,
        cordajeHorizontalId = null,
        tensionVertical = null,
        cordajeVerticalId = null,
        dosNudos = null

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
            val properties = readProperties()
            val MONGO_TYPE = DBManager.properties.getProperty("MONGO_TYPE")
            val HOST = DBManager.properties.getProperty("HOST")
            val PORT = DBManager.properties.getProperty("PORT")
            val DATABASE = DBManager.properties.getProperty("DATABASE")
            val USERNAME = DBManager.properties.getProperty("USERNAME")
            val PASSWORD = DBManager.properties.getProperty("PASSWORD")
            val OPTIONS = DBManager.properties.getProperty("OPTIONS")

            val MONGO_URI = "$MONGO_TYPE$USERNAME:$PASSWORD@$HOST/$DATABASE"
        }
    }

    @BeforeEach
    fun setUp() = runBlocking {

    }

    @AfterEach
    fun tearDown() = runBlocking {

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @DisplayName("find by id")
    fun findById() = runTest {
        val result = repository.findById(turno.id)

        assertAll(
            { assertNotNull(result) },
            { assertEquals("93a98d69-0019-48a7-b34f-05b596ea8abc".toUUID(), result!!.id) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @DisplayName("find all")
    fun findAll() = runTest {
        val result = repository.findAll().toList()

        assertAll(
            {assertNotNull(result)},
            {assertEquals("93a98d69-0019-48a7-b34f-05b596ea8abc".toUUID(), turno.uuid)}
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @DisplayName("find by id not exists")
    fun findByIdNotExists() = runTest {
        val res = repository.findById(newId())
        Assertions.assertNull(res)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @DisplayName("insert")
    fun save() = runTest {
        val res = repository.save(turno)
        Assertions.assertEquals(turno.id, res.id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @DisplayName("delete")
    fun delete() = runTest {
        val res = repository.save(turno)
        val result = repository.delete(res.id)

        assertAll(
            { Assertions.assertEquals(result?.uuid, res.uuid) },
            { Assertions.assertEquals(result?.horaInicio, res.horaInicio) }
        )
    }
}