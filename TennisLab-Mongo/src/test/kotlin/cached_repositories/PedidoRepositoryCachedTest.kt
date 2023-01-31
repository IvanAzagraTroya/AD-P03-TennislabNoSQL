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
import koin.models.pedido.Pedido
import koin.models.pedido.PedidoState
import koin.models.user.User
import koin.models.user.UserProfile
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.litote.kmongo.newId
import koin.repositories.pedido.IPedidoRepository
import koin.repositories.pedido.PedidoRepository
import koin.repositories.pedido.PedidoRepositoryCached
import java.time.LocalDate
import java.util.*
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class PedidoRepositoryCachedTest {
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

    @MockK
    lateinit var repo: PedidoRepository

    @SpyK
    var cache = PedidoRepositoryCached()

    @InjectMockKs
    lateinit var repository: PedidoRepositoryCached

    init {
        MockKAnnotations.init(this)
    }

    @Test
    fun findAll() = runTest {
        coEvery { repo.findAll() } returns flowOf(pedido)

        val result = repository.findAll().toList()

        assertAll(
            { assertEquals(pedido, result[0]) }
        )
        coVerify(exactly = 1) { repo.findAll() }
    }

    @Test
    fun findByUUID() = runTest {
        coEvery { repo.findByUUID(any()) } returns pedido

        val result = repository.findByUUID(pedido.uuid)

        assertAll(
            { assertEquals(pedido.uuid, result!!.uuid)},
            { assertEquals(pedido.state, result!!.state)}
        )
        coVerify {repo.findByUUID(any())}
    }

    @Test
    fun findById() = runTest {
        coEvery { repo.findById(any()) } returns pedido

        val result = repository.findById(pedido.id)

        assertAll(
            { assertEquals(pedido.id, result!!.id)},
            { assertEquals(pedido.state, result!!.state)}
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
        coEvery { repo.save(any()) } returns pedido

        val result = repository.save(pedido)

        assertAll(
            { assertEquals(pedido.uuid, result.uuid) },
            { assertEquals(pedido.state, result.state) },
        )

        coVerify(exactly = 1) { repo.save(any()) }
    }

    @Test
    fun delete() = runTest {
        coEvery { repo.findById(any()) } returns pedido
        coEvery { repo.delete(any()) } returns pedido

        val result = repository.delete(pedido.id)!!

        assertAll(
            { assertEquals(pedido.uuid, result.uuid) },
            { assertEquals(pedido.state, result.state) },
        )

        coVerify { repo.delete(any()) }
    }
}