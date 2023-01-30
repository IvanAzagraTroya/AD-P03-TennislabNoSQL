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
import models.pedido.Pedido
import models.pedido.PedidoState
import models.producto.Producto
import models.producto.TipoProducto
import models.tarea.Tarea
import models.tarea.TipoTarea
import models.user.User
import models.user.UserProfile
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.litote.kmongo.newId
import repositories.tarea.TareaRepository
import repositories.tarea.TareaRepositoryCached
import java.time.LocalDate
import java.util.*
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class TareaRepositoryCachedTest {
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

    @MockK
    lateinit var repo: TareaRepository

    @SpyK
    var cache = TareaRepositoryCached()

    @InjectMockKs
    lateinit var repository: TareaRepositoryCached

    init {
        MockKAnnotations.init(this)
    }

    @Test
    fun findAll() = runTest {
        coEvery { repo.findAll() } returns flowOf(tarea)

        val result = repository.findAll().toList()

        assertAll(
            { assertEquals(tarea, result[0]) }
        )
        coVerify(exactly = 1) { repo.findAll() }
    }

    @Test
    fun findByUUID() = runTest {
        coEvery { repo.findByUUID(any()) } returns tarea

        val result = repository.findByUUID(tarea.uuid)

        assertAll(
            { assertEquals(tarea.uuid, result!!.uuid)},
            { assertEquals(tarea.tipo, result!!.tipo)}
        )
        coVerify {repo.findByUUID(any())}
    }

    @Test
    fun findById() = runTest {
        coEvery { repo.findById(any()) } returns tarea

        val result = repository.findById(tarea.id)

        assertAll(
            { assertEquals(tarea.id, result!!.id)},
            { assertEquals(tarea.tipo, result!!.tipo)}
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
        coEvery { repo.save(any()) } returns tarea

        val result = repository.save(tarea)

        assertAll(
            { assertEquals(tarea.uuid, result.uuid) },
            { assertEquals(tarea.tipo, result.tipo) },
        )

        coVerify(exactly = 1) { repo.save(any()) }
    }

    @Test
    fun delete() = runTest {
        coEvery { repo.findById(any()) } returns tarea
        coEvery { repo.delete(any()) } returns tarea

        val result = repository.delete(tarea.id)!!

        assertAll(
            { assertEquals(tarea.uuid, result.uuid) },
            { assertEquals(tarea.tipo, result.tipo) },
        )

        coVerify { repo.delete(any()) }
    }
}