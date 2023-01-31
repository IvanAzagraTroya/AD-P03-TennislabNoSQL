package repositories

import db.DBManager
import db.readProperties
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import koin.models.producto.Producto
import koin.models.producto.TipoProducto
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.litote.kmongo.newId
import koin.repositories.producto.ProductoRepository
import utils.toUUID
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductoRepositoryTest {
    private val repository = ProductoRepository()

    val producto = Producto(
        id = newId(),
        uuid = UUID.fromString("93a98d69-6da6-48a7-b34f-05b596ea83aa"),
        tipo = TipoProducto.FUNDAS,
        marca = "MarcaZ",
        modelo = "ModeloZ",
        precio = 36.4,
        stock = 8
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
            { assertEquals("93a98d69-6da6-48a7-b34f-05b596ea83aa".toUUID(), result[0].uuid) },
            { assertEquals(0, result.size) },
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findById() = runTest {
        val result = repository.findById(producto.id)

        Assertions.assertAll(
            { assertEquals("93a98d69-6da6-48a7-b34f-05b596ea83aa".toUUID(), result?.uuid) },
            { assertEquals("MarcaZ", result?.marca) },
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
        val result = repository.findByUUID("93a98d69-6da6-48a7-b34f-05b596ea83aa".toUUID())

        assertAll(
            { assertNotNull(result) },
            { assertEquals("MarcaZ", result!!.marca) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun save() = runTest {
        val result = repository.save(producto)

        assertAll(
            { assertEquals(result.uuid, producto.uuid) },
            { assertEquals(result.marca, producto.marca) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun delete() = runTest {
        val res = repository.save(producto)
        val result = repository.delete(res.id)

        assertAll(
            { assertEquals(result?.uuid, res.uuid) },
            { assertEquals(result?.marca, res.marca) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteNotExists() = runTest {
        val delete = producto.copy(id = newId())
        val result = repository.delete(delete.id)

        assertNull(result)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun decreaseStock() = runTest {
        val result = repository.decreaseStock(producto.id)

        assertAll(
            { assertNotNull(result) },
            { assertEquals(result!!.stock, producto.stock-1)}
        )
    }
}