package repositories

import koin.db.DBManager
import koin.db.readProperties
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import koin.models.pedido.Pedido
import koin.models.pedido.PedidoState
import koin.models.producto.Producto
import koin.models.producto.TipoProducto
import koin.models.tarea.Tarea
import koin.models.tarea.TipoTarea
import koin.models.user.User
import koin.models.user.UserProfile
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.litote.kmongo.newId
import koin.repositories.tarea.TareaRepository
import koin.utils.toUUID
import java.time.LocalDate
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TareaRepositoryTest {
    private val repository = TareaRepository()

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
        tipo = TipoTarea.ADQUISICION,
        finalizada = false,
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
    companion object{
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

    /*@BeforeEach
    fun setUp() {
        dataBaseService.clearDataBaseData()
        dataBaseService.initDataBaseData()
    }

    @AfterAll
    fun tearDown() {
        dataBaseService.clearDataBaseData()
    }*/

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAll() = runTest {
        val result = repository.findAll().toList()

        assertAll(
            { assertNotNull(result) },
            { assertEquals("93a98d69-0013-48a7-b34f-05b596ea83cc".toUUID(), result[0].uuid) },
            { assertEquals(0, result.size) },
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findById() = runTest {
        val result = repository.findById(tarea.id)

        Assertions.assertAll(
            { assertEquals("93a98d69-0013-48a7-b34f-05b596ea83cc".toUUID(), result?.uuid) },
            { assertEquals(TipoTarea.ADQUISICION, result?.tipo) },
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByIdNotExists() = runTest {
        val result = repository.findById(newId())

        assertNull(result)

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByUuid() = runTest {
        val result = repository.findByUUID("93a98d69-0013-48a7-b34f-05b596ea83cc".toUUID())

        assertAll(
            { assertNotNull(result) },
            { assertEquals(TipoTarea.ADQUISICION, result?.tipo) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun save() = runTest {
        val result = repository.save(tarea)

        assertAll(
            { assertEquals(result.uuid, tarea.uuid) },
            { assertEquals(result.tipo, tarea.tipo) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun delete() = runTest {
        val res = repository.save(tarea)
        val result = repository.delete(res.id)

        assertAll(
            { assertEquals(result?.uuid, res.uuid) },
            { assertEquals(result?.tipo, res.tipo) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteNotExists() = runTest {
        val delete = tarea.copy(id = newId())
        val result = repository.delete(delete.id)

        assertNull(result)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun setFinalizada() = runTest {
        val result = repository.setFinalizada(tarea.id)

        assertAll(
            { assertNotNull(result) },
            { assertEquals(result?.finalizada, tarea.finalizada)}
        )
    }
}