package repositories

import kotlinx.coroutines.runBlocking
import models.producto.Producto
import models.producto.TipoProducto
import org.junit.jupiter.api.*
import repositories.producto.ProductoRepository
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductoRepositoryTest {
    private val repository = ProductoRepository()

    val producto = Producto(
        uuid = UUID.fromString("93a98d69-6da6-48a7-b34f-05b596ea83aa"),
        tipo = TipoProducto.FUNDAS,
        marca = "MarcaZ",
        modelo = "ModeloZ",
        precio = 36.4,
        stock = 8
    )

    companion object {
        @JvmStatic
        @BeforeAll
        fun initialize() = runBlocking {

        }
    }

    @BeforeEach
    fun setUp() = runBlocking {
        repository.save(producto)
        println("set up completed.")
    }

    @AfterEach
    fun tearDown() = runBlocking {
        repository.delete(producto.id)
        println("teared down successfully.")
    }

    @Test
    @DisplayName("find by id")
    fun findById() = runBlocking {
        val res = repository.findById(producto.id)
        Assertions.assertEquals(producto.id, res?.id)
    }

    @Test
    @DisplayName("find all")
    fun findAll() = runBlocking {
        val list = repository.findAll()
        val res = list.equals( it.id == producto.id )
        Assertions.assertEquals(producto.id, res?.id)
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
        val res = repository.save(producto)
        Assertions.assertEquals(producto.id, res.id)
    }

    @Test
    @DisplayName("delete")
    fun delete() = runBlocking {
        val res1 = repository.delete(producto.id)
        val res2 = repository.findById(producto.id)
        assertAll( {
            Assertions.assertNull(res2)
            Assertions.assertTrue(res1)
        } )
    }
}