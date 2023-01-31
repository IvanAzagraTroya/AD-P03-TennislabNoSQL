package cached_repositories

import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import koin.models.producto.Producto
import koin.models.producto.TipoProducto
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.litote.kmongo.newId
import koin.repositories.producto.ProductoRepository
import koin.repositories.producto.ProductoRepositoryCached
import java.util.*
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class ProductoRepositoryCachedTest {
    val producto = Producto(
        id = newId(),
        uuid = UUID.fromString("93a98d69-6da6-48a7-b34f-05b596ea83aa"),
        tipo = TipoProducto.FUNDAS,
        marca = "MarcaZ",
        modelo = "ModeloZ",
        precio = 36.4,
        stock = 8
    )

    @MockK
    lateinit var repo: ProductoRepository

    @SpyK
    var cache = ProductoRepositoryCached()

    @InjectMockKs
    lateinit var repository: ProductoRepositoryCached

    init {
        MockKAnnotations.init(this)
    }

    @Test
    fun findAll() = runTest {
        coEvery { repo.findAll() } returns flowOf(producto)

        val result = repository.findAll().toList()

        assertAll(
            { assertEquals(producto, result[0]) }
        )
        coVerify(exactly = 1) { repo.findAll() }
    }

    @Test
    fun findByUUID() = runTest {
        coEvery { repo.findByUUID(any()) } returns producto

        val result = repository.findByUUID(producto.uuid)

        assertAll(
            { assertEquals(producto.uuid, result!!.uuid)},
            { assertEquals(producto.marca, result!!.marca)}
        )
        coVerify {repo.findByUUID(any())}
    }

    @Test
    fun findById() = runTest {
        coEvery { repo.findById(any()) } returns producto

        val result = repository.findById(producto.id)

        assertAll(
            { assertEquals(producto.id, result!!.id)},
            { assertEquals(producto.marca, result!!.marca)}
        )
        coVerify {repo.findById(any())}
    }

    @Test
    fun findByIdNotExists() = runTest {
        coEvery{ repo.findById(any())} returns null

        val result = repository.findById(newId())
        assertNull(result)

        coVerify {repo.findById(any())}
    }

    @Test
    fun findByUUIDNotExists() = runTest {
        coEvery{ repo.findByUUID(any())} returns null

        val result = repository.findByUUID(UUID.randomUUID())
        assertNull(result)

        coVerify {repo.findByUUID(any())}
    }

    @Test
    fun save() = runTest {
        coEvery { repo.save(any()) } returns producto

        val result = repository.save(producto)

        assertAll(
            { assertEquals(producto.uuid, result.uuid) },
            { assertEquals(producto.marca, result.marca) },
        )

        coVerify(exactly = 1) { repo.save(any()) }
    }

    @Test
    fun delete() = runTest {
        coEvery { repo.findById(any()) } returns producto
        coEvery { repo.delete(any()) } returns producto

        val result = repository.delete(producto.id)!!

        assertAll(
            { assertEquals(producto.uuid, result.uuid) },
            { assertEquals(producto.marca, result.marca) },
        )

        coVerify { repo.delete(any()) }
    }
}